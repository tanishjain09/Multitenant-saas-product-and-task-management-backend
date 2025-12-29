package om.tanish.saas.security;

import om.tanish.saas.config.JwtTenantFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtTenantFilter jwtTenantFilter;
    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(JwtTenantFilter jwtTenantFilter, RateLimitFilter rateLimitFilter) {
        this.jwtTenantFilter = jwtTenantFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/login",
                                "/auth/refresh",
                                "/auth/logout",
                                "/h2-console/**",
                                "/api/v1/user/create",
                                "/api/v1/user/new",
                                "/api/v1/tenant/create",
                                "/ping"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtTenantFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, JwtTenantFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}