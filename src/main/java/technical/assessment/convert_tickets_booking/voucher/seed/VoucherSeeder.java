package technical.assessment.convert_tickets_booking.voucher.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import technical.assessment.convert_tickets_booking.voucher.model.Voucher;
import technical.assessment.convert_tickets_booking.voucher.repository.VoucherRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class VoucherSeeder implements CommandLineRunner {

    private final VoucherRepository voucherRepository;

    @Override
    public void run(String... args) {
        if (voucherRepository.count() == 0) {
            log.info("Seeding initial vouchers...");
            
            Voucher welcomeVoucher = Voucher.builder()
                    .code("WELCOME2024")
                    .discountAmount(new BigDecimal("50.00"))
                    .maxUsage(100)
                    .currentUsage(0)
                    .expiryDate(LocalDateTime.now().plusMonths(6))
                    .build();

            Voucher earlyBird = Voucher.builder()
                    .code("EARLYBIRD")
                    .discountAmount(new BigDecimal("100.00"))
                    .maxUsage(50)
                    .currentUsage(0)
                    .expiryDate(LocalDateTime.now().plusMonths(3))
                    .build();

            Voucher flashSale = Voucher.builder()
                    .code("FLASHSALE")
                    .discountAmount(new BigDecimal("200.00"))
                    .maxUsage(10)
                    .currentUsage(0)
                    .expiryDate(LocalDateTime.now().plusDays(7))
                    .build();

            Voucher expiredVoucher = Voucher.builder()
                    .code("EXPIRED")
                    .discountAmount(new BigDecimal("10.00"))
                    .maxUsage(1000)
                    .currentUsage(0)
                    .expiryDate(LocalDateTime.now().minusDays(1))
                    .build();

            voucherRepository.saveAll(List.of(welcomeVoucher, earlyBird, flashSale, expiredVoucher));
            log.info("Seeding completed. 4 vouchers created.");
        } else {
            log.info("Vouchers already exist, skipping seeding.");
        }
    }
}
