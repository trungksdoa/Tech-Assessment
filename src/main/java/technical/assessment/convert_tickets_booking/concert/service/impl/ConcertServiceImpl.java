package technical.assessment.convert_tickets_booking.concert.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import technical.assessment.convert_tickets_booking.concert.model.Concert;
import technical.assessment.convert_tickets_booking.concert.model.ConcertStatus;
import technical.assessment.convert_tickets_booking.concert.repository.ConcertRepository;
import technical.assessment.convert_tickets_booking.concert.service.ConcertService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertServiceImpl implements ConcertService {

    private final ConcertRepository concertRepository;

    @Override
    public Page<Concert> getAllConcerts(String title, String location, java.time.LocalDateTime startTime, Pageable pageable) {
        Specification<Concert> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (title != null && !title.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }
            if (location != null && !location.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
            }
            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startTime"), startTime));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        return concertRepository.findAll(spec, pageable);
    }

    @Override
    public Concert getConcertById(Integer id) {
        return concertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Concert not found with id: " + id));
    }

    @Override
    public Concert createConcert(Concert concert) {
        return concertRepository.save(concert);
    }

    @Override
    public Concert updateConcert(Integer id, Concert concert) {
        Concert existing = getConcertById(id);
        existing.setTitle(concert.getTitle());
        existing.setDescription(concert.getDescription());
        existing.setLocation(concert.getLocation());
        existing.setStartTime(concert.getStartTime());
        existing.setStatus(concert.getStatus());
        return concertRepository.save(existing);
    }

    @Override
    public void deleteConcert(Integer id) {
        concertRepository.deleteById(id);
    }

    @Override
    public Page<Concert> getByStatus(ConcertStatus status, Pageable pageable) {
        return concertRepository.findByStatus(status, pageable);
    }
}
