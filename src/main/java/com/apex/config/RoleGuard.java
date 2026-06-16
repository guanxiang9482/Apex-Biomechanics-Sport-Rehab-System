package com.apex.config;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.apex.domain.Role;
import com.apex.domain.User;
import com.apex.repository.interfaces.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * RBAC Interceptor — Fix 1
 *
 * Reads X-User-Id header sent by frontend on every request.
 * Looks up the user's role from DB and validates it against
 * the requested path prefix.
 *
 * Path rules:
 *   /api/auth/**       → public (no check)
 *   /api/admin/**      → ADMIN only
 *   /api/therapist/**  → THERAPIST or ADMIN
 *   /api/athlete/**    → ATHLETE or ADMIN
 */
@Component
public class RoleGuard implements HandlerInterceptor {

    private final UserRepository userRepository;

    public RoleGuard(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String path = request.getRequestURI();

        // Public — skip check entirely
        if (path.startsWith("/api/auth/")) return true;

        // All other /api/** require X-User-Id header
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.isBlank()) {
            response.sendError(403,
                "Access denied: X-User-Id header is missing.");
            return false;
        }

        int userId;
        try {
            userId = Integer.parseInt(userIdHeader);
        } catch (NumberFormatException e) {
            response.sendError(403,
                "Access denied: invalid X-User-Id header.");
            return false;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty() || !userOpt.get().isActive()) {
            response.sendError(403,
                "Access denied: user not found or account disabled.");
            return false;
        }

        Role role = userOpt.get().getRole();

        // Admin paths — ADMIN only
        if (path.startsWith("/api/admin/") && role != Role.ADMIN) {
            response.sendError(403,
                "Access denied: Administrator role required.");
            return false;
        }

        // Therapist paths — THERAPIST or ADMIN
        if (path.startsWith("/api/therapist/") &&
                role != Role.THERAPIST && role != Role.ADMIN) {
            response.sendError(403,
                "Access denied: Physiotherapist role required.");
            return false;
        }

        // Athlete paths — ATHLETE or ADMIN
        if (path.startsWith("/api/athlete/") &&
                role != Role.ATHLETE && role != Role.ADMIN) {
            response.sendError(403,
                "Access denied: Athlete role required.");
            return false;
        }

        return true;  // RBAC passed — allow request
    }
}
