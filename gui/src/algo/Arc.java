package algo;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenxian on 16/6/29.
 */
public class Arc extends Segment implements Intersectable {

    private final Point2D center;
    private final double radius;
    private final double fromAngle;
    private final double toAngle;


    public Arc(Point2D center, double radius, Point2D from, Point2D to) {
        super(from, to);
        this.center = center;
        this.radius = radius;
        this.fromAngle = Util.getAngle(from.getX() - center.getX(), from.getY() - center.getY());
        double angle = Util.getAngle(to.getX() - center.getX(), to.getY() - center.getY());
        this.toAngle = angle < fromAngle ? angle + 360 : angle;
        System.out.println(String.format("From %f", fromAngle));
        System.out.println(String.format("To %f", toAngle));

    }

    public Circle toCircle() {
        return new Circle(center, radius);
    }

    @Override
    public List<Point2D> intersect(Intersectable other) {
        if (other instanceof Arc) {
            Arc arc = (Arc) other;
            List<Point2D> inters = new ArrayList<Point2D>();
            for (Point2D p : arc.toCircle().intersect(this.toCircle())) {
                if (contains(p) && arc.contains(p)) {
                    inters.add(p);
                }
            }
            return inters;
        } else if (other instanceof Circle) {
            return ((Circle) other).intersect(this);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.strokeArc(center.getX() - radius, center.getY() - radius, 2*radius, 2*radius,
                fromAngle, toAngle-fromAngle, ArcType.OPEN);

    }

    @Override
    public Segment cutOffTail(Point2D cutPoint) {
        return new Arc(center, radius, p1, cutPoint);
    }

    @Override
    public boolean contains(Point2D point) {
        if (!Util.doubleEquals(radius, Util.distance(point, center))) {
            return false;
        }
        double angle = Util.getAngle(point.getX() - center.getX(), point.getY() - center.getY());
        return angle >= fromAngle - 1E-6 && angle <= toAngle + 1E-6;
    }
}
