package util;

import util.Util.Point;

public class Body {

    private static final double G = 6.67e-3;
    private static final double DT = 0.1;
    private static final double SOFTENING = 1e5;

    public Point p; // position
    public Point v; // velocity
    public Point f; // force vector
    public double mass;

    public Body(Point p, Point v, double mass) {
        this.p = p;
        this.v = v;
        this.f = new Point(0, 0);
        this.mass = mass;
    }

    /* Check whether quad contains the body */
    public boolean in(Quad quad) {
        return quad.contains(p.x, p.y);
    }

    /* Update the force vector */
    public void calculateForce(Body body) {
        double distance = this.dist(body);
        double magnitude = (G * this.mass * body.mass) / (Math.pow(distance, 2)); // + SOFTENING);
        double directionX = body.p.x - this.p.x;
        double directionY = body.p.y - this.p.y;
        this.f.x += magnitude * directionX / distance;
        this.f.y += magnitude * directionY / distance;
    }

    /* Update or move the bodies */
    public void moveBody() {

        // Leap frog scheme
        Point deltaV = new Point((f.x / mass) * DT,
                (f.y / mass) * DT);
        Point deltaP = new Point((v.x + deltaV.x / 2) * DT,
                (v.y + deltaV.y / 2) * DT);

        v.x += deltaV.x;
        v.y += deltaV.y;
        p.x += deltaP.x;
        p.y += deltaP.y;

        f.x = f.y = 0.0;
    }

    /* Distance between this body and another */
    public double dist(Body body) {
        return Math.sqrt(Math.pow((this.p.x - body.p.x), 2) + Math.pow((this.p.y - body.p.y), 2));
    }

    /* Aggregate two bodies */
    public Body aggregateBodies(Body body) {

        double combinedMass = this.mass + body.mass;
        double x = (this.p.x * this.mass + body.p.x * body.mass) / combinedMass;
        double y = (this.p.y * this.mass + body.p.y * body.mass) / combinedMass;

        return new Body(new Point(x, y), new Point(0, 0), combinedMass);
    }
}