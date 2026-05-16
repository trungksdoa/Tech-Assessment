package technical.assessment.convert_tickets_booking.booking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import technical.assessment.convert_tickets_booking.shared.exception.ResourceNotFoundException;
import technical.assessment.convert_tickets_booking.booking.dto.BookingRequestDto;
import technical.assessment.convert_tickets_booking.booking.model.Booking;
import technical.assessment.convert_tickets_booking.booking.model.BookingStatus;
import technical.assessment.convert_tickets_booking.booking.repository.BookingRepository;
import technical.assessment.convert_tickets_booking.booking.service.BookingService;
import technical.assessment.convert_tickets_booking.booking.service.BookingItemProcessingService;
import technical.assessment.convert_tickets_booking.voucher.service.VoucherProcessingService;
import technical.assessment.convert_tickets_booking.voucher.model.Voucher;
import technical.assessment.convert_tickets_booking.logging.service.OperationLogService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final OperationLogService operationLogService;
    private final BookingItemProcessingService itemProcessingService;
    private final VoucherProcessingService voucherProcessingService;

    @Override
    @Transactional
    public Booking createBooking(BookingRequestDto request) {
        log.info("[BOOKING] Starting new booking process. IdempotencyKey: {}", request.getIdempotencyKey());

        // 0. Check idempotency
        if (request.getIdempotencyKey() != null) {
            Booking existing = getByIdempotencyKey(request.getIdempotencyKey());
            if (existing != null) {
                log.warn("[BOOKING] Duplicate request detected for key: {}. Returning existing booking.", request.getIdempotencyKey());
                return existing;
            }
        }

        // 1. Initialize Booking Entity
        Booking booking = Booking.builder()
                .userId(request.getUserId())
                .idempotencyKey(request.getIdempotencyKey())
                .status(BookingStatus.RECEIVED)
                .build();

        // 2. Process Items and Calculate Total Price
        BigDecimal totalPrice = itemProcessingService.processBookingItems(booking, request.getBookingItems());
        booking.setTotalPrice(totalPrice);

        // 3. Handle Voucher Application
        Voucher appliedVoucher = voucherProcessingService.applyVoucher(booking, request.getUserId(), request.getVoucherCode());

        // 4. Save Booking
        Booking saved = bookingRepository.save(booking);
        
        // 5. Save Voucher History
        voucherProcessingService.saveVoucherHistory(request.getUserId(), appliedVoucher, saved);

        log.info("[BOOKING] COMPLETED - Booking ID: {} saved successfully. Total Price: {}", 
                saved.getId(), saved.getTotalPrice());

        // 6. Log operation async
        operationLogService.logOperation(saved.getId(), 0, "MANUAL_STATUS_UPDATE",
                "Booking created and tickets reserved");

        return saved;
    }

    @Override
    public Booking getBookingById(Integer id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    @Override
    public Page<Booking> getBookingsByUserId(Integer userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional
    public Booking updateBookingStatus(Integer id, BookingStatus status) {
        Booking booking = getBookingById(id);
        booking.setStatus(status);
        Booking saved = bookingRepository.save(booking);
        operationLogService.logOperation(saved.getId(), 0, "MANUAL_STATUS_UPDATE", "Status updated to " + status);
        return saved;
    }

    @Override
    public Booking getByIdempotencyKey(String idempotencyKey) {
        return bookingRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
    }
}
