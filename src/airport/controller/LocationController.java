package airport.controller;

import airport.model.Location;
import airport.response.Response;
import airport.response.StatusCode;
import airport.storage.LocationRepository;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

/**
 * Controlador para operaciones sobre Location (aeropuerto).
 * Valida:
 *  - id único de 3 letras mayúsculas
 *  - name, city, country no vacíos
 *  - latitud ∈[-90,90], longitud ∈[-180,180]
 *  - máx 4 decimales en lat/long
 */
public class LocationController {

    private static final Pattern ID_PATTERN = Pattern.compile("^[A-Z]{3}$");
    private final LocationRepository repository;

    public LocationController() {
        this.repository = new LocationRepository();
    }

    public Response<Location> createLocation(String airportId,
                                             String name,
                                             String city,
                                             String country,
                                             double latitude,
                                             double longitude) {
        // ID
        if (airportId == null || !ID_PATTERN.matcher(airportId).matches()) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "El ID debe tener 3 letras mayúsculas");
        }
        if (repository.findById(airportId).isPresent()) {
            return Response.of(StatusCode.CONFLICT,
                    "Ya existe una localización con ID=" + airportId);
        }
        // Campos no vacíos
        if (name == null || name.isBlank() ||
                city == null || city.isBlank() ||
                country == null || country.isBlank()) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "Name, City y Country no pueden estar vacíos");
        }
        // Rangos lat/long
        if (latitude < -90.0 || latitude > 90.0) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "Latitud debe estar entre -90 y 90");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "Longitud debe estar entre -180 y 180");
        }
        // Decimales ≤4
        if (decimalScale(latitude) > 4 || decimalScale(longitude) > 4) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "Latitud y Longitud pueden tener hasta 4 decimales");
        }

        // Guardar
        Location saved = repository.save(
                new Location(airportId, name, city, country, latitude, longitude)
        );
        // Clonar (Prototype)
        Location clone = new Location(
                saved.getAirportId(),
                saved.getAirportName(),
                saved.getAirportCity(),
                saved.getAirportCountry(),
                saved.getAirportLatitude(),
                saved.getAirportLongitude()
        );
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

    /** Retorna la cantidad de decimales de un número double */
    private int decimalScale(double d) {
        BigDecimal bd = BigDecimal.valueOf(d).stripTrailingZeros();
        return Math.max(0, bd.scale());
    }
}
