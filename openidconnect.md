Authors: Aarij Siddiqui, Sadik Bakiu, Dominik Renzel, Petru Nicolaescu, István Koren

Update 07/07/2014
--------

The following guide is specifically for those developers who intend to integrate their application with Open ID Connect with ease.

So basically there are three classes that are required to be integarted within your application. Out of these three classes two are required to fullfil the basic functionality of Open ID Connect, whereas third is an optional one, depending on the application.

So let's get started!

OIDCLoginStart.java
--------

In this class we compose the URL, here is the link to the class in Cloud Video Transcoder Application: 

https://github.com/learning-layers/Cloud-Video-Transcoder/blob/master/ClViTra_2.0/src/main/java/de/dbis/services/OIDCLoginStart.java

But there are some changes that are needed to be made in this class:

1. Change the client ID to the client ID of your application on line number 59.
2. Change the Redirect URL to your own URL on line number 65.

That's all for the first class.

OIDCTokens.java
--------

In this class we acquire the Access token from the server, here is the link to the class in Cloud Video Transcoder Application:

https://github.com/learning-layers/Cloud-Video-Transcoder/blob/master/ClViTra_2.0/src/main/java/de/dbis/services/OIDCTokens.java

But again there are some changes that are needed to be made in this class:

1. Change the client ID to the client ID of your application on line number 105.
2. Change the client secret to the client secret of your application on line number 106.
3. Change the Redirect URL to your own URL on line number 113.

That's all for the second class.

OIDCVerifyAccessToken.java
--------

In this class we authenticate the Access token from the server and get the user information, here is the link to the class in Cloud Video Transcoder Application:

https://github.com/learning-layers/Cloud-Video-Transcoder/blob/master/ClViTra_2.0/src/main/java/de/dbis/services/OIDCVerifyAccessToken.java

Once again this class is not a necessary requirement for Open ID Connect login process. It entirely depends on the application idf it needs to manage the user roles, or if it needs to display username on its page after login or not.

But this time there are no changes required.

That's all for the third class.


So now we just need to call these classes.

All these three classes will be called through RESTful API calls.


OIDCLoginStart.java
--------

Call this class from your application where you intend to login your user. It will return the URL which should be visited by the user to login (User can of course be automatically redirected to that URL).

A sample call is as follows:

			$.ajax({
		        	url: "/ClViTra_2.0/rest/openIDauth",
		        	type: "GET",
		        	dataType:'text',
		    		}
			});

A detailed call and implementation can be seen in the following html file:

https://github.com/learning-layers/Cloud-Video-Transcoder/blob/master/ClViTra_2.0/src/main/webapp/index.html


OIDCTokens.java
--------

This call should be in the file which was given as the redirect URL in the above java classes.

A sample call is as follows:

Here 'code' is a value from the response sent by the server after the login.

			$.ajax({
	        	        url: "/ClViTra_2.0/rest/getAccessToken",
	        	        type: "GET",
	        	        dataType:'text',
	        	        beforeSend: function(xhr) {
	        	            xhr.setRequestHeader("Code", code);
	        	        }
        		});

A detailed call and implementation can be seen in the following html file:

https://github.com/learning-layers/Cloud-Video-Transcoder/blob/master/ClViTra_2.0/src/main/webapp/FileUpload.html


OIDCVerifyAccessToken.java
--------

It (in most scenarios) is called everytime the user logs in, but it completely depends on the application requirement. It is not a necessary element of Open ID Login.

			$.ajax({
	        		url: "/ClViTra_2.0/rest/verifyAccessToken",
	        		type: "GET",
	        		dataType:'text',
	        		beforeSend: function(xhr) {
	            		xhr.setRequestHeader("AccessToken", localStorage.clvitraAccessToken);
	        		}
			});

It will respond with a username, but what is returned can of course be changed in the java class.

Once again a detailed call and implementation can be seen in the following html file:

https://github.com/learning-layers/Cloud-Video-Transcoder/blob/master/ClViTra_2.0/src/main/webapp/FileUpload.html


Old documentation
--------

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
