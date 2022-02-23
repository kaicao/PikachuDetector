package no.kaicao.learn.pikachudetector.flink.processing;

import org.apache.flink.client.program.StreamContextEnvironment;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.runtime.client.JobExecutionException;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.minicluster.MiniCluster;
import org.apache.flink.runtime.minicluster.MiniClusterConfiguration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FlinkManager {

  private static final Logger LOG = LoggerFactory.getLogger(FlinkManager.class);

  private int flinkUIPort;
  private MiniCluster cluster;

  public void setup() throws Exception {
    Configuration configuration = new Configuration();
    if (flinkUIPort > 0) {
      configuration.set(RestOptions.PORT, flinkUIPort);
    }

    MiniClusterConfiguration clusterConfiguration =
        new MiniClusterConfiguration.Builder()
            .setConfiguration(configuration)
            .setNumSlotsPerTaskManager(1)
            .setNumTaskManagers(
                0) // set to 0 to make sure not using local communication checked by
                   // MiniCluster.useLocalCommunication
            .build();
    cluster = new MiniCluster(clusterConfiguration);

    LOG.info("Start Flink MiniCluster");
    cluster.start();
  }

  public void shutdown() throws Exception {
    if (cluster != null) {
      LOG.info("Stop Flink MiniCluster");
      cluster.close();
    }
  }

  public StreamExecutionEnvironment createStreamExecutionEnvironment() {
    return StreamContextEnvironment.createLocalEnvironment();
  }

  public void submitJob(JobGraph jobGraph) {
    if (cluster == null) {
      throw new IllegalStateException("Flink MiniCluster is not available yet");
    }
    if (jobGraph == null) {
      return;
    }

    prepareTaskManagers(jobGraph);
    cluster.submitJob(jobGraph);
  }

  /**
   * Blocking execute the JobGraph.
   *
   * @param jobGraph {@link JobGraph}
   * @throws JobExecutionException Failed when execute the JobGraph
   * @throws InterruptedException Interrupted
   */
  public void executeJob(JobGraph jobGraph) throws JobExecutionException, InterruptedException {
    if (cluster == null) {
      throw new IllegalStateException("Flink MiniCluster is not available yet");
    }
    if (jobGraph == null) {
      return;
    }

    prepareTaskManagers(jobGraph);
    cluster.executeJobBlocking(jobGraph);
  }

  private void prepareTaskManagers(JobGraph jobGraph) {
    int taskManagersCount = jobGraph.getMaximumParallelism();
    try {
      for (int i = 0; i < taskManagersCount; i++) {
        cluster.startTaskManager();
      }
    } catch (Exception e) {
      throw new IllegalStateException("Failed to start task manager to Flink MiniCluster", e);
    }
  }

  public int getFlinkUIPort() {
    return flinkUIPort;
  }

  public FlinkManager setFlinkUIPort(int flinkUIPort) {
    this.flinkUIPort = flinkUIPort;
    return this;
  }

  public FlinkManager withoutUI() {
    this.flinkUIPort = 0;
    return this;
  }

}
