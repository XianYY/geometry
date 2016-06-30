package algo;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

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
        this.fromAngle = Util.getAngle(from.getX() - center.getX(), center.getY() - from.getY());
        double angle = Util.getAngle(to.getX() - center.getX(), center.getY() - to.getY());
        this.toAngle = angle < fromAngle ? angle + 360 : angle;
        System.out.println(String.format("From %f", fromAngle));
        System.out.println(String.format("To %f", toAngle));

    }



    @Override
    public List<Point2D> intersect(Intersectable other) {
        return null;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.strokeArc(center.getX() - radius, center.getY() - radius, 2*radius, 2*radius,
                fromAngle, toAngle-fromAngle, ArcType.OPEN);

    }
}
