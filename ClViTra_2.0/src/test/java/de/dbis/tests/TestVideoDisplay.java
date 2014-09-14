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


public class TestVideoDisplay {
	
	@Test
	public void transcodedVideos() throws ClientProtocolException, IOException{
	   // Given 
	   HttpUriRequest request = new HttpGet("http://cloud27.dbis.rwth-aachen.de:9080/ClViTra_2.0/rest/videos/adam/transcoded");

	   // When
	   HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
	   System.out.println(httpResponse.getStatusLine().getStatusCode());
	   
	   // Then
	   assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
	}
	
	@Test
	public void processingVideos() throws ClientProtocolException, IOException{
	   // Given 
	   HttpUriRequest request = new HttpGet("http://cloud27.dbis.rwth-aachen.de:9080/ClViTra_2.0/rest/videos/aarij/processing");

	   // When
	   HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
	   System.out.println(httpResponse.getStatusLine().getStatusCode());
	   
	   // Then
	   assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
	}
	
	@Test
	public void initializedVideos() throws ClientProtocolException, IOException{
	   // Given 
	   HttpUriRequest request = new HttpGet("http://cloud27.dbis.rwth-aachen.de:9080/ClViTra_2.0/rest/videos/aarij/initialized");
	   
	   // When
	   HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
	   System.out.println(httpResponse.getStatusLine().getStatusCode());
	   
	   // Then
	   assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
	}

}
