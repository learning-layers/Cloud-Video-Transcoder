package de.dbis.services;
import java.io.File;
import java.io.IOException;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

import de.dbis.services.Java2MySql;
import de.dbis.xmpp.PubsubSender;

public class RabbitMQReceive implements Runnable{

    private final static String QUEUE_NAME = "Send";
    private static String server;
	private final static String INPUT_FILE = "RabbitMQ";

    public static void recv() {
    	
    	(new Thread(new RabbitMQReceive())).start();
    }

    public void run() {
    	
    	server = GetProperty.getParam("server", INPUT_FILE);
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
	            String status = Split[3];
	            Java2MySql.VideoUpdate(ID, outputFile.getPath(), URI);
	            PubsubSender.xmpp_send(ID, outputFile.getName(),URI);
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