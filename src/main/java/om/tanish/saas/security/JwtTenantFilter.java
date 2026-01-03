package om.tanish.saas.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import om.tanish.saas.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtTenantFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtTenantFilter.class);

    private final JwtService jwtService;

    public JwtTenantFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/h2-console")
                || path.equals("/ping")
                || path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                unauthorized(response, "Missing or invalid Authorization header");
                return;
            }

            String token = authHeader.substring(7);
            Claims claims = jwtService.extractClaims(token);

            // ---------------- USER ID ----------------
            UUID userId = UUID.fromString(claims.getSubject());

            // ---------------- TENANT CONTEXT (OPTIONAL) ----------------
            if (claims.containsKey("tenantId")) {
                UUID tenantId = UUID.fromString(claims.get("tenantId").toString());
                TenantContext.setTenant(tenantId);
                logger.debug("Tenant context set: {}", tenantId);
            }

            // ---------------- ROLE ----------------
            String role = claims.get("role").toString();
            List<GrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority("ROLE_" + role));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Continue request
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            logger.warn("Token expired for path: {}", request.getRequestURI());
            unauthorized(response, "Token expired");

        } catch (JwtException e) {
            logger.warn("Invalid token for path {}: {}", request.getRequestURI(), e.getMessage());
            unauthorized(response, "Invalid token");

        } catch (Exception e) {
            logger.error("Authentication failed for path {}: {}", request.getRequestURI(), e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Authentication failed\"}");

        } finally {
            TenantContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
