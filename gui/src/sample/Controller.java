package sample;

import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.List;


public class Controller {

    @FXML
    private TextField offset;

    private Canvas canvas;
    
    private List<Point2D> line = new ArrayList<Point2D>();
            

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    @FXML
    private void handleGo() {
        String value = offset.getText().trim();
        try {
            double offset = Double.parseDouble(value);

            clearCanvas();
            drawLine(this.line, Color.BLACK);

            List<Point2D> offsetLine = offset(offset);
            drawLine(offsetLine, Color.BLUE);

            List<Point2D> circleAssistLine = rollThrough(this.line, offset);
            drawLine(circleAssistLine, Color.RED);

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Invalid input");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleClean() {
        line.clear();
        clearCanvas();
    }

    private void clearCanvas() {
        GraphicsContext gc = this.canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    @FXML
    private void handleMouseReleased(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY && e.getSource() instanceof Canvas) {
            this.canvas = (Canvas) e.getSource();
            Point2D p = new Point2D(e.getX(), e.getY());
            if (!line.isEmpty()) {
                Point2D p1 = line.get(line.size() - 1);
                GraphicsContext gc = this.canvas.getGraphicsContext2D();
                gc.save();
                gc.setStroke(Color.BLACK);
                gc.strokeLine(p1.getX(), p1.getY(), p.getX(), p.getY());
                gc.restore();
            }
            line.add(p);
        }
    }

    private void drawLine(List<Point2D> line, Paint paint) {
        if (line.size() < 2) {
            return;
        }
        GraphicsContext gc = this.canvas.getGraphicsContext2D();
        gc.save();
        gc.setStroke(paint);
        Point2D p1 = null;
        for (Point2D p : line) {
            if (p1 != null) {
                gc.strokeLine(p1.getX(), p1.getY(), p.getX(), p.getY());
            }
            p1 = p;
        }
        gc.restore();
    }

    private List<Point2D> offset(double offset) {
        List<Point2D> newLine = new ArrayList<Point2D>();
        Point2D p1 = null;
        for (Point2D p : this.line) {
            if (p1 != null) {
                newLine.addAll(offsetLineSeg(p1, p, offset));
            }
            p1 = p;
        }
        return newLine;
    }

    private List<Point2D> offsetLineSeg(Point2D p1, Point2D p2, double offset) {
        double d = Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
        double dx = offset * (p1.getY() - p2.getY()) / d;
        double dy = offset * (p2.getX() - p1.getX()) / d;
        List<Point2D> newLine = new ArrayList<Point2D>();
        newLine.add(new Point2D(p1.getX() + dx, p1.getY() + dy));
        newLine.add(new Point2D(p2.getX() + dx, p2.getY() + dy));
        return newLine;
    }


    private List<Point2D> rollThrough(List<Point2D> line, double offset) {
        List<Point2D> newLine = new ArrayList<Point2D>();
        Point2D p1, p2, p3;
        for (int i = 0; i < line.size(); ++i) {
            p1 = (i == 0) ? null : line.get(i-1);
            p2 = line.get(i);
            p3 = (i == line.size()-1) ? null : line.get(i+1);
            if (i == 0) {
                newLine.add(offsetLineSeg(p2, p3, offset).get(0));
            } else if (i + 1 == line.size()) {
                newLine.add(offsetLineSeg(p1, p2, offset).get(1));
            } else {
                double area = p1.getX()*p2.getY() - p1.getY()*p2.getX()
                        + p2.getX()*p3.getY() - p2.getY()*p3.getX()
                        + p3.getX()*p1.getY() - p3.getY()*p1.getX();
                if (Math.abs(area) < 1E-6) {
                    //skip the point in one line
                } else if (area < 0) {
                    //Tu
                    newLine.add(offsetLineSeg(p1, p2, offset).get(1));
                    newLine.add(offsetLineSeg(p2, p3, offset).get(0));
                } else {
                    //Ou
                    newLine.add(rollInAngle(p1, p2, p3, offset));
                }
            }
        }
        return newLine;
    }

    interface Intersectable {
        List<Point2D> intersect(Intersectable other);
        void draw(GraphicsContext gc);
    }

    public static boolean isZero(double value) {
        return Math.abs(value) < 1E-6;
    }

    static class Line implements Intersectable {
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
            if (other instanceof Line) {
                Line otherLine = (Line) other;
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
                if (isZero(b)) {
                    double x = -c/a;
                    double d = Math.abs(x - x1);
                    if (isZero(d)) {
                        points.add(new Point2D(x, y1));
                    } else if (d < r) {
                        double delta = r*r - d*d;
                        points.add(new Point2D(x, y1 - Math.sqrt(delta)));
                        points.add(new Point2D(x, y1 + Math.sqrt(delta)));
                    }
                } else if (isZero(a)) {
                    double y = -c/b;
                    double d = Math.abs(y - y1);
                    if (isZero(d)) {
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
                    if (isZero(delta)) {
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
                Line line = new Line(point, new Point2D(0, point.getY() - kPer*point.getX()));
                List<Point2D> intersects = intersect(line);
                assert intersects.size() == 1;
                return intersects.get(0);
            }
        }

        public static Point2D intersect(Line line1, Line line2) {
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


    static class LineSeg implements Intersectable {
        private Point2D p1;
        private Point2D p2;
        public LineSeg(Point2D p1, Point2D p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

        public Line toLine() {
            return new Line(p1, p2);
        }

        public boolean contains(Point2D point) {
            double t = 0;
            if (toLine().isPerpendicular()) {
                t = (point.getX() - p1.getX()) / (p2.getX() - p1.getX());
            } else {
                t = (point.getY() - p1.getY()) / (p2.getY() - p1.getY());
            }
            return t >= -1E-6 && t <= 1 + 1E-6;
        }

        @Override
        public List<Point2D> intersect(Intersectable other) {
            if (other instanceof Circle) {
                Line line = new Line(p1, p2);
                List<Point2D> intersects = line.intersect(other);
                List<Point2D> validIntersects = new ArrayList<Point2D>();
                for (Point2D p : intersects) {
                    if (contains(p)) {
                        validIntersects.add(p);
                    }
                }
                return validIntersects;
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public void draw(GraphicsContext gc) {
            gc.save();
            gc.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            gc.restore();
        }
    }

    static class Circle implements Intersectable {
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
            } else if (other instanceof Circle) {
                Circle circle = (Circle) other;
                assert circle.radius == radius;
                double dd = Math.pow(center.getX() - circle.center.getX(), 2) + Math.pow(center.getY() - circle.center.getY(), 2);
                if (isZero(dd - Math.pow(radius + circle.radius, 2))) {
                    points.add(new Point2D(0.5 * (center.getX() + circle.center.getX()), 0.5 * (center.getY() + circle.center.getY())));
                } else if (dd < Math.pow(radius + circle.radius, 2) + 1E-6) {
                    double x1 = center.getX();
                    double y1 = center.getY();
                    double x2 = circle.center.getX();
                    double y2 = circle.center.getY();
                    double r = radius;
                    if (isZero(y2 - y1)) {
                        double yd = Math.sqrt(r * r - 0.25 * dd);
                        points.add(new Point2D(0.5 * (x1 + x2), y1 - yd));
                        points.add(new Point2D(0.5 * (x1 + x2), y1 + yd));
                    } else if (isZero(x2 - x1)) {
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


    private Point2D rollInAngle(Point2D p1, Point2D p2, Point2D p3, double offset) {
        Line line1 = new Line(p1, p2);
        Line line2 = new Line(p2, p3);

        LineSeg seg1 = new LineSeg(p1, p2);
        LineSeg seg2 = new LineSeg(p2, p3);

        GraphicsContext gc = this.canvas.getGraphicsContext2D();
        gc.save();
        gc.setStroke(Color.ORANGE);

        // in/in
        Point2D incircleCenter = findInCircleCenter(p1, p2, p3, offset);
        Point2D perPoint1 = line1.findPerpendicularPoint(incircleCenter);
        Point2D perPoint2 = line2.findPerpendicularPoint(incircleCenter);
        if (seg1.contains(perPoint1) && seg2.contains(perPoint2)) {
            gc.strokeLine(perPoint1.getX(), perPoint1.getY(), incircleCenter.getX(), incircleCenter.getY());
            gc.strokeLine(perPoint2.getX(), perPoint2.getY(), incircleCenter.getX(), incircleCenter.getY());
            return incircleCenter;
        }

        // out/out
        {
            Circle c1 = new Circle(p1, offset);
            Circle c2 = new Circle(p3, offset);
            List<Point2D> intersects = c1.intersect(c2);
            for (Point2D inter : intersects) {
                if (validOutCircleCenter(inter, offset, seg1, seg2)) {
                    gc.strokeOval(p1.getX() - offset, p1.getY() - offset, 2*offset, 2*offset);
                    gc.strokeOval(p3.getX() - offset, p3.getY() - offset, 2*offset, 2*offset);
                    return inter;
                }
            }
        }

        // in/out
        {
            Line line = new Line(offsetLineSeg(p1, p2, offset));
            Circle c2 = new Circle(p3, offset);
            List<Point2D> intersects = line.intersect(c2);
            for (Point2D inter : intersects) {
//                gc.strokeOval(p3.getX() - offset, p3.getY() - offset, 2*offset, 2*offset);
//                gc.strokeLine(line.p1.getX(), line.p1.getY(), line.p2.getX(), line.p2.getY());
//                gc.strokeOval(inter.getX() - offset, inter.getY() - offset, 2*offset, 2*offset);
                if (validOutCircleCenter(inter, offset, seg1, seg2)) {
                    gc.strokeOval(p3.getX() - offset, p3.getY() - offset, 2*offset, 2*offset);
                    gc.strokeLine(line.p1.getX(), line.p1.getY(), line.p2.getX(), line.p2.getY());
                    return inter;
                }
            }
        }

        // out/in
        {
            Circle c1 = new Circle(p1, offset);
            Line line = new Line(offsetLineSeg(p2, p3, offset));
            List<Point2D> intersects = line.intersect(c1);
            for (Point2D inter : intersects) {
//                gc.strokeOval(p1.getX() - offset, p1.getY() - offset, 2*offset, 2*offset);
//                gc.strokeLine(line.p1.getX(), line.p1.getY(), line.p2.getX(), line.p2.getY());
//                gc.strokeOval(inter.getX() - offset, inter.getY() - offset, 2*offset, 2*offset);
                if (validOutCircleCenter(inter, offset, seg1, seg2)) {
                    gc.strokeOval(p1.getX() - offset, p1.getY() - offset, 2*offset, 2*offset);
                    gc.strokeLine(line.p1.getX(), line.p1.getY(), line.p2.getX(), line.p2.getY());
                    return inter;
                }
            }
        }

        throw new UnsupportedOperationException("No intersect point");

//        gc.restore();

//        Intersectable inter1 = getIntersectable(p1, p2, p1, perPoint1, offset);
//        Intersectable inter2 = getIntersectable(p2, p3, p3, perPoint2, offset);
//
//
////        Intersectable inter1 = getIntersectable(p1, p2, p3, offset);
////        Intersectable inter2 = getIntersectable2(p1, p2, p3, offset);
//        GraphicsContext gc = this.canvas.getGraphicsContext2D();
//        gc.strokeLine(perPoint1.getX(), perPoint1.getY(), incircleCenter.getX(), incircleCenter.getY());
//        gc.strokeLine(perPoint2.getX(), perPoint2.getY(), incircleCenter.getX(), incircleCenter.getY());
//        gc.strokeOval(perPoint1.getX() - 2, perPoint1.getY() - 2, 4, 4);
//        gc.strokeOval(perPoint2.getX() - 2, perPoint2.getY() - 2, 4, 4);
//        inter1.draw(gc);
//        inter2.draw(gc);
//        List<Point2D> points = inter1.intersect(inter2);
//        if (points.isEmpty()) {
//            new Line(p1, p2).draw(gc);
//            new Line(p2, p3).draw(gc);
//            throw new IllegalArgumentException("No intersection");
//        } else if (points.size() == 1) {
//            return points.get(0);
//        } else {
//            return points.get(0);//TODO
//        }
    }

    private boolean validOutCircleCenter(Point2D center, double radius, LineSeg seg1, LineSeg seg2) {
        Circle outCircle = new Circle(center, radius);
        List<Point2D> intersects1 = outCircle.intersect(seg1);
        List<Point2D> intersects2 = outCircle.intersect(seg2);
        return intersects1.size() == 1 && intersects2.size() == 1;
    }

    private Point2D findInCircleCenter(Point2D p1, Point2D p2, Point2D p3, double radius) {
        Line line1 = new Line(offsetLineSeg(p1, p2, radius));
        Line line2 = new Line(offsetLineSeg(p2, p3, radius));
        List<Point2D> intersects = line1.intersect(line2);
        assert intersects.size() == 1;
        return intersects.get(0);
    }

    private Intersectable getIntersectable(Point2D p1, Point2D p2, Point2D edgePoint, Point2D perpendPoint, double offset) {
        LineSeg line = new LineSeg(p1, p2);
        if (line.contains(perpendPoint)) {
            return new Line(offsetLineSeg(p1, p2, offset));
        }

        return new Circle(edgePoint, offset);
    }

    private Intersectable getIntersectable(Point2D p1, Point2D p2, Point2D p3, double offset) {
        Line line = new Line(offsetLineSeg(p2, p3, offset));
        if (line.intersect(new LineSeg(p1, p2)).isEmpty()) {
            return new Circle(p1, offset);
        } else {
            return new Line(offsetLineSeg(p1, p2, offset));
        }
    }

    private Intersectable getIntersectable2(Point2D p1, Point2D p2, Point2D p3, double offset) {
        Line line = new Line(offsetLineSeg(p1, p2, offset));
        if (line.intersect(new LineSeg(p2, p3)).isEmpty()) {
            return new Circle(p3, offset);
        } else {
            return new Line(offsetLineSeg(p2, p3, offset));
        }
    }
}
