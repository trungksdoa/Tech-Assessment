package technical.assessment.convert_tickets_booking.voucher.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import technical.assessment.convert_tickets_booking.booking.model.Booking;
import technical.assessment.convert_tickets_booking.shared.exception.BusinessException;
import technical.assessment.convert_tickets_booking.voucher.model.Voucher;
import technical.assessment.convert_tickets_booking.voucher.model.VoucherHistory;
import technical.assessment.convert_tickets_booking.voucher.repository.VoucherHistoryRepository;
import technical.assessment.convert_tickets_booking.voucher.repository.VoucherRepository;
import technical.assessment.convert_tickets_booking.voucher.service.VoucherProcessingService;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherProcessingServiceImpl implements VoucherProcessingService {

    private final VoucherRepository voucherRepository;
    private final VoucherHistoryRepository voucherHistoryRepository;

    @Override
    public Voucher applyVoucher(Booking booking, Integer userId, String voucherCode) {
        if (voucherCode == null || voucherCode.trim().isEmpty()) {
            return null;
        }

        log.info("[VOUCHER] Applying voucher code: {}", voucherCode);
        Voucher appliedVoucher = voucherRepository.findByCode(voucherCode)
                .orElseThrow(() -> new BusinessException("Voucher not found: " + voucherCode));

        if (appliedVoucher.getExpiryDate() != null && appliedVoucher.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessException("Voucher is expired.");
        }

        int userUsageCount = voucherHistoryRepository.countByUserIdAndVoucherId(userId, appliedVoucher.getId());
        if (appliedVoucher.getMaxUsagePerUser() != null && userUsageCount >= appliedVoucher.getMaxUsagePerUser()) {
            throw new BusinessException("User has reached the maximum usage limit for this voucher.");
        }

        int updatedVouchers = voucherRepository.incrementUsage(appliedVoucher.getId());
        if (updatedVouchers == 0) {
            throw new BusinessException("Voucher has reached its maximum usage limit.");
        }

        BigDecimal newTotal = booking.getTotalPrice().subtract(appliedVoucher.getDiscountAmount());
        if (newTotal.compareTo(BigDecimal.ZERO) < 0) {
            newTotal = BigDecimal.ZERO;
        }
        
        booking.setTotalPrice(newTotal);
        log.info("[VOUCHER] Voucher applied. New Total Price: {}", booking.getTotalPrice());
        
        return appliedVoucher;
    }

    @Override
    public void saveVoucherHistory(Integer userId, Voucher appliedVoucher, Booking savedBooking) {
        if (appliedVoucher != null) {
            VoucherHistory history = VoucherHistory.builder()
                    .userId(userId)
                    .voucher(appliedVoucher)
                    .booking(savedBooking)
                    .build();
            voucherHistoryRepository.save(history);
            log.info("[VOUCHER] Saved voucher history for user ID: {} and voucher ID: {}", userId, appliedVoucher.getId());
        }
    }
}
