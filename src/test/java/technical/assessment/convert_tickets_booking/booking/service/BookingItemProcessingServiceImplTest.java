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

    @Test
    void processBookingItems_Success_WhenSufficientInventory() {
        // Arrange
        when(ticketCategoryRepository.findById(100)).thenReturn(Optional.of(ticketCategory));
        when(ticketCategoryRepository.reduceQuantity(100, 2)).thenReturn(1); // 1 row updated

        // Act
        BigDecimal totalAmount = bookingItemProcessingService.processBookingItems(booking, List.of(itemRequest));

        // Assert
        assertEquals(new BigDecimal("1000.00"), totalAmount);
        verify(ticketCategoryRepository).reduceQuantity(100, 2);
    }

    @Test
    void processBookingItems_ThrowsException_WhenOversellingOccurs() {
        // Arrange
        when(ticketCategoryRepository.findById(100)).thenReturn(Optional.of(ticketCategory));
        // Simulate race condition where DB update fails (0 rows updated) because available_quantity < 2
        when(ticketCategoryRepository.reduceQuantity(100, 2)).thenReturn(0);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            bookingItemProcessingService.processBookingItems(booking, List.of(itemRequest));
        });

        assertTrue(exception.getMessage().contains("Tickets sold out or insufficient quantity"));
        verify(ticketCategoryRepository).reduceQuantity(100, 2);
    }

    @Test
    void processBookingItems_ThrowsException_WhenTicketCategoryNotFound() {
        // Arrange
        when(ticketCategoryRepository.findById(100)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            bookingItemProcessingService.processBookingItems(booking, List.of(itemRequest));
        });
        
        verify(ticketCategoryRepository, never()).reduceQuantity(anyInt(), anyInt());
    }
}
