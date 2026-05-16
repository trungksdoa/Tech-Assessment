package technical.assessment.convert_tickets_booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ConvertTicketsBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConvertTicketsBookingApplication.class, args);
    }

}
