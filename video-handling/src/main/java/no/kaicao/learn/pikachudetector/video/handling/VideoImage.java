package no.kaicao.learn.pikachudetector.video.handling;

public class VideoImage {

  private int imageID;
  // timestamp of image within the video to be played
  private long streamTimestampNano;
  private byte[] bytes;

  public int getImageID() {
    return imageID;
  }

  public VideoImage setImageID(int imageID) {
    this.imageID = imageID;
    return this;
  }

  public long getStreamTimestampNano() {
    return streamTimestampNano;
  }

  public VideoImage setStreamTimestampNano(long streamTimestampNano) {
    this.streamTimestampNano = streamTimestampNano;
    return this;
  }

  public byte[] getBytes() {
    return bytes;
  }

  public VideoImage setBytes(byte[] bytes) {
    this.bytes = bytes;
    return this;
  }
}
