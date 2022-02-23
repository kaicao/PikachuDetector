package no.kaicao.learn.pikachudetector.flink.processing.image;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImageHandlerTest {

  private ImageHandler imageHandler = new ImageHandler();

  @Test
  void drawRectangle() throws IOException, URISyntaxException {
    URL testFileUrl = getClass().getClassLoader().getResource("0071.jpg");
    BufferedImage image = ImageIO.read(testFileUrl);

    Rectangle rectangle = new Rectangle()
        .setTopLeft(new Point(100, 100))
        .setWidth(50)
        .setHeight(125);

    imageHandler.drawRectangle(image, rectangle, "test");

    Path outputFilePath = Paths.get(getClass().getClassLoader().getResource("").toURI())
        .resolve("output.jpg");
    ImageIO.write(image, "jpg", outputFilePath.toFile());
  }
}
