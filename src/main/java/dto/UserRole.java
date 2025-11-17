package dto;

import java.util.Objects;

public class UserRole {
    private Long userId;
    private String roleCode;

    public UserRole() {}

    public UserRole(Long userId, String roleCode) {
        this.userId = userId;
        this.roleCode = roleCode;
    }

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getRoleCode() {
        return roleCode;
    }
    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRole userRole = (UserRole) o;
        return Objects.equals(userId, userRole.userId) &&
                Objects.equals(roleCode, userRole.roleCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roleCode);
    }

    @Override
    public String toString() {
        return "Роль пользователя{пользователь = " + userId + ", роль = '" + roleCode + "'}";
    }
}