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

    /**
     * Test logic: Áp dụng voucher thành công.
     * Voucher hợp lệ, lượt dùng của User và lượt dùng toàn hệ thống đều chưa vượt ngưỡng.
     */
    @Test
    void applyVoucher_Success_WhenValidVoucherAndUsageBelowLimit() {
        // Arrange
        when(voucherRepository.findByCode("SUMMER2026")).thenReturn(Optional.of(voucher));
        when(voucherHistoryRepository.countByUserIdAndVoucherId(1, 10)).thenReturn(1); // User mới dùng 1 lần, giới hạn là 2
        when(voucherRepository.incrementUsage(10)).thenReturn(1); // Giả lập trừ quota thành công trong DB

        // Act
        Voucher appliedVoucher = voucherProcessingService.applyVoucher(booking, 1, "SUMMER2026");

        // Assert
        assertNotNull(appliedVoucher);
        assertEquals(new BigDecimal("800.00"), booking.getTotalPrice()); // 1000 - 200 = 800
        verify(voucherRepository).incrementUsage(10);
    }

    /**
     * Test logic: Chặn User sử dụng voucher quá số lần cho phép (maxUsagePerUser).
     */
    @Test
    void applyVoucher_ThrowsException_WhenUserExceedsMaxUsagePerUser() {
        // Arrange
        when(voucherRepository.findByCode("SUMMER2026")).thenReturn(Optional.of(voucher));
        // Giả lập user đã dùng 2 lần (đã chạm mốc tối đa)
        when(voucherHistoryRepository.countByUserIdAndVoucherId(1, 10)).thenReturn(2);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            voucherProcessingService.applyVoucher(booking, 1, "SUMMER2026");
        });

        assertEquals("User has reached the maximum usage limit for this voucher.", exception.getMessage());
        // Không được phép gọi lệnh tăng lượt dùng toàn hệ thống nếu user đã hết quyền dùng
        verify(voucherRepository, never()).incrementUsage(anyInt()); 
    }

    /**
     * Test logic: Chặn sử dụng voucher đã hết hạn.
     */
    @Test
    void applyVoucher_ThrowsException_WhenVoucherExpired() {
        // Arrange
        voucher.setExpiryDate(LocalDateTime.now().minusDays(1)); // Đã hết hạn từ hôm qua
        when(voucherRepository.findByCode("SUMMER2026")).thenReturn(Optional.of(voucher));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            voucherProcessingService.applyVoucher(booking, 1, "SUMMER2026");
        });

        assertEquals("Voucher is expired.", exception.getMessage());
    }

    /**
     * Test logic: Chống tranh chấp đồng thời khi Voucher sắp hết lượt dùng toàn hệ thống (Global Limit).
     * Ngay cả khi code Java check thấy còn lượt, nhưng DB update trả về 0 thì vẫn coi là thất bại.
     */
    @Test
    void applyVoucher_ThrowsException_WhenGlobalLimitReachedConcurrently() {
        // Arrange
        when(voucherRepository.findByCode("SUMMER2026")).thenReturn(Optional.of(voucher));
        when(voucherHistoryRepository.countByUserIdAndVoucherId(1, 10)).thenReturn(0);
        // DB trả về 0 nghĩa là lượt dùng cuối cùng đã bị người khác chiếm mất ngay trước đó
        when(voucherRepository.incrementUsage(10)).thenReturn(0);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            voucherProcessingService.applyVoucher(booking, 1, "SUMMER2026");
        });

        assertEquals("Voucher has reached its maximum usage limit.", exception.getMessage());
    }
}
