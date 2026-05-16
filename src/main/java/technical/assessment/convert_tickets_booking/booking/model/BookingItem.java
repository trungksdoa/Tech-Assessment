package technical.assessment.convert_tickets_booking.booking.model; 
 
import technical.assessment.convert_tickets_booking.concert.model.TicketCategory; 
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*; 
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor; 
import lombok.Builder; 
import lombok.Data; 
import lombok.NoArgsConstructor; 
 
import java.math.BigDecimal; 
 
@Entity 
@Table(name = "booking_items") 
@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class BookingItem { 
 
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Integer id; 
 
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "booking_id") 
    @JsonBackReference
    private Booking booking; 
 
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "ticket_category_id") 
    private TicketCategory ticketCategory; 
 
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(nullable = false) 
    private Integer quantity; 
 
    @Column(name = "unit_price", precision = 12, scale = 2) 
    private BigDecimal unitPrice; 
} 
