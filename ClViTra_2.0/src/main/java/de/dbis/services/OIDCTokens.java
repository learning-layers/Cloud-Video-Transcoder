package de.dbis.services;

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
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response getAccessToken(@HeaderParam("Code") String Header) throws JSONException{
		
		//OIDCLoginStart st = new OIDCLoginStart();
		//String loginURL = st.Authorize();
		
		//System.out.println(loginURL);
		System.out.println("In OIDCTokens");
		
		AuthorizationCode code = null;
		if(Header != null)
			code = new AuthorizationCode(Header);
		
		System.out.println(code);
		// *** *** *** Make a token endpoint request *** *** *** //

		// Compose an access token request, authenticating the client
		// app and exchanging the authorisation code for an ID token
		// and access token
		
		URI tokenEndpointURL = null;
		try {
			tokenEndpointURL = new URI("http://137.226.58.15:9085/openid-connect-server-webapp/token");
		} catch (URISyntaxException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		System.out.println("Sending access token request to " + tokenEndpointURL + "\n\n");

		// We authenticate with "client secret basic"
		ClientID clientID = new ClientID("clvitra");
		Secret clientSecret = new Secret("clvitra");
		ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);
		TokenRequest accessTokenRequest=null;
		try {
			accessTokenRequest = new TokenRequest(
				tokenEndpointURL,
				clientAuth,
				new AuthorizationCodeGrant(code, new URI("http://127.0.0.1:8080/ClViTra_2.0/FileUpload.html"), clientID));
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


		// *** *** *** Process ID token which contains user auth information *** *** *** //
		/*if (idToken != null) {
		
			System.out.println("ID token [raw]: " + idToken.getParsedString());

			System.out.println("ID token JWS header: " + idToken.getHeader());

			// Validate the ID token by checking its HMAC;
			// Note that PayPal HMAC generation is probably incorrect,
			// there's also a bug in the "exp" claim type
			try {
				MACVerifier hmacVerifier = new MACVerifier(clientSecret.getValue().getBytes());

				final boolean valid = idToken.verify(hmacVerifier);

				System.out.println("ID token is valid: " + valid);

				JSONObject jsonObject = idToken.getJWTClaimsSet().toJSONObject();

				System.out.println("ID token [claims set]: \n" + new PrettyJson().format(jsonObject));

				System.out.println("\n\n");

			} catch (Exception e) {

				System.out.println("Couldn't process ID token: " + e.getMessage());
			}
		}*/
		
		
		Response.ResponseBuilder r = Response.ok(accessToken.toString());
		return CORS.makeCORS(r, _corsHeaders);		
	}
}
