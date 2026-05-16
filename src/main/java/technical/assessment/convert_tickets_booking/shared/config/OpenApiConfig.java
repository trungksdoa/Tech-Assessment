package technical.assessment.convert_tickets_booking.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return org.springdoc.core.models.GroupedOpenApi.builder()
                .group("concert-booking-public")
                .packagesToScan("technical.assessment.convert_tickets_booking")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public OpenAPI concertTicketBookingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Concert Ticket Booking API")
                        .description("Backend API for the Concert Ticket Booking Platform - Technical Assessment")
                        .version("v0.0.1")
                        .contact(new Contact()
                                .name("Technical Assessment Team")
                                .email("adventure@geekup.vn"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}
