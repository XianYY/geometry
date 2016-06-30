package algo;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import sample.Controller;

import java.util.ArrayList;
import java.util.List;

/**
* Created by chenxian on 16/6/29.
*/
public class Line implements Intersectable {
    // ax + by + c = 0 (b = 0 or 1)
    private Point2D p1;
    private Point2D p2;

    public Line(Point2D p1, Point2D p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public Line(List<Point2D> points) {
        this.p1 = points.get(0);
        this.p2 = points.get(1);
    }

    public double getX1() {
        return p1.getX();
    }

    public double getY1() {
        return p1.getY();
    }

    public double getX2() {
        return p2.getX();
    }

    public double getY2() {
        return p2.getY();
    }

    public boolean isPerpendicular() {
        return Math.abs(p1.getX() - p2.getX()) < 1E-6;
    }

    public boolean isHorizontal() {
        return Math.abs(p1.getY() - p2.getY()) < 1E-6;
    }

    private double getA() {
        if (isPerpendicular()) {
            return 1;
        } else {
            return (p2.getY() - p1.getY()) / (p1.getX() - p2.getX());
        }
    }

    private double getB() {
        return isPerpendicular() ? 0 : 1;
    }

    private double getC() {
        if (isPerpendicular()) {
            return -p1.getX();
        } else {
            return (p2.getX()*p1.getY() - p1.getX()*p2.getY()) / (p1.getX() - p2.getX());
        }
    }

    @Override
    public List<Point2D> intersect(Intersectable other) {
        List<Point2D> points = new ArrayList<Point2D>();
        if (other instanceof algo.Line) {
            algo.Line otherLine = (algo.Line) other;
            Point2D point = intersect(this, otherLine);
            if (point != null) {
                points.add(point);
            }
        } else if (other instanceof LineSeg) {
            LineSeg seg = (LineSeg) other;
            Point2D point = intersect(this, seg.toLine());
            if (point != null && seg.contains(point)) {
                points.add(point);
            }
        } else if (other instanceof Circle) {
            Circle circle = (Circle) other;
            double a = getA();
            double b = getB();
            double c = getC();
            double x1 = circle.center.getX();
            double y1 = circle.center.getY();
            double r = circle.radius;
            if (Util.isZero(b)) {
                double x = -c/a;
                double d = Math.abs(x - x1);
                if (Util.isZero(d)) {
                    points.add(new Point2D(x, y1));
                } else if (d < r) {
                    double delta = r*r - d*d;
                    points.add(new Point2D(x, y1 - Math.sqrt(delta)));
                    points.add(new Point2D(x, y1 + Math.sqrt(delta)));
                }
            } else if (Util.isZero(a)) {
                double y = -c/b;
                double d = Math.abs(y - y1);
                if (Util.isZero(d)) {
                    points.add(new Point2D(x1, y));
                } else if (d < r) {
                    double delta = r*r - d*d;
                    points.add(new Point2D(x1 - Math.sqrt(delta), y));
                    points.add(new Point2D(x1 + Math.sqrt(delta), y));
                }
            } else {
                double na = 1 + a*a;
                double nb = 2 * (a*c + a*y1 - x1);
                double nc = x1*x1 + Math.pow(c+y1, 2) - r*r;
                double delta = nb * nb - 4 * na * nc;
                if (Util.isZero(delta)) {
                    double x = -0.5*nb/na;
                    points.add(new Point2D(x, -a*x-c));
                } else if (delta > 0) {
                    double sqrtDelta = Math.sqrt(delta);
                    double x = 0.5 * (-nb - sqrtDelta)/na;
                    points.add(new Point2D(x, -a*x-c));
                    x = 0.5 * (-nb + sqrtDelta)/na;
                    points.add(new Point2D(x, -a*x-c));
                }
            }
        } else {
            throw new UnsupportedOperationException();
        }
        return points;
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.save();
        gc.setStroke(Color.GREEN);
        gc.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        gc.restore();
    }

    public Point2D findPerpendicularPoint(Point2D point) {
        if (isPerpendicular()) {
            return new Point2D(p1.getX(), point.getY());
        } else if (isHorizontal()) {
            return new Point2D(point.getX(), p1.getY());
        } else {
            double kPer = (p1.getX() - p2.getX()) / (p2.getY() - p1.getY());
            algo.Line line = new algo.Line(point, new Point2D(0, point.getY() - kPer*point.getX()));
            List<Point2D> intersects = intersect(line);
            assert intersects.size() == 1;
            return intersects.get(0);
        }
    }

    public static Point2D intersect(algo.Line line1, algo.Line line2) {
        double a1 = line1.getA();
        double b1 = line1.getB();
        double c1 = line1.getC();
        double a2 = line2.getA();
        double b2 = line2.getB();
        double c2 = line2.getC();
        if (Math.abs(a1 - a2) < 1E-6) {
            return null;
        }
        double div = a2*b1 - a1*b2;
        assert Math.abs(div) > 1E-6;
        double x = (b2*c1 - b1*c2)/div;
        double y = 0;
//            if (isZero(a2)) {
            y = (a1*c2 - a2*c1)/div;
//            } else {
//                x = (a2*b2*c1 - a2*b1*c2)/(a2*div);
//            }
        return new Point2D(x, y);
    }
}
