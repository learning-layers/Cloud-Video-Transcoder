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
public class ObjectStore {

	/**
	 * Uploads a file to the open stack object store on i5 cloud.
	 * Uses 'objectStore.properties' file for configuration.
	 * @param filepath Filepath to the file that is needed to be uploaded.
	 * @return String URL to the uploaded file.
	 */
	public String ObjectStoreStart(String filepath) {
		System.out.println("TFilepath"+filepath);
		
		return new RetrieveTokenTask().OAuth(filepath);
	}
	
	class RetrieveTokenTask {

		private Exception exception;

		protected String OAuth(String filepath) {
			try {
				String oauth, stringEntity, base;
				String INPUT_FILE = "objectStore";
				String BASE_INPUT_FILE = "base";

				HttpClient client = new DefaultHttpClient();
				
				//base = GetProperty.getParam("uri", BASE_INPUT_FILE);
				//oauth = base + GetProperty.getParam("oauth", INPUT_FILE);
				oauth = GetProperty.getParam("oauth", INPUT_FILE);
				
				System.out.println("oauth: "+oauth);
				stringEntity = GetProperty.getParam("stringEntity", INPUT_FILE);

				HttpPost post = new HttpPost(oauth);

				StringEntity se = new StringEntity(stringEntity);

				post.setEntity(se);
				post.setHeader("Content-type", "application/json");
				
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				System.out.println("chk1");
				String responseBody = client.execute(post, responseHandler);
				System.out.println("chk2");
				System.out.println("ResponseBody: "+responseBody);
				
				JSONObject jsResponse = new JSONObject(responseBody);
		
				// get token
				String publicURL = jsResponse.getString("swift-url");

				String token = jsResponse.getString("X-Auth-Token");

				HashMap<String, String> values = new HashMap<String, String>();

				values.put("id", token);
				System.out.println("OBJ STR TOKEN: "+token);
				values.put("filepath", filepath);
				
				return uploadLastFile(values);
		
			} catch (Exception e) {
				 this.exception = e;
				 return e.toString();
			}
		}
	}
	
	private String uploadLastFile(HashMap<String, String> credentials) {

		String token = credentials.get("id");
		String filepath = credentials.get("filepath");
		return new UploadFileTask().Upload(token, filepath);
	}
	
	class UploadFileTask {

		private Exception exception;

		protected String Upload(String... params) {
			try {
				
				String upload, base;
				String INPUT_FILE = "objectStore";
				String BASE_INPUT_FILE = "base";
				
				HttpClient client = new DefaultHttpClient();
				
				File f = new File(params[1]);
				System.out.println("File Exists: "+f.exists());
				//base = GetProperty.getParam("uri", BASE_INPUT_FILE);
				//upload = base + GetProperty.getParam("upload", INPUT_FILE);
				upload = GetProperty.getParam("upload", INPUT_FILE);
				HttpPut put = new HttpPut(upload + new File(params[1]).getName());

				FileEntity fe = new FileEntity(new File(params[1]), "video/mp4");
				put.setEntity(fe);
				
				put.setHeader("X-Auth-Token", params[0]);
				put.setHeader("Content-type", "application/x-www-form-urlencoded");

				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				 
				String responseBody = client.execute(put, responseHandler);
				responseBody = responseBody.substring(18);
				responseBody = responseBody.substring(0, responseBody.length()- 3);
				return responseBody;

			} catch (Exception e) {
				this.exception = e;
				return e.toString();
			}
		}
	}
}