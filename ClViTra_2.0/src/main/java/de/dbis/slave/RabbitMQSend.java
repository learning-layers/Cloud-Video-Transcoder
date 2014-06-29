package de.dbis.slave;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import de.dbis.services.GetProperty;

/**
 * 
 * Sends the notification to the server when the video is transcoded.
 * Uses 'RabbitMQ.properties' file for configuration.
 *
 */
public class RabbitMQSend {

	private final static String QUEUE_NAME = "Send";
	private static String server;
	private final static String INPUT_FILE = "RabbitMQ";

	/**
	 * Sends the notification to the server when the video is transcoded.
	 * Along with the notification it also sends videoID, video URL, and video status.
	 * @param status video status from the transcoder.
	 * @throws Exception
	 */
	public static void send(String status) throws Exception {
      	      
		server = GetProperty.getParam("server", INPUT_FILE);
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(server);
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		channel.basicPublish("", QUEUE_NAME, null, status.getBytes());
		
		channel.close();
		connection.close();
	}
}	