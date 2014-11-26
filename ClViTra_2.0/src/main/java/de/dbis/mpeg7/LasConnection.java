/* ATLAS Group - Virtual Campfire - www.dbis.rwth-aachen.de
 * Copyright © 2010-2012 Lehrstuhl Informatik V, RWTH Aachen, Germany. All Rights Reserved.
 */
package de.dbis.mpeg7;

import i5.las.httpConnector.client.AccessDeniedException;
import i5.las.httpConnector.client.AuthenticationFailedException;
import i5.las.httpConnector.client.Client;
import i5.las.httpConnector.client.ConnectorClientException;
import i5.las.httpConnector.client.NotFoundException;
import i5.las.httpConnector.client.ServerErrorException;
import i5.las.httpConnector.client.TimeoutException;
import i5.las.httpConnector.client.UnableToConnectException;

import java.util.Date;
import java.util.ResourceBundle;


/**
 * The Class LasConnection.
 */
public class LasConnection {

	/** The con. */
	private static LasConnection con = null;
	
	
	/** The Constant CONNECTION_PROBLEM. */
	public static final String CONNECTION_PROBLEM = "An error accoured when connectiong to web please check your internet conenction settings";
	
	/** The Constant AUTHENTICATION_PROBLEM. */
	public static final String AUTHENTICATION_PROBLEM = "wrong username or password, please try again";
	
	/** The Constant UNDEFINED_PROBLEM. */
	public static final String UNDEFINED_PROBLEM = "an exception occured, please try again";
	
	
	// FIELDS -------------------------------------------------------------------
	/** The client. */
	private Client                           client;
	
	/** The error code. */
	private String                           errorCode;
	
	/** The connected. */
	private boolean                          connected;
	
	/** The basedir. */
	private String                           basedir;
	
	/**
	 * Gets the connection.
	 *
	 * @return the connection
	 */
	public static LasConnection getConnection(){
		if(con == null){
			con = new LasConnection();
		}
		
		return con;
	}

	// CONSTRUCTORS -------------------------------------------------------------
	/**
	 * Creates a new instance of this class with empty values.
	 *
	 */
	public LasConnection() {
		this.errorCode = new String();
		this.connected = false;

	}

	// GETTERS ------------------------------------------------------------------
	/**
	 * Checks wheter a LAS connection is active and invokes the YouTell Story Service
	 * method {@code continueConnection()}.
	 * 
	 * @return the status of the LAS connection.
	 */
	public boolean isConnected() {
		try {
	//		storyService.continueConnection();
		} catch (NullPointerException e) {
			System.err.println(e.getMessage());
			return false;
		}

		return this.connected;
	}

	/**
	 * Determins the error code.
	 * 
	 * @return the error code or an empty string.
	 */
	public String getErrorCode() {
		String err = this.errorCode;

		return err;
	}

	/**
	 * Returns the connector client.
	 * 
	 * @return the client.
	 */
	public Client getClient() {
		return client;
	}
	
	/**
	 * Gets the basedir.
	 *
	 * @return the basedir
	 */
	public String getbasedir() {
		return basedir;
	}
	

	// METHOD -------------------------------------------------------------------
	/**
	 * Invokes the specified LAS service method.
	 * 
	 * @param service the service.
	 * @param method the method.
	 * @param params the parameter list.
	 * @return the object returned by the service method.
	 */
	public Object invoke(String service, String method, Object ... params) {
		final String disconnected = "ERR4"; //$NON-NLS-1$
		final String accessDenied = "ERR6"; //$NON-NLS-1$
		final String unknownError = "ERR7"; //$NON-NLS-1$
		final String serverError = "ERR8"; //$NON-NLS-1$
		final String unkownMethod = "ERR9"; //$NON-NLS-1$

		this.errorCode = new String();

		try {
			Date before = new Date();
			Object res = client.invoke(service, method, params);
			Date after = new Date();

			String s = (after.getTime() - before.getTime()) + "ms: " + service + "." + method;
			System.out.println(s);

			return res;
		} catch (AccessDeniedException e) {
			this.errorCode = accessDenied;
			e.printStackTrace();
		} catch (AuthenticationFailedException e) {
			this.errorCode = accessDenied;
			e.printStackTrace();
		} catch (UnableToConnectException e) {
			this.errorCode = disconnected;
			this.connected = false;
			e.printStackTrace();
		} catch (ServerErrorException e) {
			this.errorCode = serverError;
			this.connected = false;
			e.printStackTrace();
		} catch (TimeoutException e) {
			this.errorCode = serverError;
			this.connected = false;
			e.printStackTrace();
		} catch (NotFoundException e) {
			this.errorCode = unkownMethod;
			e.printStackTrace();
		} catch (ConnectorClientException e) {
			this.errorCode = serverError;
			this.connected = false;
			e.printStackTrace();
		} catch (Exception e) {
			this.errorCode = unknownError;
			e.printStackTrace();
		}

		return null;
	}

