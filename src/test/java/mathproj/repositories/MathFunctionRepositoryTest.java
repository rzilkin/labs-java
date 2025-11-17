package mathproj.repositories;

import mathproj.entities.MathFunction;
import mathproj.entities.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MathFunctionRepositoryTest extends RepositoryIntegrationTestSupport {

    @Test
    void mathFunctionRepositoryStoresOwnerAndType() {
        mathFunctionRepository.deleteAll();

        User owner = userRepository.save(newUser("mathOwner"));

        MathFunction analytic = mathFunctionRepository.save(newFunction(owner, "analytic", MathFunction.FunctionType.ANALYTIC));
        mathFunctionRepository.saveAll(List.of(
                newFunction(owner, "tabulated", MathFunction.FunctionType.TABULATED),
                newFunction(owner, "composite", MathFunction.FunctionType.COMPOSITE)));

        assertEquals(3, mathFunctionRepository.count());
        assertEquals(3, mathFunctionRepository.findAll().size());

        List<MathFunction> stored = mathFunctionRepository.findAll();
        long ownedByMathOwner = stored.stream()
                .filter(function -> function.getOwner().getId().equals(owner.getId()))
                .count();
        assertEquals(3, ownedByMathOwner);

        long analyticCount = stored.stream()
                .filter(function -> function.getFunctionType() == MathFunction.FunctionType.ANALYTIC)
                .count();
        assertEquals(1, analyticCount);

        mathFunctionRepository.delete(analytic);
        assertEquals(2, mathFunctionRepository.count());

        mathFunctionRepository.deleteAll(mathFunctionRepository.findAll());
        assertEquals(0, mathFunctionRepository.count());
    }
}