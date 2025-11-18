package dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.MathFunction;
import dto.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JdbcMathFunctionDaoIntegrationTest extends AbstractDaoIntegrationTest {
    private JdbcUserDao userDao;
    private JdbcMathFunctionDao mathFunctionDao;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUpDao() {
        userDao = new JdbcUserDao(connectionManager);
        mathFunctionDao = new JdbcMathFunctionDao(connectionManager);
    }

    @Test
    void shouldPerformCrudOperationsForMathFunctions() throws Exception {
        User owner = createRandomUser();

        MathFunction function = new MathFunction();
        function.setOwnerId(owner.getId());
        function.setName("func_" + UUID.randomUUID());
        function.setFunctionType("ANALYTIC");
        function.setDefinitionBody("{\"body\":\"" + UUID.randomUUID() + "\"}");

        MathFunction created = mathFunctionDao.create(function);
        assertNotNull(created.getId());

        MathFunction loaded = mathFunctionDao.findById(created.getId()).orElseThrow();
        assertEquals(created.getName(), loaded.getName());

        List<MathFunction> ownerFunctions = mathFunctionDao.findByOwner(owner.getId());
        assertEquals(1, ownerFunctions.size());

        created.setName("updated_" + UUID.randomUUID());
        created.setDefinitionBody("{\"body\":\"" + UUID.randomUUID() + "\"}");
        assertTrue(mathFunctionDao.update(created));

        MathFunction updated = mathFunctionDao.findById(created.getId()).orElseThrow();
        assertEquals(created.getName(), updated.getName());

        JsonNode expectedJson = objectMapper.readTree(created.getDefinitionBody());
        JsonNode actualJson   = objectMapper.readTree(updated.getDefinitionBody());
        assertEquals(expectedJson, actualJson);

        assertTrue(mathFunctionDao.delete(created.getId()));
        assertTrue(mathFunctionDao.findByOwner(owner.getId()).isEmpty());
    }

    private User createRandomUser() {
        User user = new User();
        user.setUsername("user_" + UUID.randomUUID());
        user.setPasswordHash("pwd_" + UUID.randomUUID());
        return userDao.create(user);
    }
}
