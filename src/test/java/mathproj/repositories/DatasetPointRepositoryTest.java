package mathproj.repositories;

import mathproj.entities.DatasetPoint;
import mathproj.entities.DatasetPointId;
import mathproj.entities.MathFunction;
import mathproj.entities.TabulatedDataset;
import mathproj.entities.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DatasetPointRepositoryTest extends RepositoryIntegrationTestSupport {

    @Test
    void datasetPointRepositoryPersistsCoordinates() {
        datasetPointRepository.deleteAll();

        User owner = userRepository.save(newUser("pointOwner"));
        MathFunction function = mathFunctionRepository.save(newFunction(owner, "pointFunction", MathFunction.FunctionType.TABULATED));
        TabulatedDataset dataset = tabulatedDatasetRepository.save(newDataset(function, TabulatedDataset.SourceType.GENERATED));

        DatasetPoint firstSaved = datasetPointRepository.save(newDatasetPoint(dataset, 1, 0.0, 0.0));
        datasetPointRepository.saveAll(List.of(
                newDatasetPoint(dataset, 2, 1.0, 2.0),
                newDatasetPoint(dataset, 3, 2.0, 4.0)));

        assertEquals(3, datasetPointRepository.count());
        assertEquals(3, datasetPointRepository.findAll().size());

        List<DatasetPoint> datasetPoints = datasetPointRepository.findAll().stream()
                .filter(point -> point.getDataset().getId().equals(dataset.getId()))
                .collect(Collectors.toList());
        assertEquals(3, datasetPoints.size());

        DatasetPointId firstId = new DatasetPointId(dataset.getId(), 1);
        DatasetPoint firstPoint = datasetPointRepository.findById(firstId).orElse(null);
        assertNotNull(firstPoint);
        assertEquals(1, firstPoint.getPointIndex());

        datasetPointRepository.delete(firstSaved);
        assertEquals(2, datasetPointRepository.count());

        datasetPointRepository.deleteAll(datasetPointRepository.findAll());
        assertEquals(0, datasetPointRepository.count());
    }
}