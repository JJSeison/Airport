package airport.storage;

import airport.model.Plane;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repositorio in‐memory de aviones.
 * Siempre ordenado por ID (String) ascendente.
 */
public class PlaneRepository implements Repository<Plane, String> {

    private final List<Plane> data = new ArrayList<>();

    @Override
    public Plane save(Plane p) {
        data.add(p);
        data.sort(Comparator.comparing(Plane::getId));
        return p;
    }

    @Override
    public Plane update(Plane p) {
        data.removeIf(x -> x.getId().equals(p.getId()));
        data.add(p);
        data.sort(Comparator.comparing(Plane::getId));
        return p;
    }

    @Override
    public Optional<Plane> findById(String id) {
        return data.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Plane> findAll() {
        return data.stream().collect(Collectors.toList());
    }
}
