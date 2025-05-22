package airport.storage;

import airport.model.Location;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repositorio in‐memory de localizaciones (aeropuertos).
 * Ordenado por airportId (String) ascendente.
 */
public class LocationRepository implements Repository<Location, String> {

    private final List<Location> data = new ArrayList<>();

    @Override
    public Location save(Location l) {
        data.add(l);
        data.sort(Comparator.comparing(Location::getAirportId));
        return l;
    }

    @Override
    public Location update(Location l) {
        data.removeIf(x -> x.getAirportId().equals(l.getAirportId()));
        data.add(l);
        data.sort(Comparator.comparing(Location::getAirportId));
        return l;
    }

    @Override
    public Optional<Location> findById(String id) {
        return data.stream()
                .filter(l -> l.getAirportId().equals(id))
                .findFirst();
    }

    @Override
    public List<Location> findAll() {
        return data.stream().collect(Collectors.toList());
    }
}
