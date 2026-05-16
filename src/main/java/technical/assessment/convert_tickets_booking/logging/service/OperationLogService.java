package technical.assessment.convert_tickets_booking.logging.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import technical.assessment.convert_tickets_booking.logging.model.OperationLog;

public interface OperationLogService {
    
    /**
     * Records an operation log entry asynchronously to avoid blocking the main transaction.
     * Optimization: Uses EntityManager.getReference to associate the log with a booking
     * without performing an extra database SELECT query.
     *
     * @param bookingId ID of the related booking.
     * @param operatorId ID of the person/system performing the action.
     * @param action The type of action performed (converted to OperationAction enum).
     * @param notes Additional details about the operation.
     */
    void logOperation(Integer bookingId, Integer operatorId, String action, String notes);

    /**
     * Retrieves all history logs for a specific booking.
     *
     * @param bookingId ID of the booking.
     * @return A list of OperationLog entries.
     */
    Page<OperationLog> getLogsByBookingId(Integer bookingId, Pageable pageable);
}
