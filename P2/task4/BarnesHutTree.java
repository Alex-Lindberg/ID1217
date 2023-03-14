package task4;

import util.Body;

public class BarnesHutTree {

    public final double theta;

    public Body body;
    public double centerX, centerY; // center of mass
    public double width, radius;

    public BarnesHutTree NE, NW, SE, SW;
    public double totalMass;
    public double centerMassX;
    public double centerMassY;
    private boolean isBranch; // used to skip inserting into internal nodes.

    public BarnesHutTree(double centerX, double centerY, double width, double theta) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.theta = theta;
        this.radius = width / 2.0;
        this.isBranch = false;
    }

    public void insert(Body newBody) {
        // New leaf node
        if (this.body == null && !isBranch) {
            this.body = newBody;
        } else if (isExternal()) {
            // Create a new internal node and insert both bodies
            Body oldBody = body;
            this.body = null;
            this.isBranch = true;

            this.NE = new BarnesHutTree(centerX + radius / 2, centerY + radius / 2, radius, theta);
            this.NW = new BarnesHutTree(centerX - radius / 2, centerY + radius / 2, radius, theta);
            this.SE = new BarnesHutTree(centerX + radius / 2, centerY - radius / 2, radius, theta);
            this.SW = new BarnesHutTree(centerX - radius / 2, centerY - radius / 2, radius, theta);
            insert(oldBody);
            insert(newBody);

        } else {
            // Update total mass and center of mass of this node
            totalMass += newBody.mass;
            centerMassX += newBody.mass * newBody.x;
            centerMassY += newBody.mass * newBody.y;

            // Insert the body into the appropriate quadrant
            if (newBody.x > centerX) {
                if (newBody.y > centerY) {
                    NE.insert(newBody);
                } else {
                    SE.insert(newBody);
                }
            } else {
                if (newBody.y > centerY) {
                    NW.insert(newBody);
                } else {
                    SW.insert(newBody);
                }
            }
        }
    }

    public void updateForce(Body currentBody) {
        if (isExternal()) {
            if (this.body == currentBody)
                return;
            currentBody.addForce(this.body);
        } else if (this.width / distanceToCenter(currentBody) < theta) {
            double dx = this.centerMassX - currentBody.x;
            double dy = this.centerMassY - currentBody.y;
            currentBody.addForce(dx, dy, this.totalMass);
        } else {
            if (NW != null)
                NW.updateForce(currentBody);
            if (SW != null)
                SW.updateForce(currentBody);
            if (SE != null)
                SE.updateForce(currentBody);
            if (NE != null)
                NE.updateForce(currentBody);
        }
    }

    private boolean isExternal() {
        return this.body != null && NW == null && NE == null && SW == null && SE == null;
    }

    private double distanceToCenter(Body body) {
        double dx = centerMassX - body.x;
        double dy = centerMassY - body.y;
        return Math.sqrt((dx * dx) + (dy * dy));
    }

}
