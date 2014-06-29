package de.dbis.services;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet; 
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 
 * Establishes connection to the MySql database.
 * Uses 'dbconnection.properties' file for configuration.
 *
 */
public class Java2MySql
{
	private final static String INPUT_FILE = "dbconnection";
	private static String url;
	private static String hostName;
	private static String dbName;
	private static String driver;
	private static String userName;
	private static String password;
	private static String databaseServer;
	private static String useUniCode;
	private static String charEncoding;
	private static String charSet;
	private static String collation;
	
	public static void init() {

		driver = GetProperty.getParam("driverName", INPUT_FILE);
		databaseServer = GetProperty.getParam("databaseServer", INPUT_FILE);
		hostName = GetProperty.getParam("hostName", INPUT_FILE);
		dbName = GetProperty.getParam("database", INPUT_FILE);
		userName = GetProperty.getParam("username", INPUT_FILE);
		password = GetProperty.getParam("password", INPUT_FILE);
		useUniCode = GetProperty.getParam("useUniCode", INPUT_FILE);
		charEncoding = GetProperty.getParam("charEncoding", INPUT_FILE);
		charSet = GetProperty.getParam("charSet", INPUT_FILE);
		collation = GetProperty.getParam("collation", INPUT_FILE);
		
		url = "jdbc:" + databaseServer + "://" + hostName + "/";

	}

	/**
	 * DEPRICATED after Open ID Connect integration.
	 * Responsible for doing the login verification. 
	 * @param u_username
	 * @param u_password
	 * @return int code
	 */
	public static int LoginVerification(String u_username, String u_password) {
		
		init();
        boolean authentication = false;
        boolean user_available = false;
        int Return_code;
          
        try {
        	Class.forName(driver).newInstance();
        	Connection conn = DriverManager.getConnection(url+dbName,userName,password);
           
        	Statement st = conn.createStatement();
        	ResultSet res = st.executeQuery("SELECT * FROM  user");
        	while (res.next() && user_available==false) {
        		
        		// Checking for user availability in the database.
        		if(res.getString("username").compareTo(u_username)==0) {
        			
        			// Marking user as available.
        			user_available = true;
        			
        			// Converting the entered password into MD5 hash to become eligible to be compared with stored password. 
        			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        			byte[] array = md.digest(u_password.getBytes());
        			StringBuffer converted_pwd = new StringBuffer();
                  
        			for (int i = 0; i < array.length; ++i) {
        				converted_pwd.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
        			}
        			  
        			// Comparing the entered password with the stored password.
        			if(res.getString("password").compareTo(converted_pwd.toString())==0) {
        				
        				//Marking password to be correct
        				authentication = true;		  
        			}
        		}
        	}
        	conn.close();
        } catch (Exception e) {
        	  e.printStackTrace();
        }
          
    	//Login Successful!         
        if (user_available && authentication) {
        	Return_code = 0;
        }
          
    	//Wrong Password!
        else if (user_available && !authentication) {
        	Return_code = 1;
        }
          
        //User not registered!
        else {
        	Return_code = 2;
        }
        return Return_code;
    }
	
