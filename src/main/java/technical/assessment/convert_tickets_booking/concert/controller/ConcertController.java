package technical.assessment.convert_tickets_booking.concert.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import technical.assessment.convert_tickets_booking.concert.model.Concert;
import technical.assessment.convert_tickets_booking.concert.model.ConcertStatus;
import technical.assessment.convert_tickets_booking.concert.service.ConcertService;

import java.util.List;

@RestController
@RequestMapping("/api/concerts")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;

    @GetMapping
    public Page<Concert> getAllConcerts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startTime,
            @PageableDefault(size = 10) Pageable pageable) {
        return concertService.getAllConcerts(title, location, startTime, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Concert> getConcertById(@PathVariable Integer id) {
        return ResponseEntity.ok(concertService.getConcertById(id));
    }

    @PostMapping
    public Concert createConcert(@RequestBody Concert concert) {
        return concertService.createConcert(concert);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Concert> updateConcert(@PathVariable Integer id, @RequestBody Concert concert) {
        return ResponseEntity.ok(concertService.updateConcert(id, concert));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConcert(@PathVariable Integer id) {
        concertService.deleteConcert(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    public Page<Concert> getByStatus(@PathVariable ConcertStatus status, @PageableDefault(size = 10) Pageable pageable) {
        return concertService.getByStatus(status, pageable);
    }
}
