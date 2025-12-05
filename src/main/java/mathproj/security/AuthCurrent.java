package mathproj.security;

import org.springframework.security.core.Authentication;

public final class AuthCurrent {
    private AuthCurrent() {}

    public static long userId(Authentication auth) {
        if (auth == null) throw new IllegalStateException("Нет аутентификации");

        Object p = auth.getPrincipal();
        if (p instanceof CustomUserDetails cud) return cud.getId();

        throw new IllegalStateException("Неожиданный тип principal: " +
                (p == null ? "null" : p.getClass().getName()));
    }
}



