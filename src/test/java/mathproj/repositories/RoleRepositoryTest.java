package mathproj.repositories;

import mathproj.entities.Role;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RoleRepositoryTest extends RepositoryIntegrationTestSupport {

    @Test
    void roleRepositoryManagesDescriptionsAndCodes() {
        roleRepository.deleteAll();

        Role admin = roleRepository.save(newRole("ADMIN"));
        roleRepository.saveAll(List.of(newRole("ANALYST"), newRole("VIEWER")));

        assertEquals(3, roleRepository.count());
        List<Role> roles = roleRepository.findAll();
        assertEquals(3, roles.size());

        Optional<Role> foundAdmin = roleRepository.findById("ADMIN");
        assertTrue(foundAdmin.isPresent());

        long described = roles.stream()
                .filter(role -> role.getDescription().toLowerCase().contains("role"))
                .count();
        assertEquals(3, described);

        roleRepository.delete(roles.get(2));
        assertEquals(2, roleRepository.count());

        roleRepository.delete(admin);
        assertEquals(1, roleRepository.count());

        roleRepository.deleteAll();
        assertEquals(0, roleRepository.count());
    }
}