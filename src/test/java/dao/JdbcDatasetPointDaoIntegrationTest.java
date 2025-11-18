package dao;

import dto.DatasetPoint;
import dto.MathFunction;
import dto.TabulatedDataset;
import dto.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

class JdbcDatasetPointDaoIntegrationTest extends AbstractDaoIntegrationTest {
    private JdbcUserDao userDao;
    private JdbcMathFunctionDao mathFunctionDao;
    private JdbcTabulatedDatasetDao datasetDao;
    private JdbcDatasetPointDao pointDao;

    @BeforeEach
    void setUpDao() {
        userDao = new JdbcUserDao(connectionManager);
        mathFunctionDao = new JdbcMathFunctionDao(connectionManager);
        datasetDao = new JdbcTabulatedDatasetDao(connectionManager);
        pointDao = new JdbcDatasetPointDao(connectionManager);
    }

    @Test
    void shouldInsertUpdateAndDeleteDatasetPoints() {
        User owner = createRandomUser();
        MathFunction function = createRandomFunction(owner.getId());
        TabulatedDataset dataset = createDataset(function.getId());

        for (int i = 0; i < 5; i++) {
            pointDao.upsert(randomPoint(dataset.getId(), i));
        }

        List<DatasetPoint> points = pointDao.findByDatasetId(dataset.getId());
        assertEquals(5, points.size());

        DatasetPoint updatedPoint = randomPoint(dataset.getId(), 2);
        pointDao.upsert(updatedPoint);
        DatasetPoint afterUpdate = pointDao.findByDatasetId(dataset.getId()).get(2);
        assertEquals(updatedPoint.getXValue(), afterUpdate.getXValue());

        assertTrue(pointDao.deletePoint(dataset.getId(), 1));
        assertEquals(4L, pointDao.countByDataset(dataset.getId()));

        assertEquals(4, pointDao.deleteAllByDataset(dataset.getId()));
        assertEquals(0L, pointDao.countByDataset(dataset.getId()));
    }

    private DatasetPoint randomPoint(Long datasetId, int index) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        BigDecimal x = BigDecimal.valueOf(random.nextDouble(-1000, 1000))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal y = BigDecimal.valueOf(random.nextDouble(-1000, 1000))
                .setScale(2, RoundingMode.HALF_UP);
        return new DatasetPoint(datasetId, index, x, y);
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

    private TabulatedDataset createDataset(Long functionId) {
        TabulatedDataset dataset = new TabulatedDataset();
        dataset.setFunctionId(functionId);
        dataset.setSourceType("MANUAL");
        return datasetDao.create(dataset);
    }
}
