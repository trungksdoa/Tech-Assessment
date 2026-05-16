# Sequence Diagrams

This document describes the most critical business flows of the Concert Ticket Booking system, focusing on how we resolve Concurrency (Overselling) and Idempotency (Duplicate Request) risks.

## 1. Core Booking Flow

The main processing flow when a user attempts to reserve tickets. This flow solves 3 major problems:
- **Preventing Duplicate Bookings:** Using the Idempotency-Key mechanism.
- **Preventing Overselling:** Using Atomic Updates at the Database level (`reduceQuantity`).
- **Preventing Voucher Abuse:** Validating the usage history limit (`maxUsagePerUser`) and utilizing DB Locks.

```mermaid
---
config:
  theme: dark
---
sequenceDiagram
    autonumber
    actor Client
    participant Controller as BookingController
    participant Service as Booking<br/>ServiceImpl (Facade)
    participant ItemService as BookingItemService
    participant VoucherService as Voucher<br/>ProcessingService
    participant DB as PostgreSQL DB
    participant Log as Async Log Event

    Client->>Controller: POST /api/bookings (idempotencyKey, payload)
    Controller->>Service: createBooking(payload)
    
    %% Idempotency Check
    Service->>DB: Check IdempotencyKey
    alt Exists (Duplicate Request)
        DB-->>Service: Return existing Booking
        Service-->>Controller: Return Existing Booking
        Controller-->>Client: 200 OK (Booking Data)
    else Not Exists (New Request)
        %% Initialize PENDING Booking
        Service->>DB: Save Booking (Status: PENDING)
        
        %% Process Ticket Categories (Prevent Overselling)
        Service->>ItemService: processBookingItems(booking, items)
        loop For each Ticket Category
            ItemService->>DB: Atomic Update: reduceQuantity(id, qty)
            Note over ItemService, DB: UPDATE ticket_category <br/> SET available = available - qty <br/> WHERE id = ? AND available >= qty
            DB-->>ItemService: Affected rows (0 or 1)
            alt Affected rows = 0 (Out of stock)
                ItemService-->>Service: Throw BusinessException("Out of Stock")
                Service-->>Controller: Rollback Transaction & Return Error
                Controller-->>Client: 400 Bad Request
            else Affected rows = 1 (Success)
                ItemService->>DB: Save BookingItem
            end
        end
        ItemService-->>Service: Return Subtotal Amount
        
        %% Process Voucher (If present)
        opt If VoucherCode is provided
            Service->>VoucherService: applyVoucher(booking, userId, voucherCode)
            VoucherService->>DB: Validate Expiry Date & Global Limit
            VoucherService->>DB: Validate User Usage (Count VoucherHistory)
            alt Limit Exceeded
                VoucherService-->>Service: Throw BusinessException("Limit Reached")
                Service-->>Controller: Rollback Transaction
                Controller-->>Client: 400 Bad Request
            else Valid
                VoucherService->>DB: incrementUsage(voucherId) (Atomic Lock)
                VoucherService-->>Service: Deduct amount from Booking Total Price
            end
        end
        
        %% Finalize
        Service->>DB: Update Total Price in Booking
        opt If Voucher applied successfully
            Service->>VoucherService: saveVoucherHistory(userId, voucher, booking)
            VoucherService->>DB: Save VoucherHistory
        end
        
        Service-)Log: Publish Asynchronous Event (OperationLog)
        Service-->>Controller: Return successfully created Booking DTO
        Controller-->>Client: 201 Created (Booking Data)
        
        Note over DB, Log: Async thread inserts "BOOKING_CREATED" into operation_logs
    end
```

## 2. Calculate Voucher Flow

This flow is invoked *before* the user places an order, enabling the Client to display the discount amount and the final price to be paid, thus enhancing the user experience.

```mermaid
---
config:
  theme: dark
---
sequenceDiagram
    autonumber
    actor Client
    participant Controller as VoucherController
    participant Service as VoucherServiceImpl
    participant DB as PostgreSQL DB

    Client->>Controller: POST /api/vouchers/calculate (userId, voucherCode, originalPrice)
    Controller->>Service: calculateDiscount(payload)
    
    Service->>DB: Fetch Voucher by Code
    DB-->>Service: Voucher Entity
    
    %% Validate Expiration
    alt If Voucher is expired
        Service-->>Controller: Throw BusinessException("Expired")
        Controller-->>Client: 400 Bad Request
    end
    
    %% Validate Global Limit
    alt If Current Usage >= Max Usage
        Service-->>Controller: Throw BusinessException("Global Limit Reached")
        Controller-->>Client: 400 Bad Request
    end
    
    %% Validate User Limit
    Service->>DB: Count VoucherHistory by userId and voucherId
    DB-->>Service: count (Times used by user)
    alt If count >= MaxUsagePerUser
        Service-->>Controller: Throw BusinessException("User Limit Reached")
        Controller-->>Client: 400 Bad Request
    end
    
    %% Calculate Logic
    Note over Service: Calculate discountAmount based on <br/> discountPercentage, maxDiscountAmount <br/> or fixed discountAmount.
    Note over Service: finalPrice = originalPrice - discountAmount
    
    Service-->>Controller: Return DTO (discountAmount, finalPrice, voucherId)
    Controller-->>Client: 200 OK
```

## 3. Payment Status Update Flow

Simulates a Webhook callback from a third-party payment gateway (e.g., VNPay, Momo, Stripe) updating the system.

```mermaid
---
config:
  theme: dark
---
sequenceDiagram
    autonumber
    actor Payment Gateway
    participant Controller as BookingController
    participant Service as BookingServiceImpl
    participant ItemService as TicketCategoryRepository
    participant DB as PostgreSQL DB
    participant Log as Async Log Event

    Payment Gateway->>Controller: PATCH /api/bookings/{id}/status (status=PAID/FAILED)
    Controller->>Service: updateBookingStatus(id, status, notes)
    
    Service->>DB: Find Booking by ID
    DB-->>Service: Booking Entity
    
    alt If Booking is already cancelled or paid
        Service-->>Controller: Throw BusinessException("Cannot change status")
        Controller-->>Payment Gateway: 400 Bad Request
    end
    
    %% Handle ticket restoration if FAILED/CANCELLED
    alt New status is CANCELLED or FAILED
        Service->>ItemService: Get BookingItems list
        loop For each BookingItem
            Service->>DB: Atomic Update: restoreQuantity(categoryId, qty)
            Note over Service, DB: UPDATE ticket_category <br/> SET available = available + qty <br/> WHERE id = ?
        end
        Note over Service: *Note: Voucher history could also <br/> be reverted here (out of scope).*
    end
    
    Service->>DB: Save new Booking status
    Service-)Log: Publish Asynchronous Event (OperationLog)
    Service-->>Controller: Success
    Controller-->>Payment Gateway: 200 OK
    
    Note over DB, Log: Async thread inserts "STATUS_CHANGED" into operation_logs
```
