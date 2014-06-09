package de.dbis.slave;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import de.dbis.services.GetProperty;

public class RabbitMQSend {

	private final static String QUEUE_NAME = "Send";
	private static String server;
	private final static String INPUT_FILE = "RabbitMQ";

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