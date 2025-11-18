package db;

import java.util.Objects;

public final class DatabaseConfig {
    private final String url;
    private final String username;
    private final String password;

    public DatabaseConfig(String url, String username, String password) {
        this.url = Objects.requireNonNull(url, "URL подключения к БД не может быть null");
        this.username = Objects.requireNonNull(username, "Имя пользователя БД не может быть null");
        this.password = Objects.requireNonNull(password, "Пароль к БД не может быть null");
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}