package airport.storage;

import airport.controller.PassengerController;
import airport.controller.PlaneController;
import airport.controller.LocationController;
import airport.controller.FlightController;
import airport.response.Response;
import airport.model.Passenger;
import airport.model.Plane;
import airport.model.Location;
import airport.model.Flight;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Carga datos iniciales desde los ficheros JSON que están en la carpeta raíz /json.
 * Debe invocarse antes de levantar la UI para poblar los repositorios in-memory.
 */
public class JsonDataLoader {

    /**
     * Carga todos los datos en este orden:
     *  1) Pasajeros
     *  2) Aviones
     *  3) Localizaciones
     *  4) Vuelos (y sus pasajeros, si la sección “passengers” existe)
     */
    public static void loadAll(PassengerController pc,
                               PlaneController plc,
                               LocationController lc,
                               FlightController fc) throws Exception {
        loadPassengers(pc);
        loadPlanes(plc);
        loadLocations(lc);
        loadFlights(fc);
    }

    private static void loadPassengers(PassengerController pc) throws Exception {
        try (InputStream is = openJson("passengers")) {
            JSONArray arr = new JSONArray(new JSONTokener(is));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                long    id      = o.getLong("id");
                String  fn      = o.getString("firstname");
                String  ln      = o.getString("lastname");
                LocalDate bd    = LocalDate.parse(o.getString("birthDate"));
                int     code    = o.getInt("countryPhoneCode");
                long    phone   = o.getLong("phone");
                String  country = o.getString("country");

                Response<Passenger> r = pc.registerPassenger(
                        id, fn, ln,
                        bd.getYear(), bd.getMonthValue(), bd.getDayOfMonth(),
                        code, phone, country
                );
                if (!r.isSuccess()) {
                    System.err.println("Error cargando pasajero " + id + ": " + r.getMessage());
                }
            }
        }
    }

    private static void loadPlanes(PlaneController plc) throws Exception {
        try (InputStream is = openJson("planes")) {
            JSONArray arr = new JSONArray(new JSONTokener(is));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String id         = o.getString("id");
                String brand      = o.getString("brand");
                String model      = o.getString("model");
                int    maxCap     = o.getInt("maxCapacity");
                String airline    = o.getString("airline");

                Response<Plane> r = plc.createPlane(id, brand, model, maxCap, airline);
                if (!r.isSuccess()) {
                    System.err.println("Error cargando avión " + id + ": " + r.getMessage());
                }
            }
        }
    }

    private static void loadLocations(LocationController lc) throws Exception {
        try (InputStream is = openJson("locations")) {
            JSONArray arr = new JSONArray(new JSONTokener(is));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String  id      = o.getString("airportId");
                String  name    = o.getString("airportName");
                String  city    = o.getString("airportCity");
                String  country = o.getString("airportCountry");
                double  lat     = o.getDouble("airportLatitude");
                double  lon     = o.getDouble("airportLongitude");

                Response<Location> r = lc.createLocation(id, name, city, country, lat, lon);
                if (!r.isSuccess()) {
                    System.err.println("Error cargando localización " + id + ": " + r.getMessage());
                }
            }
        }
    }

    private static void loadFlights(FlightController fc) throws Exception {
        try (InputStream is = openJson("flights")) {
            JSONArray arr = new JSONArray(new JSONTokener(is));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String id           = o.getString("id");
                String planeId      = o.getString("plane");
                String depLoc       = o.getString("departureLocation");
                String arrLoc       = o.getString("arrivalLocation");
                String scaleLoc     = o.optString("scaleLocation", "").trim();
                LocalDateTime depDT = LocalDateTime.parse(o.getString("departureDate"));
                int arrH            = o.getInt("hoursDurationArrival");
                int arrM            = o.getInt("minutesDurationArrival");
                int scH             = o.getInt("hoursDurationScale");
                int scM             = o.getInt("minutesDurationScale");

                Response<Flight> r = fc.createFlight(
                        id,
                        planeId,
                        depLoc,
                        arrLoc,
                        scaleLoc,
                        depDT.getYear(), depDT.getMonthValue(), depDT.getDayOfMonth(),
                        depDT.getHour(), depDT.getMinute(),
                        arrH, arrM, scH, scM
                );
                if (!r.isSuccess()) {
                    System.err.println("Error cargando vuelo " + id + ": " + r.getMessage());
                    continue;
                }
                // Si el JSON incluye lista de pasajeros, los asociamos:
                if (o.has("passengers")) {
                    JSONArray pa = o.getJSONArray("passengers");
                    for (int j = 0; j < pa.length(); j++) {
                        long pid = pa.getLong(j);
                        fc.addPassengerToFlight(id, pid);
                    }
                }
            }
        }
    }

    /**
     * Abre un FileInputStream desde la carpeta raíz del proyecto /json/{name}.json
     */
    private static InputStream openJson(String name) throws Exception {
        String path = "json/" + name + ".json";
        return new FileInputStream(path);
    }
}

