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
                || path.equals("/ping");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Missing or invalid Authorization header\"}");
                return;
            }

            String token = authHeader.substring(7);
            Claims claims = jwtService.extractClaims(token);

            UUID userId = UUID.fromString(claims.getSubject());
            UUID tenantId = UUID.fromString(claims.get("tenantId").toString());

            System.out.println("🔐 JWT Filter:");
            System.out.println("   User ID: " + userId);
            System.out.println("   Tenant ID: " + tenantId);

            // Set tenant context
            TenantContext.setTenant(tenantId);
            System.out.println("✅ Tenant context set: " + TenantContext.getTenant());

            String role = claims.get("role").toString();
            List<GrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority("ROLE_" + role));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);


            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            System.out.println("❌ Token expired");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token expired\"}");
        } catch (JwtException e) {
            System.out.println("❌ Invalid token: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid token\"}");
        } catch (Exception e) {
            System.out.println("❌ Authentication failed: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Authentication failed\"}");
        } finally {
            // Clear context AFTER request completes
            TenantContext.clear();
            SecurityContextHolder.clearContext();
        }
    }
}