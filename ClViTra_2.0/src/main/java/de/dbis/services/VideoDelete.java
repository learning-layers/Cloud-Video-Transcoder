package de.dbis.services;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.springframework.stereotype.Component;

import de.dbis.db.Java2MySql;
import de.dbis.util.CORS;

@Path("/delete")
@Component
public class VideoDelete {
	
	@Context UriInfo uriInfo;
	private String _corsHeaders;

	@OPTIONS
	public Response corsResource(@HeaderParam("Access-Control-Request-Headers") String requestH) {
		_corsHeaders = requestH;
		return CORS.makeCORS(Response.ok(), requestH);
	}
	@DELETE
	@Produces("application/json")
	public Response Delete(@HeaderParam("user") String username, @HeaderParam("videoname") String videoName){
		
		System.out.println(username);
		System.out.println(videoName);
		Java2MySql.deleteVideo(username, videoName);
		
		
		Response.ResponseBuilder r = Response.ok();
		return CORS.makeCORS(r, _corsHeaders);
	}

}
