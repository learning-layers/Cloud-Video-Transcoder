package de.dbis.slave;

import java.io.File;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.ToolFactory;

import de.dbis.db.Java2MySql;
import de.dbis.i5cloud.ObjectStore;
import de.dbis.i5cloud.UserStore;
import de.dbis.services.*;
import de.dbis.util.DateTimeUtils;
import de.dbis.util.GetProperty;

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
	public Transcode(String IDandToken) {
		
		String[] IDandTokenList = IDandToken.split("\\?");
		String path;
		String INPUT_FILE = "tempFileLocation";
		
        if (IDandTokenList[0]!=null) {
        	path = GetProperty.getParam("location", INPUT_FILE);

        	String VideoName = Java2MySql.getVideoName(IDandTokenList[0]);
        	File inputFile = new File(VideoName);
        	String VideoNameWithoutExt = FilenameUtils.removeExtension(inputFile.getName()); 
        			//VideoName.substring(0, VideoName.length()- 4);
        	File outputFile = new File(path + VideoNameWithoutExt+".mp4");
        	
        	Date startDate = DateTimeUtils.currentTime();
            if (transcoder(inputFile, outputFile)) {
            	
            	Date endDate = DateTimeUtils.currentTime();
            	
            	String[] timeDiff = DateTimeUtils.printDifference(startDate, endDate);
            	ObjectStore ob = new ObjectStore();
    		   	String URI = ob.ObjectStoreStart(outputFile.getPath());
    		   	
    		   	//upload to user storage
			   	//UserStore us = new UserStore();
			   	//String bearer_token = "Bearer "+IDandTokenList[1];
			   	//us.uploadToUserStore(bearer_token, outputFile.getPath());
    		   	
    		   	outputFile.setWritable(true);
    			boolean a = outputFile.delete();
    			System.out.println("FILE DELETE SLAVE: "+a);
    		   	
    		   	
    		   	Return_value= IDandTokenList[0]+"%"+outputFile.getPath()+"%"+URI+"%"+FilenameUtils.getExtension(inputFile.getName())+
    		   			"%"+timeDiff[0]+"%"+timeDiff[1]+"%"+timeDiff[2]+"%success";
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