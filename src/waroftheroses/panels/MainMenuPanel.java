package waroftheroses.panels;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import waroftheroses.app.WarOfTheRoses;

/**
 *
 * @author irenton
 */
public class MainMenuPanel extends InteractivePanel {
    private final WarOfTheRoses caller;

    public MainMenuPanel(WarOfTheRoses caller) {
        this.caller = caller;
    }

    @Override
    public void paintComponent(Graphics comp) {
        Graphics2D comp2D = (Graphics2D) comp;

        comp2D.setColor(new Color(153, 204, 255));
        comp2D.fillRect(0, 0, getWidth(), getHeight());

        comp2D.setColor(new Color(240, 240, 180));
        comp2D.fillRect(getWidth()/2 - 200, getHeight()/2, 400, 50);
        comp2D.setColor(Color.black);
        comp2D.drawRect(getWidth()/2 - 200, getHeight()/2, 400, 50);
        comp2D.setFont(new Font("Lucida Sans", Font.BOLD, 18));
        comp2D.drawString("New Game", getWidth()/2 - 40, getHeight()/2 + 29);


        comp2D.drawString("War of the Roses", getWidth()/2 - 70, getHeight()/4);
    }

    public void processMouseMove(java.awt.event.MouseEvent evt) {}

    public void processMouseClick(java.awt.event.MouseEvent evt) {
        int x = evt.getPoint().x;
        int y = evt.getPoint().y;
        if ((x < getWidth()/2 + 200) && (x > getWidth()/2 - 200) && (y < getHeight()/2 + 50) && (y > getHeight()/2)) {
            caller.switchpanels(new BattlePanel(caller));
        }
        this.repaint();
    }

}
