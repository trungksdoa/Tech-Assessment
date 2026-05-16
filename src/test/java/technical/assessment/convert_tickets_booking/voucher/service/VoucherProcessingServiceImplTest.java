package technical.assessment.convert_tickets_booking.voucher.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import technical.assessment.convert_tickets_booking.booking.model.Booking;
import technical.assessment.convert_tickets_booking.shared.exception.BusinessException;
import technical.assessment.convert_tickets_booking.voucher.model.Voucher;
import technical.assessment.convert_tickets_booking.voucher.repository.VoucherHistoryRepository;
import technical.assessment.convert_tickets_booking.voucher.repository.VoucherRepository;
import technical.assessment.convert_tickets_booking.voucher.service.impl.VoucherProcessingServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoucherProcessingServiceImplTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private VoucherHistoryRepository voucherHistoryRepository;

    @InjectMocks
    private VoucherProcessingServiceImpl voucherProcessingService;

    private Booking booking;
    private Voucher voucher;

    @BeforeEach
    void setUp() {
        booking = Booking.builder()
                .id(1)
                .totalPrice(new BigDecimal("1000.00"))
                .build();

        voucher = Voucher.builder()
                .id(10)
                .code("SUMMER2026")
                .discountAmount(new BigDecimal("200.00"))
                .maxUsage(100)
                .currentUsage(50)
                .maxUsagePerUser(2)
                .expiryDate(LocalDateTime.now().plusDays(10))
                .build();
    }

    @Test
    void applyVoucher_Success_WhenValidVoucherAndUsageBelowLimit() {
        // Arrange
        when(voucherRepository.findByCode("SUMMER2026")).thenReturn(Optional.of(voucher));
        when(voucherHistoryRepository.countByUserIdAndVoucherId(1, 10)).thenReturn(1); // User used 1 time, limit is 2
        when(voucherRepository.incrementUsage(10)).thenReturn(1); // Global limit lock succeeded

        // Act
        Voucher appliedVoucher = voucherProcessingService.applyVoucher(booking, 1, "SUMMER2026");

        // Assert
        assertNotNull(appliedVoucher);
        assertEquals(new BigDecimal("800.00"), booking.getTotalPrice()); // 1000 - 200
        verify(voucherRepository).incrementUsage(10);
    }

    @Test
    void applyVoucher_ThrowsException_WhenUserExceedsMaxUsagePerUser() {
        // Arrange
        when(voucherRepository.findByCode("SUMMER2026")).thenReturn(Optional.of(voucher));
        // Simulate user has already used the voucher 2 times (the limit is 2)
        when(voucherHistoryRepository.countByUserIdAndVoucherId(1, 10)).thenReturn(2);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            voucherProcessingService.applyVoucher(booking, 1, "SUMMER2026");
        });

        assertEquals("User has reached the maximum usage limit for this voucher.", exception.getMessage());
        verify(voucherRepository, never()).incrementUsage(anyInt()); // Should not increment global usage
    }

    @Test
    void applyVoucher_ThrowsException_WhenVoucherExpired() {
        // Arrange
        voucher.setExpiryDate(LocalDateTime.now().minusDays(1)); // Expired yesterday
        when(voucherRepository.findByCode("SUMMER2026")).thenReturn(Optional.of(voucher));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            voucherProcessingService.applyVoucher(booking, 1, "SUMMER2026");
        });

        assertEquals("Voucher is expired.", exception.getMessage());
    }

    @Test
    void applyVoucher_ThrowsException_WhenGlobalLimitReachedConcurrently() {
        // Arrange
        when(voucherRepository.findByCode("SUMMER2026")).thenReturn(Optional.of(voucher));
        when(voucherHistoryRepository.countByUserIdAndVoucherId(1, 10)).thenReturn(0);
        // Simulate concurrent usage where update fails
        when(voucherRepository.incrementUsage(10)).thenReturn(0);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            voucherProcessingService.applyVoucher(booking, 1, "SUMMER2026");
        });

        assertEquals("Voucher has reached its maximum usage limit.", exception.getMessage());
    }
}
