package com.kelox.backend.config;

import com.kelox.backend.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class AdminSecurityInterceptor implements HandlerInterceptor {
    
    @Value("${admin.secret-code}")
    private String adminSecretCode;
    
    private static final String ADMIN_SECRET_HEADER = "X-Admin-Secret";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        
        // Allow OPTIONS requests for CORS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        
        String providedSecret = request.getHeader(ADMIN_SECRET_HEADER);
        
        if (providedSecret == null || providedSecret.trim().isEmpty()) {
            log.warn("Admin API access attempt without secret code from IP: {}", request.getRemoteAddr());
            throw new UnauthorizedException("Admin secret code is required in header: " + ADMIN_SECRET_HEADER);
        }
        
        if (!adminSecretCode.equals(providedSecret)) {
            log.warn("Admin API access attempt with invalid secret code from IP: {}", request.getRemoteAddr());
            throw new UnauthorizedException("Invalid admin secret code");
        }
        
        log.debug("Admin API access granted");
        return true;
    }
}

