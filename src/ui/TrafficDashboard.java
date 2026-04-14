package ui;

import controller.SmartTrafficController;
import model.Road;
import model.TrafficSignal;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;

/**
 * ============================================================
 *  TrafficDashboard.java — UI LAYER
 * ============================================================
 *  Main Swing GUI window.
 *
 *  Layout (from top to bottom):
 *   ┌─────────────────────────────────────────────────────┐
 *   │  HEADER  (title + status bar)                       │
 *   ├──────────────────────┬──────────────────────────────┤
 *   │  INTERSECTION PANEL  │  SIDE PANEL                  │
 *   │  (custom drawing)    │  (road stats cards)           │
 *   │                      │                              │
 *   ├──────────────────────┴──────────────────────────────┤
 *   │  FOOTER (Start | Stop | Reset  +  cycle counter)    │
 *   └─────────────────────────────────────────────────────┘
 *
 *  OOP Concepts Used:
 *  - Encapsulation  : panel components are private inner classes
 *  - Inheritance    : IntersectionPanel extends JPanel
 *  - Polymorphism   : paintComponent override
 * ============================================================
 */
public class TrafficDashboard extends JFrame {

    // ── Palette ───────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(15, 20, 35);
    private static final Color BG_CARD      = new Color(22, 30, 50);
    private static final Color BG_ROAD      = new Color(40, 45, 60);
    private static final Color ACCENT_BLUE  = new Color(56, 139, 253);
    private static final Color ACCENT_GREEN = new Color(35, 197, 94);
    private static final Color ACCENT_RED   = new Color(248, 81, 73);
    private static final Color ACCENT_YELL  = new Color(255, 196, 0);
    private static final Color TEXT_PRIMARY = new Color(230, 240, 255);
    private static final Color TEXT_MUTED   = new Color(130, 145, 175);
    private static final Color ROAD_ASPHALT = new Color(55, 60, 75);
    private static final Color ROAD_LINE    = new Color(255, 220, 0);
    private static final Color GRASS_COLOR  = new Color(28, 60, 38);

    // ── Road names (N / S / E / W) ────────────────────────────
    private static final String[] ROAD_LABELS = {"NORTH", "SOUTH", "EAST", "WEST"};

    // ── UI Components ─────────────────────────────────────────
    private IntersectionPanel intersectionPanel;
    private RoadCard[]        roadCards;
    private JLabel            statusLabel;
    private JLabel            cycleLabel;
    private JLabel            activeRoadLabel;
    private JLabel            timerLabel;
    private JLabel            reasonLabel;
    private JButton           startBtn, stopBtn, resetBtn;

    // ── Data references (set by Main) ─────────────────────────
    private List<Road>              roads;
    private SmartTrafficController  controller;

    // ── State ─────────────────────────────────────────────────
    private int  currentGreenIndex = -1;
    private int  countdown         =  0;
    private String currentPhase    = "IDLE";

    // ── Constructor ───────────────────────────────────────────

