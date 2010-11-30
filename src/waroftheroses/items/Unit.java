package waroftheroses.items;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import waroftheroses.ai.AI;
import waroftheroses.panels.BattlePanel;
import waroftheroses.stages.Stage;

/**
 *
 * @author irenton
 */
public class Unit {

    private int DAMAGE_MULTIPLIER = 10;
    private int baseAttack;
    private int baseDefence;
    private int baseRange;
    private int baseMove;
    private int baseMaxMorale = 100;
    private int morale;
    private int team;
    private String type;
    private String name;
    private String fullName;
    private int id;
    private String house;
    private Leader leader1;
    private Leader leader2;
    private int xPos = 0;
    private int yPos = 0;
    private boolean standingBy = true;
    private boolean defending = true;
    BufferedImage icon;
    BufferedImage rank1;
    BufferedImage rank2;
    BufferedImage rank3;
    BufferedImage selectyellow;
    BufferedImage dead;
    BufferedImage playerControlStar;
    BufferedImage allyControlStar;
    BufferedImage enemyControlStar;
    private int movesLeft;
    private boolean canAttack = true;
    private boolean hasBeenAttacked = false;
    private int height = 0;
    private int rank = 0;
    private int moveRenderStep = 0;
    private int[] moveRenderStepDelta = new int[]{0, 0};
    private int renderStepsInMove = 0;
    private boolean rendered = false;
    private int attackRenderStep = 0;
    private int[] attackRenderStepDelta = new int[]{0, 0};
    private int[][] moveGrid;

