package de.dbis.services;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.sun.jersey.core.util.Base64;

import de.dbis.util.CORS;

@Path("/auth")
@Component
public class Auth
{
	@Context UriInfo uriInfo;
	private String _corsHeaders;

	@OPTIONS
	public Response corsResource(@HeaderParam("Access-Control-Request-Headers") String requestH) {
		_corsHeaders = requestH;
		return CORS.makeCORS(Response.ok(), requestH);
	}
	
	@GET
	@Produces("application/json")
	public Response authenticateUser(@HeaderParam("authorization") String authHeader) throws JSONException{
		
		if(authHeader != null){
			String[] dauth = null;
			String[] header = authHeader.split(" ");
			if (header.length < 2) {
				Response.ResponseBuilder r = Response.status(Status.UNAUTHORIZED);
				return CORS.makeCORS(r, _corsHeaders);
			}
			String authkey = header[1];

			if(Base64.isBase64(authkey)){
				dauth = (new String(Base64.decode(authkey))).split(":");
				if (dauth.length == 1) {
					dauth = new String[]{dauth[0], ""};
				}
				int Return_code = confirmUserLoginDetails(dauth[0], dauth[1]);
				if(Return_code==2){
					Response.ResponseBuilder r = Response.status(Status.NOT_FOUND);
					return CORS.makeCORS(r, _corsHeaders);
				}
				if(Return_code==1){
					Response.ResponseBuilder r = Response.status(Status.NOT_ACCEPTABLE);
					return CORS.makeCORS(r, _corsHeaders);
				}
				
				if(Return_code==0){
					//String output = "<!DOCTYPE html> <html> <head> <h1>ClViTra</h1> </head> <body>" + VideosDisplay() + "</body> </html>";
					
					JSONObject j = new JSONObject();
					fillJSON(j, uriInfo.getBaseUriBuilder(), dauth[0], dauth[1]);
					j.remove("pass");

					Response.ResponseBuilder r = Response.ok(j.toString());
					return CORS.makeCORS(r, _corsHeaders);
				}
			}
		}

		Response.ResponseBuilder r = Response.status(Status.UNAUTHORIZED);
		return CORS.makeCORS(r, _corsHeaders);
	}
	
	public JSONObject fillJSON(JSONObject js, UriBuilder url, String User, String Pass) {
		try {
			js.put("url", url.clone().path("ClViTra/upload").build());
			js.put("pass", Pass);
			//js.put("output", output);
		} catch (JSONException e) {}
		return js;
	}
	   
   public int confirmUserLoginDetails(String username, String password) {
		
		int Return_code = Java2MySql.LoginVerification(username, password);
		
		return Return_code;
   }
}