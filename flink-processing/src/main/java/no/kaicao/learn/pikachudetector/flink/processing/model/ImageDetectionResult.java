package no.kaicao.learn.pikachudetector.flink.processing.model;

import no.kaicao.learn.pikachudetector.flink.processing.objectdetection.ObjectDetectionResult;

public class ImageDetectionResult {

  private int imageID;
  private byte[] bytes;
  private ObjectDetectionResult objectDetectionResult;

  public ImageDetectionResult() {
  }

  public int getImageID() {
    return imageID;
  }

  public ImageDetectionResult setImageID(int imageID) {
    this.imageID = imageID;
    return this;
  }

  public byte[] getBytes() {
    return bytes;
  }

  public ImageDetectionResult setBytes(byte[] bytes) {
    this.bytes = bytes;
    return this;
  }

  public ObjectDetectionResult getObjectDetectionResult() {
    return objectDetectionResult;
  }

  public ImageDetectionResult setObjectDetectionResult(ObjectDetectionResult objectDetectionResult) {
    this.objectDetectionResult = objectDetectionResult;
    return this;
  }
}
