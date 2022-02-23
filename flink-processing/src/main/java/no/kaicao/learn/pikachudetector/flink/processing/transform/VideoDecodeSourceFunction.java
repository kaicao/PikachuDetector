package no.kaicao.learn.pikachudetector.flink.processing.transform;

import no.kaicao.learn.pikachudetector.flink.processing.model.ImageDetectionInput;
import no.kaicao.learn.pikachudetector.video.handling.VideoDecoder;
import no.kaicao.learn.pikachudetector.video.handling.VideoDecoderImpl;
import no.kaicao.learn.pikachudetector.video.handling.VideoImage;
import no.kaicao.learn.pikachudetector.video.handling.VideoInfo;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class VideoDecodeSourceFunction extends RichSourceFunction<ImageDetectionInput> {

  private static final Logger LOG = LoggerFactory.getLogger(VideoDecodeSourceFunction.class);
  private static final ObjectWriter VIDEO_INFO_WRITER = new ObjectMapper().writerFor(VideoInfo.class);

  private final String videoFilePath;

  public VideoDecodeSourceFunction(Path videoFilePath) {
    this.videoFilePath = videoFilePath.toFile().getAbsolutePath();
  }


  @Override
  public void run(SourceContext<ImageDetectionInput> sourceContext) throws Exception {
    LOG.info(String.format("Start decoding video %s and generate stream source", videoFilePath));

    VideoDecoder videoHandler = new VideoDecoderImpl();

    VideoInfo videoInfo = videoHandler.decodeVideo(videoFilePath);
    List<VideoImage> images = videoInfo.getImages();

    videoInfo.setImages(new ArrayList<>()); // reset images for the state
    storeVideoInfoToFile(videoInfo);

    LOG.info(String.format("Decoded %d images from video %s", images.size(), videoFilePath));
    for (int i = 0; i < images.size(); i ++) {
      sourceContext.collect(new ImageDetectionInput()
          .setImageID(i)
          .setBytes(images.get(i).getBytes()));
    }

  }

  @Override
  public void cancel() {

  }

  private void storeVideoInfoToFile(VideoInfo videoInfo) throws IOException {
    byte[] bytes = VIDEO_INFO_WRITER.writeValueAsBytes(videoInfo);
    Path path = Paths.get("videoInfo.json");
    Files.deleteIfExists(path);
    Files.write(path, bytes, StandardOpenOption.CREATE);
  }

}
