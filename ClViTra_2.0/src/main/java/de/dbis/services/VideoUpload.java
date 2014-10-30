package de.dbis.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.xuggle.xuggler.IContainer;

import de.dbis.db.Java2MySql;
import de.dbis.i5cloud.ObjectStore;
//import de.dbis.oidc.Neo4j;
import de.dbis.rabbitmq.*;
import de.dbis.util.CORS;
import de.dbis.util.GetProperty;
import de.dbis.videoutils.Thumbnail;
import de.dbis.videoutils.VideoInfo;

/**
 * 
 * Uploads the File to server.
 * Uses 'tempFileLocation.properties' file for configuration.
 */

@Path("/upload")
@Component
public class VideoUpload
{
	@Context UriInfo uriInfo;
	private String _corsHeaders;

	@OPTIONS
	public Response corsResource(@HeaderParam("Access-Control-Request-Headers") String requestH) {
		_corsHeaders = requestH;
		return CORS.makeCORS(Response.ok(), requestH);
	}
	
	/**
	 * Uploads the file from multipart form to ClViTra v2.0 server.
	 * @param User sent as simple string.
	 * @param uploadedInputStream sent as InputStream.
	 * @param fileDetail complete file path.
	 * @return javax.ws.rs.core.Response status code along with video ID that was uploaded.
	 */
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(@HeaderParam("Token") String token,
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) {
			   
		System.out.println("Token: " + token);
		
		String User = verifyAccessToken(token);
		
		
		String uploadPath, savePath; //uploadCode;
		String INPUT_FILE = "tempFileLocation";
		//String INPUT_FILE_UploadCode = "FileUpload";
			
		uploadPath = GetProperty.getParam("location", INPUT_FILE);
		savePath = GetProperty.getParam("savePath", INPUT_FILE);
					
		String uploadedFileLocation = uploadPath + fileDetail.getFileName();
		
		String newName = fileDetail.getFileName();
		newName = newName.replaceAll(" ", "_");
		newName = User+"_"+newName;
		
		if(Java2MySql.Exists(User,newName)){
			ResponseBuilder x = Response.status(406);
			return CORS.makeCORS(x, _corsHeaders);
		}
		File newFile = new File(uploadPath +newName);
		
		
		uploadedFileLocation = uploadPath + newFile.getName();

		// saving the file
		writeToFile(uploadedInputStream, uploadedFileLocation);
			
		// Generating Thumbnail
		String ThumbnailFilename = Thumbnail.Generate_Thumbnail(uploadPath, newFile.getName());

		System.out.println("TURI: "+ThumbnailFilename);
		//Get the duration of the video
		long Duration = getDuration(uploadedFileLocation);
			
		//String ext = FilenameUtils.getExtension(newFile.getName());
		System.out.println("DB");
		
		String Codec = VideoInfo.videoInfo(uploadedFileLocation);
		String ID;
		ID = Java2MySql.VideoUpdate(savePath+newFile.getName(), Codec, ThumbnailFilename, Duration, User);
		/*String location = "Kastanienweg Aachen"; 
		Double latitude = 50.785097, longitude = 6.053766;
		
		if (latitude!=0 && longitude!=0)
			ID = Java2MySql.VideoUpdate(savePath+newFile.getName(), Codec, ThumbnailFilename, Duration, User, latitude, longitude);
		else
			ID = Java2MySql.VideoUpdate(savePath+newFile.getName(), Codec, ThumbnailFilename, Duration, User);
		
		
		//System.out.println("neo ended!");
		Neo4j.addPoint(location);
		
		Neo4j.getNearVideos(latitude, longitude);*/
		
		if(Codec.equals("h264"))
		{
			System.out.println("FileUpload -- ext==MP4");
			ObjectStore ob = new ObjectStore();
		   	String URI = ob.ObjectStoreStart(uploadPath + newFile.getName());
		   	System.out.println("VURI: "+URI);
		   	Java2MySql.VideoUpdate(ID, newFile.getName(),URI);
		   	File inputfile = new File(uploadPath+newFile.getName());
		   	inputfile.setWritable(true);
    		//boolean b = inputfile.delete();
		}
		else
		{
			try {
				RabbitMQSend.send(ID);
				RabbitMQReceive.recv();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		ResponseBuilder x = Response.status(200).entity(ID);
		return CORS.makeCORS(x, _corsHeaders);
	}
	
	// save uploaded file to new location
	private void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) {

		try {
			
			OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
			int read = 0;
			byte[] bytes = new byte[1024];

			out = new FileOutputStream(new File(uploadedFileLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
		
	
	private long getDuration(String movie) {
			
		IContainer container = IContainer.make();
		if (container.open(movie, IContainer.Type.READ, null) < 0) {
			throw new RuntimeException("Cannot open '" + movie + "'");
		}
		return container.getDuration() / 1000000;
	}
	
	private String verifyAccessToken(String Token){
		
		HttpClient client = new HttpClient();
        //HttpMethod method = new GetMethod("http://cloud27.dbis.rwth-aachen.de:9080/ClViTra_2.0/rest/verifyAccessToken");
		HttpMethod method = new GetMethod("http://10.255.255.22:9080/ClViTra_2.0/rest/verifyAccessToken");
        method.addRequestHeader("AccessToken", Token);
        String response=null;
        
        try {
            client.executeMethod(method);
 
            if (method.getStatusCode() == HttpStatus.SC_OK) {
                response = method.getResponseBodyAsString();
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }
        return response;
	}
}