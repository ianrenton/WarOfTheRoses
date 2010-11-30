package waroftheroses.items;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 *
 * @author irenton
 */
public class Leader {
    private String name;
    private String house;
    private String type;
    private int attackBonus;
    private int defenceBonus;
    private int rangeBonus;
    private int moveBonus;
    private int maxMoraleBonus;
    private String specialDescription1;
    private String specialDescription2;
    private BufferedImage face;

    public Leader(String name, String house, String type, int atk, int def, int range, int move, int maxMorale, String specialDescription) {
        this.name = name;
        this.type = type;
        this.house = house;
        attackBonus = atk;
        defenceBonus = def;
        rangeBonus = range;
        moveBonus = move;
        maxMoraleBonus = maxMorale;
        String[] specialLines = specialDescription.split("_");
            this.specialDescription1 = specialLines[0];
        if (specialLines.length > 1) {
            this.specialDescription2 = specialLines[1];
        } else {
            this.specialDescription2 = "";
        }

        try {
            URL url = getClass().getResource("faces/" + name + ".png");
            if (url != null) face = ImageIO.read(url.openStream());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Unable to read graphics files.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * @return the attackBonus
     */
    public int getAttackBonus() {
        return attackBonus;
    }

    /**
     * @return the defenceBonus
     */
    public int getDefenceBonus() {
        return defenceBonus;
    }

    /**
     * @return the rangeBonus
     */
    public int getRangeBonus() {
        return rangeBonus;
    }

    /**
     * @return the moveBonus
     */
    public int getMoveBonus() {
        return moveBonus;
    }

    /**
     * @return the maxMoraleBonus
     */
    public int getMaxMoraleBonus() {
        return maxMoraleBonus;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param moveBonus the moveBonus to set
     */
    public void setMoveBonus(int moveBonus) {
        this.moveBonus = moveBonus;
    }

    /**
     * @return the face
     */
    public BufferedImage getFace() {
        return face;
    }

    /**
     * @return the specialDescription1
     */
    public String getSpecialDescription1() {
        return specialDescription1;
    }

    /**
     * @return the specialDescription2
     */
    public String getSpecialDescription2() {
        return specialDescription2;
    }
}
