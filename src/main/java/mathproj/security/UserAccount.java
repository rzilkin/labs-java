package mathproj.security;

import java.util.Set;

public class UserAccount {
    private final long id;
    private final String username;
    private final String passwordHash;
    private final Set<AppRole> roles;

    public UserAccount(long id, String username, String passwordHash, Set<AppRole> roles) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.roles = roles;
    }

    public long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Set<AppRole> getRoles() { return roles; }
}

