package airport.controller;

import airport.model.Flight;
import airport.model.Plane;
import airport.model.Location;
import airport.model.Passenger;
import airport.observer.Observer;
import airport.observer.Subject;
import airport.response.Response;
import airport.response.StatusCode;
import airport.storage.Repository;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FlightController implements Subject {
    private static final Pattern ID_PATTERN = Pattern.compile("^[A-Z]{3}\\d{3}$");

    private final Repository<Flight, String>   flightRepo;
    private final Repository<Plane, String>    planeRepo;
    private final Repository<Location, String> locationRepo;
    private final Repository<Passenger, Long>  passengerRepo;
    private final List<Observer> observers;

    public FlightController(Repository<Flight, String> flightRepo,
                            Repository<Plane, String> planeRepo,
                            Repository<Location, String> locationRepo,
                            Repository<Passenger, Long> passengerRepo) {
        this.flightRepo    = flightRepo;
        this.planeRepo     = planeRepo;
        this.locationRepo  = locationRepo;
        this.passengerRepo = passengerRepo;
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

    public Response<Flight> createFlight(String id,
                                         String planeId,
                                         String departureLocId,
                                         String arrivalLocId,
                                         String scaleLocId,
                                         int year, int month, int day,
                                         int depHour, int depMinute,
                                         int arrDurHour, int arrDurMinute,
                                         int scaleDurHour, int scaleDurMinute) {
        if (id == null || !ID_PATTERN.matcher(id).matches())
            return Response.of(StatusCode.BAD_REQUEST,
                    "El ID debe tener formato XXXYYY");
        if (flightRepo.findById(id).isPresent())
            return Response.of(StatusCode.CONFLICT,
                    "Ya existe un vuelo con ID=" + id);

        Optional<Plane> optPlane = planeRepo.findById(planeId);
        if (optPlane.isEmpty())
            return Response.of(StatusCode.NOT_FOUND,
                    "No existe avión con ID=" + planeId);
        Plane plane = optPlane.get();

        Optional<Location> optDep = locationRepo.findById(departureLocId);
        if (optDep.isEmpty())
            return Response.of(StatusCode.NOT_FOUND,
                    "No existe localización de salida ID=" + departureLocId);
        Optional<Location> optArr = locationRepo.findById(arrivalLocId);
        if (optArr.isEmpty())
            return Response.of(StatusCode.NOT_FOUND,
                    "No existe localización de llegada ID=" + arrivalLocId);
        Location depLoc = optDep.get(), arrLoc = optArr.get();

        boolean hasScale = scaleLocId != null && !scaleLocId.isBlank();
        Location scaleLoc = null;
        if (hasScale) {
            Optional<Location> optScale = locationRepo.findById(scaleLocId);
            if (optScale.isEmpty())
                return Response.of(StatusCode.NOT_FOUND,
                        "No existe localización de escala ID=" + scaleLocId);
            scaleLoc = optScale.get();
        }
        if (!hasScale && (scaleDurHour != 0 || scaleDurMinute != 0))
            return Response.of(StatusCode.BAD_REQUEST,
                    "Si no hay escala, la duración debe ser 00:00");
        if (arrDurHour < 0 || arrDurMinute < 0
                || (arrDurHour == 0 && arrDurMinute == 0)
                || arrDurMinute > 59)
            return Response.of(StatusCode.BAD_REQUEST,
                    "Duración de vuelo debe ser > 00:00 y minutos < 60");
        if (scaleDurHour < 0 || scaleDurMinute < 0 || scaleDurMinute > 59)
            return Response.of(StatusCode.BAD_REQUEST,
                    "Duración de escala inválida");

        LocalDateTime departureDate;
        try {
            departureDate = LocalDateTime.of(year, month, day, depHour, depMinute);
        } catch (DateTimeException e) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "Fecha u hora de salida inválida");
        }

        Flight toSave = hasScale
                ? new Flight(id, plane, depLoc, scaleLoc, arrLoc,
                departureDate, arrDurHour, arrDurMinute,
                scaleDurHour, scaleDurMinute)
                : new Flight(id, plane, depLoc, arrLoc,
                departureDate, arrDurHour, arrDurMinute);

        Flight saved = flightRepo.save(toSave);
        Flight clone = hasScale
                ? new Flight(saved.getId(), saved.getPlane(),
                saved.getDepartureLocation(),
                saved.getScaleLocation(),
                saved.getArrivalLocation(),
                saved.getDepartureDate(),
                saved.getHoursDurationArrival(),
                saved.getMinutesDurationArrival(),
                saved.getHoursDurationScale(),
                saved.getMinutesDurationScale())
                : new Flight(saved.getId(), saved.getPlane(),
                saved.getDepartureLocation(),
                saved.getArrivalLocation(),
                saved.getDepartureDate(),
                saved.getHoursDurationArrival(),
                saved.getMinutesDurationArrival());
        notifyObservers("flight"); // Notify observers
        return Response.of(StatusCode.CREATED,
                "Vuelo creado exitosamente", clone);
    }

    public Response<List<Flight>> getAllFlights() {
        List<Flight> originals = flightRepo.findAll();
        List<Flight> clones = originals.stream().map(f ->
                        f.getScaleLocation() == null
                                ? new Flight(f.getId(), f.getPlane(),
                                f.getDepartureLocation(),
                                f.getArrivalLocation(),
                                f.getDepartureDate(),
                                f.getHoursDurationArrival(),
                                f.getMinutesDurationArrival())
                                : new Flight(f.getId(), f.getPlane(),
                                f.getDepartureLocation(),
                                f.getScaleLocation(),
                                f.getArrivalLocation(),
                                f.getDepartureDate(),
                                f.getHoursDurationArrival(),
                                f.getMinutesDurationArrival(),
                                f.getHoursDurationScale(),
                                f.getMinutesDurationScale()))
                .collect(Collectors.toList());
        return Response.of(StatusCode.OK, "Listado de vuelos", clones);
    }

    public Response<List<Flight>> getFlightsByPassenger(long passengerId) {
        Optional<Passenger> optP = passengerRepo.findById(passengerId);
        if (optP.isEmpty())
            return Response.of(StatusCode.NOT_FOUND,
                    "No existe pasajero con ID=" + passengerId);
        List<Flight> sorted = optP.get().getFlights().stream()
                .sorted((a, b) -> a.getDepartureDate()
                        .compareTo(b.getDepartureDate()))
                .collect(Collectors.toList());
        List<Flight> clones = sorted.stream().map(f ->
                        f.getScaleLocation() == null
                                ? new Flight(f.getId(), f.getPlane(),
                                f.getDepartureLocation(),
                                f.getArrivalLocation(),
                                f.getDepartureDate(),
                                f.getHoursDurationArrival(),
                                f.getMinutesDurationArrival())
                                : new Flight(f.getId(), f.getPlane(),
                                f.getDepartureLocation(),
                                f.getScaleLocation(),
                                f.getArrivalLocation(),
                                f.getDepartureDate(),
                                f.getHoursDurationArrival(),
                                f.getMinutesDurationArrival(),
                                f.getHoursDurationScale(),
                                f.getMinutesDurationScale()))
                .collect(Collectors.toList());
        return Response.of(StatusCode.OK, "Vuelos del pasajero", clones);
    }

    public Response<Flight> addPassengerToFlight(String flightId, long passengerId) {
        Optional<Flight> optF = flightRepo.findById(flightId);
        if (optF.isEmpty())
            return Response.of(StatusCode.NOT_FOUND,
                    "No existe vuelo con ID=" + flightId);
        Optional<Passenger> optP = passengerRepo.findById(passengerId);
        if (optP.isEmpty())
            return Response.of(StatusCode.NOT_FOUND,
                    "No existe pasajero con ID=" + passengerId);
        Flight f = optF.get(), clone;
        Passenger p = optP.get();

        // Check if plane has capacity
        if (f.getNumPassengers() >= f.getPlane().getMaxCapacity()) {
            return Response.of(StatusCode.CONFLICT,
                    "El vuelo ha alcanzado su capacidad máxima de pasajeros.");
        }

        // Check if passenger is already in flight
        // Note: This requires Flight model to have a list of passengers and a way to check them.
        // Assuming Flight class has a method like `hasPassenger(Passenger p)`
        /*
        if (f.hasPassenger(p)) { // This method needs to be implemented in Flight.java
            return Response.of(StatusCode.CONFLICT,
                    "El pasajero ya está registrado en este vuelo.");
        }
        */


        f.addPassenger(p);
        p.addFlight(f);
        flightRepo.update(f); // Persist changes to flight (e.g., updated passenger list)
        // passengerRepo.update(p); // Persist changes to passenger (e.g., updated flight list)

        clone = new Flight(f.getId(), f.getPlane(),
                f.getDepartureLocation(),
                f.getArrivalLocation(),
                f.getDepartureDate(),
                f.getHoursDurationArrival(),
                f.getMinutesDurationArrival());
        // It might be more appropriate to also notify for "passenger" changes if their flight list is critical for some views
        notifyObservers("flight");
        notifyObservers("passenger"); // If passenger's flight count shown in passenger table
        return Response.of(StatusCode.OK, "Pasajero agregado al vuelo", clone);
    }

    public Response<Flight> delayFlight(String flightId,
                                        int delayHours,
                                        int delayMinutes) {
        Optional<Flight> optF = flightRepo.findById(flightId);
        if (optF.isEmpty())
            return Response.of(StatusCode.NOT_FOUND,
                    "No existe vuelo con ID=" + flightId);
        if (delayHours < 0 || delayMinutes < 0
                || (delayHours == 0 && delayMinutes == 0)
                || delayMinutes > 59)
            return Response.of(StatusCode.BAD_REQUEST,
                    "Tiempo de retraso inválido");
        Flight f = optF.get(), clone;
        f.delay(delayHours, delayMinutes);
        flightRepo.update(f);
        clone = f.getScaleLocation() == null
                ? new Flight(f.getId(), f.getPlane(),
                f.getDepartureLocation(),
                f.getArrivalLocation(),
                f.getDepartureDate(),
                f.getHoursDurationArrival(),
                f.getMinutesDurationArrival())
                : new Flight(f.getId(), f.getPlane(),
                f.getDepartureLocation(),
                f.getScaleLocation(),
                f.getArrivalLocation(),
                f.getDepartureDate(),
                f.getHoursDurationArrival(),
                f.getMinutesDurationArrival(),
                f.getHoursDurationScale(),
                f.getMinutesDurationScale());
        notifyObservers("flight"); // Notify observers
        return Response.of(StatusCode.OK, "Vuelo retrasado exitosamente", clone);
    }
}

