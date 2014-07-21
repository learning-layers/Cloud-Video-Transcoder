package de.dbis.videoutils;

import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

/**
 * 
 * Returns the video codec of the input file
 *
 */
public class VideoInfo {
	
	/**
	 * Returns the video codec of the input file, irrespective of its extension. 
	 * @param filename video filename
	 * @return String Codec
	 */
    public static String videoInfo(String filename) {
    	// first we create a Xuggler container object
    	IContainer container = IContainer.make();
    	String codec_name = null;
    	// find the stream object
    	// we attempt to open up the container
    	int result = container.open(filename, IContainer.Type.READ, null);
    	// check if the operation was successful
    	if (result<0)
    		throw new RuntimeException("Failed to open media file");
    	
    	if (container.getNumStreams()>0){
    		IStream stream = container.getStream(0);
    		// get the pre-configured decoder that can decode this stream;
    		IStreamCoder coder = stream.getStreamCoder();

    		codec_name = coder.getCodec().getName();
    	}
    	return codec_name;
    }
}
