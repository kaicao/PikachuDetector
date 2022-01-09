package no.kaicao.learn.pikachudetector.flink.processing;

import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.Shape;
import ai.djl.tensorflow.engine.TfNDManager;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileBytesTranslator implements Translator<Path, DetectedObjects> {

  @Override
  public NDList processInput(TranslatorContext ctx, Path filePath) throws Exception {
    byte[] input = Files.readAllBytes(filePath);
    TfNDManager manager = (TfNDManager)ctx.getNDManager();
    NDArray array = manager.createStringTensor(new Shape(1), ByteBuffer.wrap(input));
    array.setName("tf_example:0");
    return new NDList(array);
  }

  @Override
  public DetectedObjects processOutput(TranslatorContext ctx, NDList list) throws Exception {
    // TODO
    return null;
  }
}
