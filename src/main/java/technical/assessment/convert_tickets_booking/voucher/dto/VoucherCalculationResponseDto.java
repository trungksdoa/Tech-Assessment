package technical.assessment.convert_tickets_booking.voucher.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class VoucherCalculationResponseDto {
    private Integer voucherId;
    private String voucherCode;
    private BigDecimal originalPrice;
    private BigDecimal discountAmount;
    private BigDecimal finalPrice;
}
