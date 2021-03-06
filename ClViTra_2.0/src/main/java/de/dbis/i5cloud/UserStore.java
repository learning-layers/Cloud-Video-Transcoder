package de.dbis.i5cloud;

import java.io.File;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import de.dbis.util.GetProperty;

/**
 * 
 * Uploads the given File to i5 cloud object store.
 *
 */
public class UserStore {

	/**
	 * Uploads a file to the open stack user store on Tethy cloud. This will also be available in owncloud.
	 * Uses 'userStore.properties' file for configuration.
	 * @param bearer_token OIDC authorization bearer token
	 * @param filepath Filepath to the file that is needed to be uploaded.
	 * @return String URL to the uploaded file.
	 */
	public String uploadToUserStore(String bearer_token, String filepath) {
		System.out.println("User Store upload Filepath"+filepath);
		
		return upload(bearer_token, filepath);
	}
	
	//class RetrieveTokenTask {

		private Exception exception;

		protected String upload(String bearer_token, String filepath) {
			try {
				String stringEntity;
				String INPUT_FILE = "userStore";

				HttpClient client = new DefaultHttpClient();
				
				//oauth = GetProperty.getParam("oauth", INPUT_FILE);
				//System.out.println("oauth: "+oauth);
				//stringEntity = GetProperty.getParam("stringEntity", INPUT_FILE);

			//	HttpPost post = new HttpPost(oauth);

			//	StringEntity se = new StringEntity(stringEntity);

			//	post.setEntity(se);
			//	post.setHeader("Content-type", "application/json");
				
			//	ResponseHandler<String> responseHandler = new BasicResponseHandler();
			//	System.out.println("chk1");
			//	String responseBody = client.execute(post, responseHandler);
			//	System.out.println("chk2");
			//	System.out.println("ResponseBody: "+responseBody);
				
			//	JSONObject jsResponse = new JSONObject(responseBody);
		
				// get token
			//	String publicURL = jsResponse.getString("swift-url");

			//	String token = jsResponse.getString("X-Auth-Token");

				HashMap<String, String> values = new HashMap<String, String>();

				values.put("id", bearer_token);
				System.out.println("OBJ STR TOKEN: "+bearer_token);
				//values.put("publicURL", publicURL);
				values.put("filepath", filepath);
				
				return uploadLastFile(values);
		
			} catch (Exception e) {
				 this.exception = e;
				 return e.toString();
			}
		}
	//}
	
	private String uploadLastFile(HashMap<String, String> credentials) {
		//String publicURL = credentials.get("publicURL");
		String token = credentials.get("id");
		String filepath = credentials.get("filepath");
		return new UploadFileTask().Upload(token, filepath);
	}
	
	class UploadFileTask {

		private Exception exception;

		protected String Upload(String... params) {
			try {
				
				String upload;
				String INPUT_FILE = "userStore";

				HttpClient client = new DefaultHttpClient();
				
				File f = new File(params[1]);
				System.out.println("File Exists: "+f.exists());
				upload = GetProperty.getParam("upload", INPUT_FILE);
				System.out.println("File to be uploaded to"+upload);
				HttpPut put = new HttpPut(upload + new File(params[1]).getName());

				FileEntity fe = new FileEntity(new File(params[1]), "video/mp4");
				put.setEntity(fe);
				
				put.setHeader("Authorization", params[0]);
				put.setHeader("Content-type", "application/x-www-form-urlencoded");

				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				 
				String responseBody = client.execute(put, responseHandler);
				System.out.println("Response body =" + responseBody);
				responseBody = responseBody.substring(18);
				responseBody = responseBody.substring(0, responseBody.length()- 3);
				return responseBody;

			} catch (Exception e) {
				this.exception = e;
				return e.toString();
				//return "error!";
			}
		}
	}
}