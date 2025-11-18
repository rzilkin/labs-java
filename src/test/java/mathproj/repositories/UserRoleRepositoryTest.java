package mathproj.repositories;

import mathproj.entities.Role;
import mathproj.entities.User;
import mathproj.entities.UserRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserRoleRepositoryTest extends RepositoryIntegrationTestSupport {

    @Test
    void userRoleRepositoryConnectsUsersAndRoles() {
        userRoleRepository.deleteAll();

        User owner = userRepository.save(newUser("owner"));
        Role admin = roleRepository.save(newRole("ADMIN"));
        List<Role> others = roleRepository.saveAll(List.of(newRole("VIEWER"), newRole("AUDITOR")));

        UserRole adminLink = userRoleRepository.save(newUserRole(owner, admin));
        userRoleRepository.saveAll(List.of(newUserRole(owner, others.get(0)), newUserRole(owner, others.get(1))));

        assertEquals(3, userRoleRepository.count());
        assertEquals(3, userRoleRepository.findAll().size());

        List<UserRole> links = userRoleRepository.findAll();
        long ownerLinks = links.stream()
                .filter(link -> link.getUser().getId().equals(owner.getId()))
                .count();
        assertEquals(3, ownerLinks);

        long adminLinks = links.stream()
                .filter(link -> link.getRole().getCode().equals("ADMIN"))
                .count();
        assertEquals(1, adminLinks);

        userRoleRepository.delete(links.stream()
                .filter(link -> link.getRole().getCode().equals("AUDITOR"))
                .findFirst()
                .orElseThrow());
        assertEquals(2, userRoleRepository.count());

        userRoleRepository.delete(adminLink);
        assertEquals(1, userRoleRepository.count());

        userRoleRepository.deleteAll();
        assertEquals(0, userRoleRepository.count());
    }
}