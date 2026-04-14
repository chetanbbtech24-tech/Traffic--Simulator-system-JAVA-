package model;

public class TrafficSignal {
    
    /** Possible states for any traffic signal */
    public enum SignalState { RED, YELLOW, GREEN }
 
    // ── Encapsulated Fields ───────────────────────────────────
    private SignalState currentState;
    private int         greenDuration;   // seconds this signal stays green
    private int         yellowDuration;  // seconds for yellow transition
    private String      roadName;        // the road this signal belongs to
 
    // ── Constructor ───────────────────────────────────────────
 
    public TrafficSignal(String roadName) {
        this.roadName      = roadName;
        this.currentState  = SignalState.RED;  // everything starts RED
        this.greenDuration  = 30;              // default 30 s
        this.yellowDuration = 3;               // default  3 s
    }
 
    // ── Signal Control Methods ────────────────────────────────
 
    /** Switch signal to GREEN */
    public void setGreen()  { currentState = SignalState.GREEN;  }
 
    /** Switch signal to YELLOW */
    public void setYellow() { currentState = SignalState.YELLOW; }
 
    /** Switch signal to RED */
    public void setRed()    { currentState = SignalState.RED;    }
 
    /** Convenience check */
    public boolean isGreen()  { return currentState == SignalState.GREEN;  }
    public boolean isYellow() { return currentState == SignalState.YELLOW; }
    public boolean isRed()    { return currentState == SignalState.RED;    }
 
    // ── Getters & Setters ─────────────────────────────────────
 
    public SignalState getCurrentState()          { return currentState; }
    public void        setCurrentState(SignalState s) { this.currentState = s; }
 
    public int  getGreenDuration()                { return greenDuration; }
    public void setGreenDuration(int duration)    { this.greenDuration = duration; }
 
    public int  getYellowDuration()               { return yellowDuration; }
    public void setYellowDuration(int duration)   { this.yellowDuration = duration; }
 
    public String getRoadName()                   { return roadName; }
    public void   setRoadName(String n)           { this.roadName = n; }
 
    @Override
    public String toString() {
        return "TrafficSignal{road='" + roadName +
               "', state=" + currentState +
               ", greenDuration=" + greenDuration + "s}";
    }
}
