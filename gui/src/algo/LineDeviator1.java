package algo;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chenxian on 16/6/29.
 */
public class LineDeviator1 {

    public interface Debuger {
        void debug(Point2D point);
        void debug(Drawable drawable);
        void debugLine(Point2D p1, Point2D p2);
        void debugCircle(Point2D center, double radius);
    }

    private Debuger debuger = new Debuger() {
        @Override
        public void debug(Point2D point) {

        }

        @Override
        public void debug(Drawable drawable) {

        }

        @Override
        public void debugLine(Point2D p1, Point2D p2) {

        }

        @Override
        public void debugCircle(Point2D center, double radius) {

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
                    List<Point2D> intersects = circle.intersect(current.toLine());
                    assert intersects.size() == 2;
                    Point2D startPoint = Util.findFarest(current.p2, intersects);
                    debuger.debug(startPoint);
                    newSegs.add(new Arc(current.p1, offset, startPoint, currentOffset.p1));
                    newSegs.add(currentOffset);
                } else if (Util.isFlat(last, current)) {
                    //skip
                } else if (Util.isConvexAngle(last, current)) {
                    newSegs.add(new Arc(current.p1, offset, newSegs.getLastPoint(), currentOffset.p1));
                    newSegs.add(currentOffset);
                } else {
                    rollInAngle(last, current, offset, newSegs);
                }
                last = current;
            } else {

                throw new IllegalArgumentException("The input should be all line segments");
            }
        }
        if (last != null) {
            Circle circle = new Circle(last.p2, offset);
            List<Point2D> inters = circle.intersect(last.toLine());
            Point2D point = Util.findFarest(last.p1, inters);
            newSegs.add(new Arc(last.p2, offset, newSegs.getLastPoint(), point));
        }
        return newSegs;
    }


    private void rollInAngle(LineSeg seg1, LineSeg seg2, double offset, Segments segments) {
        Line line1 = seg1.toLine();
        Line line2 = seg2.toLine();

        // in/in
        Point2D incircleCenter = findInCircleCenter(seg1, seg2, offset);
        Point2D perPoint1 = line1.findPerpendicularPoint(incircleCenter);
        Point2D perPoint2 = line2.findPerpendicularPoint(incircleCenter);
        if (seg1.contains(perPoint1) && seg2.contains(perPoint2)) {
            debuger.debugLine(perPoint1, incircleCenter);
            debuger.debugLine(perPoint2, incircleCenter);
            Segment lastSeg = segments.popLast();
            assert lastSeg != null;
            segments.add(new LineSeg(lastSeg.p1, incircleCenter));
            segments.add(new LineSeg(incircleCenter, seg2.offset(offset).p2));
            return;
        }

        // out/out
        {
            Circle c1 = new Circle(seg1.p1, offset);
            Circle c2 = new Circle(seg2.p2, offset);
            List<Point2D> intersects = c1.intersect(c2);
            for (Point2D inter : intersects) {
                if (validOutCircleCenter(inter, offset, seg1, seg2)) {
                    debuger.debugCircle(c1.center, offset);
                    debuger.debugCircle(c2.center, offset);
                    segments.popLast();
                    rollBack(c2, segments);
                    return;
//                    return inter;
                }
            }
        }

        // in/out
        {
            Line line = seg1.offset(offset).toLine();
            Circle c2 = new Circle(seg2.p2, offset);
            List<Point2D> intersects = line.intersect(c2);
            for (Point2D inter : intersects) {
//                gc.strokeOval(p3.getX() - offset, p3.getY() - offset, 2*offset, 2*offset);
//                gc.strokeLine(line.p1.getX(), line.p1.getY(), line.p2.getX(), line.p2.getY());
//                gc.strokeOval(inter.getX() - offset, inter.getY() - offset, 2*offset, 2*offset);
                if (validOutCircleCenter(inter, offset, seg1, seg2)) {
                    debuger.debugCircle(c2.center, offset);
                    debuger.debug(line);
                    return;
//                    return inter;
                }
            }
        }

        // out/in
        {
            Circle c1 = new Circle(seg1.p1, offset);
            Line line = seg2.offset(offset).toLine();
            List<Point2D> intersects = line.intersect(c1);
            for (Point2D inter : intersects) {
                if (validOutCircleCenter(inter, offset, seg1, seg2)) {
                    debuger.debugCircle(c1.center, offset);
                    debuger.debug(line);
                    return;
//                    return inter;
                }
            }
        }

        throw new UnsupportedOperationException("No intersect point");
    }

    private boolean validOutCircleCenter(Point2D center, double radius, LineSeg seg1, LineSeg seg2) {
        Circle outCircle = new Circle(center, radius);
        List<Point2D> intersects1 = outCircle.intersect(seg1);
        List<Point2D> intersects2 = outCircle.intersect(seg2);
        return intersects1.size() == 1 && intersects2.size() == 1;
    }

    private Point2D findInCircleCenter(LineSeg seg1, LineSeg seg2, double radius) {
        Line line1 = seg1.offset(radius).toLine();
        Line line2 = seg2.offset(radius).toLine();
        List<Point2D> intersects = line1.intersect(line2);
        assert intersects.size() == 1;
        return intersects.get(0);
    }

    private void rollBack(Circle circle, Segments segments) {

        List<Point2D> inters = new ArrayList<Point2D>();
        Segment last = null;
        while (inters.isEmpty()) {
            last = segments.popLast();
            if (last == null) {
                throw new IllegalArgumentException("Cannot offset");
            }
            inters = circle.intersect(last);
        }
        assert inters.size() == 1; //roll back if more interaction
        assert last != null;
        debuger.debug(inters.get(0));
        segments.add(last.cutOffTail(inters.get(0)));
    }
}
