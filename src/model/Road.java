package model;
import java.util.ArrayList;
import java.util.List;
public class Road {
    
    // ── Encapsulated Fields ───────────────────────────────────
    private String        roadName;
    private int           vehicleCount;
    private boolean       hasEmergencyVehicle;
    private TrafficSignal signal;
    private List<Vehicle> vehicles;
 
    // ── Constructor ───────────────────────────────────────────
 
    public Road(String roadName) {
        this.roadName            = roadName;
        this.vehicleCount        = 0;
        this.hasEmergencyVehicle = false;
        this.vehicles            = new ArrayList<>();
        this.signal              = new TrafficSignal(roadName);
    }
 
    // ── Vehicle Management ────────────────────────────────────
 
    /** Add a vehicle to this road */
    public void addVehicle(Vehicle v) {
        vehicles.add(v);
        vehicleCount = vehicles.size();
        // Check if the new vehicle is an emergency vehicle
        if (v.getType().equalsIgnoreCase("Ambulance") ||
            v.getType().equalsIgnoreCase("FireTruck")) {
            hasEmergencyVehicle = true;
        }
    }
 
    /** Clear all vehicles (used during reset) */
    public void clearVehicles() {
        vehicles.clear();
        vehicleCount        = 0;
        hasEmergencyVehicle = false;
    }
 
    /**
     * Directly set the vehicle count (used by simulator for fast bulk assignment).
     * Also rebuilds a matching list of generic Vehicle objects.
     */
    public void setVehicleCount(int count) {
        this.vehicleCount = count;
        vehicles.clear();
        for (int i = 0; i < count; i++) {
            vehicles.add(new Vehicle("Car"));
        }
        hasEmergencyVehicle = false; // reset emergency flag on new data
    }
 
    // ── Getters & Setters ─────────────────────────────────────
 
    public String getRoadName()                       { return roadName; }
    public void   setRoadName(String name)            { this.roadName = name; }
 
    public int  getVehicleCount()                     { return vehicleCount; }
 
    public boolean hasEmergencyVehicle()              { return hasEmergencyVehicle; }
    public void    setEmergencyVehicle(boolean flag)  { this.hasEmergencyVehicle = flag; }
 
    public TrafficSignal getSignal()                  { return signal; }
 
    public List<Vehicle> getVehicles()                { return new ArrayList<>(vehicles); } // defensive copy
 
    /** Traffic density as a percentage (0–100) for display purposes */
    public int getDensityPercentage() {
        // Assuming max realistic vehicle count per cycle is 60
        return Math.min(100, (int)((vehicleCount / 60.0) * 100));
    }
 
    @Override
    public String toString() {
        return "Road{name='" + roadName +
               "', vehicles=" + vehicleCount +
               ", signal=" + signal.getCurrentState() +
               ", emergency=" + hasEmergencyVehicle + "}";
    }
}
