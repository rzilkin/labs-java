package dao;

import dto.UserRole;

import java.util.List;

public interface UserRoleDao {
    UserRole create(UserRole userRole);

    List<String> findRolesByUserId(Long userId);

    List<UserRole> findByUserId(Long userId);

    boolean delete(Long userId, String roleCode);

    boolean deleteAllByUserId(Long userId);

    boolean exists(Long userId, String roleCode);
}

