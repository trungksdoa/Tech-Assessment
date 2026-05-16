package technical.assessment.convert_tickets_booking.concert.repository; 
 
import technical.assessment.convert_tickets_booking.concert.model.Concert; 
import technical.assessment.convert_tickets_booking.concert.model.ConcertStatus; 
import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository; 
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
 
@Repository 
public interface ConcertRepository extends JpaRepository<Concert, Integer>, JpaSpecificationExecutor<Concert> { 
    Page<Concert> findByStatus(ConcertStatus status, Pageable pageable); 
    Page<Concert> findByTitleContainingIgnoreCase(String title, Pageable pageable); 
}
