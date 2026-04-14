package main;

import controller.SmartTrafficController;
import model.Road;
import simulation.TrafficSimulator;
import ui.TrafficDashboard;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 *  Main.java — ENTRY POINT
 * ============================================================
 *  Wires together all layers and starts the simulation.
 *
 *  Execution Flow:
 *   1. Create Road objects (model layer)
 *   2. Create SmartTrafficController (controller layer)
 *   3. Create TrafficSimulator (simulation layer)
 *   4. Create TrafficDashboard GUI (ui layer)
 *   5. Connect controller callback → GUI update
 *   6. Wire Start / Stop / Reset buttons
 *   7. Launch simulation thread on Start
 *
 *  Multithreading:
 *   - GUI runs on Swing Event Dispatch Thread (EDT)
 *   - Simulation loop runs on a dedicated background Thread
 *   - All GUI mutations dispatched via SwingUtilities.invokeLater()
 * ============================================================
 */
public class Main {

    // ── Roads (4 for a standard intersection) ─────────────────
    private static final String[] ROAD_NAMES = {"North", "South", "East", "West"};

    // ── Simulation state ──────────────────────────────────────
    private static volatile boolean simulationRunning = false;
    private static Thread           simulationThread  = null;
    private static int              cycleCount        = 0;

    // ── Component references ──────────────────────────────────
    private static List<Road>              roads;
    private static SmartTrafficController  controller;
    private static TrafficSimulator        simulator;
    private static TrafficDashboard        dashboard;

    // ── Main ──────────────────────────────────────────────────

    public static void main(String[] args) {

        // ── Step 1: Set look-and-feel ──────────────────────────
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // ── Step 2: Create roads ───────────────────────────────
        roads = new ArrayList<>();
        for (String name : ROAD_NAMES) {
            roads.add(new Road(name));
        }

        // ── Step 3: Create controller and simulator ────────────
        controller = new SmartTrafficController(roads);
        simulator  = new TrafficSimulator();

        // ── Step 4: Launch GUI on EDT ──────────────────────────
        SwingUtilities.invokeLater(() -> {
            dashboard = new TrafficDashboard();
            dashboard.init(roads, controller);

            // ── Step 5: Wire controller → GUI callback ─────────
            controller.setOnStateChange(state -> {
                dashboard.updateUI(
                    state.greenRoadIndex,
                    state.greenDuration,
                    state.countdown,
                    state.phase,
                    state.decisionReason,
                    cycleCount
                );
            });

            // ── Step 6: Wire buttons ───────────────────────────
            dashboard.getStartBtn().addActionListener(e -> startSimulation());
            dashboard.getStopBtn() .addActionListener(e -> stopSimulation());
            dashboard.getResetBtn().addActionListener(e -> resetSimulation());
        });
    }

    // ── Simulation Control ────────────────────────────────────

    /**
     * Starts the simulation loop in a background thread.
     * The loop:
     *   1. Generate new random traffic data
     *   2. Run one full traffic cycle (green + yellow per road turn)
     *   3. Repeat until stopped
     */
    private static void startSimulation() {
        if (simulationRunning) return;

        simulationRunning = true;
        controller.setRunning(true);
        dashboard.setRunningState(true);

        simulationThread = new Thread(() -> {
            System.out.println("=== SIMULATION STARTED ===");

            while (simulationRunning) {
                // ── Generate new traffic data ──────────────────
                simulator.generateTraffic(roads);
                cycleCount = simulator.getCycleCount();

                // Update road cards immediately after data generation
                dashboard.updateRoadData();

                // ── Run one optimized traffic cycle ───────────
                controller.controlTraffic();

                // Small pause between cycles
                if (simulationRunning) {
                    try { Thread.sleep(500); }
                    catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            System.out.println("=== SIMULATION STOPPED ===");
        });

        simulationThread.setDaemon(true);
        simulationThread.setName("SimulationThread");
        simulationThread.start();
    }

    /** Signals the simulation to stop gracefully. */
    private static void stopSimulation() {
        simulationRunning = false;
        controller.setRunning(false);
        dashboard.setRunningState(false);

        if (simulationThread != null) {
            simulationThread.interrupt();
        }
    }

    /**
     * Stops the simulation and resets all state:
     * - clears vehicle counts
     * - sets all signals to RED
     * - resets cycle counter
     * - updates GUI
     */
    private static void resetSimulation() {
        stopSimulation();

        // Short pause to let thread stop cleanly
        try { Thread.sleep(200); } catch (InterruptedException ignored) {}

        // Reset model
        for (Road road : roads) {
            road.clearVehicles();
            road.getSignal().setRed();
        }
        simulator.reset();
        cycleCount = 0;

        // Reset GUI
        dashboard.updateUI(-1, 0, 0, "IDLE", "System Reset", 0);
        dashboard.updateRoadData();

        System.out.println("=== SYSTEM RESET ===");
    }
}