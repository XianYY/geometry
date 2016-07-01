package sample;

import algo.*;
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

    private boolean debugEnable = true;
            

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

            Segments segs = offset(offset);
            drawSegments(segs, Color.BLUE);

//            List<Point2D> circleAssistLine = rollThrough(this.line, offset);
//            drawLine(circleAssistLine, Color.RED);

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

    private void drawSegments(Segments segs, Paint paint) {
        GraphicsContext gc = this.canvas.getGraphicsContext2D();
        gc.save();
        gc.setStroke(paint);
        segs.draw(gc);
        gc.restore();
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

    private Segments offset(double offset) {
        LineDeviator1 deviator = new LineDeviator1();
        deviator.setDebuger(new LineDeviator1.Debuger() {

            @Override
            public void debug(Point2D point) {
                if (!debugEnable) {
                    return;
                }
                GraphicsContext gc = Controller.this.canvas.getGraphicsContext2D();
                gc.save();
                gc.setStroke(Color.ORANGE);
                gc.fillOval(point.getX()-2, point.getY()-2, 4, 4);
                gc.restore();
            }

            @Override
            public void debug(Drawable drawable) {
                if (!debugEnable) {
                    return;
                }
                GraphicsContext gc = Controller.this.canvas.getGraphicsContext2D();
                gc.save();
                gc.setStroke(Color.ORANGE);
                drawable.draw(gc);
                gc.restore();
            }

            @Override
            public void debugLine(Point2D p1, Point2D p2) {
                if (!debugEnable) {
                    return;
                }
                GraphicsContext gc = Controller.this.canvas.getGraphicsContext2D();
                gc.save();
                gc.setStroke(Color.ORANGE);
                gc.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                gc.restore();
            }

            @Override
            public void debugCircle(Point2D center, double radius) {
                if (!debugEnable) {
                    return;
                }
                GraphicsContext gc = Controller.this.canvas.getGraphicsContext2D();
                gc.save();
                gc.setStroke(Color.ORANGE);
                gc.strokeOval(center.getX() - radius, center.getY() - radius, 2*radius, 2*radius);
                gc.restore();
            }
        });
        Segments segs = Segments.fromLine(line);
        return deviator.offset(segs, offset);
    }

    //TODO: remove
    @Deprecated
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
                    gc.strokeLine(line.getX1(), line.getY1(), line.getX2(), line.getY2());
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
                    gc.strokeLine(line.getX1(), line.getY1(), line.getX2(), line.getY2());
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
