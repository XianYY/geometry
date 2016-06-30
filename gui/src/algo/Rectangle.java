package algo;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

/**
 * Created by chenxian on 16/6/29.
 */
public class Rectangle implements Intersectable {

    private final Point2D point;
    private final double width;
    private final double height;

    public Rectangle(Point2D point, double width, double height) {
        this.point = point;
        this.width = width;
        this.height = height;
    }

    @Override
    public List<Point2D> intersect(Intersectable other) {
        return null;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.strokeRect(point.getX(), point.getY(), width, height);
    }
}
