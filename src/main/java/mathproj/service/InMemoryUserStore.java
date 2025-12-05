package mathproj.service;

import mathproj.security.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class InMemoryUserStore {
    private final ConcurrentHashMap<String, UserAccount> byUsername = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);
    private final PasswordEncoder encoder;

    public InMemoryUserStore(PasswordEncoder encoder) {
        this.encoder = encoder;
        registerWithRoles("admin", "admin", Set.of(AppRole.ADMIN, AppRole.USER));
    }

    public UserAccount registerUser(String username, String rawPassword) {
        return registerWithRoles(username, rawPassword, Set.of(AppRole.USER));
    }

    public Optional<UserAccount> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return Optional.ofNullable(byUsername.get(username));
    }

    public UserAccount grantRole(String username, AppRole role) {
        UserAccount acc = byUsername.get(username);
        if (acc == null) throw new NoSuchElementException("Пользователь не найден");
        acc.getRoles().add(role);
        return acc;
    }

    private UserAccount registerWithRoles(String username, String rawPassword, Set<AppRole> roles) {
        if (username == null || username.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Имя пользователя и пароль обязательны");
        }
        String u = username.trim();
        if (byUsername.containsKey(u)) throw new IllegalStateException("Пользователь уже существует");

        long id = seq.getAndIncrement();
        UserAccount created = new UserAccount(id, u, encoder.encode(rawPassword), new HashSet<>(roles));
        UserAccount prev = byUsername.putIfAbsent(u, created);
        if (prev != null) throw new IllegalStateException("Пользователь уже существует");
        return created;
    }
}


