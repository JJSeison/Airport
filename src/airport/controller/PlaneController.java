package airport.controller;

import airport.model.Plane;
import airport.observer.Observer;
import airport.observer.Subject;
import airport.response.Response;
import airport.response.StatusCode;
import airport.storage.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PlaneController implements Subject {
    private static final Pattern ID_PATTERN = Pattern.compile("^[A-Z]{2}\\d{5}$");
    private final Repository<Plane, String> repository;
    private final List<Observer> observers;

    public PlaneController(Repository<Plane, String> repository) {
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

    public Response<Plane> createPlane(String id,
                                       String brand,
                                       String model,
                                       int maxCapacity,
                                       String airline) {
        if (id == null || !ID_PATTERN.matcher(id).matches()) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "El ID debe tener formato XXYYYYY (2 letras mayúsculas y 5 dígitos)");
        }
        if (repository.findById(id).isPresent()) {
            return Response.of(StatusCode.CONFLICT,
                    "Ya existe un avión con ID=" + id);
        }
        if (brand == null || brand.isBlank() ||
                model == null || model.isBlank() ||
                airline == null || airline.isBlank()) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "Brand, Model y Airline no pueden estar vacíos");
        }
        if (maxCapacity <= 0) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "Max Capacity debe ser un entero mayor que 0");
        }

        Plane saved = repository.save(
                new Plane(id, brand, model, maxCapacity, airline)
        );
        Plane clone = new Plane(
                saved.getId(),
                saved.getBrand(),
                saved.getModel(),
                saved.getMaxCapacity(),
                saved.getAirline()
        );
        notifyObservers("plane"); // Notify observers
        return Response.of(StatusCode.CREATED,
                "Avión creado exitosamente",
                clone);
    }

    public Response<List<Plane>> getAllPlanes() {
        List<Plane> list = repository.findAll();
        List<Plane> clones = list.stream()
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

