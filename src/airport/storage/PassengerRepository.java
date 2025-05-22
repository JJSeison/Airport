package airport.storage;

import airport.model.Passenger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repositorio in‐memory de pasajeros.
 * Mantiene la lista siempre ordenada por ID ascendente.
 */
public class PassengerRepository implements Repository<Passenger, Long> {

    private final List<Passenger> data = new ArrayList<>();

    @Override
    public Passenger save(Passenger p) {
        data.add(p);
        data.sort(Comparator.comparingLong(Passenger::getId));
        return p;
    }

    @Override
    public Passenger update(Passenger p) {
        // elimino el viejo y agrego el nuevo
        data.removeIf(x -> x.getId() == p.getId());
        data.add(p);
        data.sort(Comparator.comparingLong(Passenger::getId));
        return p;
    }

    @Override
    public Optional<Passenger> findById(Long id) {
        return data.stream()
                .filter(p -> p.getId() == id)
                .findFirst();
    }

    @Override
    public List<Passenger> findAll() {
        // devolvemos una copia de la lista
        return data.stream()
                .collect(Collectors.toList());
    }
}
