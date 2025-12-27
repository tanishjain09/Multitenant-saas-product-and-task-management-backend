package om.tanish.saas.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> resetTimes = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 100;
    private static final long TIME_WINDOW = 60000; //1min


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String clienIp = request.getRemoteAddr();
        long currentTime = System.currentTimeMillis();
        resetTimes.putIfAbsent(clienIp, currentTime + TIME_WINDOW);
        requestCounts.putIfAbsent(clienIp, new AtomicInteger(0));

        if(currentTime > resetTimes.get(clienIp)){
            requestCounts.get(clienIp).set(0);
            resetTimes.put(clienIp, currentTime + TIME_WINDOW);
        }

        if(requestCounts.get(clienIp).incrementAndGet() > MAX_REQUESTS){
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        return request.getRequestURI().startsWith("/h2-console");
    }
}
