package de.dbis.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;


public class TestVideoUpload {
	
	//@Test
	public void uploadAndDelete(){
		HttpClient httpclient = new HttpClient();
	    File file = new File( "C:\\trailer 400p.mp4" );
	
	    // DEBUG
	    System.out.println( "FILE::" + file.exists() ); // IT IS NOT NULL        
	    try
	    {
	    	PostMethod filePost = new PostMethod( "http://cloud27.dbis.rwth-aachen.de:9080/ClViTra_2.0/rest/upload" );
	    	
	    	Part[] parts = { new FilePart( "file", file ) };
	    	filePost.setRequestEntity( new MultipartRequestEntity( parts, filePost.getParams() ) );
	    	filePost.addRequestHeader("User", "petru");
	
	    	// DEBUG
	
	        int response = httpclient.executeMethod( filePost );
	        System.out.println(response);
	        //logger.info( "Response : "+response );
	        //logger.info( filePost.getResponseBodyAsString());
	        //assertEquals(response, HttpStatus.SC_OK);
	        
	        // Delete
	        
	        String url = "http://cloud27.dbis.rwth-aachen.de:9080/ClViTra_2.0/rest/delete";
	        
	        HttpUriRequest request = new HttpGet(url);

	        // add request header
	     	request.addHeader("User", "petru");
	     	request.addHeader("videoName", "petru_trailer_400p.mp4");
	        
	     	// When
	     	HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
	     	System.out.println(httpResponse.getStatusLine().getStatusCode());
	 	   
	     	// Then
			assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpStatus.SC_OK);
			
	    }
	    catch( HttpException e )
	    {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	    catch( IOException e )
	    {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	}

}
