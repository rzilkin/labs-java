package mathproj.repositories;

import mathproj.entities.FunctionComponent;
import mathproj.entities.FunctionComponentId;
import mathproj.entities.MathFunction;
import mathproj.entities.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FunctionComponentRepositoryTest extends RepositoryIntegrationTestSupport {

    @Test
    void functionComponentRepositoryCapturesCompositeRelationships() {
        functionComponentRepository.deleteAll();

        User owner = userRepository.save(newUser("compositeOwner"));
        MathFunction composite = mathFunctionRepository.save(newFunction(owner, "composite", MathFunction.FunctionType.COMPOSITE));
        MathFunction partA = mathFunctionRepository.save(newFunction(owner, "partA", MathFunction.FunctionType.ANALYTIC));
        List<MathFunction> otherParts = mathFunctionRepository.saveAll(List.of(
                newFunction(owner, "partB", MathFunction.FunctionType.ANALYTIC),
                newFunction(owner, "partC", MathFunction.FunctionType.ANALYTIC)));

        FunctionComponent firstComponent = functionComponentRepository.save(newComponent(composite, partA, (short) 1));
        functionComponentRepository.saveAll(List.of(
                newComponent(composite, otherParts.get(0), (short) 2),
                newComponent(composite, otherParts.get(1), (short) 3)));

        assertEquals(3, functionComponentRepository.count());
        assertEquals(3, functionComponentRepository.findAll().size());

        List<FunctionComponent> stored = functionComponentRepository.findAll();
        assertEquals(3, stored.stream()
                .filter(component -> component.getComposite().getId().equals(composite.getId()))
                .count());
        assertEquals(1, stored.stream()
                .filter(component -> component.getComponent().getId().equals(partA.getId()))
                .count());

        FunctionComponentId componentId =
                new FunctionComponentId(composite.getId(), firstComponent.getPosition());
        assertTrue(functionComponentRepository.findById(componentId).isPresent());

        assertTrue(functionComponentRepository.findById(componentId).isPresent());

        functionComponentRepository.delete(firstComponent);
        assertEquals(2, functionComponentRepository.count());

        functionComponentRepository.deleteAll(functionComponentRepository.findAll());
        assertEquals(0, functionComponentRepository.count());
    }
}