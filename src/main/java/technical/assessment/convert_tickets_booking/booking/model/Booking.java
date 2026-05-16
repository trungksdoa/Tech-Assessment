package technical.assessment.convert_tickets_booking.booking.model; 
 
import jakarta.persistence.*; 
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor; 
import lombok.Builder; 
import lombok.Data; 
import lombok.NoArgsConstructor; 
 
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.math.BigDecimal; 
import java.time.LocalDateTime; 
import java.util.List; 
 
@Entity 
@Table(name = "bookings") 
@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class Booking { 
 
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Integer id; 
 
    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id", nullable = false) 
    private Integer userId; 
 
    @Column(name = "total_price", precision = 12, scale = 2) 
    private BigDecimal totalPrice; 
 
    @Enumerated(EnumType.STRING) 
    private BookingStatus status; 
 
    @jakarta.validation.constraints.NotBlank(message = "Idempotency key is required")
    @Column(name = "idempotency_key", unique = true, nullable = false) 
    private String idempotencyKey; 
 
    @Column(name = "created_at", updatable = false) 
    private LocalDateTime createdAt; 
 
    @Column(name = "updated_at") 
    private LocalDateTime updatedAt; 
 
    @jakarta.validation.Valid
    @jakarta.validation.constraints.NotEmpty(message = "Booking must have at least one item")
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY) 
    @JsonManagedReference
    private List<BookingItem> bookingItems; 
 
    @PrePersist 
    protected void onCreate() { 
        createdAt = LocalDateTime.now(); 
        updatedAt = LocalDateTime.now(); 
    } 
 
    @PreUpdate 
    protected void onUpdate() { 
        updatedAt = LocalDateTime.now(); 
    } 
} 
