package de.dbis.oidc;

import java.io.*;
import java.net.*;
import java.util.Date;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.*;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.*;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.openid.connect.sdk.*;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import de.dbis.util.CORS;
import de.dbis.util.DateTimeUtils;
import de.dbis.util.GetProperty;


/**
 * Initializes OpenID Connect login procedure.
*/
@Api(value = "/oidc", description = "OIDC services")
@Path("/oidc")
@Component
public class OIDC extends HttpServlet {

	
	@Context UriInfo uriInfo;
	private String _corsHeaders;

	@OPTIONS
	public Response corsResource(@HeaderParam("Access-Control-Request-Headers") String requestH) {
		_corsHeaders = requestH;
		return CORS.makeCORS(Response.ok(), requestH);
	}

	/**
	 * Composes the URL which enables the user to login through openID and redirects to the ClViTra v2.0 app.
	 * @return javax.ws.rs.core.Response URL as String object
	 */
	
	@GET
	@ApiOperation(value = "Returns the composed URL for OIDC auth", response = OIDC.class)
	@ApiResponses(value = {
	  @ApiResponse(code = 200, message = "Success"),
	  @ApiResponse(code = 204, message = "Couldn't compose OIDC authorisation request URL")
	})
	@Path("/auth")
	@Produces(MediaType.TEXT_PLAIN)
	public Response doGet() {

		URL authzURL;
		try {
			//Date startDate = DateTimeUtils.currentTime();
			//Thread.sleep(4000);
			authzURL = composeAuthzRequestURL();

			//Date endDate = DateTimeUtils.currentTime();
        	//DateTimeUtils.printDifference(startDate, endDate);
		} catch (Exception e) {

			System.out.println("<p>Couldn't compose OIDC authorisation request URL: " + e.getMessage() + "</p>");
			Response.ResponseBuilder r = Response.noContent();
			return CORS.makeCORS(r, _corsHeaders);
		}
		Response.ResponseBuilder r = Response.ok(authzURL.toString());
		return CORS.makeCORS(r, _corsHeaders);
		
	}
	
	/**
	 * Requests the OpenID Connect service for the access token by sending the "code".
	 * Once access token is received, it is then sent to the client as Response. 
	 * @param Header Code as String object.
	 * @return javax.ws.rs.core.Response Access Token as String object.
	 * @throws JSONException
	 */
	
	@GET
	@ApiOperation(value = "Returns the access token for OIDC auth", response = OIDC.class)
	@ApiResponses(value = {
	  @ApiResponse(code = 200, message = "Success"),
	  @ApiResponse(code = 404, message = "Couldn't retreive the access token."),
	  @ApiResponse(code = 401, message = "Auth Code missing in the request header.")
	})
	@Path("/getAccessToken")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getAccessToken(@HeaderParam("Code") String Header) throws JSONException{
		
		if(Header!=null){
			String INPUT_FILE = "oidc";
			String BASE_INPUT_FILE = "base";
	
			String token, redirect, cID, cSecret, base;
			base = GetProperty.getParam("uri", BASE_INPUT_FILE);
			token = base + GetProperty.getParam("token", INPUT_FILE);
			redirect = base + GetProperty.getParam("redirect", INPUT_FILE);
			cID = GetProperty.getParam("clientid", INPUT_FILE);
			cSecret = GetProperty.getParam("clientsecret", INPUT_FILE);
			System.out.println("In OIDCTokens");
			
			AuthorizationCode code = null;
			if(Header != null)
				code = new AuthorizationCode(Header);
			
			//System.out.println(code);
			// *** *** *** Make a token endpoint request *** *** *** //
	
			// Compose an access token request, authenticating the client
			// app and exchanging the authorisation code for an ID token
			// and access token
			
			URI tokenEndpointURL = null;
			try {
				tokenEndpointURL = new URI(token);
			} catch (URISyntaxException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
	
			System.out.println("Sending access token request to " + tokenEndpointURL + "\n\n");
	
			// We authenticate with "client secret basic"
			ClientID clientID = new ClientID(cID);
			Secret clientSecret = new Secret(cSecret);
			ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
			TokenRequest accessTokenRequest=null;
			try {
				accessTokenRequest = new TokenRequest(
					tokenEndpointURL,
					clientAuth,
					new AuthorizationCodeGrant(code, new URI(redirect), clientID));
			} catch (URISyntaxException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			HTTPRequest httpRequest;
	
			try {
				httpRequest = accessTokenRequest.toHTTPRequest();
				String modifiedQuery = httpRequest.getQuery().split("&client_id")[0];
				httpRequest.setQuery(modifiedQuery);
	
	
			} catch (SerializeException e) {
	
				System.out.println("Couldn't create access token request: " + e.getMessage());
				Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_NOT_FOUND);
				return CORS.makeCORS(r, _corsHeaders);
			}
	
			HTTPResponse httpResponse;
	
			try {
				httpResponse = httpRequest.send();
				
				System.out.println("http response: "+httpResponse.toString());
	
			} catch (IOException e) {
	
				// The URL request failed
				Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_NOT_FOUND);
				return CORS.makeCORS(r, _corsHeaders);
			}
	
			TokenResponse tokenResponse;
	
			try {
				tokenResponse = OIDCTokenResponseParser.parse(httpResponse);
	
			} catch (Exception e) {
				System.out.println("Couldn't parse token response: " + e.getMessage());
				Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_NOT_FOUND);
				return CORS.makeCORS(r, _corsHeaders);
			}
			
			System.out.println("token response: "+tokenResponse.toString());
			
			if (tokenResponse instanceof TokenErrorResponse) {
	
				// The token response indicates an error, print it out
				// and return immediately
				TokenErrorResponse tokenError = (TokenErrorResponse)tokenResponse;
				System.out.println("Token error: " + tokenError.getErrorObject());
				Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_NOT_FOUND);
				return CORS.makeCORS(r, _corsHeaders);
			}
	
	
			OIDCAccessTokenResponse tokenSuccess = (OIDCAccessTokenResponse)tokenResponse;
	
