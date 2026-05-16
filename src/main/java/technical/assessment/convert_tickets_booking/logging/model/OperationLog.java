package technical.assessment.convert_tickets_booking.logging.model;

import technical.assessment.convert_tickets_booking.booking.model.Booking;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import technical.assessment.convert_tickets_booking.logging.enums.OperationAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "operation_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "operator_id")
    private Integer operatorId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;
    
    @Enumerated(EnumType.STRING)
    private OperationAction action;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}