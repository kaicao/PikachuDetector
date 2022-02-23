package no.kaicao.learn.pikachudetector.flink.processing.transform;

import no.kaicao.learn.pikachudetector.flink.processing.model.ImageDetectionResult;
import no.kaicao.learn.pikachudetector.video.handling.VideoEncoder;
import no.kaicao.learn.pikachudetector.video.handling.VideoEncoderImpl;
import no.kaicao.learn.pikachudetector.video.handling.VideoImage;
import no.kaicao.learn.pikachudetector.video.handling.VideoInfo;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectReader;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class VideoEncodeSinkFunction extends RichSinkFunction<ImageDetectionResult> {
  private static final Logger LOG = LoggerFactory.getLogger(VideoEncodeSinkFunction.class);
  private static final ObjectReader VIDEO_INFO_READER = new ObjectMapper().readerFor(VideoInfo.class);

  private final String outputVideoPathString;

  private final List<VideoImage> bufferedVideoImages = new ArrayList<>();

  public VideoEncodeSinkFunction(Path outputVideoPath) {
    this.outputVideoPathString = outputVideoPath.toFile().getAbsolutePath();
  }

  @Override
  public void close() throws Exception {
    super.close();
    VideoInfo videoInfo = readVideoInfo();
    if (videoInfo == null) {
      throw new IllegalStateException("No VideoInfo available in the state");
    }
    // construct and write video by sorted detected images
    bufferedVideoImages.sort(Comparator.comparing(VideoImage::getImageID));
    LOG.info(String.format("Got %d images for video %s", bufferedVideoImages.size(), videoInfo.getFileName()));
    videoInfo.setImages(bufferedVideoImages); // assign images to videoInfo

    Path outputVideoPath = Paths.get(outputVideoPathString);
    if (Files.exists(outputVideoPath)) {
      LOG.info(String.format("%s exist, delete now", outputVideoPath.toString()));
      Files.deleteIfExists(outputVideoPath);
    }
    String fileName = outputVideoPath.getFileName().toString();
    videoInfo.setFileName(fileName);
    videoInfo.setFileFormatName(com.google.common.io.Files.getFileExtension(fileName));

    LOG.info("Start ENcoding " + outputVideoPath);
    VideoEncoder videoEncoder = new VideoEncoderImpl();
    videoEncoder.encodeVideo(videoInfo, outputVideoPath.getParent().toString());
    LOG.info("Finished ENcoding " + outputVideoPath);
  }

  @Override
  public void invoke(ImageDetectionResult value, SinkFunction.Context context) throws Exception {
      if (value == null) {
        return;
      }

      bufferedVideoImages.add(new VideoImage()
          .setImageID(value.getImageID())
          .setBytes(value.getBytes()));
  }

  private VideoInfo readVideoInfo() throws IOException {
    Path path = Paths.get("videoInfo.json");
    byte[] bytes = Files.readAllBytes(path);
    return VIDEO_INFO_READER.readValue(bytes);
  }
}
