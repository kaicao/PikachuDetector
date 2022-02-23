package no.kaicao.learn.pikachudetector.video.handling;

import java.io.IOException;

public interface VideoEncoder {

  void encodeVideo(VideoInfo videoInfo, String targetFolder) throws IOException, InterruptedException;
}
