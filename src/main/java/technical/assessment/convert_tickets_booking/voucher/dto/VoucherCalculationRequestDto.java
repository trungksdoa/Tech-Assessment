package technical.assessment.convert_tickets_booking.voucher.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class VoucherCalculationRequestDto {

    @NotBlank(message = "Voucher code cannot be empty")
    private String voucherCode;

    @NotNull(message = "User ID cannot be null")
    private Integer userId;

    @NotNull(message = "Original price cannot be null")
    @Min(value = 0, message = "Original price must be positive")
    private BigDecimal originalPrice;
}
