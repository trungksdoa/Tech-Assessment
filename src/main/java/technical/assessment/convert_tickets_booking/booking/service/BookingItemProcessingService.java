package technical.assessment.convert_tickets_booking.booking.service;

import technical.assessment.convert_tickets_booking.booking.dto.BookingRequestDto;
import technical.assessment.convert_tickets_booking.booking.model.Booking;

import java.math.BigDecimal;
import java.util.List;

public interface BookingItemProcessingService {

    /**
     * Processes booking items, reduces ticket inventory, and calculates the total initial price.
     *
     * @param booking  The booking entity to attach items to.
     * @param itemDtos The list of items from the request.
     * @return The calculated total price before any discounts.
     */
    BigDecimal processBookingItems(Booking booking, List<BookingRequestDto.BookingItemRequestDto> itemDtos);
}
