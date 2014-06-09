package de.dbis.xmpp;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;

import de.dbis.services.GetProperty;

public class XMPP {
	
	
	private static XMPPConnection connection = null;
	private static ConnectionListener connListener = null;
	
	private XMPP(){
		super();
	}
	
	
	public static XMPPConnection getConnection(){
		
		String server, port, service, username, password;
		String INPUT_FILE = "xmpp";
		
		server = GetProperty.getParam("server", INPUT_FILE);
		port = GetProperty.getParam("port", INPUT_FILE);
		service = GetProperty.getParam("service", INPUT_FILE);
		username = GetProperty.getParam("user", INPUT_FILE);
		password = GetProperty.getParam("password", INPUT_FILE);
		
		if(connection == null){

			//XMPPConnection.DEBUG_ENABLED = true;
			XMPPConnection.DEBUG_ENABLED = false;
			
			ConnectionConfiguration connConfiguration = new ConnectionConfiguration(server, Integer.parseInt(port), service);
			connection = new XMPPConnection(connConfiguration);
			
			//configure provider manager for extra Parsers
			configureProviderManager(ProviderManager.getInstance());
			
			try {
				connection.connect();
				
				//login
				connection.login(username, password);
				System.out.println("XMPPClient"+ "Logged in as " + connection.getUser());
			
				configurePacketListeners();
				
				configureFileManager();
				
				
			}catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return connection;
	}
	
	private static void configureProviderManager(ProviderManager pm) {
		
	}
	
	private static void configurePacketListeners(){
		
	} 
	
	
	private static void configureFileManager(){
		FileTransferManager manager = new FileTransferManager(getConnection());    
	}
}
