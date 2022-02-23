package no.kaicao.learn.pikachudetector.video.handling;

import io.humble.video.*;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static no.kaicao.learn.pikachudetector.video.handling.VideoImageUtils.convertImageType;
import static no.kaicao.learn.pikachudetector.video.handling.VideoImageUtils.readImageFromBytes;

public class VideoEncoderImpl implements VideoEncoder {

  private static final Logger LOG = LoggerFactory.getLogger(VideoEncoderImpl.class);
  private static final long ONE_SECOND_TO_NANO = 1_000_000_000L;//TimeUnit.SECONDS.toNanos(1L);
  private static final long ONE_MS_IN_NANO = TimeUnit.MILLISECONDS.toNanos(1L);

  @Override
  public void encodeVideo(VideoInfo videoInfo, String targetFolder)
      throws IOException, InterruptedException {

    Path folderPath = Paths.get(targetFolder);
    if (!Files.exists(folderPath)) {
      Files.createDirectories(folderPath);
    }
    Path videoFilePath = folderPath.resolve(videoInfo.getFileName());
    Files.deleteIfExists(videoFilePath);

    // First, create a muxer with specified file name and format name
    Muxer muxer = Muxer.make(
        videoFilePath.toFile().getAbsolutePath(),
        null,
        videoInfo.getFileFormatName());

    boolean muxerOpened = false;
    try {
      // Determine codec to use to encode video.
      MuxerFormat format = muxer.getFormat();
      Codec codec = Codec.findDecodingCodecByIntID(videoInfo.getCodecID());
      if (codec == null || !codec.canEncode()) {
        codec = Codec.findEncodingCodec(format.getDefaultVideoCodecId());
        if (codec == null || !codec.canEncode()) {
          throw new IllegalStateException("Unknown Codec with ID " + videoInfo.getCodecID() + " nor there is working fallback codec " + format.getDefaultVideoCodecId());
        }
      }

      Rational systemTimeBase = Rational.make(1, 24);
      Rational streamTimeBase =
          Rational.make(
              videoInfo.getStreamTimeBaseNumerator(), videoInfo.getStreamTimeBaseDenominator());

      // Init encoder, with at least definition of:
      // - width
      // - height
      // - pixel format
      Encoder encoder = Encoder.make(codec);
      PixelFormat.Type pixelFormat = PixelFormat.Type.PIX_FMT_YUV420P;
      encoder.setWidth(videoInfo.getVideoWidth());
      encoder.setHeight(videoInfo.getVideoHeight());
      encoder.setPixelFormat(pixelFormat);
      encoder.setTimeBase(systemTimeBase);
      // Some formats need global headers, which need to tell encoder
      if (format.getFlag(MuxerFormat.Flag.GLOBAL_HEADER)) {
        encoder.setFlag(Encoder.Flag.FLAG_GLOBAL_HEADER, true);
      }

      // Open the encoder
      encoder.open(null, null);

      // include encoder stream to muxer
      muxer.addNewStream(encoder);

      // Open muxer
      muxer.open(null, null);
      muxerOpened = true;

      MediaPicture picture =
          MediaPicture.make(encoder.getWidth(), encoder.getHeight(), pixelFormat);
      picture.setTimeBase(systemTimeBase);

      // Calculate time BEFORE encoding
      long systemStartTime = System.nanoTime();
      // Reusable MediaPacket, to avoid unnecessary reallocating
      MediaPacket packet = MediaPacket.make();
      // Begin loop to encode video
      for (int i = 0; i < videoInfo.getImages().size(); i++) {
        VideoImage videoImage = videoInfo.getImages().get(i);
        encodeVideoImage(
            videoInfo,
            videoImage,
            i,
            muxer,
            encoder,
            packet,
            picture,
            systemStartTime,
            systemTimeBase,
            streamTimeBase);
      }

      /* Encoders, like decoders, sometimes cache pictures so it can do the right key-frame optimizations.
       * So, they need to be flushed as well. As with the decoders, the convention is to pass in a null
       * input until the output is not complete.
       */
      do {
        encoder.encode(packet, null);
        if (packet.isComplete()) muxer.write(packet, false);
      } while (packet.isComplete());
    } finally {
      if (muxerOpened) {
        // Finally close muxer
        muxer.close();
      }
    }
  }

  private void encodeVideoImage(
      VideoInfo videoInfo,
      VideoImage videoImage,
      int imageIndex,
      Muxer muxer,
      Encoder encoder,
      MediaPacket packet,
      MediaPicture picture,
      long systemStartTimestamp,
      Rational systemTimeBase,
      Rational streamTimeBase)
      throws IOException, InterruptedException {
    BufferedImage image = readImageFromBytes(videoImage.getBytes());
    image = convertImageType(image, BufferedImage.TYPE_3BYTE_BGR);

    // ensure same format of image as encoder
    MediaPictureConverter converter = MediaPictureConverterFactory.createConverter(image, picture);
    converter.toPicture(picture, image, imageIndex);

    // manually make it wait for roughly 42ms in between image,
    // until waitInBetweenImage is able to calculate correct wait in between
    long waitTime = (long) (systemTimeBase.getValue() * TimeUnit.SECONDS.toNanos(1)) - ONE_SECOND_TO_NANO;
    TimeUnit.NANOSECONDS.sleep(waitTime);
    //waitInBetweenImage(videoInfo, videoImage, imageIndex, systemStartTimestamp, systemTimeBase, streamTimeBase);

    // finally write the packet
    do {
      encoder.encode(packet, picture);
      if (packet.isComplete()) muxer.write(packet, false);
    } while (packet.isComplete());
  }

  // TODO this wait is not correct, need to calculate out as 24 images per second, thus wait around 41.6666ms in between
  private void waitInBetweenImage(VideoInfo videoInfo, VideoImage videoImage, int imageIndex, long systemStartTimestamp, Rational systemTimeBase, Rational streamTimeBase) throws InterruptedException {
    // wait for the time of image
    long streamStartTimestamp = videoInfo.getStreamStartTimestamp();
    long streamTimestamp = videoImage.getStreamTimestampNano();
    // convert streamTimestamp into system units (e.g. nano-seconds)
    long streamTimestampRescaledToSystemTime =
        systemTimeBase.rescale(streamTimestamp - streamStartTimestamp, streamTimeBase);

    // get current clock time
    long systemTimestamp = System.nanoTime();
    // Loop in a sleeping loop until it gets within 1ms of time for that video frame.
    // TODO 1ms is not that accurate for real video player
    long systemTimeElapsed = systemTimestamp - systemStartTimestamp;
    long waitTime = streamTimestampRescaledToSystemTime - (systemTimeElapsed + ONE_MS_IN_NANO); // with 1ms threshold
    if (waitTime > 0L) {
      LOG.info("Wait " + TimeUnit.NANOSECONDS.toMillis(waitTime) + " ms, for image " + imageIndex + " streamtime " + streamTimestamp);
      TimeUnit.NANOSECONDS.sleep(waitTime);
    } else {
      LOG.info("Invalid wait time " + waitTime + " for image " + imageIndex);
    }
  }
}
