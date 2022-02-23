package no.kaicao.learn.pikachudetector.video.handling;

import java.io.IOException;

public interface VideoDecoder {

  VideoInfo decodeVideo(String videoFilePath) throws IOException, InterruptedException;
}
