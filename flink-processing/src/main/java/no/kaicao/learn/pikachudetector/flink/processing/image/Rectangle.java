package no.kaicao.learn.pikachudetector.flink.processing.image;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents rectangle by coordinator point of its top left corner, width and height.
 * Then the other corners can be calculated.
 */
public class Rectangle {

  private Point topLeft;
  private double width;
  private double height;

  public Rectangle() {
  }

  public Rectangle(Point topLeft, double width, double height) {
    this.topLeft = topLeft;
    this.width = width;
    this.height = height;
  }

  public List<Point> getPoints() {
    return Stream.of(topLeft, getTopRight(), getBottomLeft(), getBottomRight())
        .collect(Collectors.toList());
  }

  public Point getTopRight() {
    return new Point(topLeft.getX() + width, topLeft.getY());
  }

  public Point getBottomLeft() {
    return new Point(topLeft.getX(), topLeft.getY() - height);
  }

  public Point getBottomRight() {
    return new Point(topLeft.getX() + width, topLeft.getY() - height);
  }

  public Point getTopLeft() {
    return topLeft;
  }

  public Rectangle setTopLeft(Point topLeft) {
    this.topLeft = topLeft;
    return this;
  }

  public double getWidth() {
    return width;
  }

  public Rectangle setWidth(double width) {
    this.width = width;
    return this;
  }

  public double getHeight() {
    return height;
  }

  public Rectangle setHeight(double height) {
    this.height = height;
    return this;
  }
}
