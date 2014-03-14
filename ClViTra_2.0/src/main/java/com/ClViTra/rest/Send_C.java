package com.ClViTra.rest;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class Send_C {
	

	private final static String QUEUE_NAME = "Receive";
	private static String server;
	private final static String INPUT_FILE = "RabbitMQ";

	public static void send(String ID) throws Exception {
		
		server = GetProperty.getParam("server", INPUT_FILE);
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(server);
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		//String message = "Hello World!";
		channel.basicPublish("", QUEUE_NAME, null, ID.getBytes());
		//System.out.println(" [x] Sent '" + message + "'");
		
		channel.close();
		connection.close();
  	}
}