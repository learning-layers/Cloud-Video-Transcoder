package de.dbis.services;

import java.io.File;
import java.util.HashMap;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

public class ObjectStore {

	public String ObjectStoreStart(String filepath) {
		
		return new RetrieveTokenTask().OAuth(filepath);
	}
	
	class RetrieveTokenTask {

		private Exception exception;

		protected String OAuth(String filepath) {
			try {
				String oauth, stringEntity;
				String INPUT_FILE = "objectStore";

				HttpClient client = new DefaultHttpClient();
				
				oauth = GetProperty.getParam("oauth", INPUT_FILE);
				stringEntity = GetProperty.getParam("stringEntity", INPUT_FILE);

				HttpPost post = new HttpPost(oauth);

				StringEntity se = new StringEntity(stringEntity);

				post.setEntity(se);
				post.setHeader("Content-type", "application/json");
				
				ResponseHandler responseHandler = new BasicResponseHandler();
				String responseBody = client.execute(post, responseHandler);

				JSONObject jsResponse = new JSONObject(responseBody);
		
				// get token
				String publicURL = jsResponse.getString("swift-url");

				String token = jsResponse.getString("X-Auth-Token");

				HashMap<String, String> values = new HashMap<String, String>();

				values.put("id", token);
				//values.put("publicURL", publicURL);
				values.put("filepath", filepath);
				
				return uploadLastFile(values);
		
			} catch (Exception e) {
				this.exception = e;
				return "error!";
			}
		}
	}
	
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
				String INPUT_FILE = "objectStore";

				HttpClient client = new DefaultHttpClient();
				
				upload = GetProperty.getParam("upload", INPUT_FILE);
				HttpPut put = new HttpPut(upload + new File(params[1]).getName());

				FileEntity fe = new FileEntity(new File(params[1]), "video/mp4");
				put.setEntity(fe);
				
				put.setHeader("X-Auth-Token", params[0]);
				put.setHeader("Content-type", "application/x-www-form-urlencoded");

				ResponseHandler responseHandler = new BasicResponseHandler();
				String responseBody = client.execute(put, responseHandler);
				responseBody = responseBody.substring(18);
				responseBody = responseBody.substring(0, responseBody.length()- 3);
				return responseBody;

			} catch (Exception e) {
				this.exception = e;
				return "error!";
			}
		}
	}
}