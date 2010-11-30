package waroftheroses.panels;

import javax.swing.JPanel;

/**
 *
 * @author IRENTON
 */
public abstract class InteractivePanel extends JPanel {
    public abstract void processMouseMove(java.awt.event.MouseEvent evt);

    public abstract void processMouseClick(java.awt.event.MouseEvent evt);
}
