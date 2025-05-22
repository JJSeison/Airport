package airport.controller;

import airport.model.Plane;
import airport.response.Response;
import airport.response.StatusCode;
import airport.storage.PlaneRepository;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Controlador para operaciones sobre Plane.
 */
public class PlaneController {

    // Formato XXYYYYY: 2 letras mayúsculas + 5 dígitos
    private static final Pattern ID_PATTERN = Pattern.compile("^[A-Z]{2}\\d{5}$");

    private final PlaneRepository repository;

    public PlaneController() {
        this.repository = new PlaneRepository();
    }

    /**
     * Crea un nuevo avión con validaciones:
     * - id único, formato XXYYYYY
     * - brand, model, airline no vacíos
     * - maxCapacity > 0
     */
    public Response<Plane> createPlane(String id,
                                       String brand,
                                       String model,
                                       int maxCapacity,
                                       String airline) {
        // ID válido
        if (id == null || !ID_PATTERN.matcher(id).matches()) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "El ID debe tener formato XXYYYYY (2 letras mayúsculas y 5 dígitos)");
        }
        // Unicidad
        if (repository.findById(id).isPresent()) {
            return Response.of(StatusCode.CONFLICT,
                    "Ya existe un avión con ID=" + id);
        }
        // Campos no vacíos
        if (brand == null || brand.isBlank() ||
                model == null || model.isBlank() ||
                airline == null || airline.isBlank()) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "Brand, Model y Airline no pueden estar vacíos");
        }
        // Capacidad válida
        if (maxCapacity <= 0) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "Max Capacity debe ser un entero mayor que 0");
        }

        // Guardar
        Plane saved = repository.save(
                new Plane(id, brand, model, maxCapacity, airline)
        );
        // Clonar (Prototype)
        Plane clone = new Plane(
                saved.getId(),
                saved.getBrand(),
                saved.getModel(),
                saved.getMaxCapacity(),
                saved.getAirline()
        );
        return Response.of(StatusCode.CREATED,
                "Avión creado exitosamente",
                clone);
    }

    /**
     * Devuelve todos los aviones ordenados por ID.
     */
    public Response<List<Plane>> getAllPlanes() {
        List<Plane> originals = repository.findAll();
        List<Plane> clones = originals.stream()
                .map(p -> new Plane(
                        p.getId(),
                        p.getBrand(),
                        p.getModel(),
                        p.getMaxCapacity(),
                        p.getAirline()
                ))
                .collect(Collectors.toList());
        return Response.of(StatusCode.OK,
                "Listado de aviones",
                clones);
    }
}
