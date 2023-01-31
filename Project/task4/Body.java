package task4;

public class Body {

    private static final double G = 6.67e-3;
    private static final double DT = 0.1;
    private static final double SOFTENING = 1e5;

    double px;  // position
    double py;
    double vx;  // velocity
    double vy;
    double fx;  // force vector
    double fy;
    double mass;

    public Body(double px, double py, double vx, double vy, double fx, double fy, double mass) {
        this.px = px;
        this.py = py;
        this.vx = vx;
        this.vy = vy;
        this.fx = fx;
        this.fy = fy;
        this.mass = mass;
    }

    /* Check whether quad contains the body */
    public boolean in(Quad quad) {
        return quad.contains(px, py);
    }

    /* Update the force vector */
    public void addForce(Body body) {
        double dx = body.px - this.px;
        double dy = body.py - this.py;
        double distance = this.dist(body);
        double force = (G * this.mass * body.mass) / (distance * distance + SOFTENING);
        this.fx += force * dx / distance;
        this.fy += force * dy / distance;
    }

    /* Update or move the bodies */
    public void update() {
        vx += DT * fx / mass;
        vy += DT * fy / mass;
        px += DT * vx;
        py += DT * vy;
        
        fx = fy = 0.0;
    }

    /* Distance between this body and another */
    public double dist(Body body) {
        return Math.sqrt(Math.pow((this.px - body.px), 2) + Math.pow((this.py - body.py), 2));
    }

    /* Aggregate two bodies */
    public Body add(Body body) {

        double m = this.mass + body.mass;
        double x = (this.px * this.mass + body.px * body.mass) / m;
        double y = (this.py * this.mass + body.py * body.mass) / m;

        return new Body(x, y, this.vx, body.vy, 0, 0, m);
    }
}