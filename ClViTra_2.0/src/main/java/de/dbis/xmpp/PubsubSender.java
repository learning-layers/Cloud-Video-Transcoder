package de.dbis.xmpp;

import java.util.UUID;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.SimplePayload;

import de.dbis.services.GetProperty;
import de.dbis.services.Java2MySql;

/**
 * Sends the message to the pubsub node via XMPP message transfer. 
 * Uses 'xmpp.properties' file for configuration.
 *
 */
public class PubsubSender {

	public PubsubSender(){
		
	}
	
	/**
	 * Sends the message to the pubsub node via XMPP message transfer.
	 * The message includes video ID, video name, and video URL.
	 * @param ID Video ID
	 * @param Name Video Name
	 * @param VideoURI Video URL
	 */
	public static void xmpp_send(String ID, String Name, String VideoURI){
		XMPPConnection con = XMPP.getConnection();
		
		String pubsubService, pubsubNode;
		String INPUT_FILE = "xmpp";
		
		pubsubService = GetProperty.getParam("pubsubService", INPUT_FILE);
		pubsubNode = GetProperty.getParam("pubsubNode", INPUT_FILE);
		
		PubSubManager manager = new PubSubManager(con, pubsubService);
		
		String ThumbnailURI = Java2MySql.getThumbnailURI(ID);
		
		StringBuilder buff = new StringBuilder();
		buff.append("<intent xmlns=\"http://dbis.rwth-aachen.de/~hocken/da/xsd/Intent\">");
		buff.append("<component/>");
		buff.append("<sender>clvitra@role-sandbox.eu/Smack</sender>");
		buff.append("<action>ADDED_TO_MPEG7</action>");
		buff.append("<data mime='text/html'>'clvitra@role-sandbox.eu/Smack'</data>");
		buff.append("<categories><category>video</category><category>mpeg7</category></categories>");
		buff.append("<flags><flag>PUBLISH_GLOBAL</flag></flags>");
		buff.append("<extras>");
		buff.append("{\"videoDetails\":\""+Name+"%"+VideoURI+"%"+ThumbnailURI+"\"}");
		//buff.append("{\"videoURL\":\""+VideoURI+"\"}");
		//buff.append("{\"ThumbnailURI\":\""+ThumbnailURI+"\"}");
		buff.append("</extras>");
		buff.append("</intent>");
		
		String xmlPayload = buff.toString(); 
			
			//<flags><flag>PUBLISH_GLOBAL</flag></flags></intent>";
		
		SimplePayload payload = new SimplePayload("intent",
				"http://dbis.rwth-aachen.de/~hocken/da/xsd/Intent",
				xmlPayload);
		
		try {
			// publishing the payload to XMPP
			LeafNode node = (LeafNode) manager.getNode(pubsubNode);
			PayloadItem<SimplePayload> item = new PayloadItem<SimplePayload>(UUID.randomUUID().toString(),payload);
			
			node.publish(item);
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}
}
