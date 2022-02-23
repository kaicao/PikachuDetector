package no.kaicao.learn.pikachudetector.video.handling;

import com.google.common.io.Files;
import io.humble.video.*;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static no.kaicao.learn.pikachudetector.video.handling.VideoImageUtils.readBytesFromImage;

/**
 * @see <a href="https://github.com/artclarke/humble-video/blob/master/humble-video-demos/src/main/java/io/humble/video/demos/DecodeAndPlayVideo.java">DecodeAndPlayVideo</a>
 */
public class VideoDecoderImpl implements VideoDecoder {

  private static final String JPG_FORMAT = "jpg";

  @Override
  public VideoInfo decodeVideo(String videoFilePath) throws IOException, InterruptedException {
    File videoFile = Paths.get(videoFilePath).toFile();
    if (!videoFile.exists() || !videoFile.isFile()) {
      throw new IllegalArgumentException("Invalid video file: " + videoFilePath);
    }

     // Start by creating a container object, in this case a demuxer since
     // we are reading, to get video data from.
    Demuxer demuxer = Demuxer.make();

    // Open demuxer with filename
    demuxer.open(
        videoFile.getAbsolutePath(),
        null,
        false,
        true,
        null,
        null);

    // Construct video decoder from streams of video
    VideoDecoder videoDecoder = initVideoDecoder(demuxer);

    // Open the decoder
    videoDecoder.decoder.open(null, null);

    MediaPicture picture = MediaPicture.make(
        videoDecoder.decoder.getWidth(),
        videoDecoder.decoder.getHeight(),
        videoDecoder.decoder.getPixelFormat());

    MediaPictureConverter converter =
        MediaPictureConverterFactory.createConverter(
            MediaPictureConverterFactory.HUMBLE_BGR_24,
            picture);


    VideoInfo result =
        new VideoInfo()
            .setVideoWidth(videoDecoder.decoder.getWidth())
            .setVideoHeight(videoDecoder.decoder.getHeight())
            .setFileName(videoFile.getName())
            .setFileFormatName(Files.getFileExtension(videoFile.getName()))
            .setPixelFormatName(videoDecoder.decoder.getPixelFormat().name())
            .setCodecID(videoDecoder.decoder.getCodec().getIDAsInt())
            .setStreamStartTimestamp(videoDecoder.streamStartTime)
            .setStreamTimeBaseNumerator(videoDecoder.decoder.getTimeBase().getNumerator())
            .setStreamTimeBaseDenominator(videoDecoder.decoder.getTimeBase().getDenominator());

    MediaPacket packet = MediaPacket.make();
    while(demuxer.read(packet) >= 0) {
       // Now we have a packet, let's see if it belongs to our video stream
      if (packet.getStreamIndex() == videoDecoder.videoStreamID) {
        /*
         * A packet can actually contain multiple sets of samples (or frames of samples
         * in decoding speak).  So, we may need to call decode  multiple
         * times at different offsets in the packet's data.  We capture that here.
         */
        int offset = 0;
        int bytesRead = 0;
        do {
          bytesRead += videoDecoder.decoder.decode(picture, packet, offset);
          if (picture.isComplete()) {
            VideoImage image = extractImageFromVideo(picture, converter);
            result.addImage(image);
          }
          offset += bytesRead;
        } while (offset < packet.getSize());
      }
    }

    // Some video decoders (especially advanced ones) will cache
    // video data before they begin decoding, so when you are done you need
    // to flush them. The convention to flush Encoders or Decoders in Humble Video
    // is to keep passing in null until incomplete samples or packets are returned.
    do {
      videoDecoder.decoder.decode(picture, null, 0);
      if (picture.isComplete()) {
        VideoImage image = extractImageFromVideo(picture, converter);
        result.addImage(image);
      }
    } while (picture.isComplete());

    // It is good practice to close demuxers when you're done to free
    // up file handles. Humble will EVENTUALLY detect if nothing else
    // references this demuxer and close it then, but get in the habit
    // of cleaning up after yourself, and your future girlfriend/boyfriend
    // will appreciate it.
    demuxer.close();

    return result;
  }

  private VideoDecoder initVideoDecoder(Demuxer demuxer) throws IOException, InterruptedException {
    int numStreams = demuxer.getNumStreams();

    // Iterate through the streams to find first video stream
    int videoStreamID = -1;
    long streamStartTime = Global.NO_PTS;
    Decoder videoDecoder = null;

    for (int i = 0; i < numStreams; i ++) {
      DemuxerStream stream = demuxer.getStream(i);
      streamStartTime = stream.getStartTime();
      Decoder currentDecoder = stream.getDecoder();
      if (currentDecoder != null &&
          Objects.equals(MediaDescriptor.Type.MEDIA_VIDEO, currentDecoder.getCodecType())) {
        videoStreamID = i;
        videoDecoder = currentDecoder;
        break;  // Found the video stream, stop here
      }
    }

    if (videoDecoder == null) {
      throw new IOException("Could not find video stream in video " + demuxer.getURL());
    }
    return new VideoDecoder(videoStreamID, streamStartTime, videoDecoder);
  }

  private VideoImage extractImageFromVideo(MediaPicture picture, MediaPictureConverter converter) throws IOException {
    long streamTimestamp = picture.getTimeStamp();
    BufferedImage image = converter.toImage(null, picture);

    return new VideoImage()
        .setBytes(readBytesFromImage(image, JPG_FORMAT))
        .setStreamTimestampNano(streamTimestamp);
  }


  private static class VideoDecoder {
    int videoStreamID = -1;
    long streamStartTime = Global.NO_PTS;
    Decoder decoder;

    public VideoDecoder(int videoStreamID, long streamStartTime, Decoder decoder) {
      this.videoStreamID = videoStreamID;
      this.streamStartTime = streamStartTime;
      this.decoder = decoder;
    }
  }
}
