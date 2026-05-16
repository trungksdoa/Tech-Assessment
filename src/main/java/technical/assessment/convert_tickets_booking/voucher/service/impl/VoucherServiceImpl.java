package technical.assessment.convert_tickets_booking.voucher.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import technical.assessment.convert_tickets_booking.booking.model.Booking;
import technical.assessment.convert_tickets_booking.voucher.model.Voucher;
import technical.assessment.convert_tickets_booking.voucher.model.VoucherHistory;
import technical.assessment.convert_tickets_booking.voucher.repository.VoucherHistoryRepository;
import technical.assessment.convert_tickets_booking.voucher.repository.VoucherRepository;
import technical.assessment.convert_tickets_booking.shared.exception.BusinessException;
import technical.assessment.convert_tickets_booking.shared.exception.ResourceNotFoundException;
import technical.assessment.convert_tickets_booking.voucher.service.VoucherService;
import technical.assessment.convert_tickets_booking.voucher.dto.VoucherCalculationRequestDto;
import technical.assessment.convert_tickets_booking.voucher.dto.VoucherCalculationResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherHistoryRepository voucherHistoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Voucher createVoucher(Voucher voucher) {
        return voucherRepository.save(voucher);
    }

    @Override
    public Voucher getVoucherByCode(String code) {
        return voucherRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found with code: " + code));
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherCalculationResponseDto calculateDiscount(VoucherCalculationRequestDto requestDto) {
        Voucher voucher = getVoucherByCode(requestDto.getVoucherCode());
        
        if (voucher.getExpiryDate() != null && voucher.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Voucher has expired");
        }
        
        if (voucher.getMaxUsage() != null && voucher.getCurrentUsage() >= voucher.getMaxUsage()) {
            throw new BusinessException("Global voucher usage limit reached");
        }
        
        int userUsageCount = voucherHistoryRepository.countByUserIdAndVoucherId(requestDto.getUserId(), voucher.getId());
        if (userUsageCount >= voucher.getMaxUsagePerUser()) {
            throw new BusinessException("You have reached the maximum usage limit for this voucher");
        }
        
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal originalPrice = requestDto.getOriginalPrice();

        if (voucher.getDiscountAmount() != null) {
            discountAmount = voucher.getDiscountAmount();
        }

        if (discountAmount.compareTo(originalPrice) > 0) {
            discountAmount = originalPrice;
        }

        BigDecimal finalPrice = originalPrice.subtract(discountAmount);

        return VoucherCalculationResponseDto.builder()
                .voucherId(voucher.getId())
                .voucherCode(voucher.getCode())
                .originalPrice(originalPrice)
                .discountAmount(discountAmount)
                .finalPrice(finalPrice)
                .build();
    }

    @Override
    public Page<Voucher> getAllVouchers(Pageable pageable) {
        return voucherRepository.findAll(pageable);
    }
}
