package airport.storage;

import airport.model.Flight;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repositorio in‐memory de vuelos.
 * Ordenado por departureDate de más antiguo a más nuevo.
 */
public class FlightRepository implements Repository<Flight, String> {

    private final List<Flight> data = new ArrayList<>();

    @Override
    public Flight save(Flight f) {
        data.add(f);
        data.sort(Comparator.comparing(Flight::getDepartureDate));
        return f;
    }

    @Override
    public Flight update(Flight f) {
        data.removeIf(x -> x.getId().equals(f.getId()));
        data.add(f);
        data.sort(Comparator.comparing(Flight::getDepartureDate));
        return f;
    }

    @Override
    public Optional<Flight> findById(String id) {
        return data.stream()
                .filter(f -> f.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Flight> findAll() {
        return data.stream().collect(Collectors.toList());
    }
}
