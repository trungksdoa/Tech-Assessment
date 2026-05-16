package technical.assessment.convert_tickets_booking.logging.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import technical.assessment.convert_tickets_booking.logging.model.OperationLog;
import technical.assessment.convert_tickets_booking.logging.service.OperationLogService;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping("/booking/{bookingId}")
    public Page<OperationLog> getLogsByBookingId(@PathVariable Integer bookingId, @PageableDefault(size = 10) Pageable pageable) {
        return operationLogService.getLogsByBookingId(bookingId, pageable);
    }
}
