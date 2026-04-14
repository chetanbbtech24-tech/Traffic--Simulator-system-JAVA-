package service;

import model.Road;
import java.util.List;

/**
 * ============================================================
 *  OptimizationService.java — SERVICE LAYER
 * ============================================================
 *  Contains the smart optimization algorithm that:
 *   1. Finds the road with highest traffic density
 *   2. Handles emergency-vehicle priority
 *   3. Calculates green-signal duration proportionally
 *   4. Ensures minimum green time for low-traffic roads
 *
 *  OOP Concepts Used:
 *  - Encapsulation  : algorithm details hidden inside the service
 *  - Abstraction    : controller just calls findBestRoadIndex()
 *                     without knowing implementation details
 * ============================================================
 */
public class OptimizationService {

    // ── Constants ─────────────────────────────────────────────
    private static final int MIN_GREEN_TIME =  8;   // seconds — fairness floor
    private static final int MAX_GREEN_TIME = 60;   // seconds — upper cap
    private static final int BASE_TIME      = 10;   // base seconds per cycle

    /**
     * Find the index of the road that should receive green next.
     *
     * Priority logic (highest → lowest):
     *   1. Road with an emergency vehicle (ambulance / fire truck)
     *   2. Road with the highest vehicle count
     *
     * @param roads the list of roads at the intersection
     * @return index of the road to grant GREEN
     */
    public int findBestRoadIndex(List<Road> roads) {

        // ── 1. Emergency vehicle override ─────────────────────
        for (int i = 0; i < roads.size(); i++) {
            if (roads.get(i).hasEmergencyVehicle()) {
                System.out.println("[PRIORITY] Emergency vehicle on: " +
                                   roads.get(i).getRoadName());
                return i;
            }
        }

        // ── 2. Highest traffic density ────────────────────────
        int bestIndex = 0;
        int maxCount  = -1;

        for (int i = 0; i < roads.size(); i++) {
            if (roads.get(i).getVehicleCount() > maxCount) {
                maxCount  = roads.get(i).getVehicleCount();
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    /**
     * Calculate how long the green signal should stay on for a given road.
     *
     * Formula:
     *   greenTime = BASE_TIME + (vehicleCount / totalVehicles) * extraTime
     *
     * Clamped between MIN_GREEN_TIME and MAX_GREEN_TIME.
     *
     * @param road   the road receiving the green signal
     * @param roads  all roads (needed to compute proportion)
     * @return green signal duration in seconds
     */
    public int calculateGreenDuration(Road road, List<Road> roads) {

        // Emergency vehicles always get a fixed short green burst
        if (road.hasEmergencyVehicle()) {
            return 15;
        }

        int totalVehicles = roads.stream()
                                 .mapToInt(Road::getVehicleCount)
                                 .sum();

        if (totalVehicles == 0) {
            return MIN_GREEN_TIME;
        }

        int extraTime = MAX_GREEN_TIME - BASE_TIME;  // = 50
        double proportion = (double) road.getVehicleCount() / totalVehicles;
        int greenTime = BASE_TIME + (int)(proportion * extraTime);

        // Clamp to safe range
        return Math.max(MIN_GREEN_TIME, Math.min(MAX_GREEN_TIME, greenTime));
    }

    /**
     * Calculate yellow transition time (constant, could be extended).
     * @return yellow duration in seconds
     */
    public int getYellowDuration() {
        return 3;
    }

    /**
     * Returns a user-friendly summary of why a road was chosen.
     */
    public String getDecisionReason(Road road, List<Road> roads) {
        if (road.hasEmergencyVehicle()) {
            return "🚨 EMERGENCY PRIORITY";
        }
        int totalVehicles = roads.stream().mapToInt(Road::getVehicleCount).sum();
        if (totalVehicles == 0) return "Minimum fairness time";
        int pct = (int)((double) road.getVehicleCount() / totalVehicles * 100);
        return "Highest density (" + pct + "% of total traffic)";
    }
}