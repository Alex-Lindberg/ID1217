package task4;

import java.awt.*;
import javax.swing.*;


import util.Body;
import util.Constants;

public class BarnesHutSimulationGUI extends JFrame {

    private BarnesHutSimulation sim;
    private SimulationPanel simPanel;
    private Dimension screenSize;

    // private final double POS_SCALING = Constants.POS_SCALING;
    private final double MASS_SCALING = Constants.MASS_SCALING;

    private final boolean drawQuads, drawTotalMass;

    public BarnesHutSimulationGUI(BarnesHutSimulation sim, boolean drawQuads, boolean drawTotalMass) {
        this.sim = sim;
        this.drawQuads = drawQuads;
        this.drawTotalMass = drawTotalMass;

        simPanel = new SimulationPanel();
        getContentPane().add(simPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize);
        setVisible(true);
    }

    private class SimulationPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;

            if (drawQuads || drawTotalMass)
                drawTree(g2d, sim.tree);

            // Draw each body as a small circle
            for (Body body : sim.bodies) {
                int x = (int) (body.x) + (screenSize.width / 2);
                int y = (int) (body.y) + (screenSize.height / 2);
                int r = (int) (Math.sqrt(body.mass) * MASS_SCALING);

                g2d.setColor(Color.BLACK);
                g2d.drawOval(x - r, y - r, 2 * r, 2 * r);

            }
        }

        private void drawTree(Graphics2D g2d, BarnesHutTree tree) {
            if (tree == null) {
                return;
            }

            if (drawQuads) {
                int x = (int) (tree.centerX - (tree.width / 2)) + (screenSize.width / 2);
                int y = (int) (tree.centerY - (tree.width / 2)) + (screenSize.height / 2);
                int r = (int) (tree.width / 2);

                g2d.setColor(Color.RED);
                g2d.drawRect(x, y, 2 * r, 2 * r);
            }
            drawTree(g2d, tree.NE);
            drawTree(g2d, tree.NW);
            drawTree(g2d, tree.SE);
            drawTree(g2d, tree.SW);
        }

    }
}
