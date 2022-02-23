package no.kaicao.learn.pikachudetector.flink.processing.objectdetection;

import no.kaicao.learn.pikachudetector.flink.processing.image.Rectangle;

public class ObjectDetectionResult {

  private int classID;
  private Rectangle rectangle;
  private double score;

  public ObjectDetectionResult() {
  }

  public int getClassID() {
    return classID;
  }

  public ObjectDetectionResult setClassID(int classID) {
    this.classID = classID;
    return this;
  }

  public Rectangle getRectangle() {
    return rectangle;
  }

  public ObjectDetectionResult setRectangle(Rectangle rectangle) {
    this.rectangle = rectangle;
    return this;
  }

  public double getScore() {
    return score;
  }

  public ObjectDetectionResult setScore(double score) {
    this.score = score;
    return this;
  }
}
