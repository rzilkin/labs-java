package mathproj.repositories;

import mathproj.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    List<Role> findAllByOrderByCodeAsc();

    Optional<Role> findByCode(String code);

    @Query("SELECT r FROM Role r WHERE r.code LIKE %:code%")
    List<Role> findByCodeContaining(String code);

    @Query("SELECT r FROM Role r WHERE r.description LIKE %:description%")
    List<Role> findByDescriptionContaining(String description);

    @Query("SELECT r FROM Role r WHERE LOWER(r.description) LIKE LOWER(CONCAT('%', :description, '%')) ORDER BY r.code")
    List<Role> findByDescriptionContainingIgnoreCaseOrderByCode(String description);

    @Query("SELECT DISTINCT r FROM Role r JOIN r.userRoles ur WHERE SIZE(r.userRoles) > 0 ORDER BY r.code")
    List<Role> findRolesWithUsers();

    @Query("SELECT r, COUNT(ur) as userCount FROM Role r LEFT JOIN r.userRoles ur GROUP BY r ORDER BY userCount DESC")
    List<Object[]> findRolesByPopularity();
}