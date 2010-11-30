package waroftheroses.panels;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import waroftheroses.app.WarOfTheRoses;
import waroftheroses.items.ItemFactory;
import waroftheroses.items.Unit;
import waroftheroses.stages.Stage;

/**
 * A specialised JPanel for the main battle (tile grid) display.
 * @author Ian Renton
 */
public class BattlePanel extends InteractivePanel {

    /** Is a unit currently being moved (blue tiles around)? */
    private boolean moving = false;
    /** ID of currently selected unit (if moving). */
    private int selectedUnit = 0;
    private Graphics2D comp2D;
    private final WarOfTheRoses caller;
    /** Last unit mouse-overed, so we don't redraw if the mouse moves but doesn't move to a different unit. */
    private Unit lastUnitMouseOvered;
    /** Ally unit currently having its info displayed. */
    private Unit unitInfoAllyUnit;
    /** Enemy unit currently having its info displayed. */
    private Unit unitInfoEnemyUnit;
    private BufferedImage allyUnitInfoBG = null;
    private BufferedImage enemyUnitInfoBG = null;
    /** The battle stage */
    Stage stage;
    /** Item Factory for creating units */
    ItemFactory factory = new ItemFactory();
    /** Complete list of all units in play */
    ArrayList<Unit> units = new ArrayList<Unit>();
    Timer timer = new Timer();
    int repaintCounter = 0;

    /**
     * Schedules a repaint of a certain area of the screen at some later time.
     */
    public class DelayedRepaint extends TimerTask {

        private final int x;
        private final int y;
        private final int width;
        private final int height;

        /**
         * Schedules a repaint of a certain area of the screen at some later time.
         */
        public DelayedRepaint(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Override
        public void run() {
            paintImmediately(x, y, width, height);
        }
    }

    /**
     * Schedules a repaint of the whole screen at some later time.  Run on
     * creation of the BattlePanel.
     */
    public class InitialRepaint extends TimerTask {

        @Override
        public void run() {
            repaint();
        }
    }

    /**
     * Comparator that sorts units by their position in the Y dimension - i.e.
     * we sort them far-to-near as the camera sees the battlefield.  This makes
     * sure units overlap each other properly.
     */
    public class CompareUnitsByYOrder implements Comparator<Unit> {

        public int compare(Unit arg0, Unit arg1) {
            return arg0.getY() - arg1.getY();
        }
    }

