package technical.assessment.convert_tickets_booking.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import technical.assessment.convert_tickets_booking.booking.model.BookingItem;

import java.util.List;

@Repository
public interface BookingItemRepository extends JpaRepository<BookingItem, Integer> {
    List<BookingItem> findByBookingId(Integer bookingId);
}
