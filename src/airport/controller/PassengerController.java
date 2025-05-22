package airport.controller;

import airport.model.Passenger;
import airport.observer.Observer;
import airport.observer.Subject;
import airport.response.Response;
import airport.response.StatusCode;
import airport.storage.Repository;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PassengerController implements Subject {
    private final Repository<Passenger, Long> repository;
    private final List<Observer> observers;

    public PassengerController(Repository<Passenger, Long> repository) {
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

    public Response<Passenger> registerPassenger(long id,
                                                 String firstname,
                                                 String lastname,
                                                 int birthYear,
                                                 int birthMonth,
                                                 int birthDay,
                                                 int countryPhoneCode,
                                                 long phone,
                                                 String country) {
        if (id < 0 || String.valueOf(id).length() > 15)
            return Response.of(StatusCode.BAD_REQUEST, "El ID debe ser ≥0 y tener a lo más 15 dígitos");
        if (repository.findById(id).isPresent())
            return Response.of(StatusCode.CONFLICT, "Ya existe un pasajero con ese ID");
        if (firstname == null || firstname.isBlank() || lastname == null || lastname.isBlank())
            return Response.of(StatusCode.BAD_REQUEST, "First name y Last name no pueden estar vacíos");
        LocalDate birthDate;
        try {
            birthDate = LocalDate.of(birthYear, birthMonth, birthDay);
        } catch (DateTimeException e) {
            return Response.of(StatusCode.BAD_REQUEST, "Birthdate inválida");
        }
        if (countryPhoneCode < 0 || String.valueOf(countryPhoneCode).length() > 3)
            return Response.of(StatusCode.BAD_REQUEST, "Country phone code debe ser ≥0 y ≤3 dígitos");
        if (phone < 0 || String.valueOf(phone).length() > 11)
            return Response.of(StatusCode.BAD_REQUEST, "Phone debe ser ≥0 y ≤11 dígitos");
        if (country == null || country.isBlank())
            return Response.of(StatusCode.BAD_REQUEST, "Country no puede estar vacío");

        Passenger saved = repository.save(new Passenger(id, firstname, lastname, birthDate, countryPhoneCode, phone, country));
        Passenger clone = new Passenger(
                saved.getId(),
                saved.getFirstname(),
                saved.getLastname(),
                saved.getBirthDate(),
                saved.getCountryPhoneCode(),
                saved.getPhone(),
                saved.getCountry()
        );
        notifyObservers("passenger"); // Notify observers
        return Response.of(StatusCode.CREATED, "Pasajero registrado exitosamente", clone);
    }

    public Response<Passenger> updatePassenger(long id,
                                               String firstname,
                                               String lastname,
                                               int birthYear,
                                               int birthMonth,
                                               int birthDay,
                                               int countryPhoneCode,
                                               long phone,
                                               String country) {
        Passenger existing = repository.findById(id).orElse(null);
        if (existing == null)
            return Response.of(StatusCode.NOT_FOUND, "No existe pasajero con ID=" + id);
        if (firstname == null || firstname.isBlank() || lastname == null || lastname.isBlank())
            return Response.of(StatusCode.BAD_REQUEST, "First name y Last name no pueden estar vacíos");
        LocalDate birthDate;
        try {
            birthDate = LocalDate.of(birthYear, birthMonth, birthDay);
        } catch (DateTimeException e) {
            return Response.of(StatusCode.BAD_REQUEST, "Birthdate inválida");
        }
        if (countryPhoneCode < 0 || String.valueOf(countryPhoneCode).length() > 3)
            return Response.of(StatusCode.BAD_REQUEST, "Country phone code debe ser ≥0 y ≤3 dígitos");
        if (phone < 0 || String.valueOf(phone).length() > 11)
            return Response.of(StatusCode.BAD_REQUEST, "Phone debe ser ≥0 y ≤11 dígitos");
        if (country == null || country.isBlank())
            return Response.of(StatusCode.BAD_REQUEST, "Country no puede estar vacío");

        existing.setFirstname(firstname);
        existing.setLastname(lastname);
        existing.setBirthDate(birthDate);
        existing.setCountryPhoneCode(countryPhoneCode);
        existing.setPhone(phone);
        existing.setCountry(country);

        Passenger updated = repository.update(existing);
        Passenger clone = new Passenger(
                updated.getId(),
                updated.getFirstname(),
                updated.getLastname(),
                updated.getBirthDate(),
                updated.getCountryPhoneCode(),
                updated.getPhone(),
                updated.getCountry()
        );
        notifyObservers("passenger"); // Notify observers
        return Response.of(StatusCode.OK, "Pasajero actualizado exitosamente", clone);
    }

    public Response<List<Passenger>> getAllPassengers() {
        List<Passenger> originals = repository.findAll();
        List<Passenger> clones = originals.stream()
                .map(p -> new Passenger(
                        p.getId(),
                        p.getFirstname(),
                        p.getLastname(),
                        p.getBirthDate(),
                        p.getCountryPhoneCode(),
                        p.getPhone(),
                        p.getCountry()
                ))
                .collect(Collectors.toList());
        // No notification needed for getAll, as it doesn't change data state
        return Response.of(StatusCode.OK, "Listado de pasajeros", clones);
    }
}

