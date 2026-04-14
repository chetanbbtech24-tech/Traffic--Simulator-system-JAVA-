package controller;

import model.Road;
import service.OptimizationService;

import java.util.List;
import java.util.function.Consumer;

/**
 * ============================================================
 *  SmartTrafficController.java — CONTROLLER LAYER (Concrete)
 * ============================================================
 *  Extends the abstract TrafficController and provides the
 *  actual smart optimization logic.
 *
 *  OOP Concepts Used:
 *  - Inheritance    : extends TrafficController
 *  - Polymorphism   : overrides abstract controlTraffic()
 *  - Encapsulation  : delegates to OptimizationService internally
 *  - Abstraction    : UI / simulator only calls controlTraffic()
 * ============================================================
 */
public class SmartTrafficController extends TrafficController {

    // ── Dependencies ──────────────────────────────────────────
    private final OptimizationService optimizer;

    /**
     * Callback fired every time the controller state changes.
     * The GUI registers here to receive live updates.
     */
    private Consumer<ControllerState> onStateChange;

    // ── Inner Data Class (snapshot sent to UI) ────────────────
    public static class ControllerState {
        public final int    greenRoadIndex;
        public final int    greenDuration;      // total green seconds
        public final int    countdown;          // seconds remaining
        public final String phase;              // "GREEN", "YELLOW", "RED"
        public final String decisionReason;

        public ControllerState(int idx, int dur, int cd, String phase, String reason) {
            this.greenRoadIndex  = idx;
            this.greenDuration   = dur;
            this.countdown       = cd;
            this.phase           = phase;
            this.decisionReason  = reason;
        }
    }

    // ── Constructor ───────────────────────────────────────────

    public SmartTrafficController(List<Road> roads) {
        super(roads);
        this.optimizer = new OptimizationService();
    }

    // ── Setter for UI callback ────────────────────────────────

    public void setOnStateChange(Consumer<ControllerState> callback) {
        this.onStateChange = callback;
    }

    // ── Core Algorithm (overrides abstract method) ────────────

    /**
     * One full traffic cycle:
     *  1. Pick best road via OptimizationService
     *  2. Set that road GREEN, rest RED
     *  3. Count down green timer second by second
     *  4. Switch to YELLOW for transition
     *  5. Return to all-RED before next cycle
     */
    @Override
    public void controlTraffic() {
        if (!running || roads.isEmpty()) return;

        // ── Step 1: Choose which road gets GREEN ───────────────
        currentGreenIndex = optimizer.findBestRoadIndex(roads);
        Road greenRoad    = roads.get(currentGreenIndex);

        // ── Step 2: Calculate durations ────────────────────────
        int greenSecs  = optimizer.calculateGreenDuration(greenRoad, roads);
        int yellowSecs = optimizer.getYellowDuration();
        String reason  = optimizer.getDecisionReason(greenRoad, roads);

        // Update signal model
        greenRoad.getSignal().setGreenDuration(greenSecs);
        greenRoad.getSignal().setYellowDuration(yellowSecs);

        System.out.printf("[CTRL] %s → GREEN for %ds  (%s)%n",
                          greenRoad.getRoadName(), greenSecs, reason);

        // ── Step 3: All RED → then target road GREEN ───────────
        allRed();
        greenRoad.getSignal().setGreen();

        // ── Step 4: GREEN countdown ────────────────────────────
        for (int t = greenSecs; t > 0 && running; t--) {
            fireStateChange(currentGreenIndex, greenSecs, t, "GREEN", reason);
            sleepOneSecond();
        }

        // ── Step 5: YELLOW transition ──────────────────────────
        if (running) {
            greenRoad.getSignal().setYellow();
            for (int t = yellowSecs; t > 0 && running; t--) {
                fireStateChange(currentGreenIndex, yellowSecs, t, "YELLOW", reason);
                sleepOneSecond();
            }
        }

        // ── Step 6: Back to RED ────────────────────────────────
        allRed();
        fireStateChange(-1, 0, 0, "RED", "Transitioning…");
    }

    // ── Helpers ───────────────────────────────────────────────

    private void fireStateChange(int idx, int dur, int cd,
                                 String phase, String reason) {
        if (onStateChange != null) {
            onStateChange.accept(new ControllerState(idx, dur, cd, phase, reason));
        }
    }

    private void sleepOneSecond() {
        try { Thread.sleep(1000); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}