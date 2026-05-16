package technical.assessment.convert_tickets_booking.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import technical.assessment.convert_tickets_booking.booking.dto.BookingRequestDto.BookingItemRequestDto;
import technical.assessment.convert_tickets_booking.booking.model.Booking;
import technical.assessment.convert_tickets_booking.booking.service.impl.BookingItemProcessingServiceImpl;
import technical.assessment.convert_tickets_booking.concert.model.TicketCategory;
import technical.assessment.convert_tickets_booking.concert.repository.TicketCategoryRepository;
import technical.assessment.convert_tickets_booking.shared.exception.BusinessException;
import technical.assessment.convert_tickets_booking.shared.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingItemProcessingServiceImplTest {

    @Mock
    private TicketCategoryRepository ticketCategoryRepository;

    @InjectMocks
    private BookingItemProcessingServiceImpl bookingItemProcessingService;

    private Booking booking;
    private BookingItemRequestDto itemRequest;
    private TicketCategory ticketCategory;

    @BeforeEach
    void setUp() {
        booking = Booking.builder().id(1).build();

        itemRequest = new BookingItemRequestDto();
        itemRequest.setTicketCategoryId(100);
        itemRequest.setQuantity(2);

        ticketCategory = new TicketCategory();
        ticketCategory.setId(100);
        ticketCategory.setPrice(new BigDecimal("500.00"));
        ticketCategory.setAvailableQuantity(10);
    }

    /**
     * Test logic: Xử lý đặt vé thành công khi số lượng tồn kho đủ.
     * Kiểm tra xem hệ thống có tính đúng tổng tiền và gọi hàm update inventory không.
     */
    @Test
    void processBookingItems_Success_WhenSufficientInventory() {
        // Arrange: Giả lập tìm thấy loại vé và trừ kho thành công (trả về 1 dòng ảnh hưởng)
        when(ticketCategoryRepository.findById(100)).thenReturn(Optional.of(ticketCategory));
        when(ticketCategoryRepository.reduceQuantity(100, 2)).thenReturn(1); 

        // Act: Thực hiện xử lý
        BigDecimal totalAmount = bookingItemProcessingService.processBookingItems(booking, List.of(itemRequest));

        // Assert: Tổng tiền phải là 1000.00 (2 vé * 500.00)
        assertEquals(new BigDecimal("1000.00"), totalAmount);
        verify(ticketCategoryRepository).reduceQuantity(100, 2);
    }

    /**
     * Test logic: Chống bán vượt vé (Overselling).
     * Giả lập trường hợp có nhiều người cùng mua vé cuối cùng.
     * Database sẽ trả về 0 nếu số lượng khả dụng không đủ trừ.
     */
    @Test
    void processBookingItems_ThrowsException_WhenOversellingOccurs() {
        // Arrange: Tìm thấy vé nhưng khi trừ kho thất bại (0 rows updated) vì tranh chấp đồng thời
        when(ticketCategoryRepository.findById(100)).thenReturn(Optional.of(ticketCategory));
        when(ticketCategoryRepository.reduceQuantity(100, 2)).thenReturn(0);

        // Act & Assert: Phải ném lỗi BusinessException với thông báo hết vé
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            bookingItemProcessingService.processBookingItems(booking, List.of(itemRequest));
        });

        assertTrue(exception.getMessage().contains("Tickets sold out or insufficient quantity"));
        verify(ticketCategoryRepository).reduceQuantity(100, 2);
    }

    /**
     * Test logic: Trường hợp ID loại vé không tồn tại trong hệ thống.
     */
    @Test
    void processBookingItems_ThrowsException_WhenTicketCategoryNotFound() {
        // Arrange: Không tìm thấy loại vé trong DB
        when(ticketCategoryRepository.findById(100)).thenReturn(Optional.empty());

        // Act & Assert: Phải ném lỗi ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> {
            bookingItemProcessingService.processBookingItems(booking, List.of(itemRequest));
        });
        
        // Đảm bảo không bao giờ gọi lệnh trừ kho nếu không tìm thấy vé
        verify(ticketCategoryRepository, never()).reduceQuantity(anyInt(), anyInt());
    }
}