	//  /**
	//   * Invokes a service method without parameters.
	//   * 
	//   * @param service the service name.
	//   * @param method the method name.
	//   * @return the return value of the invoked method.
	//   */
	//  public Object invoke(String service, String method) {
	//    return invoke(service, method, null);
	//  }

	/**
	 * Sets the error code.
	 *
	 * @param err the new error code
	 */
	public void setErrorCode(String err) {
		this.errorCode = err;
	}
	
	
	/*
	 * private function addMediaDescription(mUri:String,description:String,keywords:String):void{
    var nKeywords:Array = seperateStringByComma(keywords);
    callLasService("mpeg7_multimediacontent_service", "addMediaDescription",[mUri,description,nKeywords,[""]],addMediaDescriptionHandler,ioErrorHandler);
   }
	 * */
	
	
	
	/**
	 * Exists media description.
	 *
	 * @param params the params
	 * @return true, if successful
	 */
	public boolean existsMediaDescription(Object [] params){
		
		boolean resp = false;
		try {
			
			resp = (Boolean) this.invoke("mpeg7_multimediacontent_service","existsMediaDescription", params);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(resp){
			System.out.println("Media uid "+params[0]+ "is already exists");
			return true;
		}else{
			return false;
		}
		
	}
	
	/**
	 * Adds the media description.
	 *
	 * @param params the params
	 */
	public String addMediaDescription(Object [] params){
		String resp = null;
		
		try {
			
			resp = (String) this.invoke("mpeg7_multimediacontent_service",  "addMediaDescription", params);
			System.out.println("Response:"+resp);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return resp;
	}
	
	
	/**
	 * Set the media creation title.
	 *
	 * @param params the params
	 */
	public boolean setMediaCreationTitle(Object [] params){
		boolean resp = false;
		
		try {
			
			resp = (Boolean) this.invoke("mpeg7_multimediacontent_service",  "setMediaCreationTitle", params);
			System.out.println("Response:"+resp);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return resp;
	}
	
	
	/*callLasService("mpeg7_multimediacontent_service", "setAudioVisualSegments", [mediaURI,vsSemanticRefs,vsAllocations,vsTimePoints], setAudioVisualSegmentsHandler, ioErrorHandler);
setAudioVisualSegments*/
	/**
	 * Sets the audio visual segments.
	 *
	 * @param params the new audio visual segments
	 */
	public void setAudioVisualSegments(Object [] params){
		boolean resp = (Boolean) this.invoke("mpeg7_multimediacontent_service","setAudioVisualSegments", params);
		if(resp){
			System.out.println("setAudioVisualSegments is succesful");
		}else{
			System.out.println("setAudioVisualSegments is NOT succesful");
		}
		
	}
	
	
	
	
/*
	Story generateStoryData(String xml) {
		if (xml == null || xml.length() == 0) {
			System.out.println("NO XML FOUND");
			return null;
		}

		final String noMedium = "noMedium"; //$NON-NLS-1$
		Story s = null;

		try {
			s = new Story(xml, basedir);
			String imgUri = s.getImageUri();

			if (imgUri.indexOf(noMedium) != -1) {
				String mediaUri = new String();
				int pos = 1;
				do {

					mediaUri = storyService.getStoryMediaUri(s.getId(),
							String.valueOf(pos));
					s.computeImageUri(mediaUri);
					imgUri = s.getImageUri();
					pos++;
				} while (mediaUri.length() != 0 && imgUri.indexOf(noMedium) != -1);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return s;
	}
*/
	/**
 * Establishes a connection with the LAS server for the specified user. If the
 * connection cannot be established an error code will be returned:
 * <ul>
 * <li>{@code ERR15} - the port number could not be parse.</li>
 * <li>{@code -1} - Client could not connect to LAS.</li>
 * <li>{@code -2} - User could not be authenticated.</li>
 * <li>{@code -3} - Any other error occurred when connecting.</li>
 * </ul>
 *
 * @param username the username
 * @param password the user's password.
 * @return the session ID or an error code as specified above.
 */
	public String connect(String username,String password) {
		final String configFileErr = "ERR15"; //$NON-NLS-1$
		//ResourceBundle b;
		//b = ResourceBundle.getBundle("peseWebClient"); //$NON-NLS-1$

		String lasHostname;
		int lasPort;
		long timeOut;
		try {
			lasHostname = "steen.informatik.rwth-aachen.de"; //$NON-NLS-1$
			lasPort = 9914; //$NON-NLS-1$
			timeOut = 100;

		} catch (Exception e) {
			return configFileErr;
		}

		System.out.println("LAS Hostname: " + lasHostname);
		System.out.println("LAS Port: " + lasPort);

		//userService.setCurrentUser(new User());
		
		client = new Client(lasHostname, lasPort, timeOut*60000, username,password);
		//client.getMobileContext().setApplicationCode("AnViAnno");
		
		System.out.println("CLIENT TIMEOUT: "+client.getTimeoutMs());
		try {
			
			
			client.connect();
			//client.setApplicationCode("AndroFire");		
			String user = client.getUser();
			
			/*
			long userId = (Long) this.invoke("usermanager", "getUserId",
					new Object [] {user});

			System.out.println("Receiving user data");
			
			userService.setCurrentUser(userService.getUserData(String.valueOf(userId)));
			User currentUser = userService.getCurrentUser();
		
			if (currentUser != null) {
				currentUser.setId(String.valueOf(userId));
				currentUser.setLogin(user);
				this.connected = true;
			}
			*/

		} catch (UnableToConnectException e) {
			e.printStackTrace();
			this.connected = false;
			return CONNECTION_PROBLEM;
		} catch (AuthenticationFailedException e) {
			e.printStackTrace();
			this.connected = false;
			return AUTHENTICATION_PROBLEM;
		} catch (Exception e) {
			e.printStackTrace();
			this.connected = false;
			return UNDEFINED_PROBLEM;
		}

		try {
			String appCode = "vc"; //$NON-NLS-1$
			String constraints = "v2"; //$NON-NLS-1$

			Object [] params = {appCode, constraints};

			this.invoke("xmldbxs-context-service", "instantiateContext", params);
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}

		return client.getSessionId();
	}

	
	/**
	 * Establishes a connection with the LAS server for the specified user. If the
	 * connection cannot be established an error code will be returned:
	 * <ul>
	 * <li>{@code ERR15} - the port number could not be parse.</li>
	 * <li>{@code -1} - Client could not connect to LAS.</li>
	 * <li>{@code -2} - User could not be authenticated.</li>
	 * <li>{@code -3} - Any other error occurred when connecting.</li>
	 * </ul>
	 *
	 * @param username the username
	 * @param password the user's password.
	 * @return the session ID or an error code as specified above.
	 */
		public String connect(String sessionId) {
			final String configFileErr = "ERR15"; //$NON-NLS-1$
			//ResourceBundle b;
			//b = ResourceBundle.getBundle("peseWebClient"); //$NON-NLS-1$

			String lasHostname;
			int lasPort;
			long timeOut;
			try {
				lasHostname = "steen.informatik.rwth-aachen.de"; //$NON-NLS-1$
				lasPort = 9914; //$NON-NLS-1$
				timeOut = 100;

			} catch (Exception e) {
				return configFileErr;
			}

			System.out.println("LAS Hostname: " + lasHostname);
			System.out.println("LAS Port: " + lasPort);

			//userService.setCurrentUser(new User());
			
			client = new Client(lasHostname, lasPort);
			//client.getMobileContext().setApplicationCode("AnViAnno");
			//((Client) client).setApplicationCode("vc")
			//setApplicationCode("vc");
			
			System.out.println("CLIENT TIMEOUT: "+client.getTimeoutMs());
			String user = null;
			try {
				
				//client.setSessionId(sessionId);
				//user = client.getUser();
				
				/*
				long userId = (Long) this.invoke("usermanager", "getUserId",
						new Object [] {user});

				System.out.println("Receiving user data");
				
				userService.setCurrentUser(userService.getUserData(String.valueOf(userId)));
				User currentUser = userService.getCurrentUser();
			
				if (currentUser != null) {
					currentUser.setId(String.valueOf(userId));
					currentUser.setLogin(user);
					this.connected = true;
				}
				*/

			} catch (Exception e) {
				e.printStackTrace();
				this.connected = false;
				return UNDEFINED_PROBLEM;
			}

			try {
				
				String appCode = "vc"; //$NON-NLS-1$
				String constraints = "v2"; //$NON-NLS-1$

				Object [] params = {appCode, constraints};

				this.invoke("xmldbxs-context-service", "instantiateContext", params);
				

			} catch (Exception e) {
				e.printStackTrace();
			}

			return sessionId;
		}
	
	/**
	 * Disconnect.
	 *
	 * @return true, if successful
	 */
	public boolean disconnect() {
		try {
			client.disconnect();
			this.connected = false;
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	
}
