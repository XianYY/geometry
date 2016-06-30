package algo;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

/**
* Created by chenxian on 16/6/29.
*/
public class Circle implements Intersectable {
    Point2D center;
    double radius;
    public Circle(Point2D center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    @Override
    public List<Point2D> intersect(Intersectable other) {
        List<Point2D> points = new ArrayList<Point2D>();
        if (other instanceof Line) {
            return ((Line) other).intersect(this);
        } else if (other instanceof algo.Circle) {
            algo.Circle circle = (algo.Circle) other;
            assert circle.radius == radius;
            double dd = Math.pow(center.getX() - circle.center.getX(), 2) + Math.pow(center.getY() - circle.center.getY(), 2);
            if (Util.isZero(dd - Math.pow(radius + circle.radius, 2))) {
                points.add(new Point2D(0.5 * (center.getX() + circle.center.getX()), 0.5 * (center.getY() + circle.center.getY())));
            } else if (dd < Math.pow(radius + circle.radius, 2) + 1E-6) {
                double x1 = center.getX();
                double y1 = center.getY();
                double x2 = circle.center.getX();
                double y2 = circle.center.getY();
                double r = radius;
                if (Util.isZero(y2 - y1)) {
                    double yd = Math.sqrt(r * r - 0.25 * dd);
                    points.add(new Point2D(0.5 * (x1 + x2), y1 - yd));
                    points.add(new Point2D(0.5 * (x1 + x2), y1 + yd));
                } else if (Util.isZero(x2 - x1)) {
                    double xd = Math.sqrt(r * r - 0.25 * dd);
                    points.add(new Point2D(x1 - xd, 0.5 * (y1 + y2)));
                    points.add(new Point2D(x1 + xd, 0.5 * (y1 + y2)));
                } else {
                    double k = (x1 - x2) / (y2 - y1);
                    double lx = 0.5 * (x1 + x2);
                    double ly = 0.5 * (y1 + y2);
                    Line line = new Line(new Point2D(lx, ly), new Point2D(0, ly - k * lx));
                    points.addAll(line.intersect(this));
                }
            }
        } else if (other instanceof LineSeg) {
            return ((LineSeg) other).intersect(this);
        } else {
            throw new UnsupportedOperationException();
        }
        return points;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.save();
        gc.strokeOval(center.getX() - radius, center.getY() - radius, 2*radius, 2*radius);
        gc.restore();
    }
}
