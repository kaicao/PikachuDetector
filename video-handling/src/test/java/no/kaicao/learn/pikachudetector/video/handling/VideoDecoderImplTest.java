package no.kaicao.learn.pikachudetector.video.handling;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static no.kaicao.learn.pikachudetector.video.handling.VideoImageUtils.readImageFromBytes;

class VideoDecoderImplTest {

  private static final Logger LOG = LoggerFactory.getLogger(VideoDecoderImplTest.class);

  @Test
  void decodeVideo() throws IOException, InterruptedException {
    String fileName = getClass().getClassLoader().getResource("pokemon_detective_pikachu.mp4").getFile();
    LOG.info("Start decoding " + fileName);
    VideoDecoder videoHandler = new VideoDecoderImpl();

    VideoInfo videoInfo = videoHandler.decodeVideo(fileName);
    LOG.info("Finished decoding " + fileName);
    Path imagesFolderPath = Paths.get("src/test/resources/images");
    imagesFolderPath.toFile().delete();
    imagesFolderPath.toFile().mkdir();

    LOG.info("Start write images");
    int i = 0;
    for (VideoImage videoImage : videoInfo.getImages()) {
      BufferedImage image = readImageFromBytes(videoImage.getBytes());
      File imageFile = imagesFolderPath.resolve("image" + i + ".jpg").toFile();
      imageFile.createNewFile();
      ImageIO.write(image, "jpg", imageFile);
      i ++;
    }
    LOG.info("Finished write images");
  }
}
