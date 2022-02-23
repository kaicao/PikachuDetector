package no.kaicao.learn.pikachudetector.flink.processing.model;

public class ImageDetectionInput {

  private int imageID;
  private byte[] bytes;

  public ImageDetectionInput() {
  }

  public int getImageID() {
    return imageID;
  }

  public ImageDetectionInput setImageID(int imageID) {
    this.imageID = imageID;
    return this;
  }

  public byte[] getBytes() {
    return bytes;
  }

  public ImageDetectionInput setBytes(byte[] bytes) {
    this.bytes = bytes;
    return this;
  }
}
