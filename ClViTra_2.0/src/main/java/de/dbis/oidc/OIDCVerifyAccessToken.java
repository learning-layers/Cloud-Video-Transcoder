package de.dbis.oidc;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.UserInfoSuccessResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;

import de.dbis.db.Java2MySql;
import de.dbis.util.CORS;
import de.dbis.util.GetProperty;

/**
 * 
 * Verifies the Access Token for validity.
 *
 */
@Path("/verifyAccessToken")
@Component
public class OIDCVerifyAccessToken {
	
	@Context UriInfo uriInfo;
	private String _corsHeaders;

	@OPTIONS
	public Response corsResource(@HeaderParam("Access-Control-Request-Headers") String requestH) {
		_corsHeaders = requestH;
		return CORS.makeCORS(Response.ok(), requestH);
	}
	
	/**
	 * Verifies access token and request user information corresponding to that token.
	 * The information is then sent to the client as a Response.   
	 * @param Header Access Token as String object
	 * @return javax.ws.rs.core.Response username along with Status Code
	 * @throws JSONException
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response verifyAccessToken(@HeaderParam("authorization_bearer") String Header) throws JSONException{
		
		String INPUT_FILE = "oidc";

		String userinfo;
		userinfo = GetProperty.getParam("userinfo", INPUT_FILE);
		
		BearerAccessToken accessToken = null;
		
		if(Header != null)
			accessToken = new BearerAccessToken(Header);
		
		
		System.out.println("Verify, accessToken:  "+accessToken);
		// *** *** *** Make a UserInfo endpoint request *** *** *** //

		// Note: The PayPal IdP uses an older OIDC draft version and
		// is at present not compatible with the Nimbus OIDC SDK so
		// we cannot use its helper call. We can however make a direct
		// call and simply display the raw data.

		URI userinfoEndpointURL = null;
		try {
			userinfoEndpointURL = new URI(userinfo);
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Append the access token to form actual request
		UserInfoRequest userInfoRequest = new UserInfoRequest(userinfoEndpointURL, accessToken);
		
		HTTPResponse httpResponse;
		
		try {
			httpResponse = userInfoRequest.toHTTPRequest().send();

		} catch (Exception e) {

			// The URL request failed
			System.out.println("Couldn't send HTTP request to UserInfo endpoint: " + e.getMessage());
			Response.ResponseBuilder r = Response.status(404);
			return CORS.makeCORS(r, _corsHeaders);
		}
		
		UserInfoResponse userInfoResponse;
		
		try {
			userInfoResponse = UserInfoResponse.parse(httpResponse);
			
		} catch (ParseException e) {
			
			System.out.println("Couldn't parse UserInfo response: " + e.getMessage());
			Response.ResponseBuilder r = Response.status(404);
			return CORS.makeCORS(r, _corsHeaders);
		}
		
		
		if (userInfoResponse instanceof UserInfoErrorResponse) {
			
			System.out.println("UserInfo request failed");
			Response.ResponseBuilder r = Response.status(404);
			return CORS.makeCORS(r, _corsHeaders);
		}
		
		
		UserInfo userInfo = ((UserInfoSuccessResponse)userInfoResponse).getUserInfo();
		

		//System.out.println("UserInfo: " + userInfo.getProfile().);
		
		String username = null;
		try {
			JSONObject jsonObject = new JSONObject(userInfo.toJSONObject());
			System.out.println("UserInfo: " +jsonObject.toString());
			username = jsonObject.getString("preferred_username");
			/*if(!Java2MySql.approvedUser(username)){
				Response.ResponseBuilder r = Response.status(401);
				return CORS.makeCORS(r, _corsHeaders);
			}*/
			//System.out.println(new PrettyJson().parseAndFormat(userInfo.toJSONObject().toString()));

		} catch (Exception e) {

			System.out.println("Couldn't parse UserInfo JSON object: " + e.getMessage());
		}
		
		
		Response.ResponseBuilder r = Response.ok(username);
		return CORS.makeCORS(r, _corsHeaders);
	}

}
