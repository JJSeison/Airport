package airport.controller;

import airport.model.Location;
import airport.observer.Observer;
import airport.observer.Subject;
import airport.response.Response;
import airport.response.StatusCode;
import airport.storage.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LocationController implements Subject {
    private static final Pattern ID_PATTERN = Pattern.compile("^[A-Z]{3}$");
    private final Repository<Location, String> repository;
    private final List<Observer> observers;

    public LocationController(Repository<Location, String> repository) {
        this.repository = repository;
        this.observers = new ArrayList<>();
    }

    @Override
    public void registerObserver(Observer o) {
        if (o != null && !observers.contains(o)) {
            observers.add(o);
        }
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(String dataType) {
        for (Observer observer : observers) {
            observer.update(dataType);
        }
    }

    public Response<Location> createLocation(String airportId,
                                             String name,
                                             String city,
                                             String country,
                                             double latitude,
                                             double longitude) {
        if (airportId == null || !ID_PATTERN.matcher(airportId).matches())
            return Response.of(StatusCode.BAD_REQUEST,
                    "El ID debe tener 3 letras mayúsculas");
        if (repository.findById(airportId).isPresent())
            return Response.of(StatusCode.CONFLICT,
                    "Ya existe una localización con ID=" + airportId);
        if (name == null || name.isBlank() ||
                city == null || city.isBlank() ||
                country == null || country.isBlank())
            return Response.of(StatusCode.BAD_REQUEST,
                    "Name, City y Country no pueden estar vacíos");
        if (latitude < -90.0 || latitude > 90.0)
            return Response.of(StatusCode.BAD_REQUEST,
                    "Latitud debe estar entre -90 y 90");
        if (longitude < -180.0 || longitude > 180.0)
            return Response.of(StatusCode.BAD_REQUEST,
                    "Longitud debe estar entre -180 y 180");
        if (decimalScale(latitude) > 4 || decimalScale(longitude) > 4)
            return Response.of(StatusCode.BAD_REQUEST,
                    "Latitud y Longitud pueden tener hasta 4 decimales");

        Location saved = repository.save(
                new Location(airportId, name, city, country, latitude, longitude)
        );
        Location clone = new Location(
                saved.getAirportId(),
                saved.getAirportName(),
                saved.getAirportCity(),
                saved.getAirportCountry(),
                saved.getAirportLatitude(),
                saved.getAirportLongitude()
        );
        notifyObservers("location"); // Notify observers
        return Response.of(StatusCode.CREATED,
                "Localización creada exitosamente",
                clone);
    }

    public Response<List<Location>> getAllLocations() {
        List<Location> originals = repository.findAll();
        List<Location> clones = originals.stream()
                .map(l -> new Location(
                        l.getAirportId(),
                        l.getAirportName(),
                        l.getAirportCity(),
                        l.getAirportCountry(),
                        l.getAirportLatitude(),
                        l.getAirportLongitude()))
                .collect(Collectors.toList());
        return Response.of(StatusCode.OK,
                "Listado de localizaciones",
                clones);
    }

    private int decimalScale(double value) {
        BigDecimal bd = BigDecimal.valueOf(value).stripTrailingZeros();
        return Math.max(0, bd.scale());
    }
}


