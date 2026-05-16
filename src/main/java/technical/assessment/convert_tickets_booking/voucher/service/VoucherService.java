package technical.assessment.convert_tickets_booking.voucher.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import technical.assessment.convert_tickets_booking.voucher.model.Voucher;

import technical.assessment.convert_tickets_booking.voucher.dto.VoucherCalculationRequestDto;
import technical.assessment.convert_tickets_booking.voucher.dto.VoucherCalculationResponseDto;

import java.util.List;

public interface VoucherService {
    
    /**
     * Creates a new promotional voucher in the system.
     *
     * @param voucher The voucher data to save.
     * @return The persisted Voucher entity.
     */
    Voucher createVoucher(Voucher voucher);

    /**
     * Retrieves a voucher by its unique code.
     * Throws RuntimeException if the code is invalid.
     *
     * @param code The unique alphanumeric voucher code.
     * @return The found Voucher entity.
     */
    Voucher getVoucherByCode(String code);

    /**
     * Calculates the discount and final price for a given original amount and voucher code.
     * Validates if the voucher is active, not expired, and not exceeding user's limits.
     *
     * @param requestDto The calculation request payload.
     * @return DTO containing original price, discount amount, and final price.
     */
    VoucherCalculationResponseDto calculateDiscount(VoucherCalculationRequestDto requestDto);

    /**
     * Retrieves all vouchers currently configured in the system.
     *
     * @return A list of all vouchers.
     */
    Page<Voucher> getAllVouchers(Pageable pageable);
}
