package om.tanish.saas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {
    
    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return () -> {
            Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
            return principal instanceof UUID ? 
                Optional.of((UUID) principal) : Optional.empty();
        };
    }
}