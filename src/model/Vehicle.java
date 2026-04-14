package model;

public class Vehicle {
    
    // ── Encapsulated Fields ───────────────────────────────────
    private String type;   // e.g. "Car", "Bus", "Bike", "Truck"
    private int    speed;  // speed in km/h (optional attribute)
 
    // ── Constructors ──────────────────────────────────────────
  // Existing constructor
   
    /** Full constructor */
    public Vehicle(String type, int speed) {
        this.type  = type;
        this.speed = speed;
    }
 
    /** Convenience constructor — speed defaults to a typical value */
    public Vehicle(String type) {
        this(type, getDefaultSpeed(type));
    }
 
    // ── Getters & Setters ─────────────────────────────────────
 
    public String getType()        { return type; }
    public void   setType(String t){ this.type = t; }
 
    public int  getSpeed()         { return speed; }
    public void setSpeed(int s)    { this.speed = s; }
 
    // ── Helper ────────────────────────────────────────────────
 
    /** Returns a realistic default speed for each vehicle type */
    private static int getDefaultSpeed(String type) {
        return switch (type.toLowerCase()) {
            case "bus"       -> 40;
            case "truck"     -> 35;
            case "bike"      -> 50;
            case "ambulance" -> 80;
            default          -> 60;  // car
        };
    }
 
    @Override
    public String toString() {
        return "Vehicle{type='" + type + "', speed=" + speed + " km/h}";
    }
}
