package no.kaicao.learn.pikachudetector.flink.processing.image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class ImageHandler {

  public void drawRectangle(BufferedImage image, Rectangle rectangle, String label) {
    Objects.requireNonNull(image, "image is required");
    Objects.requireNonNull(rectangle, "rectangle is required");

    Graphics2D graphics2D = image.createGraphics();
    int stroke = 2;
    graphics2D.setStroke(new BasicStroke(stroke));
    graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int imageWidth = image.getWidth();
    int imageHeight = image.getHeight();

    int x = (int) rectangle.getTopLeft().getX();
    int y = (int) rectangle.getTopLeft().getY();

    graphics2D.setPaint(Color.BLUE.darker());
    graphics2D.drawRect(
        x,
        y,
        (int) rectangle.getWidth(),
        (int) rectangle.getHeight()
    );
    if (label != null) {
      drawText(graphics2D, label, x, y, stroke, 4);
    }
    graphics2D.dispose();
  }

  private void drawText(Graphics2D graphics2D, String text, int x, int y, int stroke, int padding) {
    FontMetrics metrics = graphics2D.getFontMetrics();
    x += stroke / 2;
    y += stroke / 2;
    int width = metrics.stringWidth(text) + padding * 2 - stroke / 2;
    int height = metrics.getHeight() + metrics.getDescent();
    int ascent = metrics.getAscent();
    java.awt.Rectangle background = new java.awt.Rectangle(x, y, width, height);
    graphics2D.fill(background);
    graphics2D.setPaint(Color.WHITE);
    graphics2D.drawString(text, x + padding, y + ascent);
  }
}
