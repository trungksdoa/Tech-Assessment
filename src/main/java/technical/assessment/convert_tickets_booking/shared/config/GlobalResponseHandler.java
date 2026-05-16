package technical.assessment.convert_tickets_booking.shared.config;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import technical.assessment.convert_tickets_booking.shared.model.ApiResponse;

@RestControllerAdvice(basePackages = "technical.assessment.convert_tickets_booking")
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Do not wrap if it's already an ApiResponse or if it's a Swagger/OpenAPI response
        return !returnType.getParameterType().equals(ApiResponse.class) &&
               !returnType.getDeclaringClass().getSimpleName().contains("OpenApi") &&
               !returnType.getDeclaringClass().getSimpleName().contains("Swagger");
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        
        // Get the actual HTTP status code
        int status = 200;
        if (response instanceof org.springframework.http.server.ServletServerHttpResponse) {
            status = ((org.springframework.http.server.ServletServerHttpResponse) response).getServletResponse().getStatus();
        }

        // If body is null, still return a success wrapper
        if (body == null) {
            return new ApiResponse<>(status, status < 400 ? "Success" : "Error", null);
        }

        // If body is already an ApiResponse (from exception handler), just return it
        if (body instanceof ApiResponse) {
            return body;
        }
        
        // Handle String return types separately because they use StringHttpMessageConverter
        if (body instanceof String) {
            return body;
        }

        return new ApiResponse<>(status, status < 400 ? "Success" : "Error", body);
    }
}
