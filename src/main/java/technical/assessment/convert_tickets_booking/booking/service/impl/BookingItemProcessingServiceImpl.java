package technical.assessment.convert_tickets_booking.booking.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import technical.assessment.convert_tickets_booking.booking.dto.BookingRequestDto;
import technical.assessment.convert_tickets_booking.booking.model.Booking;
import technical.assessment.convert_tickets_booking.booking.model.BookingItem;
import technical.assessment.convert_tickets_booking.booking.service.BookingItemProcessingService;
import technical.assessment.convert_tickets_booking.concert.model.TicketCategory;
import technical.assessment.convert_tickets_booking.concert.repository.TicketCategoryRepository;
import technical.assessment.convert_tickets_booking.shared.exception.BusinessException;
import technical.assessment.convert_tickets_booking.shared.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingItemProcessingServiceImpl implements BookingItemProcessingService {

    private final TicketCategoryRepository ticketCategoryRepository;

    @Override
    public BigDecimal processBookingItems(Booking booking, List<BookingRequestDto.BookingItemRequestDto> itemDtos) {
        final BigDecimal[] calculatedTotal = {BigDecimal.ZERO};

        List<BookingItem> items = itemDtos.stream().map(itemDto -> {
            log.info("[BOOKING_ITEM] Processing item - Category ID: {}, Quantity: {}", 
                    itemDto.getTicketCategoryId(), itemDto.getQuantity());

            TicketCategory category = ticketCategoryRepository.findById(itemDto.getTicketCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket category not found: " + itemDto.getTicketCategoryId()));

            int updatedRows = ticketCategoryRepository.reduceQuantity(
                    itemDto.getTicketCategoryId(),
                    itemDto.getQuantity());
            
            if (updatedRows == 0) {
                log.error("[BOOKING_ITEM] FAILED - Out of stock for category ID: {}", itemDto.getTicketCategoryId());
                throw new BusinessException("Tickets sold out or insufficient quantity for category ID: "
                        + itemDto.getTicketCategoryId());
            }

            BigDecimal itemTotal = category.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            calculatedTotal[0] = calculatedTotal[0].add(itemTotal);

            log.info("[BOOKING_ITEM] SUCCESS - Reserved {} tickets for category ID: {}. Unit Price: {}", 
                    itemDto.getQuantity(), itemDto.getTicketCategoryId(), category.getPrice());

            return BookingItem.builder()
                    .booking(booking)
                    .ticketCategory(category)
                    .quantity(itemDto.getQuantity())
                    .unitPrice(category.getPrice())
                    .build();
        }).collect(Collectors.toList());

        booking.setBookingItems(items);
        return calculatedTotal[0];
    }
}
