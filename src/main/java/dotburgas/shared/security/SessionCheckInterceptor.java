package dotburgas.shared.security;

import dotburgas.user.model.User;
import dotburgas.user.model.UserRole;
import dotburgas.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;
import java.util.UUID;

@Component
public class SessionCheckInterceptor implements HandlerInterceptor {

    // preHandle
    // postHandle
    // afterCompletion

    private final UserService userService;

    private final Set<String> UNAUTHENTICATED_ENDPOINTS = Set
            .of("/",
                    "/login",
                    "/register",
                    "/discover-burgas",
                    "/accommodation",
                    "/about",
                    "/contact",
                    "/privacy");

    @Autowired
    public SessionCheckInterceptor(UserService userService) {
        this.userService = userService;
    }

    // This method will proceed without any request
    // HttpServletRequest request - the request that is sent to our application.
    // HttpServletRequest response - the response we return. 
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Allow unauthenticated endpoints
        String endpoint = request.getServletPath();
        if (UNAUTHENTICATED_ENDPOINTS.contains(endpoint)) {
            return true;
        }

        // Skip static resource requests (e.g., CSS, JS, images)
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // Get the session (without creating a new one)
        HttpSession currentUserSession = request.getSession(false);
        if (currentUserSession == null) {
            response.sendRedirect("/login");
            return false;
        }

        UUID userID = (UUID) currentUserSession.getAttribute("user_id");
        User user = userService.getById(userID);

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        if (handlerMethod.hasMethodAnnotation(RequireAdminRole.class) && user.getRole() != UserRole.ADMIN) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("Access denied, you don't have the necessary permissions!");
            return false;
        }

        return true;
    }
}
