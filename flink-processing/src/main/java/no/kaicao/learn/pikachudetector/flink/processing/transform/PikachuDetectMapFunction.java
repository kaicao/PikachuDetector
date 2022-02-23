package no.kaicao.learn.pikachudetector.flink.processing.transform;

import no.kaicao.learn.pikachudetector.flink.processing.handler.TFPikachuDetectHandler;
import no.kaicao.learn.pikachudetector.flink.processing.model.ImageDetectionInput;
import no.kaicao.learn.pikachudetector.flink.processing.model.ImageDetectionResult;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;

import java.nio.file.Path;

public class PikachuDetectMapFunction extends RichMapFunction<ImageDetectionInput, ImageDetectionResult> {

  private final static Logger LOG = LoggerFactory.getLogger(PikachuDetectMapFunction.class);

  private final String tensorFlowModelPathString;
  private final double accuracyToDraw;

  private Session tensorFlowSession;

  public PikachuDetectMapFunction(Path tensorFlowModelPath, double accuracyToDraw) {
    this.tensorFlowModelPathString = tensorFlowModelPath.toFile().getAbsolutePath();
    this.accuracyToDraw = accuracyToDraw;
  }

  @Override
  public void open(Configuration parameters) throws Exception {
    super.open(parameters);
    LOG.info("Load Tensorflow model from " + tensorFlowModelPathString);
    SavedModelBundle bundle = SavedModelBundle.load(tensorFlowModelPathString, "serve");
    tensorFlowSession = bundle.session();

    LOG.info("Initialize session for Tensorflow model of " + tensorFlowModelPathString);
    tensorFlowSession.initialize();
  }

  @Override
  public void close() throws Exception {
    super.close();
    if (tensorFlowSession != null) {
      LOG.info("Close session for Tensorflow model of " + tensorFlowModelPathString);
      tensorFlowSession.close();
    }
  }

  @Override
  public ImageDetectionResult map(ImageDetectionInput input) throws Exception {
    if (input == null) {
      return null;
    }
    return new TFPikachuDetectHandler().detect(
        tensorFlowSession,
        input.getImageID(),
        input.getBytes(),
        accuracyToDraw);
  }
}
