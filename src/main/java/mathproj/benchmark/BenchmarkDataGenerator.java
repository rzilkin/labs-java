package mathproj.benchmark;

import mathproj.entities.*;
import mathproj.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class BenchmarkDataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(BenchmarkDataGenerator.class);
    private static final int TOTAL_POINTS = 10_000;
    private static final int USERS_COUNT = 50;
    private static final int FUNCTIONS_PER_USER = 4;
    private static final int DATASETS_PER_FUNCTION = 1;

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final MathFunctionRepository mathFunctionRepository;
    private final TabulatedDatasetRepository tabulatedDatasetRepository;
    private final DatasetPointRepository datasetPointRepository;
    private final FunctionComponentRepository functionComponentRepository;

    public BenchmarkDataGenerator(RoleRepository roleRepository,
                                  UserRepository userRepository,
                                  UserRoleRepository userRoleRepository,
                                  MathFunctionRepository mathFunctionRepository,
                                  TabulatedDatasetRepository tabulatedDatasetRepository,
                                  DatasetPointRepository datasetPointRepository,
                                  FunctionComponentRepository functionComponentRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.mathFunctionRepository = mathFunctionRepository;
        this.tabulatedDatasetRepository = tabulatedDatasetRepository;
        this.datasetPointRepository = datasetPointRepository;
        this.functionComponentRepository = functionComponentRepository;
    }

    @Transactional
    public DatasetStatistics generateSyntheticDataset() {
        clearExistingData();
        List<Role> roles = seedRoles();
        List<User> users = seedUsers();
        seedUserRoles(users, roles);
        List<MathFunction> functions = seedFunctions(users);
        List<TabulatedDataset> datasets = seedDatasets(functions);
        long pointCount = seedDatasetPoints(datasets);
        seedFunctionComponents(functions);
        logger.info("Случайные данные для бенчмарка сгенерированы");
        return new DatasetStatistics(users.size(), functions.size(), datasets.size(), pointCount);
    }

    private void clearExistingData() {
        functionComponentRepository.deleteAllInBatch();
        datasetPointRepository.deleteAllInBatch();
        tabulatedDatasetRepository.deleteAllInBatch();
        mathFunctionRepository.deleteAllInBatch();
        userRoleRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    private List<Role> seedRoles() {
        List<Role> roles = List.of(
                new Role("ADMIN", "Администратор"),
                new Role("USER", "Обычный пользователь"),
                new Role("ANALYST", "Аналитик функций")
        );
        return roleRepository.saveAll(roles);
    }

    private List<User> seedUsers() {
        List<User> users = new ArrayList<>(USERS_COUNT);
        for (int i = 0; i < USERS_COUNT; i++) {
            String username = "user_" + i;
            String password = "pwd_" + i + '_' + ThreadLocalRandom.current().nextInt(10_000, 99_999);
            users.add(new User(username, password));
        }
        return userRepository.saveAll(users);
    }

    private void seedUserRoles(List<User> users, List<Role> roles) {
        List<UserRole> userRoles = new ArrayList<>(users.size());
        for (User user : users) {
            Role role = roles.get(ThreadLocalRandom.current().nextInt(roles.size()));
            userRoles.add(new UserRole(user, role));
        }
        userRoleRepository.saveAll(userRoles);
    }

    private List<MathFunction> seedFunctions(List<User> users) {
        List<MathFunction> functions = new ArrayList<>(USERS_COUNT * FUNCTIONS_PER_USER);
        for (User user : users) {
            for (int i = 0; i < FUNCTIONS_PER_USER; i++) {
                MathFunction.FunctionType type = MathFunction.FunctionType.values()[
                        ThreadLocalRandom.current().nextInt(MathFunction.FunctionType.values().length)
                        ];
                String definition = String.format("{\"expression\":\"%.3fx^2 + %.3f\"}",
                        ThreadLocalRandom.current().nextDouble(0.1, 5.0),
                        ThreadLocalRandom.current().nextDouble(0.1, 2.0));
                functions.add(new MathFunction(user, user.getUsername() + "_fn_" + i, type, definition));
            }
        }
        return mathFunctionRepository.saveAll(functions);
    }

    private List<TabulatedDataset> seedDatasets(List<MathFunction> functions) {
        List<TabulatedDataset> datasets = new ArrayList<>(functions.size() * DATASETS_PER_FUNCTION);
        for (MathFunction function : functions) {
            for (int i = 0; i < DATASETS_PER_FUNCTION; i++) {
                TabulatedDataset.SourceType sourceType = TabulatedDataset.SourceType.values()[
                        ThreadLocalRandom.current().nextInt(TabulatedDataset.SourceType.values().length)
                        ];
                datasets.add(new TabulatedDataset(function, sourceType));
            }
        }
        return tabulatedDatasetRepository.saveAll(datasets);
    }

    private long seedDatasetPoints(List<TabulatedDataset> datasets) {
        if (datasets.isEmpty()) {
            return 0;
        }
        List<DatasetPoint> points = new ArrayList<>(TOTAL_POINTS);
        long remainingPoints = TOTAL_POINTS;
        long remainingDatasets = datasets.size();
        int datasetIndex = 0;
        for (TabulatedDataset dataset : datasets) {
            if (remainingPoints <= 0) {
                break;
            }
            int pointsForDataset = (int) Math.max(1, remainingPoints / remainingDatasets);
            if (pointsForDataset > remainingPoints) {
                pointsForDataset = (int) remainingPoints;
            }
            for (int i = 0; i < pointsForDataset; i++) {
                BigDecimal x = BigDecimal.valueOf(datasetIndex + ThreadLocalRandom.current().nextDouble())
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal y = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(-1000.0, 1000.0))
                        .setScale(2, RoundingMode.HALF_UP);
                points.add(new DatasetPoint(dataset, i, x, y));
            }
            remainingPoints -= pointsForDataset;
            remainingDatasets--;
            datasetIndex++;
        }
        datasetPointRepository.saveAll(points);
        return points.size();
    }

    private void seedFunctionComponents(List<MathFunction> functions) {
        if (functions.size() < 2) {
            return;
        }
        List<FunctionComponent> components = new ArrayList<>();
        for (int i = 0; i < functions.size() - 1; i++) {
            if (i % 5 == 0) {
                MathFunction composite = functions.get(i);
                MathFunction component = functions.get(i + 1);
                components.add(new FunctionComponent(composite, component, (short) 1));
            }
        }
        if (!components.isEmpty()) {
            functionComponentRepository.saveAll(components);
        }
    }

    public record DatasetStatistics(int users, int functions, int datasets, long points) {
    }
}