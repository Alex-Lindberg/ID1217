package task4;

import util.Body;
import util.Quad;

public class BHTree {

    /*
     * Threshold value for center-mass calc.
     * Ratio between Quad_side_length / distance_to_center_mass
     */
    private static final double THETA = 1.5;

    private Body body; // body or aggregate body stored in this node
    private Quad quad; // square region that the tree represents

    private BHTree NW; // northwest quadrant sub-tree
    private BHTree NE; // northeast quadrant sub-tree
    private BHTree SW; // southwest quadrant sub-tree
    private BHTree SE; // southeast quadrant sub-tree

    public BHTree(Quad q) {
        this.quad = q;
    }

    /**
     * Returns true iff this tree node is external.
     */
    private boolean checkExternal() {
        // a node is external iff all four children are null
        return (NW == null && NE == null && SW == null && SE == null);
    }

    /**
     * Adds the Body p to the invoking Barnes-Hut tree.
     */
    public void insert(Body nb) {
        if (body == null) {
            this.body = nb;
        }
        // Internal node
        else if (!checkExternal()) {
            // moveBody the center-of-mass and total mass
            this.body = body.aggregateBodies(nb);
            putInQuad(nb);
        }
        // External node
        else {
            NW = new BHTree(quad.NW());
            NE = new BHTree(quad.NE());
            SE = new BHTree(quad.SE());
            SW = new BHTree(quad.SW());

            putInQuad(this.body);
            putInQuad(nb);

            body = body.aggregateBodies(nb);
        }
    }

    /* Inserts a body into the appropriate quadrant. */
    private void putInQuad(Body body) {
        if (body.in(quad.NW()))
            NW.insert(body);
        else if (body.in(quad.NE()))
            NE.insert(body);
        else if (body.in(quad.SW()))
            SW.insert(body);
        else if (body.in(quad.SE()))
            SE.insert(body);
    }

    /**
     * Approximates the net force acting on Body p from all bodies
     * in the invoking Barnes-Hut tree, and updates b's force accordingly.
     */
    public void updateForce(Body nb) {
        if (body == null || nb.equals(body))
            return;
        if (checkExternal() && this.body != nb){
            nb.calculateForce(body);
            return;
        }
        // internal node
        // distance between Body p and this node's center-of-mass
        double distance = body.dist(nb);

        // compare ratio (quad_side / d) to threshold value Theta
        if ((quad.length() / distance) < THETA)
            nb.calculateForce(body);
        else {
            NW.updateForce(nb);
            NE.updateForce(nb);
            SW.updateForce(nb);
            SE.updateForce(nb);
        }   
    }
}