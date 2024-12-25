package com.phanta.paicentre.config;
import com.phanta.paicentre.oauthToken.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableMethodSecurity // Enables role-based method security using annotations like @Secured or @PreAuthorize
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configure HTTP security for the application.
     * - Disable CSRF as we're using JWT (stateless authentication).
     * - Define public and restricted endpoints.
     * - Use a stateless session policy.
     * - Add the JWT authentication filter to validate tokens.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Updated approach for disabling CSRF
                .authorizeHttpRequests(authorize -> authorize
                        // Permit login and refresh token endpoints without authentication
                        .requestMatchers("/api/auth/login", "/api/auth/refresh", "/api/user/createDTO").permitAll()
                        // Restrict the secure endpoint to admin users only
                        .requestMatchers("/api/auth/secure").hasRole("ADMIN")
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // JWT is stateless; no session needed
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Add JWT filter before the default authentication filter

        return http.build();
    }

    /**
     * Password encoder bean to hash passwords securely.
     * Use this wherever passwords are stored or compared.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}