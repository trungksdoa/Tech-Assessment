package technical.assessment.convert_tickets_booking.logging.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import technical.assessment.convert_tickets_booking.logging.model.OperationLog;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Integer> {
    Page<OperationLog> findByBookingId(Integer bookingId, Pageable pageable);
}
