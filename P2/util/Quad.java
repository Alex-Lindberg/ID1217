/**
 * https://www.cs.princeton.edu/courses/archive/fall03/cs126/assignments/barnes-hut.html
 * 
 */
package util;

import util.Util.Point;

public class Quad {

    private Point center;
    private double length;
    private double distanceToEdge;
    private double distanceToSubQuadCenter;

    public Quad(Point p, double length) {
        this.center = new Point(p.x, p.y);
        this.distanceToEdge = this.length / 2.0;
        this.distanceToSubQuadCenter = this.length / 4.0;
    }

    public double length() {
        return length;
    }

    public boolean contains(double x, double y) {
        return (x <= this.center.x + this.distanceToEdge &&
                x >= this.center.x - this.distanceToEdge &&
                y <= this.center.y + this.distanceToEdge &&
                y >= this.center.y - this.distanceToEdge);
    }

    /* North west quadrant of the current quad */
    public Quad NW() {
        Point p = new Point(
                this.center.x - this.distanceToSubQuadCenter,
                this.center.y + this.distanceToSubQuadCenter);
        return new Quad(p, this.distanceToEdge);
    }

    /* North east quadrant of the current quad */
    public Quad NE() {
        Point p = new Point(
                this.center.x + this.distanceToSubQuadCenter,
                this.center.y + this.distanceToSubQuadCenter);
        return new Quad(p, this.distanceToEdge);
    }

    /* South west quadrant of the current quad */
    public Quad SW() {
        Point p = new Point(
                this.center.x - this.distanceToSubQuadCenter,
                this.center.y - this.distanceToSubQuadCenter);
        return new Quad(p, this.distanceToEdge);
    }

    /* South east quadrant of the current quad */
    public Quad SE() {
        Point p = new Point(
                this.center.x + this.distanceToSubQuadCenter,
                this.center.y - this.distanceToSubQuadCenter);
        return new Quad(p, this.distanceToEdge);
    }
}
