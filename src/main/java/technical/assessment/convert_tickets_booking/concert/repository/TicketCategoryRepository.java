package technical.assessment.convert_tickets_booking.concert.repository; 
 
import technical.assessment.convert_tickets_booking.concert.model.TicketCategory; 
import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository; 
 
import java.util.List; 
 
@Repository 
public interface TicketCategoryRepository extends JpaRepository<TicketCategory, Integer> { 
    List<TicketCategory> findByConcertId(Integer concertId); 
    List<TicketCategory> findByAvailableQuantityGreaterThan(Integer quantity); 
 
    @Modifying
    @Query("UPDATE TicketCategory t SET t.availableQuantity = t.availableQuantity - :qty " +
           "WHERE t.id = :id AND t.availableQuantity >= :qty")
    int reduceQuantity(@Param("id") Integer id, @Param("qty") Integer qty);
}
