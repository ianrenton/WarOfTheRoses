package waroftheroses.items;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import javax.swing.JOptionPane;

/**
 *
 * @author irenton
 */
public class ItemFactory {

    private BufferedReader reader = null;
    private HashMap<String, String[]> unitDefs = new HashMap<String, String[]>();
    private HashMap<String, String[]> leaderDefs = new HashMap<String, String[]>();
    private int idCounter = 0;

    public ItemFactory() {
        try {
            String line;

            // Units
            reader = new BufferedReader(new InputStreamReader(getClass().getResource("data/units.csv").openStream()));
            reader.readLine(); // read header
            do {
                line = reader.readLine();
                if (line != null) {
                    String[] fields = line.split(",");
                    unitDefs.put(fields[0], fields);
                }
            } while (line != null);

            // Leaders
            reader = new BufferedReader(new InputStreamReader(getClass().getResource("data/leaders.csv").openStream()));
            reader.readLine(); // read header
            do {
                line = reader.readLine();
                if (line != null) {
                    String[] fields = line.split(",");
                    leaderDefs.put(fields[0], fields);
                }
            } while (line != null);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Unable to read unit definition files.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public Leader newLeader(String name) {
        String[] fields = leaderDefs.get(name);
        return new Leader(fields[0], fields[1], fields[2], Integer.parseInt(fields[3]), Integer.parseInt(fields[4]), Integer.parseInt(fields[5]), Integer.parseInt(fields[6]), Integer.parseInt(fields[7]), (fields.length > 8)?fields[8]:"No ability");
    }
    
    public Unit newUnit(String name, String house, int team) {
        String[] fields = unitDefs.get(name);
        return new Unit(idCounter++, fields[0], fields[1], house, team, Integer.parseInt(fields[2]), Integer.parseInt(fields[3]), Integer.parseInt(fields[4]), Integer.parseInt(fields[5]), 0);
    }

    public void resetIDCount() {
        idCounter = 0;
    }

    static Leader getNoLeader() {
        return new Leader("No Leader", null, null, 0, 0, 0, 0, 0, "");
    }
}
