/**
 * https://www.cs.princeton.edu/courses/archive/fall03/cs126/assignments/barnes-hut.html
 * 
 */
package task4;

public class Quad {
    
    private double cx;  // center x
    private double cy;  // center y
    private double length;   
 
    public Quad(double x, double y, double length) {
        this.cx = x;
        this.cy = y;
        this.length = length;
    }

    public double length() {
        return length;
    }

    public boolean contains(double x, double y) {
        double middle = this.length / 2.0;
        return (x <= this.cx + middle && 
                x >= this.cx - middle &&
                y <= this.cy + middle && 
                y >= this.cy - middle);
    }

    /* North west quadrant of the current quad */
    public Quad NW() {
        double x = this.cx - this.length / 4.0;
        double y = this.cy + this.length / 4.0;
        return new Quad(x, y, this.length / 2.0);
    }

    /* North east quadrant of the current quad */
    public Quad NE() {
        double x = this.cx + this.length / 4.0;
        double y = this.cy + this.length / 4.0;
        return new Quad(x, y, this.length / 2.0);
    }

    /* South west quadrant of the current quad */
    public Quad SW() {
        double x = this.cx - this.length / 4.0;
        double y = this.cy - this.length / 4.0;
        return new Quad(x, y, this.length / 2.0);
    }

    /* South east quadrant of the current quad */
    public Quad SE() {
        double x = this.cx + this.length / 4.0;
        double y = this.cy - this.length / 4.0;
        return new Quad(x, y, this.length / 2.0);
    }
}
