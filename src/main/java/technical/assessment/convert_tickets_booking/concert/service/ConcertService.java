package technical.assessment.convert_tickets_booking.concert.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import technical.assessment.convert_tickets_booking.concert.model.Concert;
import technical.assessment.convert_tickets_booking.concert.model.ConcertStatus;

import java.util.List;

public interface ConcertService {
    
    /**
     * Retrieves all available concerts, optionally filtered by title, location, and startTime.
     */
    Page<Concert> getAllConcerts(String title, String location, java.time.LocalDateTime startTime, Pageable pageable);

    /**
     * Retrieves a concert by its unique ID.
     */
    Concert getConcertById(Integer id);

    /**
     * Creates a new concert entry.
     */
    Concert createConcert(Concert concert);

    /**
     * Updates an existing concert's information.
     */
    Concert updateConcert(Integer id, Concert concert);

    /**
     * Deletes a concert by its unique ID.
     */
    void deleteConcert(Integer id);

    /**
     * Retrieves concerts filtered by their current status.
     */
    Page<Concert> getByStatus(ConcertStatus status, Pageable pageable);
}
