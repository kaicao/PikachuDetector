package no.kaicao.learn.pikachudetector.flink.processing.handler;

import com.google.protobuf.ByteString;
import no.kaicao.learn.pikachudetector.flink.processing.image.ImageHandler;
import no.kaicao.learn.pikachudetector.flink.processing.model.ImageDetectionResult;
import no.kaicao.learn.pikachudetector.flink.processing.objectdetection.ObjectDetectionResult;
import no.kaicao.learn.pikachudetector.flink.processing.objectdetection.SingleShotDetectionResultConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tensorflow.*;
import org.tensorflow.ndarray.NdArrays;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.buffer.DataBuffers;
import org.tensorflow.proto.example.*;
import org.tensorflow.types.TString;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TFPikachuDetectHandler {

  private static final Logger LOG = LoggerFactory.getLogger(TFPikachuDetectHandler.class);

  public ImageDetectionResult detect(Session session, int imageID, byte[] imageBytes, double accuracyToDraw) throws IOException {
    try (TString tensor = normalizeImageString(imageBytes)) {
      List<Tensor> result =
          session
              .runner()
              .feed("encoded_image_string_tensor", 0, tensor)
              .fetch("detection_boxes")
              .fetch("detection_classes")
              .fetch("detection_multiclass_scores")
              .fetch("detection_scores")
              .fetch("num_detections")
              .fetch("raw_detection_boxes")
              .fetch("raw_detection_scores")
              .run();

      SingleShotDetectionResultConverter converter = new SingleShotDetectionResultConverter();
      BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
      ObjectDetectionResult objectDetectionResult = converter.convert(result, image);
      if (objectDetectionResult.getScore() > accuracyToDraw) {
        ImageHandler imageHandler = new ImageHandler();
        imageHandler.drawRectangle(
            image,
            objectDetectionResult.getRectangle(),
            String.format("%.2f%%", objectDetectionResult.getScore() * 100));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        LOG.info(String.format("Draw detection rectangle on image %d that has accuracy of %.2f", imageID, objectDetectionResult.getScore()));
        imageBytes = baos.toByteArray();  // Replace image bytes with new image with detection rectangle drew
      }
      return new ImageDetectionResult()
          .setImageID(imageID)
          .setBytes(imageBytes)
          .setObjectDetectionResult(objectDetectionResult);
      }
  }

  public static void main(String[] args) throws IOException {
    Path folderPath = Paths.get("flink-processing/src/main/resources");

    Path modelPath = folderPath.resolve("ssd_mobilenet_v2_encoded_image_input/saved_model");
    //Path modelPath = folderPath.resolve("ssd_inception_v3_encoded_image_input/saved_model");
    //Path imagesFolderPath = folderPath.resolve("images");
    Path imagesFolderPath = folderPath.resolve("movie/images");
    //Path imagesFolderPath = folderPath.resolve("pikachu/images");


    SavedModelBundle bundle = SavedModelBundle.load(modelPath.toString(), "serve");
    System.out.println();

    Session session = bundle.session();
    session.initialize();
    Files.list(imagesFolderPath)
        .forEach(
            file -> {
              if (file.toFile().getName().endsWith(".jpg")) {
                System.out.println("Handle " + file.getFileName());
                try (TString tensor = normalizeImageString(Files.readAllBytes(file))) {
                  List<Tensor> result =
                      session
                          .runner()
                          // .feed("tf_example", 0, tensor)
                          .feed("encoded_image_string_tensor", 0, tensor)
                          .fetch("detection_boxes")
                          .fetch("detection_classes")
                          .fetch("detection_multiclass_scores")
                          .fetch("detection_scores")
                          .fetch("num_detections")
                          .fetch("raw_detection_boxes")
                          .fetch("raw_detection_scores")
                          .run();

                  SingleShotDetectionResultConverter converter =
                      new SingleShotDetectionResultConverter();
                  BufferedImage image = ImageIO.read(file.toFile());
                  ObjectDetectionResult objectDetectionResult = converter.convert(result, image);
                  if (objectDetectionResult.getScore() > 0.95) {
                    ImageHandler imageHandler = new ImageHandler();
                    imageHandler.drawRectangle(
                        image,
                        objectDetectionResult.getRectangle(),
                        String.format("%.2f%%", objectDetectionResult.getScore() * 100));

                    String fileName = file.getFileName().toString();
                    String filePrefix = fileName.substring(0, fileName.length() - ".jpg".length());
                    ImageIO.write(
                        image,
                        "jpg",
                        imagesFolderPath
                            .resolveSibling("result")
                            .resolve(filePrefix + "-output.jpg")
                            .toFile());
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }
            });

    session.close();

  }

  private static TString normalizeImageString(byte[] bytes) throws IOException {
    /*
    Need to use tensorOfBytes as answered in
    https://stackoverflow.com/questions/68211406/why-do-i-get-slice-index-0-of-dimension-0-out-of-bounds-error-in-tensorflow-fo
     */
    return TString.tensorOfBytes(NdArrays.vectorOfObjects(bytes));
  }

  /**
   * TODO not working yet, still get "Could not parse example input" when execute.
   *
   * @param imagePath
   * @return
   * @throws IOException
   */
  private static TString normalizeImagTFExample(Path imagePath) throws IOException {
    BufferedImage image = ImageIO.read(imagePath.toFile());
    byte[] data = ((DataBufferByte) image.getData().getDataBuffer()).getData();
    Shape shape = Shape.of();

    Example example = Example.newBuilder()
        .setFeatures(Features.newBuilder()
            .putFeature("image/height", Feature.newBuilder()
                .setInt64List(Int64List.newBuilder().addValue(image.getHeight()).build())
                .build())
            .putFeature("image/width", Feature.newBuilder()
                .setInt64List(Int64List.newBuilder().addValue(image.getWidth()).build())
                .build())
            .putFeature("image/source_id", Feature.newBuilder()
                .setBytesList(BytesList.newBuilder().addValue(ByteString.copyFromUtf8(imagePath.getFileName().toString())).build())
                .build())
            .putFeature("image/format", Feature.newBuilder()
                .setBytesList(BytesList.newBuilder().addValue(ByteString.copyFromUtf8("jpg")).build())
                .build())
            .putFeature("image/encoded", Feature.newBuilder()
                .setBytesList(BytesList.newBuilder()
                    .addValue(ByteString.copyFrom(data))
                    .build())
                .build())
            .build())
        .build();

    String serialized = example.toByteString().toString(StandardCharsets.UTF_8);
    return TString.tensorOf(shape, DataBuffers.ofObjects(serialized));
  }
}
