package technical.assessment.convert_tickets_booking.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import technical.assessment.convert_tickets_booking.booking.model.Booking;
import technical.assessment.convert_tickets_booking.booking.model.BookingStatus;

import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    Page<Booking> findByUserId(Integer userId, Pageable pageable);

    Optional<Booking> findByIdempotencyKey(String idempotencyKey);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);
}