			BearerAccessToken accessToken = (BearerAccessToken)tokenSuccess.getAccessToken();
			RefreshToken refreshToken = tokenSuccess.getRefreshToken();
			SignedJWT idToken = (SignedJWT)tokenSuccess.getIDToken();
	
			System.out.println("tokensuccess: "+tokenSuccess.toString());
			
			System.out.println("Token response:");
			System.out.println(accessToken.toString());
	
			//System.out.println("\tAccess token: " + accessToken.toJSONObject().toString());
			//System.out.println("\tRefresh token: " + refreshToken);
			//System.out.println("\n\n");
			
			Response.ResponseBuilder r = Response.ok(accessToken.toString());
			return CORS.makeCORS(r, _corsHeaders);	
		} 
		else{
		   	Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_UNAUTHORIZED);
	   		return CORS.makeCORS(r, _corsHeaders);
	   }
			
	}
	
	/**
	 * Verifies access token and request user information corresponding to that token.
	 * The information is then sent to the client as a Response.   
	 * @param Header Access Token as String object
	 * @return javax.ws.rs.core.Response username along with Status Code
	 * @throws JSONException
	 */
	@GET
	@ApiOperation(value = "Verifies the access token for OIDC auth", response = OIDC.class)
	@ApiResponses(value = {
	  @ApiResponse(code = 200, message = "Success."),
	  @ApiResponse(code = 404, message = "Couldn't verify the access token."),
	  @ApiResponse(code = 401, message = "Authorization bearer missing from the request header.")
	  
	})
	@Path("/verifyAccessToken")
	@Produces(MediaType.TEXT_PLAIN)
	public Response verifyAccessToken(@HeaderParam("Authorization") String Header) throws JSONException{

		if(Header!=null){
			String INPUT_FILE = "oidc";
			String BASE_INPUT_FILE = "base";
	
			Header = Header.replace("Bearer ","");
			String userinfo, base;
			base = GetProperty.getParam("uri", BASE_INPUT_FILE);
			userinfo = base + GetProperty.getParam("userinfo", INPUT_FILE);
			
			BearerAccessToken accessToken = null;
			
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
				Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_NOT_FOUND);
				return CORS.makeCORS(r, _corsHeaders);
			}
			
			UserInfoResponse userInfoResponse;
			
			try {
				userInfoResponse = UserInfoResponse.parse(httpResponse);
				
			} catch (ParseException e) {
				
				System.out.println("Couldn't parse UserInfo response: " + e.getMessage());
				Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_NOT_FOUND);
				return CORS.makeCORS(r, _corsHeaders);
			}
			
			
			if (userInfoResponse instanceof UserInfoErrorResponse) {
				
				System.out.println("UserInfo request failed");
				Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_NOT_FOUND);
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
		else{
		   	Response.ResponseBuilder r = Response.status(HttpServletResponse.SC_UNAUTHORIZED);
	   		return CORS.makeCORS(r, _corsHeaders);
	   }
	}
	
	private URL composeAuthzRequestURL()
		throws Exception {
		String INPUT_FILE = "oidc";
		String BASE_INPUT_FILE = "base";

		String authorize, redirect, cID, base;
		base = GetProperty.getParam("uri", BASE_INPUT_FILE);
		authorize = base + GetProperty.getParam("authorize", INPUT_FILE);
		redirect = base + GetProperty.getParam("redirect", INPUT_FILE);
		cID = GetProperty.getParam("clientid", INPUT_FILE);
		// Set the requested response_type (code, token and / or 
		// id_token):
		// Use CODE for authorisation code flow
		// Use TOKEN for implicit flow
		ResponseType rt = new ResponseType("code");

		// Set the requested scope of access
		Scope scope = new Scope("openid", "email", "profile");

		// Identify the client app by its registered ID
		ClientID clientID = new ClientID(cID);

		// Set the redirect URL after successful OIDC login / 
		// authorisation. This URL is typically registered in advance 
		// with the OIDC server
		URI redirectURI = new URI(redirect);
		
		// Generate random state value. It's used to link the
		// authorisation response back to the original request, also to
		// prevent replay attacks
		State state = new State();

		// Generate random nonce value.
		Nonce nonce = new Nonce();

		// Create the actual OIDC authorisation request object
		AuthenticationRequest authRequest = new AuthenticationRequest(redirectURI, rt, scope, clientID, redirectURI, state, nonce);


		// Get the resulting URL query string with the authorisation
		// request encoded into it
		String queryString = authRequest.toQueryString();


		// Set the base URL of the OIDC server authorisation endpoint
		URL authzEndpointURL = new URL(authorize);


		// Construct and output the final OIDC authorisation URL for
		// redirect
		URL authzURL = new URL(authzEndpointURL + "?" + queryString);

		return authzURL;
	}

	
}