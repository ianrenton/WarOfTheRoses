package waroftheroses.ai;

import java.util.ArrayList;
import waroftheroses.items.Unit;
import waroftheroses.panels.BattlePanel;
import waroftheroses.stages.Stage;

/**
 * AI routines for War of the Roses.
 * @author Ian Renton
 */
public class AI {

    /**
     * Finds the best tile for an AI-controlled unit to move to.  Currently, the
     * behaviour is that units wait until an enemy comes close enough.  At this
     * point non-ranged units charge and sit next to the weakest enemy, while ranged
     * units remain at bow range.
     * @param you The AI-controlled unit
     * @param panel The panel
     * @param stage The stage
     * @param units All units in the stage
     * @param targetTeam The team that the AI should consider targetting
     * @param ranged Whether or not this unit is ranged.  Ranged units do not
     * charge.
     * @return The x, y co-ords of the best location.
     */
    public static int[] findBestMove(Unit you, BattlePanel panel, Stage stage, ArrayList<Unit> units, int targetTeam, boolean ranged) {
        ArrayList<Unit> targets = new ArrayList<Unit>();
        for (Unit u : units) {
            if (u.getTeam() == targetTeam) {
                targets.add(u);
            }
        }

        int[] bestMove = new int[]{you.getX(), you.getY()};
        double minRank = 9999;
        for (int i = 0; i < stage.getXSize(); i++) {
            for (int j = 0; j < stage.getYSize(); j++) {
                // Consider moving to squares within range, that are passable, and not occupied
                if (you.canMoveTo(i, j) && (panel.positionOccupiedByTeam(i, j) == -1)) {
                    for (Unit u : targets) {
                        // Consider moving near to units that are alive and that you can hurt.
                        // Ranged units don't care if they can hurt the target or not, they'll
                        // attack anyway to soften targets up for infantry/cavalry.
                        if ((u.isAlive()) && ((you.getAttack() >= u.getDefence()) || ranged)) {
                            if (u.calcMoveDistance(i, j) < minRank) {
                                minRank = u.calcMoveDistance(i, j);
                                bestMove = new int[]{i, j};
                            }
                        }
                    }
                }
            }
        }
        return bestMove;
    }

    /**
     * Finds the best target for an AI-controlled unit to attack.  Units will
     * tend to attack the weakest enemy unit within their range.
     * @param you The AI-controlled unit
     * @param panel The panel
     * @param stage The stage
     * @param units All units in the stage
     * @param targetTeam The team that the AI should consider targetting
     * @return The best unit to attack.
     */
    public static Unit findBestTarget(Unit you, BattlePanel panel, Stage stage, ArrayList<Unit> units, int targetTeam) {
        Unit target = null;
        double minDef = 9999;
        for (Unit u : units) {
            // Consider attacking units within range, alive and of the correct team.
            // Thereafter pick the weakest defence unit.
            if ((you.isWithinRange(u)) && (u.isAlive()) && (u.getTeam() == targetTeam) && (u.getDefence() < minDef)) {
                target = u;
                minDef = u.getDefence();
            }
        }
        return target;
    }
}
