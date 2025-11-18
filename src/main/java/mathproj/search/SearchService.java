package mathproj.search;

import mathproj.entities.*;
import mathproj.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private MathFunctionRepository mathFunctionRepository;
    @Autowired
    private TabulatedDatasetRepository tabulatedDatasetRepository;
    @Autowired
    private DatasetPointRepository datasetPointRepository;
    @Autowired
    private FunctionComponentRepository functionComponentRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private PerformanceMetricRepository performanceMetricRepository;

    public User findUserById(Long id) {
        logger.info("Одиночный поиск пользователя по ID: {}", id);
        return userRepository.findById(id).orElse(null);
    }

    public MathFunction findFunctionById(Long id) {
        logger.info("Одиночный поиск функции по ID: {}", id);
        return mathFunctionRepository.findById(id).orElse(null);
    }

    public TabulatedDataset findDatasetById(Long id) {
        logger.info("Одиночный поиск набора данных по ID: {}", id);
        return tabulatedDatasetRepository.findById(id).orElse(null);
    }

    public Role findRoleByCode(String code) {
        logger.info("Одиночный поиск роли по коду: {}", code);
        return roleRepository.findByCode(code).orElse(null);
    }

    public List<User> findAllUsers() {
        logger.info("Множественный поиск всех пользователей");
        return userRepository.findAll();
    }

    public List<MathFunction> findAllFunctions() {
        logger.info("Множественный поиск всех функций");
        return mathFunctionRepository.findAll();
    }

    public List<TabulatedDataset> findAllDatasets() {
        logger.info("Множественный поиск всех наборов данных");
        return tabulatedDatasetRepository.findAll();
    }

    public List<Role> findAllRoles() {
        logger.info("Множественный поиск всех ролей");
        return roleRepository.findAll();
    }

    public List<PerformanceMetric> findAllPerformanceMetrics() {
        logger.info("Множественный поиск всех метрик производительности");
        return performanceMetricRepository.findAll();
    }

    public List<MathFunction> depthFirstSearchFunctionHierarchy(Long rootFunctionId) {
        logger.info("Поиск в глубину для иерархии функций. Корень: {}", rootFunctionId);
        List<MathFunction> result = new ArrayList<>();
        Set<Long> visited = new HashSet<>();

        dfsFunction(rootFunctionId, result, visited);

        logger.info("Поиск в глубину завершен. Найдено функций: {}", result.size());
        return result;
    }

    private void dfsFunction(Long functionId, List<MathFunction> result, Set<Long> visited) {
        if (visited.contains(functionId)) return;

        MathFunction function = mathFunctionRepository.findById(functionId).orElse(null);
        if (function == null) return;

        visited.add(functionId);
        result.add(function);
        logger.debug("DFS: добавлена функция '{}' (ID: {})", function.getName(), functionId);

        if (function.getFunctionType() == MathFunction.FunctionType.COMPOSITE) {
            List<FunctionComponent> components = functionComponentRepository.findByCompositeIdOrderByPositionAsc(functionId);
            for (FunctionComponent component : components) {
                dfsFunction(component.getComponent().getId(), result, visited);
            }
        }
    }

    public List<MathFunction> breadthFirstSearchFunctionHierarchy(Long rootFunctionId) {
        logger.info("Поиск в ширину для иерархии функций. Корень: {}", rootFunctionId);
        List<MathFunction> result = new ArrayList<>();
        Queue<Long> queue = new LinkedList<>();
        Set<Long> visited = new HashSet<>();

        queue.offer(rootFunctionId);
        visited.add(rootFunctionId);

        while (!queue.isEmpty()) {
            Long currentId = queue.poll();
            MathFunction currentFunction = mathFunctionRepository.findById(currentId).orElse(null);

            if (currentFunction != null) {
                result.add(currentFunction);
                logger.debug("BFS: добавлена функция '{}' (ID: {})", currentFunction.getName(), currentId);

                if (currentFunction.getFunctionType() == MathFunction.FunctionType.COMPOSITE) {
                    List<FunctionComponent> components = functionComponentRepository.findByCompositeIdOrderByPositionAsc(currentId);
                    for (FunctionComponent component : components) {
                        Long componentId = component.getComponent().getId();
                        if (!visited.contains(componentId)) {
                            visited.add(componentId);
                            queue.offer(componentId);
                        }
                    }
                }
            }
        }

        logger.info("Поиск в ширину завершен. Найдено функций: {}", result.size());
        return result;
    }

    public List<MathFunction> searchFunctionComponentsHierarchy(Long compositeId) {
        logger.info("Поиск по иерархии компонентов функции: {}", compositeId);
        return breadthFirstSearchFunctionHierarchy(compositeId);
    }

    public List<MathFunction> findCompositesUsingComponent(Long componentId) {
        logger.info("Поиск композитных функций, использующих компонент: {}", componentId);
        List<MathFunction> composites = functionComponentRepository.findCompositesByComponentId(componentId);
        logger.debug("Найдено композитных функций: {}", composites.size());
        return composites;
    }

    public List<User> findUsersSortedByUsername() {
        logger.info("Поиск пользователей с сортировкой по имени");
        return userRepository.findAllByOrderByUsernameAsc();
    }

    public List<MathFunction> findFunctionsSortedByName() {
        logger.info("Поиск функций с сортировкой по имени");
        return mathFunctionRepository.findAllByOrderByNameAsc();
    }

    public List<MathFunction> findFunctionsSortedById() {
        logger.info("Поиск функций с сортировкой по ID");
        return mathFunctionRepository.findAllByOrderByIdAsc();
    }

    public List<Role> findRolesSortedByCode() {
        logger.info("Поиск ролей с сортировкой по коду");
        return roleRepository.findAllByOrderByCodeAsc();
    }

    public List<TabulatedDataset> findDatasetsSortedById() {
        logger.info("Поиск наборов данных с сортировкой по ID");
        return tabulatedDatasetRepository.findAllByOrderByIdAsc();
    }

    public List<DatasetPoint> findDatasetPointsSortedByIndex(Long datasetId) {
        logger.info("Поиск точек данных с сортировкой по индексу для dataset: {}", datasetId);
        return datasetPointRepository.findByDatasetIdOrderByPointIndexAsc(datasetId);
    }

    public List<DatasetPoint> findDatasetPointsSortedByXValue(Long datasetId) {
        logger.info("Поиск точек данных с сортировкой по X для dataset: {}", datasetId);
        return datasetPointRepository.findByDatasetIdOrderByXValueAsc(datasetId);
    }

    public List<PerformanceMetric> findPerformanceMetricsSortedBySpeed() {
        logger.info("Поиск метрик производительности с сортировкой по скорости");
        return performanceMetricRepository.findAllByOrderByElapsedMsAsc();
    }

    public List<PerformanceMetric> findPerformanceMetricsSortedByRecords() {
        logger.info("Поиск метрик производительности с сортировкой по записям");
        return performanceMetricRepository.findAllByOrderByRecordsProcessedDesc();
    }
}