    public TrafficDashboard() {
        setTitle("Real-Time Traffic Signal Optimization System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));

        buildHeader();
        buildCenter();
        buildFooter();

        setVisible(true);
    }

    // ── Layout Builders ───────────────────────────────────────

    private void buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ACCENT_BLUE),
            new EmptyBorder(12, 24, 12, 24)
        ));

        // Title
        JLabel title = new JLabel("🚦  Real-Time Traffic Signal Optimization System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        // Status pills row
        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        statusRow.setOpaque(false);

        activeRoadLabel = makeStatusPill("● ACTIVE: NONE", ACCENT_BLUE);
        timerLabel      = makeStatusPill("⏱ --s",          TEXT_MUTED);
        statusLabel     = makeStatusPill("IDLE",            TEXT_MUTED);

        statusRow.add(activeRoadLabel);
        statusRow.add(timerLabel);
        statusRow.add(statusLabel);
        header.add(statusRow, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    private JLabel makeStatusPill(String text, Color fg) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(fg);
        lbl.setBorder(new EmptyBorder(4, 10, 4, 10));
        return lbl;
    }

    private void buildCenter() {
        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setBackground(BG_DARK);
        center.setBorder(new EmptyBorder(16, 16, 0, 16));

        // ── Left: intersection drawing ─────────────────────────
        intersectionPanel = new IntersectionPanel();
        intersectionPanel.setPreferredSize(new Dimension(520, 520));
        center.add(intersectionPanel, BorderLayout.CENTER);

        // ── Right: road stat cards ─────────────────────────────
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBackground(BG_DARK);
        sidePanel.setPreferredSize(new Dimension(330, 500));

        // Info card at top
        JPanel infoCard = buildInfoCard();
        sidePanel.add(infoCard);
        sidePanel.add(Box.createVerticalStrut(12));

        // Road cards
        roadCards = new RoadCard[4];
        for (int i = 0; i < 4; i++) {
            roadCards[i] = new RoadCard(ROAD_LABELS[i]);
            sidePanel.add(roadCards[i]);
            sidePanel.add(Box.createVerticalStrut(8));
        }

        JScrollPane scroll = new JScrollPane(sidePanel);
        scroll.setBorder(null);
        scroll.setBackground(BG_DARK);
        scroll.getViewport().setBackground(BG_DARK);
        center.add(scroll, BorderLayout.EAST);

        add(center, BorderLayout.CENTER);
    }

    private JPanel buildInfoCard() {
        JPanel card = new JPanel(new GridLayout(3, 1, 0, 4));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_BLUE, 1, true),
            new EmptyBorder(10, 14, 10, 14)
        ));
        card.setMaximumSize(new Dimension(320, 100));

        cycleLabel  = styledInfoRow("Cycle", "0");
        reasonLabel = styledInfoRow("Reason", "—");
        JLabel algoLabel = styledInfoRow("Algorithm", "Density-Priority Optimization");

        card.add(cycleLabel);
        card.add(reasonLabel);
        card.add(algoLabel);

        return card;
    }

    private JLabel styledInfoRow(String key, String val) {
        JLabel lbl = new JLabel("<html><font color='#8291AF'>" + key +
                                ":</font>&nbsp;<b><font color='#E6F0FF'>"
                                + val + "</font></b></html>");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return lbl;
    }

    private void buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG_CARD);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40, 50, 80)),
            new EmptyBorder(12, 24, 12, 24)
        ));

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setOpaque(false);

        startBtn = styledButton("▶  Start Simulation", ACCENT_GREEN);
        stopBtn  = styledButton("■  Stop Simulation",  ACCENT_RED);
        resetBtn = styledButton("↺  Reset System",     ACCENT_BLUE);

        stopBtn.setEnabled(false);

        btnPanel.add(startBtn);
        btnPanel.add(stopBtn);
        btnPanel.add(resetBtn);
        footer.add(btnPanel, BorderLayout.WEST);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        legend.setOpaque(false);
        legend.add(legendDot(ACCENT_GREEN, "Green: Go"));
        legend.add(legendDot(ACCENT_YELL,  "Yellow: Caution"));
        legend.add(legendDot(ACCENT_RED,   "Red: Stop"));
        footer.add(legend, BorderLayout.EAST);

        add(footer, BorderLayout.SOUTH);
    }

    private JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isArmed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(isEnabled() ? bg : bg.darker().darker());
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(180, 38));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel legendDot(Color color, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JLabel dot  = new JLabel("●");
        dot.setForeground(color);
        dot.setFont(new Font("Dialog", Font.PLAIN, 16));
        JLabel lbl  = new JLabel(text);
        lbl.setForeground(TEXT_MUTED);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(dot); p.add(lbl);
        return p;
    }

    // ── Public Update API (called by simulation thread) ───────

    /**
     * Called by Main to wire up the data model to this view.
     */
    public void init(List<Road> roads, SmartTrafficController ctrl) {
        this.roads      = roads;
        this.controller = ctrl;
    }

    /** Refresh the entire dashboard with the latest simulation state. */
    public void updateUI(int greenIdx, int dur, int countdown,
                         String phase, String reason, int cycle) {
        SwingUtilities.invokeLater(() -> {
            this.currentGreenIndex = greenIdx;
            this.countdown         = countdown;
            this.currentPhase      = phase;

            // Header pills
            if (greenIdx >= 0 && greenIdx < ROAD_LABELS.length) {
                activeRoadLabel.setText("● ACTIVE: " + ROAD_LABELS[greenIdx]);
                activeRoadLabel.setForeground(
                    phase.equals("GREEN")  ? ACCENT_GREEN :
                    phase.equals("YELLOW") ? ACCENT_YELL  : ACCENT_RED);
            } else {
                activeRoadLabel.setText("● ACTIVE: NONE");
                activeRoadLabel.setForeground(TEXT_MUTED);
            }

            timerLabel.setText("⏱ " + countdown + "s");
            timerLabel.setForeground(
                phase.equals("GREEN")  ? ACCENT_GREEN :
                phase.equals("YELLOW") ? ACCENT_YELL  : TEXT_MUTED);

            statusLabel.setText(phase);
            statusLabel.setForeground(
                phase.equals("GREEN")  ? ACCENT_GREEN :
                phase.equals("YELLOW") ? ACCENT_YELL  :
                phase.equals("RED")    ? ACCENT_RED    : TEXT_MUTED);

            // Info card
            cycleLabel.setText ("<html><font color='#8291AF'>Cycle:</font>&nbsp;" +
                                "<b><font color='#E6F0FF'>" + cycle + "</font></b></html>");
            reasonLabel.setText("<html><font color='#8291AF'>Reason:</font>&nbsp;" +
                                "<b><font color='#E6F0FF'>" + reason + "</font></b></html>");

            // Road cards
            if (roads != null) {
                for (int i = 0; i < roadCards.length && i < roads.size(); i++) {
                    Road road = roads.get(i);
                    TrafficSignal.SignalState state = road.getSignal().getCurrentState();
                    roadCards[i].update(
                        road.getVehicleCount(),
                        road.getDensityPercentage(),
                        state,
                        road.hasEmergencyVehicle(),
                        i == greenIdx ? countdown : 0,
                        dur
                    );
                }
            }

            // Intersection repaint
            intersectionPanel.repaint();
        });
    }

    /** Update road vehicle counts on the cards (called after each simulator tick) */
    public void updateRoadData() {
        SwingUtilities.invokeLater(() -> {
            if (roads == null) return;
            for (int i = 0; i < roadCards.length && i < roads.size(); i++) {
                Road road = roads.get(i);
                roadCards[i].update(
                    road.getVehicleCount(),
                    road.getDensityPercentage(),
                    road.getSignal().getCurrentState(),
                    road.hasEmergencyVehicle(),
                    0, 0
                );
            }
            intersectionPanel.repaint();
        });
    }

    // ── Button accessors ──────────────────────────────────────

    public JButton getStartBtn() { return startBtn; }
    public JButton getStopBtn()  { return stopBtn;  }
    public JButton getResetBtn() { return resetBtn; }

    public void setRunningState(boolean running) {
        startBtn.setEnabled(!running);
        stopBtn .setEnabled( running);
    }

    // ═══════════════════════════════════════════════════════════
    //  INNER CLASS: IntersectionPanel
    //  Custom painting of the 4-road intersection with signals
    // ═══════════════════════════════════════════════════════════
    private class IntersectionPanel extends JPanel {

        IntersectionPanel() {
            setBackground(BG_DARK);
            setBorder(BorderFactory.createLineBorder(new Color(40, 50, 80), 1));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();
            int cx = W / 2, cy = H / 2;
            int roadW = 90; // pixel width of each road arm

            // ── Background (grass) ────────────────────────────
            g2.setColor(GRASS_COLOR);
            g2.fillRect(0, 0, W, H);

            // ── Road arms ─────────────────────────────────────
            g2.setColor(ROAD_ASPHALT);
            // North arm
            g2.fillRect(cx - roadW/2, 0, roadW, cy - roadW/2);
            // South arm
            g2.fillRect(cx - roadW/2, cy + roadW/2, roadW, H - cy - roadW/2);
            // West arm
            g2.fillRect(0, cy - roadW/2, cx - roadW/2, roadW);
            // East arm
            g2.fillRect(cx + roadW/2, cy - roadW/2, W - cx - roadW/2, roadW);
            // Center box
            g2.fillRect(cx - roadW/2, cy - roadW/2, roadW, roadW);

            // ── Centre box overlay color ──────────────────────
            g2.setColor(new Color(50, 55, 70));
            g2.fillRect(cx - roadW/2 + 2, cy - roadW/2 + 2,
                        roadW - 4, roadW - 4);

            // ── Road lane markings (dashes) ───────────────────
            drawLaneMarkings(g2, cx, cy, roadW, W, H);

            // ── Traffic signals ───────────────────────────────
            int lightSize = 18; // diameter of each light bulb
            // Positions: each signal is placed near the stop-line of each arm
            drawSignalPole(g2, cx - roadW/2 - 36, cy - roadW/2 - 36, 0, lightSize); // NW → North road
            drawSignalPole(g2, cx + roadW/2 + 18, cy + roadW/2 + 18, 1, lightSize); // SE → South road
            drawSignalPole(g2, cx + roadW/2 + 18, cy - roadW/2 - 36, 2, lightSize); // NE → East road
            drawSignalPole(g2, cx - roadW/2 - 36, cy + roadW/2 + 18, 3, lightSize); // SW → West road

            // ── Road name labels ──────────────────────────────
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            drawCentredLabel(g2, "NORTH", cx, 14, TEXT_PRIMARY);
            drawCentredLabel(g2, "SOUTH", cx, H - 8, TEXT_PRIMARY);
            drawRotatedLabel(g2, "WEST",  12, cy);
            drawRotatedLabelR(g2, "EAST", W - 12, cy);

            // ── Vehicle count bubbles ─────────────────────────
            if (roads != null) {
                drawVehicleBubble(g2, roads.get(0).getVehicleCount(),
                                  cx, 38, roads.get(0).hasEmergencyVehicle());
                drawVehicleBubble(g2, roads.get(1).getVehicleCount(),
                                  cx, H - 30, roads.get(1).hasEmergencyVehicle());
                drawVehicleBubble(g2, roads.get(2).getVehicleCount(),
                                  W - 30, cy, roads.get(2).hasEmergencyVehicle());
                drawVehicleBubble(g2, roads.get(3).getVehicleCount(),
                                  30, cy, roads.get(3).hasEmergencyVehicle());
            }

            // ── Animated cars (simple rectangles) ─────────────
            if (roads != null) {
                drawTrafficCars(g2, cx, cy, roadW);
            }
        }

        private void drawLaneMarkings(Graphics2D g2, int cx, int cy,
                                      int roadW, int W, int H) {
            g2.setColor(ROAD_LINE);
            float[] dash = {12f, 10f};
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT,
                         BasicStroke.JOIN_MITER, 10f, dash, 0));

            // North centre line
            g2.drawLine(cx, 0, cx, cy - roadW/2);
            // South centre line
            g2.drawLine(cx, cy + roadW/2, cx, H);
            // West centre line
            g2.drawLine(0, cy, cx - roadW/2, cy);
            // East centre line
            g2.drawLine(cx + roadW/2, cy, W, cy);

            g2.setStroke(new BasicStroke(1.5f));
        }

        /**
         * Draw a traffic signal pole + housing + 3 lights.
         * @param roadIndex 0=North, 1=South, 2=East, 3=West
         */
        private void drawSignalPole(Graphics2D g2, int x, int y,
                                    int roadIndex, int ls) {
            // Pole
            g2.setColor(new Color(80, 90, 110));
            g2.fillRect(x + 6, y + 58, 4, 16);

            // Housing
            g2.setColor(new Color(30, 35, 50));
            g2.fillRoundRect(x, y, 16, 56, 6, 6);
            g2.setColor(new Color(60, 70, 90));
            g2.drawRoundRect(x, y, 16, 56, 6, 6);

            // Determine signal state
            TrafficSignal.SignalState state = TrafficSignal.SignalState.RED;
            if (roads != null && roadIndex < roads.size()) {
                state = roads.get(roadIndex).getSignal().getCurrentState();
            }

            Color redC   = (state == TrafficSignal.SignalState.RED)    ? ACCENT_RED   : dim(ACCENT_RED);
            Color yellC  = (state == TrafficSignal.SignalState.YELLOW) ? ACCENT_YELL  : dim(ACCENT_YELL);
            Color greenC = (state == TrafficSignal.SignalState.GREEN)  ? ACCENT_GREEN : dim(ACCENT_GREEN);

            // Glow effect for active light
            if (state != TrafficSignal.SignalState.RED) {
                // subtle glow around the active light
            }

            drawLight(g2, x + 4, y + 5,  ls, redC);
            drawLight(g2, x + 4, y + 22, ls, yellC);
            drawLight(g2, x + 4, y + 39, ls, greenC);
        }

        private void drawLight(Graphics2D g2, int x, int y, int size, Color c) {
            // Glow
            if (c.getRed() > 100 || c.getGreen() > 100 || c.getBlue() > 100) {
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 40));
                g2.fillOval(x - 4, y - 4, size + 8, size + 8);
            }
            g2.setColor(c);
            g2.fillOval(x, y, size, size);
            // Specular
            g2.setColor(new Color(255, 255, 255, 60));
            g2.fillOval(x + 2, y + 2, size/2, size/3);
        }

        private Color dim(Color c) {
            return new Color(c.getRed()/5, c.getGreen()/5, c.getBlue()/5);
        }

        private void drawCentredLabel(Graphics2D g2, String text,
                                      int cx, int y, Color fg) {
            g2.setColor(fg);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, cx - fm.stringWidth(text)/2, y);
        }

        private void drawRotatedLabel(Graphics2D g2, String text, int x, int cy) {
            g2.setColor(TEXT_PRIMARY);
            Graphics2D g3 = (Graphics2D) g2.create();
            g3.rotate(-Math.PI/2, x, cy);
            FontMetrics fm = g3.getFontMetrics();
            g3.drawString(text, x - fm.stringWidth(text)/2, cy + 4);
            g3.dispose();
        }

        private void drawRotatedLabelR(Graphics2D g2, String text, int x, int cy) {
            g2.setColor(TEXT_PRIMARY);
            Graphics2D g3 = (Graphics2D) g2.create();
            g3.rotate(Math.PI/2, x, cy);
            FontMetrics fm = g3.getFontMetrics();
            g3.drawString(text, x - fm.stringWidth(text)/2, cy + 4);
            g3.dispose();
        }

        private void drawVehicleBubble(Graphics2D g2, int count,
                                       int x, int y, boolean emergency) {
            Color bg = emergency ? new Color(200, 50, 50, 200)
                                 : new Color(30, 40, 65, 200);
            g2.setColor(bg);
            g2.fillRoundRect(x - 20, y - 12, 40, 22, 10, 10);
            g2.setColor(emergency ? Color.WHITE : ACCENT_BLUE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            String lbl = (emergency ? "🚨" : "") + count + " v";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(String.valueOf(count),
                          x - fm.stringWidth(String.valueOf(count))/2, y + 4);
        }

        /** Draw simple car rectangles on roads proportional to vehicle count */
        private void drawTrafficCars(Graphics2D g2, int cx, int cy, int roadW) {
            int[] counts = new int[4];
            for (int i = 0; i < Math.min(4, roads.size()); i++) {
                counts[i] = roads.get(i).getVehicleCount();
            }

            // North road — cars stacked from bottom up
            drawCarsOnArm(g2, cx - 10, cy - roadW/2 - 5, 0, -16,
                          Math.min(counts[0], 8), isGreen(0));
            // South road — cars stacked from top down
            drawCarsOnArm(g2, cx - 10, cy + roadW/2 + 8, 0, 16,
                          Math.min(counts[1], 8), isGreen(1));
            // East road — cars right-to-left
            drawCarsOnArm(g2, cx + roadW/2 + 8, cy - 8, 16, 0,
                          Math.min(counts[2], 8), isGreen(2));
            // West road — cars left-to-right
            drawCarsOnArm(g2, cx - roadW/2 - 8, cy - 8, -16, 0,
                          Math.min(counts[3], 8), isGreen(3));
        }

        private boolean isGreen(int i) {
            return currentGreenIndex == i && currentPhase.equals("GREEN");
        }

        private void drawCarsOnArm(Graphics2D g2, int startX, int startY,
                                   int dx, int dy, int count, boolean moving) {
            for (int i = 0; i < count; i++) {
                int x = startX + dx * i;
                int y = startY + dy * i;
                // Car body
                Color carColor = moving ? new Color(56, 200, 100) : new Color(200, 80, 80);
                g2.setColor(carColor);
                if (dx == 0) { // vertical road
                    g2.fillRoundRect(x, y, 14, 10, 3, 3);
                } else {       // horizontal road
                    g2.fillRoundRect(x, y, 10, 14, 3, 3);
                }
                // Headlights
                g2.setColor(new Color(255, 250, 200, 180));
                if (dx == 0) {
                    g2.fillRect(x + 1, y + (dy > 0 ? 1 : 7), 3, 2);
                    g2.fillRect(x + 10, y + (dy > 0 ? 1 : 7), 3, 2);
                } else {
                    g2.fillRect(x + (dx > 0 ? 7 : 1), y + 1, 2, 3);
                    g2.fillRect(x + (dx > 0 ? 7 : 1), y + 10, 2, 3);
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  INNER CLASS: RoadCard
    //  One card per road showing signal + stats
    // ═══════════════════════════════════════════════════════════
    private class RoadCard extends JPanel {

        private final String  name;
        private JLabel  signalDot;
        private JLabel  countLabel;
        private JLabel  densityLabel;
        private JLabel  timerLbl;
        private JLabel  emergencyLbl;
        private JProgressBar densityBar;

        RoadCard(String name) {
            this.name = name;
            setLayout(new BorderLayout(8, 0));
            setBackground(BG_CARD);
            setMaximumSize(new Dimension(320, 72));
            setPreferredSize(new Dimension(310, 72));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 55, 90), 1, true),
                new EmptyBorder(8, 12, 8, 12)
            ));

            // Signal dot
            signalDot = new JLabel("●");
            signalDot.setFont(new Font("Dialog", Font.PLAIN, 28));
            signalDot.setForeground(ACCENT_RED);
            signalDot.setPreferredSize(new Dimension(34, 60));
            add(signalDot, BorderLayout.WEST);

            // Centre info
            JPanel info = new JPanel(new GridLayout(3, 1, 0, 2));
            info.setOpaque(false);

            JLabel nameLabel = new JLabel(name + " ROAD");
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            nameLabel.setForeground(TEXT_PRIMARY);

            countLabel   = new JLabel("Vehicles: 0");
            countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            countLabel.setForeground(TEXT_MUTED);

            densityLabel = new JLabel("Density: 0%");
            densityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            densityLabel.setForeground(TEXT_MUTED);

            info.add(nameLabel);
            info.add(countLabel);
            info.add(densityLabel);
            add(info, BorderLayout.CENTER);

            // Right: timer + emergency
            JPanel right = new JPanel(new GridLayout(2, 1, 0, 2));
            right.setOpaque(false);
            right.setPreferredSize(new Dimension(70, 60));

            timerLbl     = new JLabel("", SwingConstants.RIGHT);
            timerLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
            timerLbl.setForeground(TEXT_MUTED);

            emergencyLbl = new JLabel("", SwingConstants.RIGHT);
            emergencyLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            emergencyLbl.setForeground(ACCENT_RED);

            right.add(timerLbl);
            right.add(emergencyLbl);
            add(right, BorderLayout.EAST);
        }

        void update(int vehicles, int densityPct,
                    TrafficSignal.SignalState state,
                    boolean emergency, int countdown, int totalDur) {

            countLabel.setText  ("Vehicles: " + vehicles);
            densityLabel.setText("Density:  " + densityPct + "%");
            emergencyLbl.setText(emergency ? "🚨 EMERGENCY" : "");

            switch (state) {
                case GREEN  -> { signalDot.setForeground(ACCENT_GREEN);
                                 timerLbl.setForeground(ACCENT_GREEN);
                                 timerLbl.setText(countdown > 0 ? countdown + "s" : ""); }
                case YELLOW -> { signalDot.setForeground(ACCENT_YELL);
                                 timerLbl.setForeground(ACCENT_YELL);
                                 timerLbl.setText(countdown > 0 ? countdown + "s" : ""); }
                default     -> { signalDot.setForeground(ACCENT_RED);
                                 timerLbl.setForeground(TEXT_MUTED);
                                 timerLbl.setText(""); }
            }

            // Card border highlights active road
            if (state == TrafficSignal.SignalState.GREEN) {
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_GREEN, 2, true),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            } else if (emergency) {
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_RED, 2, true),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            } else {
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(40, 55, 90), 1, true),
                    new EmptyBorder(8, 12, 8, 12)
                ));
            }

            repaint();
        }
    }
}