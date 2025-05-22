package airport;

import airport.controller.PassengerController;
import airport.controller.PlaneController;
import airport.controller.LocationController;
import airport.controller.FlightController;
import airport.model.Passenger;
import airport.model.Plane;
import airport.model.Location;
import airport.model.Flight;
import airport.storage.Repository;
import airport.storage.PassengerRepository;
import airport.storage.PlaneRepository;
import airport.storage.LocationRepository;
import airport.storage.FlightRepository;
import airport.storage.JsonDataLoader;
import airport.view.AirportFrame;

public class Main {
    public static void main(String[] args) {
        try {
            Repository<Passenger, Long> passengerRepo = new PassengerRepository();
            Repository<Plane,    String> planeRepo     = new PlaneRepository();
            Repository<Location, String> locationRepo  = new LocationRepository();
            Repository<Flight,   String> flightRepo    = new FlightRepository();

            PassengerController passengerController =
                    new PassengerController(passengerRepo);
            PlaneController planeController =
                    new PlaneController(planeRepo);
            LocationController locationController =
                    new LocationController(locationRepo);
            FlightController flightController =
                    new FlightController(flightRepo,
                            planeRepo,
                            locationRepo,
                            passengerRepo);

            JsonDataLoader.loadAll(
                    passengerController,
                    planeController,
                    locationController,
                    flightController
            );

            java.awt.EventQueue.invokeLater(() ->
                    new AirportFrame(
                            passengerController,
                            planeController,
                            locationController,
                            flightController
                    ).setVisible(true)
            );
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}