    /**
     * Create a new Battle Panel.
     * @param caller the app itself.
     */
    public BattlePanel(WarOfTheRoses caller) {
        this.caller = caller;

        // Demo only - load only stage immediately.
        stage = new Stage("test01", factory);
        units = stage.getUnits();

        // Load unit info backgrounds
        try {
            allyUnitInfoBG = ImageIO.read(getClass().getResource("gui/allyunitinfo.png").openStream());
            enemyUnitInfoBG = ImageIO.read(getClass().getResource("gui/enemyunitinfo.png").openStream());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Unable to read graphics files.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        // First repaint
        InitialRepaint repainter = new InitialRepaint();
        timer.schedule(repainter, 1000);
    }

    /**
     * Master paint operation.  Will repaint everything on the panel.
     * @param comp
     */
    @Override
    public void paintComponent(Graphics comp) {
        comp2D = (Graphics2D) comp;

        comp2D.setColor(new Color(153, 204, 255));
        comp2D.fillRect(0, 0, getWidth(), getHeight());

        // Render stage
        stage.render(this, comp2D, moving, units.get(selectedUnit));

        // Sort units by Y order
        Collections.sort(units, new CompareUnitsByYOrder());

        // Render units
        for (Unit u : units) {
            u.renderSquare(comp2D, stage);
        }
        for (Unit u : units) {
            u.render(comp2D, stage);
        }

        // Render unit info
        renderUnitInfoBoxes(comp2D);
        if (unitInfoAllyUnit != null) {
            unitInfoAllyUnit.renderUnitInfo(this, comp2D);
        }
        if (unitInfoEnemyUnit != null) {
            unitInfoEnemyUnit.renderUnitInfo(this, comp2D);
        }

        // Render End Turn button
        comp2D.setColor(new Color(240, 240, 180));
        comp2D.fillRect(getWidth() - 100, getHeight() - 50, 90, 40);
        comp2D.setColor(Color.black);
        comp2D.drawRect(getWidth() - 100, getHeight() - 50, 90, 40);
        comp2D.setFont(new Font("Lucida Typewriter", Font.PLAIN, 12));
        comp2D.drawString("End Turn", getWidth() - 80, getHeight() - 26);

        repaintCounter++;
    }

    /**
     * Renders the Unit Info boxes
     * @param comp2D
     */
    private void renderUnitInfoBoxes(Graphics2D comp2D) {
        comp2D.drawImage(allyUnitInfoBG, getWidth() - 210, 10, null);
        comp2D.drawImage(enemyUnitInfoBG, getWidth() - 210, 220, null);
    }

    /**
     * Deals with a mouse movement.  Updates the unit info panels if the mouse
     * has moved over a new unit.
     * During a move operation, the selected unit remains in the Ally info panel
     * at all times, and is not replaced if you mouse-over another ally unit.
     * @param evt
     */
    public void processMouseMove(java.awt.event.MouseEvent evt) {
        int[] pos = stage.mouseCoordsToTile(evt.getPoint().x, evt.getPoint().y);
        Unit unit = getUnitAtPosition(pos[0], pos[1]);
        if ((unit != null) && (unit != lastUnitMouseOvered)) {
            lastUnitMouseOvered = unit;
            // If unit is yours and you're not moving, or an enemy, update the unit box.
            // The check for not moving means that once you've selected one of
            // your units for moving, it'll stay with its stats displayed until
            // you stop moving.
            if ((unit.getTeam() == 0) && !moving) {
                unitInfoAllyUnit = unit;
            } else if (unit.getTeam() != 0) {
                unitInfoEnemyUnit = unit;
            }
            paintImmediately(getWidth() - 210, 0, getWidth(), 420);
        }
    }

    /**
     * Deals with a mouse click - selects a unit if nothing selected, moves /
     * attacks if a unit is selected, ends turn if button clicked.
     * @param evt
     */
    public void processMouseClick(java.awt.event.MouseEvent evt) {
        int x = evt.getPoint().x;
        int y = evt.getPoint().y;
        if ((x < getWidth() - 10) && (x > getWidth() - 100) && (y < getHeight() - 10) && (y > getHeight() - 50)) {
            endTurn();
        } else {

            int[] pos = stage.mouseCoordsToTile(x, y);
            if (moving) {
                if (units.get(selectedUnit).isOwnSquare(pos[0], pos[1])) {
                    moving = false;
                } else if ((units.get(selectedUnit).canMoveTo(pos[0], pos[1])) && (positionOccupiedByTeam(pos[0], pos[1]) == -1)) {
                    moving = false;
                    units.get(selectedUnit).moveTo(this, stage, pos[0], pos[1], true);
                } else if ((units.get(selectedUnit).isWithinRange(pos[0], pos[1])) && (positionOccupiedByTeam(pos[0], pos[1]) > 0) && (units.get(selectedUnit).canAttack()) && (getUnitAtPosition(pos[0], pos[1]).isAlive())) {
                    moving = false;
                    units.get(selectedUnit).attack(this, stage, getUnitAtPosition(pos[0], pos[1]), true);
                } else if (positionOccupiedByTeam(pos[0], pos[1]) == 0) {
                    moving = false;
                }
            } else {
                selectUnitAtPosition(pos[0], pos[1]);
            }
        }
        repaint();
    }

    /**
     * Marks the unit at the chosen position as selected (move/attack indicators
     * will be shown on the next repaint.
     * @param x
     * @param y
     */
    private void selectUnitAtPosition(int x, int y) {
        for (int i = 0; i < units.size(); i++) {
            if ((units.get(i).getX() == x) && (units.get(i).getY() == y) && (units.get(i).getTeam() == 0) && (units.get(i).canAct()) && (units.get(i).isAlive())) {
                selectedUnit = i;
                moving = true;
                units.get(i).refreshMoveGrid(stage);
            }
        }
    }

    public Unit getUnitAtPosition(int x, int y) {
        for (int i = 0; i < units.size(); i++) {
            if ((units.get(i).getX() == x) && (units.get(i).getY() == y)) {
                return units.get(i);
            }
        }
        return null;
    }

    public int positionOccupiedByTeam(int x, int y) {
        for (int i = 0; i < units.size(); i++) {
            if ((units.get(i).getX() == x) && (units.get(i).getY() == y)) {
                return units.get(i).getTeam();
            }
        }
        return -1;
    }

    private void endTurn() {
        moving = false;
        checkForWinOrLose();

        for (Unit u : units) {
            u.refreshMoveGrid(stage);
        }
        for (Unit u : units) {
            if (u.getTeam() > 0) {
                u.aiAct(this, stage, units, 0);
            }
        }
        checkForWinOrLose();

        for (Unit u : units) {
            u.newTurn();
        }
    }

    public void checkForWinOrLose() {
        int enemyUnitsRemaining = 0;
        int playerUnitsRemaining = 0;
        for (Unit u : units) {
            if ((u.getTeam() > 0) && u.isAlive()) {
                enemyUnitsRemaining++;
            } else if ((u.getTeam() == 0) && u.isAlive()) {
                playerUnitsRemaining++;
            }
        }
        if (playerUnitsRemaining == 0) {
            JOptionPane.showMessageDialog(this, "All your units have been defeated.");
            caller.switchpanels(new MainMenuPanel(caller));
        } else if (enemyUnitsRemaining == 0) {
            JOptionPane.showMessageDialog(this, "Conglaturation!  All enemy units have been defeated.");
            caller.switchpanels(new MainMenuPanel(caller));
        }
    }

    public void scheduleUpdates(int number, int[] region) {
        int currentNumber = repaintCounter;
        // Single repaint of the whole thing first, to make sure we're up-to-date
        paintImmediately(0, 0, getWidth(), getHeight());
        timer = new Timer();
        DelayedRepaint repainter = new DelayedRepaint(region[0], region[1], region[2], region[3]);
        timer.scheduleAtFixedRate(repainter, 100, 100);
        while (repaintCounter <= currentNumber + number + 1) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
            }
        }
        timer.cancel();
    }
}
