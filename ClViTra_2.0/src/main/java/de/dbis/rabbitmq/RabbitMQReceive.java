package de.dbis.rabbitmq;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

import de.dbis.db.Java2MySql;
import de.dbis.mpeg7.sevianno;
import de.dbis.util.GetProperty;
import de.dbis.xmpp.PubsubSender;

/**
 * 
 * Receives the notification from slave nodes through RabbitMQ server once the video is uploaded.
 * Pushes the notification to the client through XMPP. 
 * Uses 'RabbitMQ.properties' file for configuration.
 * Uses 'tempFileLocation.properties' file for configuration.
 *
 */
public class RabbitMQReceive implements Runnable{

    private final static String QUEUE_NAME = "Send";
    private static String server, path;
	private final static String INPUT_FILE = "RabbitMQ";
	private final static String INPUT_FILE_PATH = "tempFileLocation";

    public static void recv() {
    	
    	(new Thread(new RabbitMQReceive())).start();
    }

    /**
     * The notification is received along with videoID, video URL, and video status.
     * The URL and status are then updated in the database for the given ID.
     */
    public void run() {
    	
    	server = GetProperty.getParam("server", INPUT_FILE);
    	path = GetProperty.getParam("location", INPUT_FILE_PATH);
    	ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(server);
        Connection connection;
		try {
			connection = factory.newConnection();
			Channel channel = connection.createChannel();
			channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
	        
	        QueueingConsumer consumer = new QueueingConsumer(channel);
	        channel.basicConsume(QUEUE_NAME, true, consumer);
	        
	        while (true) {
	            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
	            String message = new String(delivery.getBody());
	            String[] Split = message.split("%");
	            String ID = Split[0];
	            File outputFile = new File(Split[1]);
	            String URI = Split[2];
	            String ext = Split[3];
	            String hours = Split[4];
	            String mins = Split[5];
	            String secs = Split[6];
	            String status = Split[7];
	            
	            Java2MySql.VideoUpdate(ID, outputFile.getPath(), URI, hours, mins, secs);
	            
	            String[] Details = Java2MySql.getVideoDetails(ID);
	            String title = Details[0];
	            String thumbnailUri = Details[2];
	            String uploader = Details[4];
	            
	            sevianno.addMediaDescription(URI, thumbnailUri, title, uploader);
	            File file = new File(outputFile.getPath());
	            String Filewoext = FilenameUtils.removeExtension(file.getName());
	    		file.setWritable(true);
	    		boolean a = file.delete();
	    		
	    		File inputfile = new File(path+Filewoext+"."+ext);
	    		inputfile.setWritable(true);
	    		boolean b = inputfile.delete();
	    		
	    		System.out.println("FILE DELETE MP4 ("+ outputFile.getPath() + ") : " + a);
	    		System.out.println("FILE DELETE INPUT ("+ path+Filewoext+"."+ext + ") : " + b);
	    		
	            //PubsubSender.xmpp_send(ID, outputFile.getName(),URI);
	            System.out.println(" [x] Received '" + ID + "  " +status + "'");
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ShutdownSignalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConsumerCancelledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}