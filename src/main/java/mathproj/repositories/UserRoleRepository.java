package mathproj.repositories;

import mathproj.entities.UserRole;
import mathproj.entities.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId ORDER BY ur.role.code")
    List<UserRole> findByUserIdOrderByRoleCode(Long userId);

    @Query("SELECT ur FROM UserRole ur WHERE ur.role.code = :roleCode ORDER BY ur.user.username")
    List<UserRole> findByRoleCodeOrderByUserUsername(String roleCode);

    @Query("SELECT COUNT(ur) > 0 FROM UserRole ur WHERE ur.user.id = :userId AND ur.role.code = :roleCode")
    boolean existsByUserIdAndRoleCode(Long userId, String roleCode);

    @Query("SELECT ur FROM UserRole ur ORDER BY ur.user.username, ur.role.code")
    List<UserRole> findAllOrderByUserAndRole();

    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id IN :userIds ORDER BY ur.user.username, ur.role.code")
    List<UserRole> findByUserIdsOrderByUserAndRole(List<Long> userIds);

    @Query("SELECT ur FROM UserRole ur WHERE ur.role.code IN :roleCodes ORDER BY ur.role.code, ur.user.username")
    List<UserRole> findByRoleCodesOrderByRoleAndUser(List<String> roleCodes);

    @Query("SELECT ur.role.code, COUNT(ur) FROM UserRole ur GROUP BY ur.role.code ORDER BY COUNT(ur) DESC")
    List<Object[]> getRoleUsageStatistics();
}