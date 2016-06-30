package algo;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import java.util.Iterator;
import java.util.List;

/**
 * Created by chenxian on 16/6/29.
 */
public class LineDeviator1 {

    public interface Debuger {
        void debug(Point2D point);
        void debug(Drawable drawable);
    }

    private Debuger debuger = new Debuger() {
        @Override
        public void debug(Point2D point) {

        }

        @Override
        public void debug(Drawable drawable) {

        }
    };

    public void setDebuger(Debuger debuger) {
        this.debuger = debuger;
    }

    public Segments offset(Segments segs, double offset) {
        Segments newSegs = new Segments();
        Iterator<Segment> iter = segs.getIterator();
        LineSeg last = null;
        while (iter.hasNext()) {
            Segment seg = iter.next();
            if (seg instanceof LineSeg) {
                LineSeg current = (LineSeg) seg;
                LineSeg currentOffset = current.offset(offset);
                if (last == null) {
                    Circle circle = new Circle(current.p1, offset);
                    List<Point2D> intersects = circle.intersect(current);
                    assert intersects.size() == 1;
                    Point2D startPoint = Util.dotSymmetry(intersects.get(0), current.p1);
                    debuger.debug(startPoint);
                    newSegs.add(new Arc(current.p1, offset, startPoint, currentOffset.p1));
                    newSegs.add(currentOffset);
                } else if (Util.isFlat(last, current)) {
                    //skip
                } else if (Util.isConvexAngle(last, current)) {
                    newSegs.add(new Arc(current.p1, offset, newSegs.getLastPoint(), currentOffset.p1));
                    newSegs.add(currentOffset);
                } else {
                    throw new UnsupportedOperationException("Don't support concave angle");
                }
                last = current;
            } else {
                throw new IllegalArgumentException("The input should be all line segments");
            }
        }
        return newSegs;
    }


}
