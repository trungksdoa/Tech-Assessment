package technical.assessment.convert_tickets_booking.voucher.service;

import technical.assessment.convert_tickets_booking.booking.model.Booking;
import technical.assessment.convert_tickets_booking.voucher.model.Voucher;

public interface VoucherProcessingService {

    /**
     * Validates and applies a voucher to a booking, updating the booking's total price.
     *
     * @param booking     The current booking being processed.
     * @param userId      The ID of the user making the booking.
     * @param voucherCode The code of the promotional voucher.
     * @return The applied Voucher entity, or null if no code is provided.
     */
    Voucher applyVoucher(Booking booking, Integer userId, String voucherCode);

    /**
     * Saves the usage history of an applied voucher after the booking has been persisted.
     *
     * @param userId         The ID of the user.
     * @param appliedVoucher The voucher that was applied.
     * @param savedBooking   The successfully persisted booking.
     */
    void saveVoucherHistory(Integer userId, Voucher appliedVoucher, Booking savedBooking);
}
