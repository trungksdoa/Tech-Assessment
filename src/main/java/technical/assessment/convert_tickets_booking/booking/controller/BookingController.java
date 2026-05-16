package technical.assessment.convert_tickets_booking.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import technical.assessment.convert_tickets_booking.booking.dto.BookingRequestDto;
import technical.assessment.convert_tickets_booking.booking.model.Booking;
import technical.assessment.convert_tickets_booking.booking.model.BookingStatus;
import technical.assessment.convert_tickets_booking.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Management", description = "APIs for creating and managing concert ticket bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a new booking", description = "Atomic operation to reserve tickets and create a booking record")
    public Booking createBooking(@Valid @RequestBody BookingRequestDto request) {
        return bookingService.createBooking(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID", description = "Retrieve detailed information of a specific booking")
    public ResponseEntity<Booking> getBookingById(@PathVariable Integer id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user bookings", description = "Retrieve all bookings associated with a specific user ID")
    public Page<Booking> getBookingsByUserId(@PathVariable Integer userId, @PageableDefault(size = 10) Pageable pageable) {
        return bookingService.getBookingsByUserId(userId, pageable);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update booking status", description = "Manually update the status of a booking and log the change")
    public ResponseEntity<Booking> updateBookingStatus(@PathVariable Integer id, @RequestParam BookingStatus status) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(id, status));
    }
}
