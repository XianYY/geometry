package algo;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chenxian on 16/6/29.
 */
public class Segments implements Drawable {
    private List<Point2D> points = new ArrayList<Point2D>();
    private List<Segment> segments = new ArrayList<Segment>();

    public static Segments fromLine(List<Point2D> line) {
        Segments segs = new Segments();
        Point2D p1 = null;
        for (Point2D p : line) {
            segs.points.add(p);
            if (p1 != null) {
                segs.segments.add(new LineSeg(p1, p));
            }
            p1 = p;
        }
        return segs;
    }

    public void add(Segment segment) {
        if (!points.contains(segment.p1)) {
            points.add(segment.p1);
        }
        if (!points.contains(segment.p2)) {
            points.add(segment.p2);
        }
        segments.add(segment);
    }

    public Iterator<Segment> getIterator() {
        return segments.iterator();
    }

    @Override
    public void draw(GraphicsContext gc) {
        for (Segment segment : segments) {
            segment.draw(gc);
        }
    }

    public Point2D getLastPoint() {
        if (segments.isEmpty()) {
            return null;
        } else {
            return segments.get(segments.size()-1).p2;
        }
    }
}