	/**
	 * Returns the corresponding UserId for a given username.
	 * @param username
	 * @return int UserID
	 */
	public static int getUserId(String username) {
		
		init();
		int ID = 0;
		try {
			Class.forName(driver).newInstance();
			Connection conn = DriverManager.getConnection(url+dbName,userName,password);
			
			String insertQuery = "SELECT * FROM  user WHERE username=?";
			PreparedStatement pstmt = conn.prepareStatement(insertQuery);
			pstmt.setString(1, username);
			ResultSet res = pstmt.executeQuery();
  	  
			//if(res.getString("Status").compareTo("INITIALIZED")==0)
			if (res.next())	
				ID = res.getInt("ID");

			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ID;
	}
	
	/**
	 * Updates or enter the database with the video details. 
	 * @param filename
	 * @param ext
	 * @param ThumbnailFilename
	 * @param Duration
	 * @param UserId
	 * @return String Video ID
	 */
	public static String VideoUpdate(String filename, String ext, String ThumbnailFilename, long Duration, int UserId) {
        
		init();
		PreparedStatement pstmt = null;
        int rowCount = 0;
        UUID ID = null;
        
        try {
        	Class.forName(driver).newInstance();
      	  	Connection conn = DriverManager.getConnection(url+dbName,userName,password);
      	  	ID = UUID.randomUUID();

      	  	String insertQuery = "INSERT INTO video (ID, Name, Format, Status, Duration, Thumbnail, UserId) VALUES (?,?,?,?,?,?,?)";
      	  	
      	  	pstmt = conn.prepareStatement(insertQuery);
      	  	pstmt.setString(1, ID.toString());
      	  	pstmt.setString(2, filename);
      	  	pstmt.setString(3, ext);
      	  	if(ext.equals("h264")){
      	  		System.out.println("ext == MP4:  "+ext);
      	  		pstmt.setString(4, "TRANSCODED");
      	  	}
      	  	else{
      	  		System.out.println("ext != MP4/mp4:   "+ext);
      	  		pstmt.setString(4, "INITIALIZED");
      	  	}

      	  	pstmt.setLong(5, Duration);
      	  	pstmt.setString(6, ThumbnailFilename);
      	  	pstmt.setInt(7, UserId);
      	  	System.out.println(ThumbnailFilename);
      	  	rowCount = pstmt.executeUpdate();
      	  	
      	  	conn.close();
        } catch (Exception e) {
        	e.printStackTrace();
        }
		return ID.toString();
	}
	
	/**
	 * Updates the database with the video details.
	 * @param ID
	 * @param outputFile
	 * @param URI
	 * @return
	 */
	public static int VideoUpdate(String ID, String outputFile, String URI) {
        
		init();
		PreparedStatement pstmt = null;
        int rowCount = 0;
        
        try {
        	Class.forName(driver).newInstance();
      	  	Connection conn = DriverManager.getConnection(url+dbName,userName,password);
      	  	String insertQuery = "UPDATE  video SET  Status = ?, Name = ?, Format = ?, URI = ? WHERE  ID = ?";
      	  	
      	  	pstmt = conn.prepareStatement(insertQuery);
      	  	pstmt.setString(1, "TRANSCODED");
      	  	pstmt.setString(2, new File(outputFile).getName());
      	  	pstmt.setString(3, "MP4");
      	  	pstmt.setString(4, URI);
      	  	pstmt.setString(5, ID);
      	  	rowCount = pstmt.executeUpdate();
      	  	
      	  	conn.close();
        } catch (Exception e) {
        	e.printStackTrace();
        }
		return rowCount;
	}

	/**
	 * Returns the first initialized video, from the database, to be transcoded.
	 * @return String Video ID
	 */
	public static String getFirstInitializedVideo() {
	
		init();
		String ID = null;
		try {
			Class.forName(driver).newInstance();
			Connection conn = DriverManager.getConnection(url+dbName,userName,password);
			
			String insertQuery = "SELECT * FROM  video WHERE Status=?";
			PreparedStatement pstmt = conn.prepareStatement(insertQuery);
			pstmt.setString(1, "INITIALIZED");
			ResultSet res = pstmt.executeQuery();
  	  
			//if(res.getString("Status").compareTo("INITIALIZED")==0)
			if (res.next())	
				ID = res.getString("ID");

			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ID;
	}
	
	/**
	 * Sets the status of the video to 'Processing'
	 * @param ID Video ID
	 */
	public static void Processing(String ID) {
		init();
		
		try {
        	Class.forName(driver).newInstance();
      	  	Connection conn = DriverManager.getConnection(url+dbName,userName,password);
      	  	String insertQuery = "UPDATE  video SET  Status = ? WHERE  ID = ?";
      	  	PreparedStatement pstmt = null;
      	  	pstmt = conn.prepareStatement(insertQuery);
      	  	pstmt.setString(1, "PROCESSING");
      	  	pstmt.setString(2, ID);
      	  	pstmt.executeUpdate();
      	  	
      	  	conn.close();
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	/**
	 * Returns the name of the video for the corresponding ID.
	 * @param ID
	 * @return String Name
	 */
	public static String getVideoName(String ID) {
		
		init();
		
		String Name = null;
		try {
			Class.forName(driver).newInstance();
			Connection conn = DriverManager.getConnection(url+dbName,userName,password);
			String insertQuery;

			insertQuery = "SELECT * FROM  video WHERE ID=?";
			PreparedStatement pstmt = conn.prepareStatement(insertQuery);
      	  	
      	  	pstmt.setString(1, ID);
      	  	ResultSet res = pstmt.executeQuery();
      	  	if (res.next())
      	  		Name = res.getString("Name");

			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Name;
	}
	
	/**
	 * Returns the Thumbnail URI for the given video ID.
	 * @param ID
	 * @return String Thumbnail URI
	 */
	public static String getThumbnailURI(String ID) {
		
		init();
		
		String ThumbnailURI = null;
		try {
			Class.forName(driver).newInstance();
			Connection conn = DriverManager.getConnection(url+dbName,userName,password);
			String insertQuery;

			insertQuery = "SELECT * FROM  video WHERE ID=?";
			PreparedStatement pstmt = conn.prepareStatement(insertQuery);
      	  	
      	  	pstmt.setString(1, ID);
      	  	ResultSet res = pstmt.executeQuery();
      	  	if (res.next())
      	  	ThumbnailURI = res.getString("Thumbnail");

			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ThumbnailURI;
	}
	
	/**
	 * Returns a list of video(s) uploaded by the given User having the given status. 
	 * @param UserId
	 * @param status 'Initialized', 'Processing', or 'Transcoded'.
	 * @return List<String> List of Videos with their Name, Thumbnail URL, and Video URL.
	 */
	public static List<String> getVideos(int UserId, String status) {
		
		init();
		
		List<String> myList = new ArrayList<String>();
        //myList.add("java");
		String Thumbnail, Name, URI;
        try {
			Class.forName(driver).newInstance();
			Connection conn = DriverManager.getConnection(url+dbName,userName,password);

			String Query = "SELECT * FROM video WHERE UserId = ? AND Status = ?";
			PreparedStatement pstmt = conn.prepareStatement(Query);
			pstmt.setInt(1, UserId);
			pstmt.setString(2, status);
			ResultSet res = pstmt.executeQuery();
			while (res.next()) {
				URI = res.getString("URI");
				Thumbnail = res.getString("Thumbnail");
				Name = res.getString("Name");
				myList.add(Name);
				myList.add(URI);
				myList.add(Thumbnail);
			}

			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
        return myList;
	}

	/**
	 * Returns the Video URL, Thumbnail URL, Name, and Status for the given video ID uploaded by the given User.
	 * @param UserId
	 * @param videoId
	 * @return String[] Video details.
	 */
	public static String[] getVideoDetails(int UserId, String videoId) {
		
		init();
		
		//ArrayList<String> Details = new ArrayList<String>();
		String Details[]=new String[4];
        //myList.add("java");
		String Thumbnail, Name, URI, Status;
        try {
			Class.forName(driver).newInstance();
			Connection conn = DriverManager.getConnection(url+dbName,userName,password);

			String Query = "SELECT * FROM video WHERE UserId = ? AND ID = ?";
			PreparedStatement pstmt = conn.prepareStatement(Query);
			pstmt.setInt(1, UserId);
			pstmt.setString(2, videoId);
			ResultSet res = pstmt.executeQuery();
			//while (res.next()) {
			if (res.next()){
				
				URI = res.getString("URI");
				Thumbnail = res.getString("Thumbnail");
				Name = res.getString("Name");
				Status = res.getString("Status");
				Details[0]=Name;
				Details[1]=URI;
				Details[2]=Thumbnail;
				Details[3]=Status;
			}
			else{
				Details[0]="Not Found";
			}

			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
        return Details;
	}

	
	
}