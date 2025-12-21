package mathproj.ui.controllers;

import mathproj.functions.Insertable;
import mathproj.functions.Removable;
import mathproj.functions.TabulatedFunction;
import mathproj.ui.dto.FunctionDetailsDto;
import mathproj.ui.dto.FunctionInfoDto;
import mathproj.ui.service.CurrentFunctionService;
import mathproj.ui.service.FunctionStoreService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ui/library")
public class FunctionLibraryController {

    private final CurrentFunctionService current;
    private final FunctionStoreService store;

    public FunctionLibraryController(CurrentFunctionService current, FunctionStoreService store) {
        this.current = current;
        this.store = store;
    }

    @GetMapping
    public List<FunctionInfoDto> list() {
        return store.list();
    }

    @GetMapping("/{id}")
    public FunctionDetailsDto details(@PathVariable String id) {
        var s = store.getOrThrow(id);
        TabulatedFunction f = s.f();

        int n = f.getCount();
        double xMin = f.getX(0);
        double xMax = f.getX(n - 1);

        double yMin = f.getY(0);
        double yMax = f.getY(0);
        for (int i = 1; i < n; i++) {
            double y = f.getY(i);
            if (y < yMin) yMin = y;
            if (y > yMax) yMax = y;
        }

        boolean insertable = f instanceof Insertable;
        boolean removable = f instanceof Removable;

        return new FunctionDetailsDto(
                s.id(), s.name(), n,
                xMin, xMax,
                yMin, yMax,
                insertable, removable
        );
    }

    @PostMapping("/save-current")
    public FunctionInfoDto saveCurrent(@RequestParam("name") String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя не должно быть пустым.");
        }
        TabulatedFunction f = current.getOrThrow();
        String id = store.add(name, f);

        current.setCurrent(id, f);

        return new FunctionInfoDto(id, name, f.getCount());
    }

    @PostMapping("/{id}/open")
    public void open(@PathVariable String id) {
        var s = store.getOrThrow(id);
        current.setCurrent(s.id(), s.f());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        store.delete(id);
        current.clearIfCurrent(id);
    }
}


