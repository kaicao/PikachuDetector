package no.kaicao.learn.pikachudetector.flink.processing;

import no.kaicao.learn.pikachudetector.flink.processing.transform.PikachuDetectMapFunction;
import no.kaicao.learn.pikachudetector.flink.processing.transform.VideoDecodeSourceFunction;
import no.kaicao.learn.pikachudetector.flink.processing.transform.VideoEncodeSinkFunction;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.graph.StreamGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PikachuDetectJobExecute {

  private final static Logger LOG = LoggerFactory.getLogger(PikachuDetectJobExecute.class);

  private final Path inputVideoPath;
  private final Path outputVideoPath;
  private final Path tensorFlowTensorFlowModelPath;

  private double accuracyToDraw = 0.95;
  private int flinkUIPort = 10_000;
  private FlinkManager flinkManager;

  public PikachuDetectJobExecute(
      Path inputVideoPath,
      Path outputVideoPath,
      Path tensorFlowTensorFlowModelPath) {
    this.inputVideoPath = inputVideoPath;
    this.outputVideoPath = outputVideoPath;
    this.tensorFlowTensorFlowModelPath = tensorFlowTensorFlowModelPath;
  }

  public static void main(String[] args) throws Exception {
    Path inputVideoFilePath = Paths.get(args[0]);
    Path outputVideoFilePath = Paths.get(args[1]);
    Path tensorFlowModelFilePath = Paths.get(args[2]);

    if (!Files.exists(inputVideoFilePath)) {
      throw new IllegalArgumentException("inputVideoFile does not exist in " + inputVideoFilePath);
    }
    if (!Files.exists(tensorFlowModelFilePath)) {
      throw new IllegalArgumentException("tensorFlowModelFilePath does not exist in " + tensorFlowModelFilePath);
    }

    PikachuDetectJobExecute jobExecute = new PikachuDetectJobExecute(
        inputVideoFilePath,
        outputVideoFilePath,
        tensorFlowModelFilePath);

    LOG.info(String.format("Execute %s job with inputVideo path %s, outputVideo path %s, tensorFlow model path %s",
        PikachuDetectJobExecute.class.getSimpleName(),
        inputVideoFilePath,
        outputVideoFilePath,
        tensorFlowModelFilePath));
    jobExecute.execute();

    jobExecute.shutdown();
    LOG.info(String.format("Finished executing %s", PikachuDetectJobExecute.class.getSimpleName()));

    System.exit(0);
  }

  public void execute() throws Exception {
    flinkManager = new FlinkManager()
        .setFlinkUIPort(flinkUIPort);
    flinkManager.setup();

    PikachuDetectJobExecute job = new PikachuDetectJobExecute(
        inputVideoPath,
        outputVideoPath,
        tensorFlowTensorFlowModelPath);

    LOG.info("Create job for " + PikachuDetectJobExecute.class.getSimpleName());
    StreamExecutionEnvironment environment = flinkManager.createStreamExecutionEnvironment();
    JobGraph jobGraph = job.createJobGraph(environment);
    flinkManager.executeJob(jobGraph);
    LOG.info("Finished execute job for " + PikachuDetectJobExecute.class.getSimpleName());
  }

  public void shutdown() throws Exception {
    if (flinkManager != null) {
      flinkManager.shutdown();
    }
  }

  private JobGraph createJobGraph(StreamExecutionEnvironment environment) {
    environment
        // input
        .addSource(new VideoDecodeSourceFunction(inputVideoPath))
        .uid(VideoDecodeSourceFunction.class.getSimpleName())
        .name(VideoDecodeSourceFunction.class.getSimpleName())
        .setParallelism(1)
        // stream and detect
        .map(new PikachuDetectMapFunction(tensorFlowTensorFlowModelPath, accuracyToDraw))
        .uid(PikachuDetectMapFunction.class.getSimpleName())
        .name(PikachuDetectMapFunction.class.getSimpleName())
        .setParallelism(8)
        // output
        .addSink(new VideoEncodeSinkFunction(outputVideoPath))
        .uid(VideoEncodeSinkFunction.class.getSimpleName())
        .name(VideoEncodeSinkFunction.class.getSimpleName())
        .setParallelism(1);

    StreamGraph streamGraph = environment.getStreamGraph();
    streamGraph.setJobName(PikachuDetectJobExecute.class.getSimpleName());
    return streamGraph.getJobGraph();
  }

  public PikachuDetectJobExecute setFlinkUIPort(int flinkUIPort) {
    this.flinkUIPort = flinkUIPort;
    return this;
  }
}
