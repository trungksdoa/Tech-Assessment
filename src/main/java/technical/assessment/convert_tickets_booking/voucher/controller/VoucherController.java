package technical.assessment.convert_tickets_booking.voucher.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import technical.assessment.convert_tickets_booking.voucher.model.Voucher;
import technical.assessment.convert_tickets_booking.voucher.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import technical.assessment.convert_tickets_booking.voucher.dto.VoucherCalculationRequestDto;
import technical.assessment.convert_tickets_booking.voucher.dto.VoucherCalculationResponseDto;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@Tag(name = "Voucher Management", description = "APIs for creating and applying discount vouchers")
public class VoucherController {

    private final VoucherService voucherService;

    @PostMapping
    @Operation(summary = "Create a new voucher", description = "Define a new discount voucher with code and usage limits")
    public Voucher createVoucher(@Valid @RequestBody Voucher voucher) {
        return voucherService.createVoucher(voucher);
    }

    @GetMapping
    @Operation(summary = "Get all vouchers", description = "Retrieve a list of all promotional vouchers")
    public Page<Voucher> getAllVouchers(@PageableDefault(size = 10) Pageable pageable) {
        return voucherService.getAllVouchers(pageable);
    }

    @PostMapping("/calculate")
    @Operation(summary = "Calculate discount", description = "Calculate the discount and final price before placing a booking")
    public ResponseEntity<VoucherCalculationResponseDto> calculateDiscount(@Valid @RequestBody VoucherCalculationRequestDto requestDto) {
        return ResponseEntity.ok(voucherService.calculateDiscount(requestDto));
    }
}
