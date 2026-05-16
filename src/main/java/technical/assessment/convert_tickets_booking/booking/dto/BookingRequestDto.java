package technical.assessment.convert_tickets_booking.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDto {

    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    private String voucherCode;

    @NotEmpty(message = "Booking must have at least one item")
    @Valid
    private List<BookingItemRequestDto> bookingItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingItemRequestDto {
        @NotNull(message = "Ticket category ID is required")
        private Integer ticketCategoryId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }
}
