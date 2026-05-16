package technical.assessment.convert_tickets_booking.voucher.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "vouchers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @NotBlank(message = "Voucher code is required")
    @Column(unique = true, nullable = false)
    private String code;
    
    @NotNull(message = "Discount amount is required")
    @Min(value = 0, message = "Discount amount must be non-negative")
    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount;
    
    @Min(value = 1, message = "Max usage must be at least 1")
    @Column(name = "max_usage")
    private Integer maxUsage;
    
    @Column(name = "current_usage")
    @Builder.Default
    private Integer currentUsage = 0;
    
    @Min(value = 1, message = "Max usage per user must be at least 1")
    @Column(name = "max_usage_per_user")
    @Builder.Default
    private Integer maxUsagePerUser = 1;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VoucherHistory> voucherHistories;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}