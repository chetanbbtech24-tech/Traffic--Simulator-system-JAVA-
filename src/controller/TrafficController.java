package controller;

import model.Road;
import java.util.List;

/**
 * ============================================================
 *  TrafficController.java — CONTROLLER LAYER (Abstract)
 * ============================================================
 *  Abstract base class for all traffic controllers.
 *
 *  OOP Concepts Used:
 *  - Abstraction   : declares abstract method controlTraffic()
 *                    forcing all subclasses to provide logic
 *  - Inheritance   : SmartTrafficController extends this class
 *  - Polymorphism  : caller code works with TrafficController
 *                    reference, actual runtime type can vary
 * ============================================================
 */
public abstract class TrafficController {

    // ── Protected Fields (accessible to subclasses) ───────────
    protected List<Road> roads;
    protected int        currentGreenIndex; // which road is currently GREEN
    protected boolean    running;

    // ── Constructor ───────────────────────────────────────────

    public TrafficController(List<Road> roads) {
        this.roads             = roads;
        this.currentGreenIndex = 0;
        this.running           = true;
    }

    // ── Abstract Method (must be implemented by subclass) ─────

    /**
     * Core traffic control logic.
     * Each subclass decides HOW to assign green signals.
     */
    public abstract void controlTraffic();

    // ── Concrete Shared Behaviour ─────────────────────────────

    /** Set all signals to RED — safe baseline state */
    protected void allRed() {
        for (Road road : roads) {
            road.getSignal().setRed();
        }
    }

    /** How many roads are being managed */
    public int getRoadCount() { return roads.size(); }

    // ── Getters & Setters ─────────────────────────────────────

    public List<Road> getRoads()              { return roads; }
    public int        getCurrentGreenIndex()  { return currentGreenIndex; }
    public boolean    isRunning()             { return running; }

    public void setRunning(boolean r)         { this.running = r; }
}