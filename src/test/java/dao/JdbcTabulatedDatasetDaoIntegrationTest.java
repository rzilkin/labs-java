package dao;

import dto.MathFunction;
import dto.TabulatedDataset;
import dto.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JdbcTabulatedDatasetDaoIntegrationTest extends AbstractDaoIntegrationTest {
    private JdbcUserDao userDao;
    private JdbcMathFunctionDao mathFunctionDao;
    private JdbcTabulatedDatasetDao datasetDao;

    @BeforeEach
    void setUpDao() {
        userDao = new JdbcUserDao(connectionManager);
        mathFunctionDao = new JdbcMathFunctionDao(connectionManager);
        datasetDao = new JdbcTabulatedDatasetDao(connectionManager);
    }

    @Test
    void shouldHandleCrudForDatasets() {
        User owner = createRandomUser();
        MathFunction function = createRandomFunction(owner.getId());

        TabulatedDataset dataset = new TabulatedDataset();
        dataset.setFunctionId(function.getId());
        dataset.setSourceType("MANUAL");

        TabulatedDataset created = datasetDao.create(dataset);
        assertNotNull(created.getId());

        TabulatedDataset loaded = datasetDao.findById(created.getId()).orElseThrow();
        assertEquals("MANUAL", loaded.getSourceType());

        assertEquals(1, datasetDao.findByFunctionId(function.getId()).size());

        created.setSourceType("GENERATED");
        assertTrue(datasetDao.update(created));
        assertEquals("GENERATED", datasetDao.findById(created.getId()).orElseThrow().getSourceType());

        assertTrue(datasetDao.delete(created.getId()));
        assertTrue(datasetDao.findByFunctionId(function.getId()).isEmpty());
    }

    private User createRandomUser() {
        User user = new User();
        user.setUsername("user_" + UUID.randomUUID());
        user.setPasswordHash("pwd_" + UUID.randomUUID());
        return userDao.create(user);
    }

    private MathFunction createRandomFunction(Long ownerId) {
        MathFunction function = new MathFunction();
        function.setOwnerId(ownerId);
        function.setName("func_" + UUID.randomUUID());
        function.setFunctionType("ANALYTIC");
        function.setDefinitionBody("{\"body\":\"" + UUID.randomUUID() + "\"}");
        return mathFunctionDao.create(function);
    }
}