    public Unit(int id, String name, String type, String house, int team, int atk, int def, int range, int move, int rank) {
        this.id = id;
        baseAttack = atk;
        baseDefence = def;
        baseRange = range;
        baseMove = move;
        this.name = name;
        this.team = team;
        this.type = type;
        this.house = house;
        this.rank = rank;
        leader1 = ItemFactory.getNoLeader();
        leader2 = ItemFactory.getNoLeader();
        fullMorale();
        rename();
        newTurn();

        try {
            selectyellow = ImageIO.read(getClass().getResource("tiles/select-yellow.png").openStream());
            dead = ImageIO.read(getClass().getResource("tiles/dead.png").openStream());
            rank1 = ImageIO.read(getClass().getResource("tiles/rank1.png").openStream());
            rank2 = ImageIO.read(getClass().getResource("tiles/rank2.png").openStream());
            rank3 = ImageIO.read(getClass().getResource("tiles/rank3.png").openStream());
            icon = ImageIO.read(getClass().getResource("tiles/" + name + ".png").openStream());
            playerControlStar = ImageIO.read(getClass().getResource("tiles/player-control-star.png").openStream());
            allyControlStar = ImageIO.read(getClass().getResource("tiles/ally-control-star.png").openStream());
            enemyControlStar = ImageIO.read(getClass().getResource("tiles/enemy-control-star.png").openStream());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Unable to read graphics files.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean setLeader1(Leader leader) {
        if (leader.getType().equals(type)) {
            leader1 = leader;
            fullMorale();
            rename();
            return true;
        } else {
            return false;
        }
    }

    public boolean setLeader2(Leader leader) {
        if (leader.getType().equals(type)) {
            leader2 = leader;
            fullMorale();
            rename();
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return the baseAttack
     */
    public int getAttack() {
        return baseAttack + leader1.getAttackBonus() + leader2.getAttackBonus() + getRank();
    }

    /**
     * @return the baseDefence
     */
    public int getDefence() {
        double def = baseDefence + leader1.getDefenceBonus() + leader2.getDefenceBonus() + getRank();
        if (standingBy) {
            def = def * 1.25;
        }
        if (defending) {
            def = def * 1.25;
        }
        if (hasBeenAttacked) {
            def = def / 2;
        }
        return (int) Math.floor(def);
    }

    /**
     * @return the baseRange
     */
    public int getRange() {
        return baseRange + leader1.getRangeBonus() + leader2.getRangeBonus();
    }

    /**
     * @return the baseMove
     */
    public int getMove() {
        return baseMove + leader1.getMoveBonus() + leader2.getMoveBonus();
    }

    /**
     * @return the baseMaxMorale
     */
    public int getMaxMorale() {
        return baseMaxMorale + leader1.getMaxMoraleBonus() + leader2.getMaxMoraleBonus();
    }

    /**
     * @return the morale
     */
    public int getMorale() {
        return morale;
    }

    /**
     * @return the hasAdjacentAllies
     */
    public boolean hasAdjacentAllies() {
        return false;
    }

    /**
     * @return the hasAdjacentEnemies
     */
    public boolean hasAdjacentEnemies() {
        return false;
    }

    /**
     * @return the team
     */
    public int getTeam() {
        return team;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the house
     */
    public String getHouse() {
        return house;
    }

    private void rename() {
        if (leader1.getName().equals("None")) {
            fullName = house + " " + name;
        } else {
            fullName = leader1.getName() + "'s " + name;
        }
    }

    private void fullMorale() {
        morale = getMaxMorale();
    }

    /**
     * Just put the unit at a certain point, without all the move logic.  (For
     * initial setup of the battle.)
     * @param x X co-ord to put the unit at.
     * @param y Y co-ord to put the unit at.
     */
    public void placeAt(int x, int y) {
        xPos = x;
        yPos = y;
    }

    public void moveTo(BattlePanel panel, Stage stage, int x, int y, boolean animate) {
        int moveDist = calcMoveDistance(x, y);
        movesLeft = movesLeft - moveDist;
        if (movesLeft < 0) {
            movesLeft = 0;
        }
        
        /*int[] moveRenderSrc = new int[2];
        int[] moveRenderDest = new int[2];
        if (animate) {
        moveRenderStep = 1;

        moveRenderSrc = stage.tileToCoords(getX(), getY());
        moveRenderDest = stage.tileToCoords(x, y);
        renderStepsInMove = (int) Math.floor(20.0 * (float) moveDist / (float) getMove());
        if (renderStepsInMove == 0) {
        renderStepsInMove++;
        }
        moveRenderStepDelta = new int[]{((moveRenderDest[0] - moveRenderSrc[0]) / renderStepsInMove), ((moveRenderDest[1] - moveRenderSrc[1]) / renderStepsInMove)};
        }*/

        xPos = x;
        yPos = y;
        defending = false;

        // Ranged can't move & fire
        if (type.equals("Ranged")) {
            canAttack = false;
        }

        height = stage.getHeightAt(x, y);

        /*if (animate) {
        int[] repaintRegion = calculateRepaintRegion(moveRenderSrc, moveRenderDest);
        panel.scheduleUpdates(renderStepsInMove - 1, repaintRegion);
        }*/
    }

    public void refreshMoveGrid(Stage stage) {
        int[][] passabilityGrid = stage.getPassability();
        int[][] tmpMoveGrid = new int[stage.getXSize()][stage.getYSize()];
        for (int x = 0; x < tmpMoveGrid.length; x++) {
            for (int y = 0; y < tmpMoveGrid[0].length; y++) {
                tmpMoveGrid[x][y] = passabilityGrid[x][y];
            }
        }
        moveGrid = populateMoveGrid(tmpMoveGrid, 0, movesLeft);

        /*System.out.println(getFullName() + " " + getX() + " " + getY());
        for (int y = 0; y < moveGrid[0].length; y++) {
        for (int x = 0; x < moveGrid.length; x++) {
        System.out.print(moveGrid[x][y] + " ");
        }
        System.out.println();
        }*/
    }

    private int[][] populateMoveGrid(int[][] tmpMoveGrid, int number, int max) {
        if (number == 0) {
            tmpMoveGrid[getX()][getY()] = 0;
        } else {
            for (int x = 0; x < tmpMoveGrid.length; x++) {
                for (int y = 0; y < tmpMoveGrid[0].length; y++) {
                    if (tmpMoveGrid[x][y] == number - 1) {
                        int[][] checkSquares = new int[][]{new int[]{x - 1, y}, new int[]{x, y - 1}, new int[]{x + 1, y}, new int[]{x, y + 1}};
                        for (int i = 0; i < checkSquares.length; i++) {
                            try {
                                if (tmpMoveGrid[checkSquares[i][0]][checkSquares[i][1]] == -1) {
                                    tmpMoveGrid[checkSquares[i][0]][checkSquares[i][1]] = number;
                                }
                            } catch (ArrayIndexOutOfBoundsException ex) {
                                // Square is off the edge of the map, so ignore it.
                            }
                        }
                    }
                }
            }
        }
        if (number < max) {
            tmpMoveGrid = populateMoveGrid(tmpMoveGrid, number + 1, max);
        }
        return tmpMoveGrid;
    }

    public boolean canMove() {
        return (movesLeft > 0);
    }

    public boolean canAttack() {
        return canAttack;
    }

    public boolean canAct() {
        return (canMove() || canAttack());
    }

    public boolean isAlive() {
        return (morale > 0);
    }

    public void attack(BattlePanel panel, Stage stage, Unit target, boolean animate) {
        standingBy = false; // No "hasn't attacked" defense bonus
        defending = false; // No "hasn't done anything" defense bonus
        canAttack = false; // Can't attack twice
        // Ranged & Infantry can't hit & run (Cavalry can)
        if ((type.equals("Ranged")) || (type.equals("Infantry"))) {
            movesLeft = 0;
        }

        if (animate) {
            attackRenderStep = 1;

            int[] attackRenderSrc = stage.tileToCoords(getX(), getY());
            int[] attackRenderDest = stage.tileToCoords(target.getX(), target.getY());
            attackRenderStepDelta = new int[]{((attackRenderDest[0] - attackRenderSrc[0]) / 3), ((attackRenderDest[1] - attackRenderSrc[1]) / 3)};

            int[] repaintRegion = calculateRepaintRegion(attackRenderSrc, attackRenderDest);
            panel.scheduleUpdates(6, repaintRegion);
        }

        // Deal attack damage
        double damage = (getAttack() - target.getDefence()) * DAMAGE_MULTIPLIER;
        // Calc bonus based on height difference
        int heightBonus = Math.min(Math.max((getHeight() - target.getHeight()), 0), 10);
        damage += heightBonus;
        // If wouldn't damage at all, deal some minimal damage.
        if (damage < 0) {
            damage = DAMAGE_MULTIPLIER / 4;
        }
        target.damage((int) Math.floor(damage));
        // If attacker in range of defender, deal some damage back.
        if ((target.isWithinRange(this)) && (target.isAlive())) {
            this.damage(target.getDefence() * DAMAGE_MULTIPLIER / 2);
        }

        panel.checkForWinOrLose();
    }

    public int getX() {
        return xPos;
    }

    public int getY() {
        return yPos;
    }

    public void newTurn() {
        standingBy = true;
        defending = true;
        movesLeft = getMove();
        canAttack = true;
        hasBeenAttacked = false;
    }

    public void battleOver() {
        // Promote
        if (isAlive()) {
            rank++;
            if (getRank() > 3) {
                setRank(3);
            }
        }
    }

    public double calcRange(int otherX, int otherY) {
        int xOffset = Math.abs(xPos - otherX);
        int yOffset = Math.abs(yPos - otherY);
        return Math.sqrt(xOffset * xOffset + yOffset * yOffset);
    }

    public int calcMoveDistance(int otherX, int otherY) {
        int dist = moveGrid[otherX][otherY];
        if (dist < 0) {
            dist = 9999;
        }
        return dist;
    }

    public boolean isAdjacent(Unit unit) {
        return isAdjacent(unit.getX(), unit.getY());
    }

    public boolean isWithinRange(Unit unit) {
        return isWithinRange(unit.getX(), unit.getY());
    }

    public boolean isAdjacent(int x, int y) {
        return (calcRange(x, y) <= 1);
    }

    public boolean isWithinRange(int x, int y) {
        return (calcRange(x, y) <= getRange());
    }

    public boolean canMoveTo(int x, int y) {
        return ((calcMoveDistance(x, y) <= movesLeft) && (calcMoveDistance(x, y) > 0));
    }

    public boolean isOwnSquare(int x, int y) {
        return (calcMoveDistance(x, y) == 0);
    }

    public int[] calculateRepaintRegion(int[] startPos, int[] endPos) {
        int rpx = (int) Math.floor(Math.min(startPos[0], endPos[0]) - 50);
        int rpwidth = (int) Math.floor(Math.max(startPos[0], endPos[0]) + 50 - rpx);
        int rpy = (int) Math.floor(Math.min(startPos[1], endPos[1]) - 50);
        int rpheight = (int) Math.floor(Math.max(startPos[1], endPos[1]) + 50 - rpy);
        return new int[]{rpx, rpy, rpwidth, rpheight};
    }

    public void render(Graphics2D comp2D, Stage stage) {
        int[] tilePos = stage.tileToCoords(getX(), getY());

        if ((moveRenderStep > 0) && (moveRenderStep <= renderStepsInMove)) {
            tilePos[0] = tilePos[0] + (moveRenderStepDelta[0] * (moveRenderStep - renderStepsInMove));
            tilePos[1] = tilePos[1] + (moveRenderStepDelta[1] * (moveRenderStep - renderStepsInMove));
            moveRenderStep++;
        } else if (moveRenderStep > renderStepsInMove) {
            moveRenderStep = 0;
        }

        if ((attackRenderStep > 0) && (attackRenderStep <= 3)) {
            tilePos[0] = tilePos[0] + (attackRenderStepDelta[0] * (attackRenderStep));
            tilePos[1] = tilePos[1] + (attackRenderStepDelta[1] * (attackRenderStep));
            attackRenderStep++;
        } else if ((attackRenderStep > 3) && (attackRenderStep <= 6)) {
            tilePos[0] = tilePos[0] + (attackRenderStepDelta[0] * (7 - attackRenderStep));
            tilePos[1] = tilePos[1] + (attackRenderStepDelta[1] * (7 - attackRenderStep));
            attackRenderStep++;
        } else if (attackRenderStep > 6) {
            attackRenderStep = 0;
        }

        comp2D.drawImage(icon, tilePos[0] + 3, tilePos[1] - 20 - 3, null);

        switch (getRank()) {
            case 1:
                comp2D.drawImage(rank1, tilePos[0], tilePos[1] - 20 - 35, null);
                break;
            case 2:
                comp2D.drawImage(rank2, tilePos[0], tilePos[1] - 20 - 35, null);
                break;
            case 3:
                comp2D.drawImage(rank3, tilePos[0], tilePos[1] - 20 - 35, null);
                break;
        }

        switch (getTeam()) {
            case 0:
                comp2D.drawImage(playerControlStar, tilePos[0], tilePos[1] - 20 - 35, null);
                break;
            default:
                comp2D.drawImage(enemyControlStar, tilePos[0], tilePos[1] - 20 - 35, null);
                break;
        }

        renderHealthBar(comp2D, tilePos);

        if (!isAlive()) {
            comp2D.drawImage(dead, tilePos[0], tilePos[1] - 20 - 35, null);
        }

        rendered = true;
    }

    public void renderSquare(Graphics2D comp2D, Stage stage) {
        int[] tilePos = stage.tileToCoords(getX(), getY());

        if ((getTeam() == 0) && (canAct()) && (isAlive())) {
            comp2D.drawImage(selectyellow, tilePos[0], tilePos[1] - 20 - 35, null);
        }
    }

    private void renderHealthBar(Graphics2D comp2D, int[] tilePos) {
        int healthPercent = (int) Math.floor((double) getMorale() / (double) getMaxMorale() * 100);
        int barLength = healthPercent / 2;
        Color barColour = Color.green;
        if (healthPercent < 30) {
            barColour = Color.yellow;
        }
        if (healthPercent < 10) {
            barColour = Color.red;
        }

        comp2D.setColor(Color.black);
        comp2D.drawRect(tilePos[0] - 1, tilePos[1] + 29, 51, 4);
        comp2D.setColor(barColour);
        comp2D.fillRect(tilePos[0], tilePos[1] + 30, barLength, 3);
    }

    public void renderUnitInfo(BattlePanel panel, Graphics2D comp2D) {
        int[] tilePos;
        if (getTeam() == 0) {
            tilePos = new int[]{panel.getWidth() - 210, 10};
        } else {
            tilePos = new int[]{panel.getWidth() - 210, 220};
        }

        comp2D.setFont(new Font("Georgia", Font.PLAIN, 18));
        if (leader1.getName().equals("No Leader")) {
            comp2D.setColor(Color.lightGray);
        } else {
            comp2D.setColor(Color.black);
        }
        comp2D.drawString(leader1.getName(), tilePos[0] + 52, tilePos[1] + 43);
        if (leader1.getFace() != null) {
            comp2D.drawImage(leader1.getFace(), tilePos[0] + 1, tilePos[1] + 23, null);
        }
        if (leader2.getName().equals("No Leader")) {
            comp2D.setColor(Color.lightGray);
        } else {
            comp2D.setColor(Color.black);
        }
        comp2D.drawString(leader2.getName(), tilePos[0] + 52, tilePos[1] + 94);
        if (leader2.getFace() != null) {
            comp2D.drawImage(leader2.getFace(), tilePos[0] + 1, tilePos[1] + 74, null);
        }

        comp2D.setColor(Color.black);
        comp2D.drawString(name, tilePos[0] + 4, tilePos[1] + 145);
        comp2D.drawImage(icon, tilePos[0] + 149, tilePos[1] + 130, null);

        comp2D.setFont(new Font("Lucida Typewriter", Font.PLAIN, 12));
        comp2D.drawString(leader1.getSpecialDescription1(), tilePos[0] + 52, tilePos[1] + 59);
        comp2D.drawString(leader1.getSpecialDescription2(), tilePos[0] + 52, tilePos[1] + 74);
        comp2D.drawString(leader2.getSpecialDescription1(), tilePos[0] + 52, tilePos[1] + 109);
        comp2D.drawString(leader2.getSpecialDescription2(), tilePos[0] + 52, tilePos[1] + 125);


        comp2D.drawString("Rank: " + getRankName(), tilePos[0] + 4, tilePos[1] + 162);
        comp2D.drawString("Morale: " + getMorale() + " / " + getMaxMorale(), tilePos[0] + 4, tilePos[1] + 178);
        comp2D.drawString("Atk: " + getAttack(), tilePos[0] + 4, tilePos[1] + 194);
        comp2D.drawString("Def: " + getDefence(), tilePos[0] + 53, tilePos[1] + 194);
        comp2D.drawString("Rng: " + getRange(), tilePos[0] + 102, tilePos[1] + 194);
        comp2D.drawString("Mov: " + getMove(), tilePos[0] + 151, tilePos[1] + 194);

    }

    private void damage(int damage) {
        morale = getMorale() - damage;
        if (morale < 0) {
            morale = 0;
        }
        hasBeenAttacked = true;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * @return the rank
     */
    public String getRankName() {
        String rankname = "";
        switch (rank) {
            case 1:
                rankname = "Experienced";
                break;
            case 2:
                rankname = "Veteran";
                break;
            case 3:
                rankname = "Elite";
                break;
            default:
                rankname = "Fresh";
        }
        return rankname;
    }

    /**
     * @param rank the rank to set
     */
    public void setRank(int rank) {
        this.rank = rank;
    }

    /**
     * Acts according to its AI.  Run on all CPU-controlled units after the
     * end of the player's turn
     */
    public void aiAct(BattlePanel panel, Stage stage, ArrayList<Unit> units, int targetTeam) {
        if (isAlive()) {
            if (getType().equals("Ranged")) {
                Unit target = AI.findBestTarget(this, panel, stage, units, targetTeam);
                if (target != null) {
                    attack(panel, stage, target, true);
                } else {
                    int[] bestMove = AI.findBestMove(this, panel, stage, units, targetTeam, true);
                    moveTo(panel, stage, bestMove[0], bestMove[1], true);
                }
            } else {
                int[] bestMove = AI.findBestMove(this, panel, stage, units, targetTeam, false);
                moveTo(panel, stage, bestMove[0], bestMove[1], true);
                Unit target = AI.findBestTarget(this, panel, stage, units, targetTeam);
                if (target != null) {
                    attack(panel, stage, target, true);
                }
            }
        }
    }
}
