package algo;

import javafx.geometry.Point2D;

/**
 * Created by chenxian on 16/6/29.
 */
public abstract class Segment implements Drawable, Intersectable {

    protected final Point2D p1;
    protected final Point2D p2;


    protected Segment(Point2D p1, Point2D p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public abstract Segment cutOffTail(Point2D cutPoint);

    public abstract boolean contains(Point2D point);
}
