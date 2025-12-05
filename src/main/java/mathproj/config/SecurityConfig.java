package mathproj.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import mathproj.dto.ErrorResponse;
import mathproj.web.HttpErrorMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Instant;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, ObjectMapper om) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/register").permitAll()
                        .requestMatchers("/api/v1/auth/login").authenticated()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                writeError(om, request, response, HttpStatus.UNAUTHORIZED, "Не авторизован"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeError(om, request, response, HttpStatus.FORBIDDEN, "Доступ запрещён"))
                )
                .build();
    }

    private void writeError(ObjectMapper om,
                            HttpServletRequest request,
                            jakarta.servlet.http.HttpServletResponse response,
                            HttpStatus status,
                            String message) throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status.value(),
                HttpErrorMapper.fromStatus(status.value()),
                message,
                request.getRequestURI()
        );
        om.writeValue(response.getOutputStream(), body);
    }
}




