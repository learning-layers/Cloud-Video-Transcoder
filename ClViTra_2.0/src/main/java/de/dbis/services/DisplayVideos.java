package de.dbis.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import de.dbis.db.Java2MySql;
import de.dbis.util.CORS;

/**
 * 
 * Retrieves the list of Videos from the server for a specific category for the logged in user.
 *
 */
@Path("/videos/{id}/{request}")
@Component
public class DisplayVideos {
	
	@Context UriInfo uriInfo;
	private String _corsHeaders;

	@OPTIONS
	public Response corsResource(@HeaderParam("Access-Control-Request-Headers") String requestH) {
		_corsHeaders = requestH;
		return CORS.makeCORS(Response.ok(), requestH);
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
	@Produces("application/json")
	public Response VideosDisplay(@PathParam("id") String username, @PathParam("request") String request) throws JSONException{
		   String Name, URI, Thumbnail;

		   List<String> myList = Java2MySql.getVideos(username, request);
		   
		   JSONObject j[] =  new JSONObject[myList.size()/3];//null; =
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
}