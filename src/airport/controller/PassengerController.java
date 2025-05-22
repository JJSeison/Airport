package airport.controller;

import airport.model.Passenger;
import airport.response.Response;
import airport.response.StatusCode;
import airport.storage.PassengerRepository;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para operaciones sobre Pasajero.
 * Aplica validaciones según enunciado y devuelve Response con StatusCode y datos (clonados).
 */
public class PassengerController {

    private final PassengerRepository repository;

    public PassengerController() {
        this.repository = new PassengerRepository();
    }

    /**
     * Registra un nuevo pasajero tras validar:
     *  - id único, >=0, ≤15 dígitos
     *  - nombres/apellidos no vacíos
     *  - fecha válida
     *  - código país teléfono ≥0, ≤3 dígitos
     *  - teléfono ≥0, ≤11 dígitos
     *  - country no vacío
     */
    public Response<Passenger> registerPassenger(long id,
                                                 String firstname,
                                                 String lastname,
                                                 int birthYear,
                                                 int birthMonth,
                                                 int birthDay,
                                                 int countryPhoneCode,
                                                 long phone,
                                                 String country) {
        // ID
        if (id < 0 || String.valueOf(id).length() > 15) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "El ID debe ser ≥0 y tener a lo más 15 dígitos");
        }
        if (repository.findById(id).isPresent()) {
            return Response.of(StatusCode.CONFLICT,
                    "Ya existe un pasajero con ese ID");
        }
        // Nombres
        if (firstname == null || firstname.isBlank() ||
                lastname  == null || lastname.isBlank()) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "First name y Last name no pueden estar vacíos");
        }
        // Fecha
        LocalDate birthDate;
        try {
            birthDate = LocalDate.of(birthYear, birthMonth, birthDay);
        } catch (DateTimeException ex) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "Birthdate inválida");
        }
        // Código país teléfono
        if (countryPhoneCode < 0 ||
                String.valueOf(countryPhoneCode).length() > 3) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "Country phone code debe ser ≥0 y ≤3 dígitos");
        }
        // Teléfono
        if (phone < 0 || String.valueOf(phone).length() > 11) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "Phone debe ser ≥0 y ≤11 dígitos");
        }
        // Country
        if (country == null || country.isBlank()) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "Country no puede estar vacío");
        }

        // Pasa todas las validaciones: guardar
        Passenger saved = repository.save(
                new Passenger(id, firstname, lastname,
                        birthDate, countryPhoneCode, phone, country)
        );
        // Clonar para retornar
        Passenger clone = new Passenger(
                saved.getId(),
                saved.getFirstname(),
                saved.getLastname(),
                saved.getBirthDate(),
                saved.getCountryPhoneCode(),
                saved.getPhone(),
                saved.getCountry()
        );
        return Response.of(StatusCode.CREATED,
                "Pasajero registrado exitosamente",
                clone);
    }

    /**
     * Actualiza un pasajero existente.
     * Aplica mismas validaciones que en register, y además:
     *  - Debe existir el pasajero con ese ID.
     */
    public Response<Passenger> updatePassenger(long id,
                                               String firstname,
                                               String lastname,
                                               int birthYear,
                                               int birthMonth,
                                               int birthDay,
                                               int countryPhoneCode,
                                               long phone,
                                               String country) {
        // Debe existir
        Passenger existing = repository.findById(id).orElse(null);
        if (existing == null) {
            return Response.of(StatusCode.NOT_FOUND,
                    "No existe pasajero con ID=" + id);
        }
        // Reaplicar validaciones de registro
        if (firstname == null || firstname.isBlank() ||
                lastname  == null || lastname.isBlank()) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "First name y Last name no pueden estar vacíos");
        }
        LocalDate birthDate;
        try {
            birthDate = LocalDate.of(birthYear, birthMonth, birthDay);
        } catch (DateTimeException ex) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "Birthdate inválida");
        }
        if (countryPhoneCode < 0 ||
                String.valueOf(countryPhoneCode).length() > 3) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "Country phone code debe ser ≥0 y ≤3 dígitos");
        }
        if (phone < 0 || String.valueOf(phone).length() > 11) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "Phone debe ser ≥0 y ≤11 dígitos");
        }
        if (country == null || country.isBlank()) {
            return Response.of(StatusCode.BAD_REQUEST,
                    "Country no puede estar vacío");
        }
        // Actualizar campos
        existing.setFirstname(firstname);
        existing.setLastname(lastname);
        existing.setBirthDate(birthDate);
        existing.setCountryPhoneCode(countryPhoneCode);
        existing.setPhone(phone);
        existing.setCountry(country);

        Passenger updated = repository.update(existing);
        // Clonar
        Passenger clone = new Passenger(
                updated.getId(),
                updated.getFirstname(),
                updated.getLastname(),
                updated.getBirthDate(),
                updated.getCountryPhoneCode(),
                updated.getPhone(),
                updated.getCountry()
        );
        return Response.of(StatusCode.OK,
                "Pasajero actualizado exitosamente",
                clone);
    }

    /**
     * Devuelve todos los pasajeros ordenados por ID (ascendente).
     */
    public Response<List<Passenger>> getAllPassengers() {
        List<Passenger> originals = repository.findAll();
        // Clonar cada uno
        List<Passenger> clones = originals.stream()
                .map(p -> new Passenger(
                        p.getId(),
                        p.getFirstname(),
                        p.getLastname(),
                        p.getBirthDate(),
                        p.getCountryPhoneCode(),
                        p.getPhone(),
                        p.getCountry() ))
                .collect(Collectors.toList());
        return Response.of(StatusCode.OK,
                "Listado de pasajeros",
                clones);
    }
}