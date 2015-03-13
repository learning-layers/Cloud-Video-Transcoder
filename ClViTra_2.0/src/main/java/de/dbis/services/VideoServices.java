package de.dbis.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.xuggle.xuggler.IContainer;

import de.dbis.db.Java2MySql;
import de.dbis.i5cloud.ObjectStore;
import de.dbis.i5cloud.UserStore;
//import de.dbis.mpeg7.sevianno;
import de.dbis.rabbitmq.RabbitMQReceive;
import de.dbis.rabbitmq.RabbitMQSend;
import de.dbis.util.CORS;
import de.dbis.util.GetProperty;
import de.dbis.videoutils.Thumbnail;
import de.dbis.videoutils.VideoInfo;

/**
 * 
 * Retrieves the list of Videos from the server for a specific category for the logged in user.
 *
 */
@Path("/videos")
@Api(value = "/videos", description = "API for Videos")
@Component
public class VideoServices {
	
	@Context UriInfo uriInfo;
	private String _corsHeaders;

	@OPTIONS
	public Response corsResource(@HeaderParam("Access-Control-Request-Headers") String requestH) {
		_corsHeaders = requestH;
		return CORS.makeCORS(Response.ok(), requestH);
	}
	
	@GET
	@ApiOperation(value = "Returns all the videos for a user", response = VideoServices.class)
	@ApiResponses(value = {
	  @ApiResponse(code = 200, message = "Success"),
	  @ApiResponse(code = 401, message = "Unauthorized")
	})
	@Path("/")
	@Produces("application/json")
	public Response VideosDisplay(@HeaderParam("Authorization") String token) throws JSONException{
		   String Name, URI, Thumbnail;
		   
		   if(token!=null){
			   token = token.replace("Bearer ","");
			   String username = verifyAccessToken(token);
			   
			   List<String> myList = null;
			   
			   if(username!=null){
				   
				   myList = Java2MySql.getVideos(username);
			   
				   JSONObject j[] =  new JSONObject[myList.size()/3];
				   JSONObject j_final = new JSONObject();
				   
				   int a=0;
				   while (!myList.isEmpty()) {
					   
					   j[a] = new JSONObject();
					   Name = myList.remove(0);
					   URI = myList.remove(0);
					   Thumbnail = myList.remove(0);
					   j[a].put("Video_Name", Name);
					   j[a].put("Video_URL", URI);
					   j[a].put("Thumbnail_URL", Thumbnail);
					   
					   a++;
				   }
				   j_final.put("Videos", j);
				   Response.ResponseBuilder r = Response.ok(j_final.toString());
				   return CORS.makeCORS(r, _corsHeaders);
			   }
			   else{
				   	Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_UNAUTHORIZED);
			   		return CORS.makeCORS(r, _corsHeaders);
			   }
		   }
		   else{
			   	Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_UNAUTHORIZED);
		   		return CORS.makeCORS(r, _corsHeaders);
		   }
	 }
	
	/**
	 * Returns a list of Videos in a particular request category.
	 * For each video it contains Video Name, Video URL, and Thumbnail URL.
	 * @param username Simple string username.
	 * @param request "Transcoded", "Processing" or "Initialized".
	 * @return javax.ws.rs.core.Response
	 * @throws JSONException
	 */
	@GET
	@ApiOperation(value = "Returns all the videos for a user with given status", response = VideoServices.class)
	@ApiResponses(value = {
	  @ApiResponse(code = 200, message = "Success"),
	  @ApiResponse(code = 401, message = "Unauthorized")
	})
	@Path("/{status}")
	@Produces("application/json")
	public Response VideosDisplay(@HeaderParam("Authorization") String token, @PathParam("status") String status) throws JSONException{
		   String Name, URI, Thumbnail;

		   if(token!=null){
			   token = token.replace("Bearer ","");
			   
			   String username = verifyAccessToken(token);
			   List<String> myList;
			   if(username!=null){
				   
				   myList = Java2MySql.getVideos(username, status);
			   
				   JSONObject j[] =  new JSONObject[myList.size()/3];
				   JSONObject j_final = new JSONObject();
				   
				   int a=0;
				   while (!myList.isEmpty()) {
					   
					   j[a] = new JSONObject();
					   Name = myList.remove(0);
					   URI = myList.remove(0);
					   Thumbnail = myList.remove(0);
					   j[a].put("Video_Name", Name);
					   j[a].put("Video_URL", URI);
					   j[a].put("Thumbnail_URL", Thumbnail);
					   
					   a++;
				   }
				   j_final.put("Videos", j);
				   Response.ResponseBuilder r = Response.ok(j_final.toString());
				   return CORS.makeCORS(r, _corsHeaders);
			   }
			   else{
				   	Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_UNAUTHORIZED);
			   		return CORS.makeCORS(r, _corsHeaders);
			   }
		   }
		   else{
			   	Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_UNAUTHORIZED);
		   		return CORS.makeCORS(r, _corsHeaders);
		   }
	 }
	
	
	
	@DELETE
	@Path("/delete")
	@ApiOperation(value = "Delete the video", response = VideoServices.class)
	@ApiResponses(value = {
	  @ApiResponse(code = 200, message = "Success"),
	  @ApiResponse(code = 401, message = "Unauthorized")
	})
	@Produces("application/json")
	public Response Delete(@HeaderParam("Authorization") String token,  @QueryParam("videoname") String videoName){
		
		if(token!=null){
			token = token.replace("Bearer ","");
			String username = verifyAccessToken(token);
			
			if(username!=null){
				System.out.println(username);
				System.out.println(videoName);
				Java2MySql.deleteVideo(username, videoName);
	
				Response.ResponseBuilder r = Response.ok();
				return CORS.makeCORS(r, _corsHeaders);
			}
			else{
			   	Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_UNAUTHORIZED);
		   		return CORS.makeCORS(r, _corsHeaders);
		   }
		}
		else{
		   	Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_UNAUTHORIZED);
	   		return CORS.makeCORS(r, _corsHeaders);
	   }
	}
	
	/**
	 * Returns the Video URL, Thumbnail URL, Video Name, and Video status for the given video ID uploaded by the logged in User. 
	 * @param username
	 * @param videoId
	 * @return javax.ws.rs.core.Response JSON formatted string
	 * @throws JSONException
	 */
	@GET
	@Path("/details")
	@ApiOperation(value = "Provide details by video URL", response = VideoServices.class)
	@ApiResponses(value = {
	  @ApiResponse(code = 200, message = "Success"),
	  @ApiResponse(code = 404, message = "Video with given URL not found"), 
	  @ApiResponse(code = 401, message = "Unauthorized")
	})
	@Produces("application/json")
	public Response Details(@HeaderParam("Authorization") String token, @QueryParam("clientType") String clientType, @QueryParam("videoURI") String videoURI) 
					throws JSONException{
		   String Name, Thumbnail, Status, Time;

		   if(token!=null){
			   token = token.replace("Bearer ","");
			   String username = verifyAccessToken(token);
			   String[] Details;
			   if(username!=null){
				   if(videoURI!=null){
					   if(clientType==null){
						   Details = Java2MySql.getVideoDetails(username, videoURI, "app");
					   }
					   else{
						   Details = Java2MySql.getVideoDetails(username, videoURI, clientType);
					   }
					   
					   JSONObject j =  new JSONObject();
					   
					   if(!"Not Found".equals(Details[0])){
						   
						   Name = Details[0];
						   //URI = Details[1];
						   Thumbnail = Details[1];
						   Status = Details[2];
						   Time = Details[3];
						   j.put("Video_Name", Name);
						   //j.put("Video_URL", URI);
						   j.put("Thumbnail_URL", Thumbnail);
						   j.put("Status", Status);
						   j.put("Transcoding_Time", Time);
						   
					   }
					   else{
						   
						   j.put("Status", Details[0]);
						   Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_NOT_FOUND);
						   return CORS.makeCORS(r, _corsHeaders);
					   }
					   Response.ResponseBuilder r = Response.ok(j.toString());
					   return CORS.makeCORS(r, _corsHeaders);
				   }
				   else{
					   	Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_NOT_FOUND);
				   		return CORS.makeCORS(r, _corsHeaders);
				   }
			   }
			   else{
				   	Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_UNAUTHORIZED);
			   		return CORS.makeCORS(r, _corsHeaders);
			   }
		   }
		   else{
			   	Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_UNAUTHORIZED);
		   		return CORS.makeCORS(r, _corsHeaders);
		   }
	}
	
	@POST
	@Path("/")
	@ApiOperation(value = "Upload Video", response = VideoServices.class)
	@ApiResponses(value = {
	  @ApiResponse(code = 200, message = "Success"),
	  @ApiResponse(code = 406, message = "Video already exists"),
	})
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(@HeaderParam("Authorization") String token,
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) {
		
		if(token!=null){
			token = token.replace("Bearer ","");
			System.out.println("Token: " + token);
			
			String username = verifyAccessToken(token);
			
			if(username!=null){
				String uploadPath, savePath, base; //uploadCode;
				String INPUT_FILE = "tempFileLocation";
				//String BASE_INPUT_FILE = "base";
				//String INPUT_FILE_UploadCode = "FileUpload";
				
				//base = GetProperty.getParam("uri", BASE_INPUT_FILE);
				uploadPath = GetProperty.getParam("location", INPUT_FILE);
				savePath = GetProperty.getParam("savePath", INPUT_FILE);
							
				String uploadedFileLocation = uploadPath + fileDetail.getFileName();
				
				String newName = fileDetail.getFileName();
				newName = newName.replaceAll(" ", "_");
				newName = username+"_"+newName;
				System.out.println("NEWNAME: "+newName);
				
				if(Java2MySql.Exists(username,FilenameUtils.removeExtension(newName))){
					ResponseBuilder x = Response.status(HttpServletResponse.SC_CONFLICT).entity("File name already exists.");
					return CORS.makeCORS(x, _corsHeaders);
				}
				File newFile = new File(uploadPath +newName);
				
				
				uploadedFileLocation = uploadPath + newFile.getName();
		
				// saving the file
				writeToFile(uploadedInputStream, uploadedFileLocation);
					
				// Generating Thumbnail
				String thumbnails[] = Thumbnail.Generate_Thumbnail(uploadPath, newFile.getName());
		
				System.out.println("TURI: "+thumbnails[0]);
				//Get the duration of the video
				long Duration = getDuration(uploadedFileLocation);
					
				//String ext = FilenameUtils.getExtension(newFile.getName());
				System.out.println("DB");
				
				String Codec = VideoInfo.videoInfo(uploadedFileLocation);
				String ID;
				ID = Java2MySql.VideoUpdate(savePath+newFile.getName(), Codec, thumbnails[0], thumbnails[1], Duration, username);
				
				String bearer_token=null;
				if(Codec.equals("h264"))
				{
					System.out.println("FileUpload -- ext==MP4");
					ObjectStore ob = new ObjectStore();
				   	String URI = ob.ObjectStoreStart(uploadPath + newFile.getName());
				   	System.out.println("VURI: "+URI);
				   	Java2MySql.VideoUpdate(ID, newFile.getName(),URI, "0", "0","0");
				   	
				   	//upload to user storage
				   	//UserStore us = new UserStore();
				   	//bearer_token = "Bearer "+token;
				   	//System.out.println("USER STORE: "+us.uploadToUserStore(bearer_token, uploadPath + newFile.getName()));
				   	
				   	File inputfile = new File(uploadPath+newFile.getName());
				   	inputfile.setWritable(true);
				   	System.out.println("FILE: "+uploadPath+newFile.getName());
		    		boolean b = inputfile.delete();
		    		System.out.println("MP4 delete: "+b);
				   	
				   	String[] Details = Java2MySql.getVideoDetails(ID);
		            String title = Details[0];
		            String thumbnailUri = Details[2];
		            String uploader = Details[4];
		            
		            //sevianno.addMediaDescription(URI, thumbnailUri, title, uploader);
				}
				else
				{
					try {
						System.out.println("SENT TO SLAVE: "+bearer_token);
						RabbitMQSend.send(ID+"?"+bearer_token);
						RabbitMQReceive.recv();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				ResponseBuilder x = Response.status(HttpServletResponse.SC_OK).entity(ID);
				return CORS.makeCORS(x, _corsHeaders);
			}
			else{
			   	Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_UNAUTHORIZED);
		   		return CORS.makeCORS(r, _corsHeaders);
		   }
		}
		else{
		   	Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_UNAUTHORIZED);
	   		return CORS.makeCORS(r, _corsHeaders);
	   }
		
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
		
		String verifyAccessTokenURL;
		String INPUT_FILE = "oidc"; 
		String localaccesstoken = GetProperty.getParam("localaccesstoken", INPUT_FILE);
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(localaccesstoken);
        method.addRequestHeader("Authorization", "Bearer "+Token);
        String response=null;
        
        try {
            client.executeMethod(method);
 
            if (method.getStatusCode() == HttpStatus.SC_OK) {
                response = method.getResponseBodyAsString();
                
            }
            else
            	response = null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            method.releaseConnection();
        }
        return response;
	}
}