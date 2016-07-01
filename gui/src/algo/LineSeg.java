package algo;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import sample.Controller;

import java.util.ArrayList;
import java.util.List;

/**
* Created by chenxian on 16/6/29.
*/
public class LineSeg extends Segment implements Intersectable {

    private final double slope;


    public LineSeg(Point2D p1, Point2D p2) {
        super(p1, p2);
        if (Util.doubleEquals(p1.getX(), p2.getX())) {
            slope = Double.MAX_VALUE;
        } else {
            slope = (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
        }
    }

    @Override
    public Segment cutOffTail(Point2D cutPoint) {
        return new LineSeg(p1, cutPoint);
    }

    public Line toLine() {
        return new Line(p1, p2);
    }

    @Override
    public boolean contains(Point2D point) {
        double t = 0;
        if (toLine().isPerpendicular()) {
            t = (point.getX() - p1.getX()) / (p2.getX() - p1.getX());
        } else {
            t = (point.getY() - p1.getY()) / (p2.getY() - p1.getY());
        }
        return t >= -1E-6 && t <= 1 + 1E-6;
    }

    public double getSlope() {
        return slope;
    }

    public LineSeg offset(double offset) {
        double d = Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
        double dx = offset * (p1.getY() - p2.getY()) / d;
        double dy = offset * (p2.getX() - p1.getX()) / d;
        Point2D np1 = new Point2D(p1.getX() + dx, p1.getY() + dy);
        Point2D np2 = new Point2D(p2.getX() + dx, p2.getY() + dy);
        return new LineSeg(np1, np2);
    }

    @Override
    public List<Point2D> intersect(Intersectable other) {
        if (other instanceof Circle) {
            Line line = new Line(p1, p2);
            List<Point2D> intersects = line.intersect(other);
            List<Point2D> validIntersects = new ArrayList<Point2D>();
            for (Point2D p : intersects) {
                if (contains(p)) {
                    validIntersects.add(p);
                }
            }
            return validIntersects;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.save();
        gc.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        gc.restore();
    }
}
