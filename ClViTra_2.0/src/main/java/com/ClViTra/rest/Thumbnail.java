package com.ClViTra.rest;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;

public class Thumbnail
{
    public static final double SECONDS_BETWEEN_FRAMES = 1;

    private static String inputFilename = null;//"C:/Users/Aarij/Desktop/Performance.mp4";
    private static String outputFilePrefix = null;//"C:/Users/Aarij/Desktop/";
    private static String outputFilename = null;

    // The video stream index, used to ensure we display frames from one and
    // only one video stream from the media container.
    private static int mVideoStreamIndex = -1;

    // Time of last frame write
    private static long mLastPtsWrite = Global.NO_PTS;

    public static final long MICRO_SECONDS_BETWEEN_FRAMES = (long) (Global.DEFAULT_PTS_PER_SECOND * SECONDS_BETWEEN_FRAMES);

    public static String Generate_Thumbnail(String FileLocation, String Filename)
    {
    	inputFilename = FileLocation + Filename;
    	outputFilePrefix = FileLocation;
        long startTime = System.currentTimeMillis();
        long stopTime = 0L;

        IMediaReader mediaReader = ToolFactory.makeReader(inputFilename);

        // stipulate that we want BufferedImages created in BGR 24bit color
        // space

        try
        {
            mediaReader
            .setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
            ImageSnapListener isListener = new ImageSnapListener();
            mediaReader.addListener(isListener);
            // read out the contents of the media file and
            // dispatch events to the attached listener
            while (!isListener.isImageGrabbed())
            {
                mediaReader.readPacket();
            }
            /*
            while (mediaReader.readPacket() == null)
                ;
            */
            //mediaReader.readPacket();
            stopTime = System.currentTimeMillis();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        //System.out.println("Total Time: " + (stopTime-startTime));
		return outputFilename;
    }

    private static class ImageSnapListener extends MediaListenerAdapter
    {
        public boolean imageGrabbed = false;
        public void onVideoPicture(IVideoPictureEvent event)
        {
            if (event.getStreamIndex() != mVideoStreamIndex)
            {
                // if the selected video stream id is not yet set, go ahead an
                // select this lucky video stream
                if (mVideoStreamIndex == -1)
                    mVideoStreamIndex = event.getStreamIndex();
                // no need to show frames from this video stream
                else
                    return;
            }
            // if uninitialized, back date mLastPtsWrite to get the very first
            // frame
            if (mLastPtsWrite == Global.NO_PTS)
                mLastPtsWrite = event.getTimeStamp() - MICRO_SECONDS_BETWEEN_FRAMES;
            // if it's time to write the next frame
            if (event.getTimeStamp() - mLastPtsWrite >= MICRO_SECONDS_BETWEEN_FRAMES)
            {
                outputFilename = dumpImageToFile(event.getImage());
                this.imageGrabbed = true; //set this var to true once an image is grabbed out of the movie.
                // indicate file written
                double seconds = ((double) event.getTimeStamp()) / Global.DEFAULT_PTS_PER_SECOND;
                //System.out.printf("at elapsed time of %6.3f seconds wrote: %s\n",seconds, outputFilename);
                //System.out.printf("at elapsed time of %6.3f seconds wrote: SOMEFILE\n",seconds);
                // update last write time
                mLastPtsWrite += MICRO_SECONDS_BETWEEN_FRAMES;
            }
        }

        private String dumpImageToFile(BufferedImage image)
        {
            try
            {
                String outputFilename = outputFilePrefix + System.currentTimeMillis() + ".jpg";
                //System.out.println("Thumbnail image name is going to be : =====>" + outputFilename);
                ImageIO.write(image, "jpg", new File(outputFilename));
                return outputFilename;
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return null;
            }
        }

        public boolean isImageGrabbed() {
            return imageGrabbed;
        }
    }
}