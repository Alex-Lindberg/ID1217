package task4;

public class BarnesHutTree {

    public final double G = Constants.G;
    public final double SOFTENING = Constants.SOFTENING;
    public final double theta;

    public Body body;
    public double centerX, centerY; // center of mass
    public double width, radius;

    public BarnesHutTree NE, NW, SE, SW;
    public double totalMass;
    public double totalCenterOfMassX;
    public double totalCenterOfMassY;

    public BarnesHutTree(double centerX, double centerY, double width, double theta) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.theta = theta;
        this.radius = width / 2.0;
    }

    public void insert(Body newBody) {

        // New leaf node
        if (this.body == null && isExternal()) {
            this.body = newBody;
            return;
        }

        if (isExternal()) {
            // Create a new internal node and insert both bodies
            Body oldBody = body;
            body = null;
            NE = new BarnesHutTree(centerX + radius / 2, centerY + radius / 2, radius, theta);
            NW = new BarnesHutTree(centerX - radius / 2, centerY + radius / 2, radius, theta);
            SE = new BarnesHutTree(centerX + radius / 2, centerY - radius / 2, radius, theta);
            SW = new BarnesHutTree(centerX - radius / 2, centerY - radius / 2, radius, theta);
            insert(oldBody);
            insert(newBody);

        } else {
            // Update total mass and center of mass of this node
            totalMass += newBody.mass;
            totalCenterOfMassX += newBody.mass * newBody.x;
            totalCenterOfMassY += newBody.mass * newBody.y;

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

    public void updateForce(Body newBody) {
        if (this.body == null) {
            return;
        }
        if (isExternal()) {
            if (this.body.equals(newBody))
                return;
            double dx = this.body.x - newBody.x;
            double dy = this.body.y - newBody.y;
            double distance = distance(dx, dy);
            double F = G * this.body.mass * newBody.mass / (distance * distance);
            newBody.addForce(F * dx / distance, F * dy / distance);
        } else if (this.width / distance(this.body, newBody) < theta) {
            double dx = this.totalCenterOfMassX / this.totalMass - newBody.x;
            double dy = this.totalCenterOfMassY / this.totalMass - newBody.y;
            double distance = distance(dx, dy);
            double F = G * this.totalMass * newBody.mass / (distance * distance);
            newBody.addForce(F * dx / distance, F * dy / distance);
        } else {
            if (NW != null)
                NW.updateForce(newBody);
            if (SW != null)
                SW.updateForce(newBody);
            if (SE != null)
                SE.updateForce(newBody);
            if (NE != null)
                NE.updateForce(newBody);
        }
    }

    private boolean isExternal() {
        return NW == null && NE == null && SW == null && SE == null;
    }

    private double distance(Body bodyA, Body bodyB) {
        double dx = bodyA.x - bodyB.x;
        double dy = bodyA.y - bodyB.y;
        return distance(dx, dy);
    }

    private double distance(double dx, double dy) {
        return Math.sqrt((dx * dx) + (dy * dy));
    }

}
