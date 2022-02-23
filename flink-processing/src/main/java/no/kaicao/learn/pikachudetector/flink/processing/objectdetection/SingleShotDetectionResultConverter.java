package no.kaicao.learn.pikachudetector.flink.processing.objectdetection;

import no.kaicao.learn.pikachudetector.flink.processing.image.Point;
import no.kaicao.learn.pikachudetector.flink.processing.image.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.impl.dense.FloatDenseNdArray;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class SingleShotDetectionResultConverter {

  private static final Logger LOG = LoggerFactory.getLogger(SingleShotDetectionResultConverter.class);

  /**
   * Order of tensors in the list must be:
   *
   * <ul>
   *   <li>1. detection_boxes, dtype: DT_FLOAT, shape: (1, 100, 4): Outputs float32 tensors of the
   *       form [batch, num_boxes, 4] containing detected boxes.
   *
   *   <li>2. detection_classes, dtype: DT_FLOAT, shape: (1, 100): Outputs float32 tensors of the
   *       form [batch, num_boxes] containing classes for the detections.
   *
   *   <li>3. detection_multiclass_scores, dtype: DT_FLOAT, shape: (1, 100, 38): (Optional) Outputs
   *       float32 tensor of shape [batch, num_boxes, num_classes_with_background] for containing
   *       class score distribution for detected boxes including background if any.
   *
   *   <li>4. detection_scores, dtype: DT_FLOAT, shape: (1, 100): Outputs float32 tensors of the
   *       form [batch, num_boxes] containing class scores for the detections.
   *
   *   <li>5. num_detections, dtype: DT_FLOAT, shape: (1): Outputs float32 tensors of the form
   *       [batch] that specifies the number of valid boxes per image in the batch.
   *
   *   <li>6. raw_detection_boxes, dtype: DT_FLOAT, shape: (1, 5919, 4): Outputs float32 tensors of
   *       the form [batch, raw_num_boxes, 4] containing detection boxes without post-processing.
   *
   *   <li>7. raw_detection_scores, dtype: DT_FLOAT, shape: (1, 5919, 38): Outputs float32 tensors
   *       of the form [batch, raw_num_boxes, num_classes_with_background] containing class score
   *       logits for raw detection boxes.
   * </ul>
   *
   * @param tensors list of result {@link Tensor}
   * @param image   image that was predicted on
   * @see <a href="https://raw.githubusercontent.com/tensorflow/models/master/research/object_detection/export_inference_graph.py">export_inference_graph.py</a>
   */
  public ObjectDetectionResult convert(List<Tensor> tensors, BufferedImage image) {
    FloatDenseNdArray detectionBoxesNdArray = (FloatDenseNdArray) tensors.get(0);
    FloatDenseNdArray detectionClassesNdArray = (FloatDenseNdArray) tensors.get(1);
    FloatDenseNdArray detectionMulticlassScoresNdArray = (FloatDenseNdArray) tensors.get(2);
    FloatDenseNdArray detectionScoresNdArray = (FloatDenseNdArray) tensors.get(3);
    FloatDenseNdArray numDetectionsNdArray = (FloatDenseNdArray) tensors.get(4);
    FloatDenseNdArray rawDetectionBoxesNdArray = (FloatDenseNdArray) tensors.get(5);
    FloatDenseNdArray rawDetectionScoresNdArray = (FloatDenseNdArray) tensors.get(6);

    int numDetections = (int) numDetectionsNdArray.getFloat(0);
    List<Double> detectionScorePerDetection = new ArrayList<>();
    int largestScoreIndex = 0;
    double largestScore = 0.0;
    for (int i = 0; i < numDetections; i ++) {
      double score = (double) detectionScoresNdArray.get(0).getFloat(i);
      detectionScorePerDetection.add(score);
      if (score > largestScore) {
        largestScoreIndex = i;
        largestScore = score;
      }
    }

    // Normalized coordinates of (ymin, xmin, ymax, xmax)
    FloatNdArray boxNdArray = detectionBoxesNdArray.get(0).get(0);
    float detectionClass = detectionClassesNdArray.get(0).getFloat(0);  // 1.0 means the first item

    float detectionScore = detectionScoresNdArray.get(0).getFloat(0);

    Rectangle rectangle = convertBox(boxNdArray, image, largestScoreIndex, largestScore);
    return new ObjectDetectionResult()
        .setClassID((int) detectionClass)
        .setRectangle(rectangle)
        .setScore(detectionScore);
  }

  private Rectangle convertBox(FloatNdArray boxArray, BufferedImage image, int largestIndex, double score) {
    // https://stackoverflow.com/questions/48915003/get-the-bounding-box-coordinates-in-the-tensorflow-object-detection-api-tutorial
    float ymin = boxArray.getFloat(0);
    float xmin = boxArray.getFloat(1);
    float ymax = boxArray.getFloat(2);
    float xmax = boxArray.getFloat(3);

    int imageWidth = image.getWidth();
    int imageHeight = image.getHeight();
    double topLeftX = imageWidth * xmin;
    double topLeftY = imageHeight * ymin;
    double width = imageWidth * (xmax - xmin);
    double height = imageHeight * (ymax - ymin);
    LOG.info(String.format("index %d, score %f.2, ymin %f.2, xmin %f.2, ymax %f.2, xmax %f.2",
        largestIndex, score, ymin, xmin, ymax, xmax));

    return new Rectangle()
        .setTopLeft(new Point(topLeftX, topLeftY))
        .setWidth(width)
        .setHeight(height);
  }

}
