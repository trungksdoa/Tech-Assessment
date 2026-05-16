package technical.assessment.convert_tickets_booking.logging.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import technical.assessment.convert_tickets_booking.booking.model.Booking;
import technical.assessment.convert_tickets_booking.logging.enums.OperationAction;
import technical.assessment.convert_tickets_booking.logging.model.OperationLog;
import technical.assessment.convert_tickets_booking.logging.repository.OperationLogRepository;
import technical.assessment.convert_tickets_booking.logging.service.OperationLogService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogRepository operationLogRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Async
    public void logOperation(Integer bookingId, Integer operatorId, String action, String notes) {
        Booking bookingRef = entityManager.getReference(Booking.class, bookingId);

        OperationLog log = OperationLog.builder()
                .booking(bookingRef)
                .operatorId(operatorId)
                .action(OperationAction.valueOf(action))
                .notes(notes)
                .createdAt(LocalDateTime.now())
                .build();
        operationLogRepository.save(log);
    }

    @Override
    public Page<OperationLog> getLogsByBookingId(Integer bookingId, Pageable pageable) {
        return operationLogRepository.findByBookingId(bookingId, pageable);
    }
}
