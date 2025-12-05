package mathproj.security;

public enum AppRole {
    USER, ADMIN;

    public String asAuthority() {
        return "ROLE_" + name();
    }
}

