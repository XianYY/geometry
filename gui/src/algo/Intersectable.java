package algo;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenxian on 16/6/29.
 */
public interface Intersectable extends Drawable {
    List<Point2D> intersect(Intersectable other);

}
