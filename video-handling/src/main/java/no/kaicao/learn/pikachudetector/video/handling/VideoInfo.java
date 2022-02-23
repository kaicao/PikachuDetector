package no.kaicao.learn.pikachudetector.video.handling;

import java.util.ArrayList;
import java.util.List;

public class VideoInfo {

  private int videoWidth;
  private int videoHeight;
  private String fileName;
  private String fileFormatName;
  private String pixelFormatName;
  private int codecID;
  private long streamStartTimestamp;

  private int streamTimeBaseNumerator;
  private int streamTimeBaseDenominator;

  private List<VideoImage> images = new ArrayList<>();

  @Override
  public String toString() {
    return "VideoInfo{" +
        "videoWidth=" + videoWidth +
        ", videoHeight=" + videoHeight +
        ", fileName='" + fileName + '\'' +
        ", fileFormatName='" + fileFormatName + '\'' +
        ", pixelFormatName='" + pixelFormatName + '\'' +
        ", codecID='" + codecID + '\'' +
        ", streamStartTimestamp=" + streamStartTimestamp +
        ", streamTimeBaseNumerator=" + streamTimeBaseNumerator +
        ", streamTimeBaseDenominator=" + streamTimeBaseDenominator +
        ", images.size=" + images.size() +
        '}';
  }

  public int getVideoWidth() {
    return videoWidth;
  }

  public VideoInfo setVideoWidth(int videoWidth) {
    this.videoWidth = videoWidth;
    return this;
  }

  public int getVideoHeight() {
    return videoHeight;
  }

  public VideoInfo setVideoHeight(int videoHeight) {
    this.videoHeight = videoHeight;
    return this;
  }

  public String getFileName() {
    return fileName;
  }

  public VideoInfo setFileName(String fileName) {
    this.fileName = fileName;
    return this;
  }

  public String getFileFormatName() {
    return fileFormatName;
  }

  public VideoInfo setFileFormatName(String fileFormatName) {
    this.fileFormatName = fileFormatName;
    return this;
  }

  public String getPixelFormatName() {
    return pixelFormatName;
  }

  public VideoInfo setPixelFormatName(String pixelFormatName) {
    this.pixelFormatName = pixelFormatName;
    return this;
  }

  public int getCodecID() {
    return codecID;
  }

  public VideoInfo setCodecID(int codecID) {
    this.codecID = codecID;
    return this;
  }

  public long getStreamStartTimestamp() {
    return streamStartTimestamp;
  }

  public VideoInfo setStreamStartTimestamp(long streamStartTimestamp) {
    this.streamStartTimestamp = streamStartTimestamp;
    return this;
  }

  public List<VideoImage> getImages() {
    return images;
  }

  public VideoInfo setImages(List<VideoImage> images) {
    this.images = images;
    return this;
  }

  public VideoInfo addImage(VideoImage image) {
    if (image == null) {
      return this;
    }
    if (this.images == null) {
      this.images = new ArrayList<>();
    }
    this.images.add(image);
    return this;
  }

  public int getStreamTimeBaseNumerator() {
    return streamTimeBaseNumerator;
  }

  public VideoInfo setStreamTimeBaseNumerator(int streamTimeBaseNumerator) {
    this.streamTimeBaseNumerator = streamTimeBaseNumerator;
    return this;
  }

  public int getStreamTimeBaseDenominator() {
    return streamTimeBaseDenominator;
  }

  public VideoInfo setStreamTimeBaseDenominator(int streamTimeBaseDenominator) {
    this.streamTimeBaseDenominator = streamTimeBaseDenominator;
    return this;
  }
}
