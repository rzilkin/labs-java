package mathproj.repositories;

import mathproj.entities.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest extends RepositoryIntegrationTestSupport {

    @Test
    void userRepositorySupportsFullCrudLifecycle() {
        userRepository.deleteAll();

        User alice = userRepository.save(newUser("alice"));
        userRepository.saveAll(List.of(newUser("bob"), newUser("carol")));

        assertEquals(3, userRepository.count());
        assertEquals(3, userRepository.findAll().size());

        Optional<User> aliceLookup = userRepository.findById(alice.getId());
        assertTrue(aliceLookup.isPresent());

        long containsLetterO = userRepository.findAll().stream()
                .filter(user -> user.getUsername().toLowerCase().contains("o"))
                .count();
        assertEquals(2, containsLetterO);

        userRepository.delete(userRepository.findAll().stream()
                .filter(user -> user.getUsername().equals("carol"))
                .findFirst()
                .orElseThrow());
        assertEquals(2, userRepository.count());

        userRepository.delete(alice);
        assertEquals(1, userRepository.count());

        userRepository.deleteAll();
        assertEquals(0, userRepository.count());
    }
}