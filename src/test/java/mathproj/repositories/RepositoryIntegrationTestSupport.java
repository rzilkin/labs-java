package mathproj.repositories;

import mathproj.entities.DatasetPoint;
import mathproj.entities.FunctionComponent;
import mathproj.entities.MathFunction;
import mathproj.entities.PerformanceMetric;
import mathproj.entities.Role;
import mathproj.entities.TabulatedDataset;
import mathproj.entities.User;
import mathproj.entities.UserRole;
import mathproj.Main;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

@SpringBootTest(classes = Main.class)
public abstract class RepositoryIntegrationTestSupport {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected UserRoleRepository userRoleRepository;

    @Autowired
    protected MathFunctionRepository mathFunctionRepository;

    @Autowired
    protected TabulatedDatasetRepository tabulatedDatasetRepository;

    @Autowired
    protected DatasetPointRepository datasetPointRepository;

    @Autowired
    protected FunctionComponentRepository functionComponentRepository;

    @Autowired
    protected PerformanceMetricRepository performanceMetricRepository;

    @BeforeEach
    void cleanDatabase() {
        datasetPointRepository.deleteAll();
        functionComponentRepository.deleteAll();
        tabulatedDatasetRepository.deleteAll();
        userRoleRepository.deleteAll();
        mathFunctionRepository.deleteAll();
        performanceMetricRepository.deleteAll();
        roleRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected User newUser(String username) {
        return new User(username, "hash_" + username);
    }

    protected Role newRole(String code) {
        return new Role(code, "Role description for " + code);
    }

    protected MathFunction newFunction(User owner, String name, MathFunction.FunctionType type) {
        return new MathFunction(owner, name, type, "{\"expr\":\"" + name + "\"}");
    }

    protected TabulatedDataset newDataset(MathFunction function, TabulatedDataset.SourceType sourceType) {
        return new TabulatedDataset(function, sourceType);
    }

    protected DatasetPoint newDatasetPoint(TabulatedDataset dataset, int index, double x, double y) {
        return new DatasetPoint(dataset, index, BigDecimal.valueOf(x), BigDecimal.valueOf(y));
    }

    protected FunctionComponent newComponent(MathFunction composite, MathFunction component, short position) {
        return new FunctionComponent(composite, component, position);
    }

    protected PerformanceMetric newMetric(PerformanceMetric.Engine engine, String operation, int processed, int elapsed) {
        return new PerformanceMetric(engine, operation, processed, elapsed);
    }

    protected UserRole newUserRole(User user, Role role) {
        return new UserRole(user, role);
    }
}