package task4;

public class Body {

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

    public void addForce(double x, double y) {
        this.ax += x;
        this.ay += y;
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
