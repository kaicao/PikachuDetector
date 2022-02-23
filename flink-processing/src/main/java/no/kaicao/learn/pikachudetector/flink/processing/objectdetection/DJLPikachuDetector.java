package no.kaicao.learn.pikachudetector.flink.processing.objectdetection;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.basicmodelzoo.cv.object_detection.ssd.SingleShotDetection;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.MultiBoxDetection;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.SingleShotDetectionTranslator;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.nn.Block;
import ai.djl.nn.LambdaBlock;
import ai.djl.nn.SequentialBlock;
import ai.djl.translate.TranslateException;
import no.kaicao.learn.pikachudetector.flink.processing.image.ImageHandler;
import org.tensorflow.Tensor;
import org.tensorflow.types.TString;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DJLPikachuDetector {

  public static void main(String[] args) throws IOException {
    Path folderPath = Paths.get("flink-processing/src/main/resources");

    Path modelPath = folderPath.resolve("djl_pikachu_ssd");
    Path imagesFolderPath = folderPath.resolve("images");
    Path outputPath = imagesFolderPath.resolveSibling("result");

    DJLPikachuDetector detector = new DJLPikachuDetector();
    Files.list(imagesFolderPath)
        .forEach(
            file -> {
              if (file.toFile().getName().endsWith(".jpg")) {
                System.out.println("Handle " + file.getFileName());

                try {
                  detector.predict(
                      modelPath,
                      outputPath.toFile().getAbsolutePath(),
                      file.getFileName().toString());
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }
            });
  }

  public int predict(Path modelPath, String outputDir, String imageFile)
      throws IOException, MalformedModelException, TranslateException {
    try (Model model = Model.newInstance("pikachu-ssd")) {
      float detectionThreshold = 0.6f;
      // load parameters back to original training block
      model.setBlock(getSsdTrainBlock());
      model.load(modelPath);
      // append prediction logic at end of training block with parameter loaded
      Block ssdTrain = model.getBlock();
      model.setBlock(getSsdPredictBlock(ssdTrain));
      Path imagePath = Paths.get(imageFile);
      SingleShotDetectionTranslator translator =
          SingleShotDetectionTranslator.builder()
              .addTransform(new ToTensor())
              .optSynset(Collections.singletonList("pikachu"))
              .optThreshold(detectionThreshold)
              .build();
      try (Predictor<Image, DetectedObjects> predictor = model.newPredictor(translator)) {
        Image image = ImageFactory.getInstance().fromFile(imagePath);
        DetectedObjects detectedObjects = predictor.predict(image);
        image.drawBoundingBoxes(detectedObjects);
        Path out = Paths.get(outputDir).resolve("pikachu_output.jpg");
        image.save(Files.newOutputStream(out), "jpg");
        // return number of pikachu detected
        return detectedObjects.getNumberOfObjects();
      }
    }
  }

  public Block getSsdTrainBlock() {
    int[] numFilters = {16, 32, 64};
    SequentialBlock baseBlock = new SequentialBlock();
    for (int numFilter : numFilters) {
      baseBlock.add(SingleShotDetection.getDownSamplingBlock(numFilter));
    }

    List<List<Float>> sizes = new ArrayList<>();
    List<List<Float>> ratios = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      ratios.add(Arrays.asList(1f, 2f, 0.5f));
    }
    sizes.add(Arrays.asList(0.2f, 0.272f));
    sizes.add(Arrays.asList(0.37f, 0.447f));
    sizes.add(Arrays.asList(0.54f, 0.619f));
    sizes.add(Arrays.asList(0.71f, 0.79f));
    sizes.add(Arrays.asList(0.88f, 0.961f));

    return SingleShotDetection.builder()
        .setNumClasses(1)
        .setNumFeatures(3)
        .optGlobalPool(true)
        .setRatios(ratios)
        .setSizes(sizes)
        .setBaseNetwork(baseBlock)
        .build();
  }

  public Block getSsdPredictBlock(Block ssdTrain) {
    // add prediction process
    SequentialBlock ssdPredict = new SequentialBlock();
    ssdPredict.add(ssdTrain);
    ssdPredict.add(
        new LambdaBlock(
            output -> {
              NDArray anchors = output.get(0);
              NDArray classPredictions = output.get(1).softmax(-1).transpose(0, 2, 1);
              NDArray boundingBoxPredictions = output.get(2);
              MultiBoxDetection multiBoxDetection =
                  MultiBoxDetection.builder().build();
              NDList detections =
                  multiBoxDetection.detection(
                      new NDList(
                          classPredictions,
                          boundingBoxPredictions,
                          anchors));
              return detections.singletonOrThrow().split(new long[] {1, 2}, 2);
            }));
    return ssdPredict;
  }
}
