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
        this.totalMass = 0;
        this.totalCenterOfMassX = 0;
        this.totalCenterOfMassY = 0;
    }

    public void insert(Body newBody) {

        // Insert new leaf node
        if (this.body == null) {
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
            if (NE.contains(newBody)) {
                NE.insert(newBody);
            } 
            else if (SE.contains(newBody)) {
                SE.insert(newBody);
            }
            else if (NW.contains(newBody)) {
                NW.insert(newBody);
            }
            else if (SW.contains(newBody)) {
                SW.insert(newBody);
            }
        }
    }

    public void updateForce(Body newBody) {
        if (this.body != null || this.body.equals(newBody)) 
            return;
        if (isExternal()) {
            
            double dx = this.body.x - newBody.x;
            double dy = this.body.y - newBody.y;
            double distance = distance(dx, dy);
            double F = G * this.body.mass * newBody.mass / (distance * distance);
            newBody.addForce(F * dx / distance, F * dy / distance);
            
        } else if (radius / distance(newBody) < theta) {

            double dx = totalCenterOfMassX / totalMass - newBody.x;
            double dy = totalCenterOfMassY / totalMass - newBody.y;
            double distance = distance(dx, dy);
            double F = G * totalMass * newBody.mass / (distance * distance);
            newBody.addForce(F * dx / distance, F * dy / distance);

        } else {
            if (NW != null) {
                NW.updateForce(newBody);
            }
            if (SW != null) {
                SW.updateForce(newBody);
            }
            if (SE != null) {
                SE.updateForce(newBody);
            }
            if (NE != null) {
                NE.updateForce(newBody);
            }
        }
    }

    private boolean isExternal() {
        return NW == null && NE == null && SW == null && SE == null;
    }

    private boolean contains(Body body) {
        return body.x <= centerX + radius && body.x >= centerX - radius && body.y <= centerY + radius && body.y >= centerY - radius;
    }

    private double distance(Body body) {
        double dx = centerX - body.x;
        double dy = centerY - body.y;
        return distance(dx, dy);
    }
    private double distance(double dx, double dy) {
        return Math.sqrt((dx * dx) + (dy * dy));
    }

}
