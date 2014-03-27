package de.dbis.slave;

import java.io.File;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.ToolFactory;

import de.dbis.services.*;

public class Transcode implements Runnable {
	
	private static String Return_value;
	
	public void run() {
	}
	
	public Transcode(String ID) {
		
		String path;
		String INPUT_FILE = "tempFileLocation";
		
        if (ID!=null) {
        	path = GetProperty.getParam("location", INPUT_FILE);

        	String VideoName = Java2MySql.getVideoName(ID);
        	File inputFile = new File(VideoName);
        	String VideoNameWithoutExt = VideoName.substring(0, VideoName.length()- 4);
        	File outputFile = new File(path + VideoNameWithoutExt.substring(30)+".mp4");
            if (transcoder(inputFile, outputFile)) {
            	
            	ObjectStore ob = new ObjectStore();
    		   	String URI = ob.ObjectStoreStart(outputFile.getPath());
    		   	Return_value= ID+"%"+outputFile.getPath()+"%"+URI+"%success";
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