Authors: Aarij Siddiqui, Sadik Bakiu, Dominik Renzel, Petru Nicolaescu, István Koren

Summary
--------

This is a summary of the procedure on how to have integrate Open ID Connect into a project. This was implemented for the Cloud Video Transcoder Open ID Connect integration. 


Useful Links
--------
OpenID Connect Server [installation](http://cloud15.dbis.rwth-aachen.de:9085/openid-connect-server-webapp/ "")

Simple registration for OpenID Connect [page](http://cloud15.dbis.rwth-aachen.de:9086/register2.php "")

##Steps

###Step 1

First we need to compose a URL that contains the following: 

1. Redirect URI
2. Response Token
3. Scope
4. ClientID
5. State 
6. Nonce

Here ClientID is the ID assigned to your application, and Response type is the application code.

Attach this query string to the openID connect service endpoint: "cloud15.dbis.rwth-aachen.de:9085/openid-connect-server-webapp/authorize" with a '?' in between.

When the user will visit this link he will be prompted with the login, and will be redirected to the Redirect URI. 
After the redirect the page will have a code in the URL response, which is a one time usable code, and now you can proceed to step 2 and utilize this code.

###Step 2

The code will be used to retrieve an Access Token which is should be stored on the client side (for the RESTFul service) so that user does not have to login again. 

Cloud Video Transcoder composes a HttpRequest for getting the Access Token.

Compose an Access Token request:  
`accessTokenRequest = new TokenRequest(  
                      tokenEndpointURL,    
                      clientAuth,    
                      new AuthorizationCodeGrant(code, new URI(), clientID));`
                      
`clientAuth` is declared as follows:
`ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);`

and tokenEndpointURL: http://10.255.255.17:9085/openid-connect-server-webapp/token 

For the clientID in the query, there is a simple workout that needs to be done:
`String modifiedQuery = httpRequest.getQuery().split("&client_id")[0];`     
	`httpRequest.setQuery(modifiedQuery);`
	
Now our request is ready and after sending it, the Access Token should be received. This should be sent to the user and be saved (e.g. in the local browser storage), for later use (with further API calls).

###Step 3


As a next step, information about the user who logged in for the service should be retrieved.

Its a simple HttpRequest which is composed as follows:
`UserInfoRequest userInfoRequest = new UserInfoRequest(userinfoEndpointURL, accessToken);`

Here userinfoEndpointURL is: http://10.255.255.17:9085/openid-connect-server-webapp/userinfo

and accessToken is the one that was received in step 2.

In response of this request we will get back the user information like their username, name , email, etc.


###Note
We simply store the Access Token in the user's browser and send it to the service whenever user tries to access the service. The service tries to get the user information with that Access Token and if it successfully gets the user information with that accessToken then the user is allowed to access the service.      
If it receives any error with the request then it parses the error. If the user is not authorized, or if the Access Token has been expired, then the user is redirected to the login page.
