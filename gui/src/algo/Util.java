package algo;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenxian on 16/6/29.
 */
public class Util {
    public static boolean isZero(double value) {
        return Math.abs(value) < 1E-6;
    }

    public static boolean doubleEquals(double value1, double value2) {
        return isZero(value1 - value2);
    }

    public static Point2D dotSymmetry(Point2D point, Point2D dot) {
        assert point != null && dot != null;
        return new Point2D(2*dot.getX()-point.getX(), 2*dot.getY()-point.getY());
    }


    public static boolean isFlat(LineSeg seg1, LineSeg seg2) {
        if (Double.isInfinite(seg1.getSlope()) && Double.isInfinite(seg2.getSlope())) {
            if ((seg1.p2.getY() - seg1.p1.getY()) * (seg2.p2.getY() - seg2.p1.getY()) > 0) {
                return true;
            } else {
                throw new IllegalArgumentException("Overlap line");
            }
        } else {
            return doubleEquals(seg1.getSlope(), seg2.getSlope());
        }
    }

    public static boolean isConvexAngle(LineSeg seg1, LineSeg seg2) {
        assert seg1.p2 == seg2.p1;
        if (isFlat(seg1, seg2)) {
            return false;
        } else {
            return isConvexAngle(seg1.p1, seg1.p2, seg2.p2);
        }
    }

    public static boolean isConvexAngle(Point2D p1, Point2D p2, Point2D p3) {
        double area = p1.getX()*p2.getY() - p1.getY()*p2.getX()
                + p2.getX()*p3.getY() - p2.getY()*p3.getX()
                + p3.getX()*p1.getY() - p3.getY()*p1.getX();
        return area < 0;
    }

    public static double getAngle(double x, double y) {
        System.out.println(String.format("XY  %f, %f", x, y));
        y = -y;
        if (isZero(x)) {
            return y > 0 ? 90 : 270;
        } else {
            double degree = Math.toDegrees(Math.atan(y/x));
            if (x > 0 && y > 0) {
                return degree;
            } else if (x < 0 && y > 0) {
                return 180 + degree;
            } else if (x < 0 && y < 0) {
                return 180 + degree;
            } else if (x > 0 && y < 0) {
                return 360 + degree;
            } else {
                return degree;
            }
        }
    }

    public static double distance(Point2D p1, Point2D p2) {
        return Math.sqrt(Math.pow(p2.getX() - p1.getX(), 2) + Math.pow(p2.getY() - p1.getY(), 2));
    }

    public static Point2D findFarest(Point2D point, List<Point2D> points) {
        double maxD = 0;
        Point2D farest = null;
        for (Point2D p : points) {
            if (farest == null) {
                farest = p;
            }
            double d = distance(p, point);
            if (d > maxD) {
                farest = p;
                maxD = d;
            }
        }
        return farest;
    }
}
