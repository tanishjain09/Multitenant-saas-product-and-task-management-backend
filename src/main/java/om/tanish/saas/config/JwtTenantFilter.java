package om.tanish.saas.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import om.tanish.saas.security.JwtService;
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
                || path.equals("/api/v1/user/create")
                || path.equals("/api/v1/user/new")
                || path.equals("/api/v1/tenant/create")
                || path.startsWith("/h2-console")
                || path.equals("/ping")
                || path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.debug("Missing or invalid Authorization header for path: {}", request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Missing or invalid Authorization header\"}");
                return;
            }

            String token = authHeader.substring(7);
            Claims claims = jwtService.extractClaims(token);

            UUID userId = UUID.fromString(claims.getSubject());
            UUID tenantId = UUID.fromString(claims.get("tenantId").toString());

            logger.debug("JWT Filter - User ID: {}, Tenant ID: {}", userId, tenantId);

            // Set tenant context
            TenantContext.setTenant(tenantId);
            logger.debug("Tenant context set: {}", TenantContext.getTenant());

            String role = claims.get("role").toString();
            List<GrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority("ROLE_" + role));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Continue with the request
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            logger.warn("Token expired for path: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token expired\"}");
        } catch (JwtException e) {
            logger.warn("Invalid token for path {}: {}", request.getRequestURI(), e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid token\"}");
        } catch (Exception e) {
            logger.error("Authentication failed for path {}: {}", request.getRequestURI(), e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Authentication failed\"}");
        } finally {
            // Clear context AFTER response is sent
            SecurityContextHolder.clearContext();
        }
        TenantContext.clear();
    }
}