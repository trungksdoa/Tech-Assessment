package technical.assessment.convert_tickets_booking.concert.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import technical.assessment.convert_tickets_booking.concert.model.Concert;
import technical.assessment.convert_tickets_booking.concert.model.ConcertStatus;
import technical.assessment.convert_tickets_booking.concert.model.TicketCategory;
import technical.assessment.convert_tickets_booking.concert.repository.ConcertRepository;
import technical.assessment.convert_tickets_booking.concert.repository.TicketCategoryRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // Run first to ensure base data is ready
public class ConcertSeeder implements CommandLineRunner {

    private final ConcertRepository concertRepository;
    private final TicketCategoryRepository ticketCategoryRepository;

    @Override
    public void run(String... args) {
        if (concertRepository.count() == 0) {
            log.info("Seeding initial concerts and ticket categories...");

            Concert concert = Concert.builder()
                    .title("The Eras Tour - Vietnam 2024")
                    .description("Special concert by Taylor Swift in Vietnam")
                    .location("My Dinh National Stadium, Hanoi")
                    .startTime(LocalDateTime.now().plusMonths(2))
                    .status(ConcertStatus.PUBLISHED)
                    .build();

            Concert savedConcert = concertRepository.save(concert);

            TicketCategory standard = TicketCategory.builder()
                    .concert(savedConcert)
                    .name("STANDARD")
                    .price(new BigDecimal("2000000.00"))
                    .totalQuantity(5000)
                    .availableQuantity(100) // Small quantity to test "sold out" scenario
                    .build();

            TicketCategory vip = TicketCategory.builder()
                    .concert(savedConcert)
                    .name("VIP")
                    .price(new BigDecimal("5000000.00"))
                    .totalQuantity(500)
                    .availableQuantity(500)
                    .build();

            TicketCategory vvip = TicketCategory.builder()
                    .concert(savedConcert)
                    .name("VVIP")
                    .price(new BigDecimal("10000000.00"))
                    .totalQuantity(100)
                    .availableQuantity(100)
                    .build();

            ticketCategoryRepository.saveAll(List.of(standard, vip, vvip));
            log.info("Seeding completed. Concert ID: {}, Standard Ticket Category ID: {}", 
                    savedConcert.getId(), standard.getId());
        } else {
            log.info("Concerts already exist, skipping seeding.");
        }
    }
}
