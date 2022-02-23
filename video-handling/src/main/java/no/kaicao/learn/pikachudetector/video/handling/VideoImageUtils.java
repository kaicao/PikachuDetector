package no.kaicao.learn.pikachudetector.video.handling;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class VideoImageUtils {

  private VideoImageUtils() {}

  public static BufferedImage readImageFromBytes(byte[] bytes) throws IOException {
    return ImageIO.read(new ByteArrayInputStream(bytes));
  }

  public static byte[] readBytesFromImage(BufferedImage image, String formatName) throws IOException {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      ImageIO.write(image, formatName, outputStream);
      return outputStream.toByteArray();
    }
  }

  public static BufferedImage convertImageType(BufferedImage sourceImage, int targetType) {
    if (sourceImage.getType() == targetType) {
      return sourceImage;
    }
    BufferedImage image = new BufferedImage(
        sourceImage.getWidth(),
        sourceImage.getHeight(),
        targetType);
    image.getGraphics().drawImage(sourceImage, 0, 0, null);
    return image;
  }
}
