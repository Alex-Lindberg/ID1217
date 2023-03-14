package util;

public class Body {

    private final double G = Constants.G;
    private final double SOFTENING = Constants.SOFTENING;
    private final double DT;

    public double x, y; // position
    public double vx, vy; // velocity
    public double ax, ay; // velocity
    public double mass;

    public Body(double x, double y, double vx, double vy, double mass, double dt) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.ax = 0;
        this.ay = 0;
        this.mass = mass;
        this.DT = dt;
    }

    public void addForce(double dx, double dy, double mass) {
        double dist = Math.sqrt(dx * dx + dy * dy);
        double F = (G * mass * mass) / (dist * dist + SOFTENING * SOFTENING);
        ax += F * dx / dist;
        ay += F * dy / dist;
    }

    public void addForce(Body b) {
        double dx = b.x - x;
        double dy = b.y - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        double F = (G * mass * b.mass) / (dist * dist + SOFTENING * SOFTENING);
        ax += F * dx / dist;
        ay += F * dy / dist;
    }

    public void move() {

        // Leap frog scheme
        double dv_x = (ax / mass) * DT;
        double dv_y = (ay / mass) * DT;

        double dx = (vx + dv_x / 2) * DT;
        double dy = (vy + dv_y / 2) * DT;

        vx += dv_x;
        vy += dv_y;
        x += dx;
        y += dy;
        ax = ay = 0.0;
    }
}
