package de.dbis.rabbitmq;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import de.dbis.util.GetProperty;

/**
 * 
 * Sends the video to the RabbitMQ server for transcoding.
 * The video is then forwarded by the RabbitMQ server to any available slave node.
 * Uses 'RabbitMQ.properties' file for configuration.
 *
 */
public class RabbitMQSend {

	private final static String QUEUE_NAME = "Receive";
	private static String server;
	private final static String INPUT_FILE = "RabbitMQ";

	/**
	 * Sends the video to the RabbitMQ server for transcoding.
	 * @param ID VideoID of the video to be sent for transcoding
	 * @throws Exception
	 */
	public static void send(String ID) throws Exception {
		
		server = GetProperty.getParam("server", INPUT_FILE);
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(server);
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		channel.basicPublish("", QUEUE_NAME, null, ID.getBytes());

		channel.close();
		connection.close();
  	}
}