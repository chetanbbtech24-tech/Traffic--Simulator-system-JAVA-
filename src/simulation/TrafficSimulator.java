package simulation;

import model.Road;
import model.Vehicle;

import java.util.List;
import java.util.Random;

public class TrafficSimulator {

    private static final int MIN_VEHICLES = 0;
    private static final int MAX_VEHICLES = 50;
    private static final double EMERGENCY_CHANCE = 0.07;

    private static final String[] VEHICLE_TYPES = {
        "Car", "Car", "Car", "Bus", "Bike", "Truck"
    };

    private final Random random;
    private int cycleCount;

    public TrafficSimulator() {
        this.random = new Random();
        this.cycleCount = 0;
    }

    public void generateTraffic(List<Road> roads) {
        cycleCount++;
        System.out.println("\n===== SIMULATION CYCLE " + cycleCount + " =====");

        for (Road road : roads) {
            road.clearVehicles();

            int count = MIN_VEHICLES +
                        random.nextInt(MAX_VEHICLES - MIN_VEHICLES + 1);

            road.setVehicleCount(count);

            if (random.nextDouble() < EMERGENCY_CHANCE) {
                String emergencyType = random.nextBoolean() ? "Ambulance" : "FireTruck";
                road.addVehicle(new Vehicle(emergencyType, 80));
                road.setEmergencyVehicle(true);
                System.out.println("EMERGENCY: " + emergencyType +
                                   " on " + road.getRoadName());
            }

            System.out.println(road.getRoadName() + " → " + road.getVehicleCount() + " vehicles");
        }
    }

    public int getCycleCount() {
        return cycleCount;
    }

    public void reset() {
        cycleCount = 0;
    }
}