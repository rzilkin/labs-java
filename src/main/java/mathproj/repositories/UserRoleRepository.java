package mathproj.repositories;

import mathproj.entities.UserRole;
import mathproj.entities.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
}