package dotburgas.shared.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class SessionCheckInterceptor implements HandlerInterceptor {

    // preHandle
    // postHandle
    // afterCompletion

    private final Set<String> UNAUTHENTICATED_ENDPOINTS = Set
            .of("/",
                    "/login",
                    "/register",
                    "/discover-burgas",
                    "/accommodation",
                    "/about",
                    "/contact",
                    "/privacy");

    // This method will proceed without any request
    // HttpServletRequest request - the request that is sent to our application.
    // HttpServletRequest response - the response we return. 
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //Endpoint 
        String endpoint = request.getServletPath();
        if (UNAUTHENTICATED_ENDPOINTS.contains(endpoint)) {
            // if it requires to access endpoint that does not require session, we allow this request to go through.
            return true;
        }

        // getting the session if we don't have it we create new session.
        // request.getSession(false), if there is no session returns null
        HttpSession currentUserSession = request.getSession(false);
        if (currentUserSession == null) {

            // we send him to login page in order not to leave him with a blank page and require him to login.
            response.sendRedirect("/login");
            return false;
        }

        return true;
    }
}
