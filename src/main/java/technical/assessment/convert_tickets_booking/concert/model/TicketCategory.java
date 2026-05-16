package technical.assessment.convert_tickets_booking.concert.model; 

import jakarta.persistence.*; 
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor; 
import lombok.Builder; 
import lombok.Data; 
import lombok.NoArgsConstructor; 

import java.math.BigDecimal; 

@Entity 
@Table(name = "ticket_categories") 
@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class TicketCategory { 

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Integer id; 

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "concert_id") 
    @JsonBackReference
    private Concert concert; 

    private String name; 

    @Column(nullable = false, precision = 12, scale = 2) 
    private BigDecimal price; 

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;
} 
