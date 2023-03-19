package task3;

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
    private final double MASS_SCALING_SUN = Constants.MASS_SCALING_SUN;

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
            drawPoint(g2d, sim.bodies[0], MASS_SCALING_SUN, Color.ORANGE, false);
            for (int i = 1; i < sim.gnumBodies; i++) {
                drawPoint(g2d, sim.bodies[i], MASS_SCALING, Color.BLACK, true);
            }
        }

        private void drawPoint(Graphics2D g2d, Body b, double massScaling, Color c, boolean isFilled) {
            int x = (int) (b.x) + (screenSize.width / 2);
            int y = (int) (b.y) + (screenSize.height / 2);
            int r = Math.max((int) (Math.sqrt(b.mass) * massScaling),1);
            g2d.setColor(c);
            if(isFilled)
                g2d.fillOval(x - r, y - r, 2 * r, 2 * r);
            else
                g2d.drawOval(x - r, y - r, 2 * r, 2 * r);
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
            if (drawTotalMass) {
                if (tree.totalMass > 0) {
                    int x = (int) (tree.centerMassX) + (screenSize.width / 2);
                    int y = (int) (tree.centerMassY) + (screenSize.height / 2);
                    int r = (int) (Math.sqrt(tree.totalMass) / MASS_SCALING);

                    g2d.setColor(Color.BLUE);
                    g2d.drawOval(x - r, y - r, 2 * r, 2 * r);
                }
            }
            drawTree(g2d, tree.NE);
            drawTree(g2d, tree.NW);
            drawTree(g2d, tree.SE);
            drawTree(g2d, tree.SW);
        }

    }
}
