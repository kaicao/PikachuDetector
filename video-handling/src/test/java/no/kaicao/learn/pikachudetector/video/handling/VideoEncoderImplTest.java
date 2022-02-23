package no.kaicao.learn.pikachudetector.video.handling;

import io.humble.video.Rational;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

class VideoEncoderImplTest {
  private static final Logger LOG = LoggerFactory.getLogger(VideoEncoderImplTest.class);

  @Test
  void a() {
    // 1896 images
    // 79 sec
    // 79 / 1896 = 0.04166666 sec/image
    // 1896 / 79 = 24 images/sec
    Rational test = Rational.make(1001, 48000);
    int snaps = (int) TimeUnit.SECONDS.toNanos(1L);
    Rational system = Rational.make(1, (int) TimeUnit.SECONDS.toNanos(24));
    long value = system.rescale(1001, test);
    System.out.println();
  }

  @Test
  void encodeVideo() throws IOException, InterruptedException {
    String fileName = getClass().getClassLoader().getResource("pokemon_detective_pikachu.mp4").getFile();
    LOG.info("Start DEcoding " + fileName);
    VideoDecoder videoDecoder = new VideoDecoderImpl();

    VideoInfo videoInfo = videoDecoder.decodeVideo(fileName);
    LOG.info("Finished DEcoding " + fileName);

    LOG.info("Decoded VideoInfo: " + videoInfo);

    LOG.info("Start ENcoding " + videoInfo.getFileName());
    VideoEncoder videoEncoder = new VideoEncoderImpl();
    videoEncoder.encodeVideo(videoInfo, "src/test/resources/encoded");
    LOG.info("Finished ENcoding " + videoInfo.getFileName());
  }
}
