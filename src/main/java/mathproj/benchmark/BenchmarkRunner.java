package mathproj.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "mathproj.benchmark.enabled", havingValue = "true")
public class BenchmarkRunner implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(BenchmarkRunner.class);

    private final BenchmarkDataGenerator dataGenerator;
    private final PerformanceMeasurementService performanceMeasurementService;

    public BenchmarkRunner(BenchmarkDataGenerator dataGenerator,
                           PerformanceMeasurementService performanceMeasurementService) {
        this.dataGenerator = dataGenerator;
        this.performanceMeasurementService = performanceMeasurementService;
    }

    @Override
    public void run(String... args) {
        BenchmarkDataGenerator.DatasetStatistics stats = dataGenerator.generateSyntheticDataset();
        logger.info("Созданы случайные таблицы: пользователей={}, функций={}, наборов={}, точек={}",
                stats.users(), stats.functions(), stats.datasets(), stats.points());
        performanceMeasurementService.runBenchmarks();
    }
}