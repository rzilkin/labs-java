package mathproj.api;

import mathproj.benchmark.PerformanceMeasurementService;
import mathproj.benchmark.BenchmarkDataGenerator;
import mathproj.entities.*;
import mathproj.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ApiService {
    private final DatasetPointRepository datasetPointRepository;
    private final FunctionComponentRepository functionComponentRepository;
    private final MathFunctionRepository mathFunctionRepository;
    private final PerformanceMetricRepository performanceMetricRepository;
    private final RoleRepository roleRepository;
    private final TabulatedDatasetRepository tabulatedDatasetRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PerformanceMeasurementService performanceMeasurementService;
    private final BenchmarkDataGenerator benchmarkDataGenerator;

    public ApiService(DatasetPointRepository datasetPointRepository, FunctionComponentRepository functionComponentRepository,
                        MathFunctionRepository mathFunctionRepository, PerformanceMetricRepository performanceMetricRepository,
                       RoleRepository roleRepository, TabulatedDatasetRepository tabulatedDatasetRepository,
                       UserRepository userRepository, UserRoleRepository userRoleRepository,
                       PerformanceMeasurementService performanceMeasurementService, BenchmarkDataGenerator benchmarkDataGenerator) {
        this.datasetPointRepository = datasetPointRepository;
        this.functionComponentRepository = functionComponentRepository;
        this.mathFunctionRepository = mathFunctionRepository;
        this.performanceMetricRepository = performanceMetricRepository;
        this.roleRepository = roleRepository;
        this.tabulatedDatasetRepository = tabulatedDatasetRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.performanceMeasurementService = performanceMeasurementService;
        this.benchmarkDataGenerator = benchmarkDataGenerator;
    }

    @Transactional
    public BenchmarkDataGenerator.DatasetStatistics generateSyntheticDataset() {
        return benchmarkDataGenerator.generateSyntheticDataset();
    }

    public void runBenchmarks() {
        performanceMeasurementService.runBenchmarks();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Optional<Role> getRoleByCode(String code) {
        return roleRepository.findByCode(code);
    }

    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }

    public void deleteRole(String code) {
        roleRepository.deleteById(code);
    }

    public List<UserRole> getAllUserRoles() {
        return userRoleRepository.findAll();
    }

    public UserRole saveUserRole(UserRole userRole) {
        return userRoleRepository.save(userRole);
    }

    public void deleteUserRole(UserRoleId id) {
        userRoleRepository.deleteById(id);
    }

    public List<MathFunction> getAllFunctions() {
        return mathFunctionRepository.findAll();
    }

    public Optional<MathFunction> getFunctionById(Long id) {
        return mathFunctionRepository.findById(id);
    }

    public MathFunction saveFunction(MathFunction function) {
        return mathFunctionRepository.save(function);
    }

    public void deleteFunction(Long id) {
        mathFunctionRepository.deleteById(id);
    }

    public List<TabulatedDataset> getAllDatasets() {
        return tabulatedDatasetRepository.findAll();
    }

    public Optional<TabulatedDataset> getDatasetById(Long id) {
        return tabulatedDatasetRepository.findById(id);
    }

    public TabulatedDataset saveDataset(TabulatedDataset dataset) {
        return tabulatedDatasetRepository.save(dataset);
    }

    public void deleteDataset(Long id) {
        tabulatedDatasetRepository.deleteById(id);
    }

    public List<DatasetPoint> getAllDatasetPoints() {
        return datasetPointRepository.findAll();
    }

    public Optional<DatasetPoint> getDatasetPoint(Long datasetId, Integer pointIndex) {
        return datasetPointRepository.findByDatasetIdAndPointIndex(datasetId, pointIndex);
    }

    public DatasetPoint saveDatasetPoint(DatasetPoint point) {
        return datasetPointRepository.save(point);
    }

    public void deleteDatasetPoint(DatasetPointId id) {
        datasetPointRepository.deleteById(id);
    }

    public List<FunctionComponent> getAllFunctionComponents() {
        return functionComponentRepository.findAll();
    }

    public Optional<FunctionComponent> getFunctionComponent(FunctionComponentId id) {
        return functionComponentRepository.findById(id);
    }

    public FunctionComponent saveFunctionComponent(FunctionComponent component) {
        return functionComponentRepository.save(component);
    }

    public void deleteFunctionComponent(FunctionComponentId id) {
        functionComponentRepository.deleteById(id);
    }

    public List<PerformanceMetric> getAllPerformanceMetrics() {
        return performanceMetricRepository.findAll();
    }

    public Optional<PerformanceMetric> getPerformanceMetricById(Long id) {
        return performanceMetricRepository.findById(id);
    }

    public PerformanceMetric savePerformanceMetric(PerformanceMetric metric) {
        return performanceMetricRepository.save(metric);
    }

    public void deletePerformanceMetric(Long id) {
        performanceMetricRepository.deleteById(id);
    }

    public Object[] getDatasetStats(Long datasetId) {
        return datasetPointRepository.getDatasetStats(datasetId);
    }

    public List<Object[]> getMultipleDatasetStats(List<Long> datasetIds) {
        return datasetPointRepository.getMultipleDatasetStats(datasetIds);
    }

    public List<DatasetPoint> findMaxYValuePoints(Long datasetId) {
        return datasetPointRepository.findMaxYValuePoints(datasetId);
    }

    public List<FunctionComponent> getComponentsByCompositeId(Long compositeId) {
        return functionComponentRepository.findByCompositeIdOrderByPositionAsc(compositeId);
    }

    public List<MathFunction> getCompositesByComponentId(Long componentId) {
        return functionComponentRepository.findCompositesByComponentId(componentId);
    }

    public List<MathFunction> getFunctionByNameContaining(String name) {
        return mathFunctionRepository.findByNameContaining(name);
    }

    public List<Object[]> getEngineStatistics() {
        return performanceMetricRepository.getEngineStatistics();
    }

    public List<Object[]> getRolesByPopularity() {
        return roleRepository.findRolesByPopularity();
    }

    public List<TabulatedDataset> getDatasetsBySourceType(TabulatedDataset.SourceType sourceType) {
        return tabulatedDatasetRepository.findBySourceTypeOrderByIdAsc(sourceType);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsUserRole(Long userId, String roleCode) {
        return userRoleRepository.existsByUserIdAndRoleCode(userId, roleCode);
    }
}
