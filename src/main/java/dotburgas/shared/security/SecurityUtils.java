package dotburgas.shared.security;

import jakarta.servlet.http.HttpSession;

public final class SecurityUtils {

    // private constructor to prevent instantiation
    private SecurityUtils() {
    }

    public static boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return "ADMIN".equals(role);
    }
}
