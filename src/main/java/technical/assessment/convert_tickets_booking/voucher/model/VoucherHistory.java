package technical.assessment.convert_tickets_booking.voucher.model;

import technical.assessment.convert_tickets_booking.booking.model.Booking;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "voucher_histories", indexes = {
    @Index(name = "idx_user_voucher", columnList = "user_id, voucher_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;
    
    @Column(name = "applied_at")
    private LocalDateTime appliedAt;
    
    @PrePersist
    protected void onCreate() {
        appliedAt = LocalDateTime.now();
    }
}