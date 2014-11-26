package de.dbis.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import de.dbis.db.Java2MySql;
import de.dbis.util.CORS;

/**
 * Returns video information and status.
 */
@Path("/videoDetail")// /{user}/{clientType}/{videoURI}")
@Component
public class VideoDetails {
	
	@Context UriInfo uriInfo;
	private String _corsHeaders;

	@OPTIONS
	public Response corsResource(@HeaderParam("Access-Control-Request-Headers") String requestH) {
		_corsHeaders = requestH;
		return CORS.makeCORS(Response.ok(), requestH);
	}
	
	/**
	 * Returns the Video URL, Thumbnail URL, Video Name, and Video status for the given video ID uploaded by the logged in User. 
	 * @param username
	 * @param videoId
	 * @return javax.ws.rs.core.Response JSON formatted string
	 * @throws JSONException
	 */
	@GET
	@Produces("application/json")
	public Response Details(@QueryParam("user") String username, @QueryParam("clientType") String clientType, @QueryParam("videoURI") String videoURI)
			//@PathParam("user") String username, @PathParam("clientType") String clientType, @PathParam("videoURI") String videoURI) 
					throws JSONException{
		   String Name, URI, Thumbnail, Status;

		   String[] Details = Java2MySql.getVideoDetails(username, videoURI, clientType);
		   
		   JSONObject j =  new JSONObject();
		   
		   if(!"Not Found".equals(Details[0])){
			   
			   Name = Details[0];
			   //URI = Details[1];
			   Thumbnail = Details[1];
			   Status = Details[2];
			   j.put("Video_Name", Name);
			   //j.put("Video_URL", URI);
			   j.put("Thumbnail_URL", Thumbnail);
			   j.put("Status", Status);
			   
		   }
		   else{
			   
			   j.put("Status", Details[0]);
			   Response.ResponseBuilder r = Response.status(404);
			   return CORS.makeCORS(r, _corsHeaders);
		   }
		   Response.ResponseBuilder r = Response.ok(j.toString());
		   return CORS.makeCORS(r, _corsHeaders);
	 }
}