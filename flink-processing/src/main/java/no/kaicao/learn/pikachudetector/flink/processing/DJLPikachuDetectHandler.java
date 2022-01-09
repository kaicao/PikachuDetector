package no.kaicao.learn.pikachudetector.flink.processing;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.output.DetectedObjects;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DJLPikachuDetectHandler {

  public static void main(String[] args)
      throws IOException, MalformedModelException {
    Path folderPath = Paths.get("flink-processing/src/main/resources");

    Path imagesFolderPath = folderPath.resolve("images");

    Model model = Model.newInstance("pikachu-detector");
    model.load(folderPath, "PikachuDetector-");

    Predictor<Path, DetectedObjects> filePredictor = model.newPredictor(new FileBytesTranslator());

    Files.list(imagesFolderPath)
        .forEach(
            file -> {
              if (file.toFile().getName().endsWith(".jpg")) {
                System.out.println("Handle " + file.getFileName());
                try {
                  DetectedObjects detectedObjects = filePredictor.predict(file);
                  // TODO https://github.com/deepjavalibrary/djl/discussions/1442
                  System.out.println();
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }
            });

  }
}
