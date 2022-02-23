package no.kaicao.learn.pikachudetector.flink.processing.image;

/**
 * Represents point of location in {@code (x, y)} coordinate space.
 */
public class Point {

  private double x;
  private double y;

  public Point() {
  }

  public Point(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public double getX() {
    return x;
  }

  public Point setX(double x) {
    this.x = x;
    return this;
  }

  public double getY() {
    return y;
  }

  public Point setY(double y) {
    this.y = y;
    return this;
  }
}
