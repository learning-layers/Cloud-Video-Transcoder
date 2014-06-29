package de.dbis.slave;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.ToolFactory;

import de.dbis.services.*;

/**
 * 
 * Transcodes the given video to MP4. Transcoding is done using Xuggler which is a Java wrapper for ffmpeg.
 * Uses 'tempFileLocation.properties' file for configuration.
 *
 */
public class Transcode implements Runnable {
	
	private static String Return_value;
	
	public void run() {
	}
	
	/**
	 * Starts the transcoding for the given video ID.
	 * @param ID video ID
	 */
	public Transcode(String ID) {
		
		String path;
		String INPUT_FILE = "tempFileLocation";
		
        if (ID!=null) {
        	path = GetProperty.getParam("location", INPUT_FILE);

        	String VideoName = Java2MySql.getVideoName(ID);
        	File inputFile = new File(VideoName);
        	String VideoNameWithoutExt = FilenameUtils.removeExtension(inputFile.getName()); 
        			//VideoName.substring(0, VideoName.length()- 4);
        	File outputFile = new File(path + VideoNameWithoutExt+".mp4");
            if (transcoder(inputFile, outputFile)) {
            	
            	ObjectStore ob = new ObjectStore();
    		   	String URI = ob.ObjectStoreStart(outputFile.getPath());
    		   	outputFile.setWritable(true);
    			boolean a = outputFile.delete();
    			System.out.println("FILE DELETE SLAVE: "+a);
    		   	
    		   	
    		   	Return_value= ID+"%"+outputFile.getPath()+"%"+URI+"%"+FilenameUtils.getExtension(inputFile.getName())+"%success";
            }
        }
        try {
			RabbitMQSend.send(Return_value);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void transcode(String ID) {
		
		(new Thread(new Transcode(ID))).start();
	}
	
    public static boolean transcoder(File inputFile, File outputFile) {
        try {
        //Create an IMediaReader using the ToolFactory and the pat to the input file.
        IMediaReader reader = ToolFactory.makeReader(inputFile.getPath());

        //Attach a listener to the reader.  The listener is an IMediaWriter.
        reader.addListener(ToolFactory.makeWriter(outputFile.getAbsolutePath(), reader));

        //outputs it to the given file in the requested format.
        while(reader.readPacket() == null);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}