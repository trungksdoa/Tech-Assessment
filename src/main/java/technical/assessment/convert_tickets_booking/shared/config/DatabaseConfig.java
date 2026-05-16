package technical.assessment.convert_tickets_booking.shared.config; 
 
import org.springframework.context.annotation.Configuration; 
import org.springframework.data.jpa.repository.config.EnableJpaRepositories; 
import org.springframework.transaction.annotation.EnableTransactionManagement; 
 
@Configuration 
@EnableJpaRepositories(basePackages = "technical.assessment.convert_tickets_booking") 
@EnableTransactionManagement 
public class DatabaseConfig { 
} 
