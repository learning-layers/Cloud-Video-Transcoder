package de.dbis.oidc;

import java.io.IOException;
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

//import net.minidev.json.JSONObject;


import org.json.*;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.SerializeException;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.openid.connect.sdk.OIDCAccessTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.UserInfoSuccessResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.thetransactioncompany.json.pretty.PrettyJson;

import de.dbis.util.CORS;
import de.dbis.util.GetProperty;


/**
 * 
 * Get Access Tokens for Open ID connect login.
 *
 */
@Path("/getAccessToken")
@Component
public class OIDCTokens {
	
	@Context UriInfo uriInfo;
	private String _corsHeaders;

	@OPTIONS
	public Response corsResource(@HeaderParam("Access-Control-Request-Headers") String requestH) {
		_corsHeaders = requestH;
		return CORS.makeCORS(Response.ok(), requestH);
	}
	
	/**
	 * Requests the OpenID Connect service for the access token by sending the "code".
	 * Once access token is received, it is then sent to the client as Response. 
	 * @param Header Code as String object.
	 * @return javax.ws.rs.core.Response Access Token as String object.
	 * @throws JSONException
	 */
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response getAccessToken(@HeaderParam("Code") String Header) throws JSONException{
		
		String INPUT_FILE = "oidc";

		String token, redirect, cID, cSecret;
		token = GetProperty.getParam("token", INPUT_FILE);
		redirect = GetProperty.getParam("redirect", INPUT_FILE);
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
			Response.ResponseBuilder r = Response.status(404);
			return CORS.makeCORS(r, _corsHeaders);
		}

		HTTPResponse httpResponse;

		try {
			httpResponse = httpRequest.send();

		} catch (IOException e) {

			// The URL request failed
			Response.ResponseBuilder r = Response.status(404);
			return CORS.makeCORS(r, _corsHeaders);
		}

		TokenResponse tokenResponse;

		try {
			tokenResponse = OIDCTokenResponseParser.parse(httpResponse);

		} catch (Exception e) {
			System.out.println("Couldn't parse token response: " + e.getMessage());
			Response.ResponseBuilder r = Response.status(404);
			return CORS.makeCORS(r, _corsHeaders);
		}
		
		System.out.println(tokenResponse);
		
		if (tokenResponse instanceof TokenErrorResponse) {

			// The token response indicates an error, print it out
			// and return immediately
			TokenErrorResponse tokenError = (TokenErrorResponse)tokenResponse;
			System.out.println("Token error: " + tokenError.getErrorObject());
			Response.ResponseBuilder r = Response.status(404);
			return CORS.makeCORS(r, _corsHeaders);
		}


		OIDCAccessTokenResponse tokenSuccess = (OIDCAccessTokenResponse)tokenResponse;

		BearerAccessToken accessToken = (BearerAccessToken)tokenSuccess.getAccessToken();
		RefreshToken refreshToken = tokenSuccess.getRefreshToken();
		SignedJWT idToken = (SignedJWT)tokenSuccess.getIDToken();

		System.out.println("Token response:");
		System.out.println(accessToken.toString());

		//System.out.println("\tAccess token: " + accessToken.toJSONObject().toString());
		//System.out.println("\tRefresh token: " + refreshToken);
		//System.out.println("\n\n");
		
		Response.ResponseBuilder r = Response.ok(accessToken.toString());
		return CORS.makeCORS(r, _corsHeaders);		
	}
}
