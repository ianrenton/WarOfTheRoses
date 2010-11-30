package waroftheroses.stages;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import waroftheroses.items.ItemFactory;
import waroftheroses.items.Unit;
import waroftheroses.panels.BattlePanel;

/**
 *
 * @author IRENTON
 */
public class Stage {

    BufferedImage grassTile;
    BufferedImage waterTile;
    BufferedImage stoneTile;
    BufferedImage tree1;
    BufferedImage selectred;
    BufferedImage selectblue;
    int xDim;
    int yDim;
    int[][] terrain;
    /** Array the size of terrain, representing whether units can move through
     * certain squares.  -1 = passable, -2 = impassable. */
    int[][] passability;
    int[][] height;
    int[][] features;
    private BufferedReader reader;
    private final String name;
    private final ArrayList<Unit> units = new ArrayList<Unit>();

    public Stage(String name, ItemFactory factory) {
        this.name = name;

        loadSprites();

        loadTerrain();

        loadUnits(factory);
    }

    private void loadSprites() {
        try {
            grassTile = ImageIO.read(getClass().getResource("tiles/grass.png").openStream());
            waterTile = ImageIO.read(getClass().getResource("tiles/water.png").openStream());
            stoneTile = ImageIO.read(getClass().getResource("tiles/stone.png").openStream());
            tree1 = ImageIO.read(getClass().getResource("tiles/tree1.png").openStream());
            selectred = ImageIO.read(getClass().getResource("tiles/select-red.png").openStream());
            selectblue = ImageIO.read(getClass().getResource("tiles/select-blue.png").openStream());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Unable to read graphics files.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTerrain() {
        try {
            String line;
            reader = new BufferedReader(new InputStreamReader(getClass().getResource("maps/" + name + ".csv").openStream()));
            line = reader.readLine(); // read dimensions
            String[] dims = line.split(",");
            xDim = Integer.parseInt(dims[0]);
            yDim = Integer.parseInt(dims[1]);

            terrain = new int[xDim][yDim];
            passability = new int[xDim][yDim];
            height = new int[xDim][yDim];
            features = new int[xDim][yDim];

            for (int j = 0; j < yDim; j++) {
                line = reader.readLine();
                if (line != null) {
                    String[] tiles = line.split(",");
                    for (int i = 0; i < tiles.length; i++) {
                        int val = Integer.parseInt(tiles[i]);
                        terrain[i][j] = val;
                        if (val >= 0) {
                            passability[i][j] = -1;
                        } else {
                            passability[i][j] = -2;
                        }
                    }
                }
            }

            reader.readLine(); // read spacer

            for (int j = 0; j < yDim; j++) {
                line = reader.readLine();
                if (line != null) {
                    String[] tiles = line.split(",");
                    for (int i = 0; i < tiles.length; i++) {
                        height[i][j] = Integer.parseInt(tiles[i]);
                    }
                }
            }

            reader.readLine(); // read spacer

            for (int j = 0; j < yDim; j++) {
                line = reader.readLine();
                if (line != null) {
                    String[] tiles = line.split(",");
                    for (int i = 0; i < tiles.length; i++) {
                        features[i][j] = Integer.parseInt(tiles[i]);
                    }
                }
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Unable to read map files.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadUnits(ItemFactory factory) {
        try {
            String line;

            // Units
            reader = new BufferedReader(new InputStreamReader(getClass().getResource("units/" + name + ".csv").openStream()));
            reader.readLine(); // read header
            do {
                line = reader.readLine();
                if (line != null) {
                    String[] fields = line.split(",");
                    Unit u = factory.newUnit(fields[0], fields[1], Integer.parseInt(fields[2]));
                    if (!fields[3].equals("")) {
                        u.setLeader1(factory.newLeader(fields[3]));
                    }
                    if (!fields[4].equals("")) {
                        u.setLeader1(factory.newLeader(fields[4]));
                    }
                    u.setRank(Integer.parseInt(fields[5]));
                    u.placeAt(Integer.parseInt(fields[6]), Integer.parseInt(fields[7]));
                    u.newTurn();
                    getUnits().add(u);
                }
            } while (line != null);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Unable to read unit setup files.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public int getTerrainAt(int x, int y) {
        return terrain[x][y];
    }

    public int getHeightAt(int x, int y) {
        return height[x][y];
    }

    public int getFeatureAt(int x, int y) {
        return features[x][y];
    }

    public int getXSize() {
        return xDim;
    }

    public int getYSize() {
        return yDim;
    }

    public void render(BattlePanel panel, Graphics2D comp2D, boolean moving, Unit selectedUnit) {
        for (int i = 0; i <
                getXSize(); i++) {
            for (int j = 0; j <
                    getYSize(); j++) {
                int tileXPos = i * 50 + 50;
                int tileYPos = (j * 40) - (getHeightAt(i, j) * 5) + 50;
                BufferedImage tile;

                switch (getTerrainAt(i, j)) {
                    case -1:
                        tile = waterTile;
                        break;
                    case 1:
                        tile = stoneTile;
                        break;
                    default:
                        tile = grassTile;
                }

                comp2D.drawImage(tile, tileXPos, tileYPos, null);

                if ((moving) && (selectedUnit.canMoveTo(i, j)) && (panel.positionOccupiedByTeam(i, j) == -1)) {
                    comp2D.drawImage(selectblue, tileXPos, tileYPos - 20, null);
                }

                if ((moving) && (selectedUnit.isWithinRange(i, j)) && (panel.positionOccupiedByTeam(i, j) > 0) && (selectedUnit.canAttack()) && (panel.getUnitAtPosition(i, j).isAlive())) {
                    comp2D.drawImage(selectred, tileXPos, tileYPos - 20, null);
                }

                if (getFeatureAt(i, j) != 0) {
                    switch (getFeatureAt(i, j)) {
                        case 1:
                            tile = tree1;
                    }

                    comp2D.drawImage(tile, tileXPos, tileYPos - 20, null);
                }

            }
        }
    }

    public int[] mouseCoordsToTile(int mouseX, int mouseY) {
        int xTile = (int) Math.floor((double) (mouseX - 50) / 50);
        int yTile = (int) Math.floor((double) (mouseY - 50 - 10) / 40);
        return new int[]{xTile, yTile};
    }

    public int[] tileToCoords(int x, int y) {
        int tileXPos = x * 50 + 50;
        int tileYPos = (y * 40) - (getHeightAt(x, y) * 5) + 50 + 35;
        return new int[]{tileXPos, tileYPos};
    }

    public boolean isPassable(int x, int y) {
        return (passability[x][y] >= -1);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the units
     */
    public ArrayList<Unit> getUnits() {
        return units;
    }

    public int[][] getPassability() {
        return passability;
    }
}
