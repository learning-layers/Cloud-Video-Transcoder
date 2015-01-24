package de.dbis.tests;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

import de.dbis.util.GetProperty;

public class TestVideoDetails {

	
	//@Test
	public void videoAvailable() throws ClientProtocolException, IOException{
	   // Given cloud27.dbis.rwth-aachen.de
	   HttpUriRequest request = new HttpGet("http://10.255.255.10:8080/clvitra/rest/videos/details?clientType=app&videoURI=http://137.226.58.2:8888/v1/AUTH_451035e5f9504a878946697522070c43/public/aarij_Performance2.mp4");

	   // When
	   String INPUT_FILE = "test";
	
	   String token = GetProperty.getParam("token", INPUT_FILE);
	   
	   //HttpUriRequest request = new HttpGet(url);

       // add request header
	   request.addHeader("Authorization", "Bearer "+token);
	   //request.addHeader("videoName", "petru_trailer_400p.mp4");
	   
	   HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
	   System.out.println(httpResponse.getStatusLine().getStatusCode());
	   
	   // Then
	   assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
	}
	
	//@Test
	public void videoUnavailable() throws ClientProtocolException, IOException{
	   // Given
	   HttpUriRequest request = new HttpGet("http://cloud27.dbis.rwth-aachen.de:9080/ClViTra_2.0/rest/videoDetail/aarij/77247334-ac3d-4f2e-b324-8aeae897740a");
	 
	   // When
	   HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
	   System.out.println(httpResponse.getStatusLine().getStatusCode());
	   
	   // Then
	   assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_FOUND);
	}
	
	//@Test
	public void userUnavailable() throws ClientProtocolException, IOException{
	   // Given
	   HttpUriRequest request = new HttpGet("http://cloud27.dbis.rwth-aachen.de:9080/ClViTra_2.0/rest/videoDetail/adam/77243334-ac3d-4f2e-b324-8aeae897740a");
	   
	   // When
	   HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
	   System.out.println(httpResponse.getStatusLine().getStatusCode());
	   
	   // Then
	   assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_FOUND);
	}
}
