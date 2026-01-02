package om.tanish.saas.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);


    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> resetTimes = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 100;
    private static final long TIME_WINDOW = 60000; // 1 minute

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = getClientIp(request);
        long currentTime = System.currentTimeMillis();

        resetTimes.putIfAbsent(clientIp, currentTime + TIME_WINDOW);
        requestCounts.putIfAbsent(clientIp, new AtomicInteger(0));

        if (currentTime > resetTimes.get(clientIp)) {
            requestCounts.get(clientIp).set(0);
            resetTimes.put(clientIp, currentTime + TIME_WINDOW);
            logger.debug("Rate limit counter reset for IP: {}", clientIp);
        }

        int currentCount = requestCounts.get(clientIp).incrementAndGet();

        if (currentCount > MAX_REQUESTS) {
            logger.warn("Rate limit exceeded for IP: {} (count: {})", clientIp, currentCount);
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            return;
        }

        logger.trace("Request allowed for IP: {} (count: {}/{})", clientIp, currentCount, MAX_REQUESTS);
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth/login")
                || path.startsWith("/auth/refresh")
                || path.startsWith("/h2-console");
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            String clientIp = xForwardedFor.split(",")[0].trim();
            if(isValidIpAddress(clientIp)) return clientIp;
        }
        return request.getRemoteAddr();
    }
    private boolean isValidIpAddress(String ip) {
        String ipPattern =
                "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
                        "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip.matches(ipPattern);
    }
}