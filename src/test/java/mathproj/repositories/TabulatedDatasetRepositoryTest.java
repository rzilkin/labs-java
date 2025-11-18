package mathproj.repositories;

import mathproj.entities.MathFunction;
import mathproj.entities.TabulatedDataset;
import mathproj.entities.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TabulatedDatasetRepositoryTest extends RepositoryIntegrationTestSupport {

    @Test
    void tabulatedDatasetRepositoryConnectsFunctionsAndSources() {
        tabulatedDatasetRepository.deleteAll();

        User owner = userRepository.save(newUser("datasetOwner"));
        MathFunction function = mathFunctionRepository.save(newFunction(owner, "tabFunction", MathFunction.FunctionType.TABULATED));

        TabulatedDataset manualDataset = tabulatedDatasetRepository.save(newDataset(function, TabulatedDataset.SourceType.MANUAL));
        tabulatedDatasetRepository.saveAll(List.of(
                newDataset(function, TabulatedDataset.SourceType.GENERATED),
                newDataset(function, TabulatedDataset.SourceType.DIFFERENTIATED)));

        assertEquals(3, tabulatedDatasetRepository.count());
        assertEquals(3, tabulatedDatasetRepository.findAll().size());

        List<TabulatedDataset> datasets = tabulatedDatasetRepository.findAll();
        long linkedToFunction = datasets.stream()
                .filter(dataset -> dataset.getFunction().getId().equals(function.getId()))
                .count();
        assertEquals(3, linkedToFunction);
        assertEquals(1, datasets.stream()
                .filter(dataset -> dataset.getSourceType() == TabulatedDataset.SourceType.MANUAL)
                .count());

        tabulatedDatasetRepository.delete(manualDataset);
        assertEquals(2, tabulatedDatasetRepository.count());

        tabulatedDatasetRepository.deleteAll(tabulatedDatasetRepository.findAll());
        assertEquals(0, tabulatedDatasetRepository.count());
    }
}