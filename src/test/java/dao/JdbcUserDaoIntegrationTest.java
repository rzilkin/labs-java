package dao;

import dto.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JdbcUserDaoIntegrationTest extends AbstractDaoIntegrationTest {
    private JdbcUserDao userDao;

    @BeforeEach
    void setUpDao() {
        userDao = new JdbcUserDao(connectionManager);
    }

    @Test
    void shouldPerformFullCrudCycleWithRandomData() {
        User randomUser = new User();
        randomUser.setUsername("user_" + UUID.randomUUID());
        randomUser.setPasswordHash("pwd_" + UUID.randomUUID());

        User created = userDao.create(randomUser);
        assertNotNull(created.getId());

        Optional<User> loadedById = userDao.findById(created.getId());
        assertTrue(loadedById.isPresent());
        assertEquals(created.getUsername(), loadedById.get().getUsername());

        Optional<User> loadedByUsername = userDao.findByUsername(created.getUsername());
        assertTrue(loadedByUsername.isPresent());

        created.setUsername("updated_" + UUID.randomUUID());
        created.setPasswordHash("pwd_" + UUID.randomUUID());
        assertTrue(userDao.update(created));

        User updated = userDao.findById(created.getId()).orElseThrow();
        assertEquals(created.getUsername(), updated.getUsername());
        assertEquals(created.getPasswordHash(), updated.getPasswordHash());

        assertEquals(1, userDao.findAll().size());

        assertTrue(userDao.delete(created.getId()));
        assertTrue(userDao.findAll().isEmpty());
    }
}
