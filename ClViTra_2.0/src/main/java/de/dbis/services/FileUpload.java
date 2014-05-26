package de.dbis.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.xuggle.xuggler.IContainer;

import de.dbis.util.CORS;

@Path("/upload")
@Component
public class FileUpload
{
	@Context UriInfo uriInfo;
	private String _corsHeaders;

	@OPTIONS
	public Response corsResource(@HeaderParam("Access-Control-Request-Headers") String requestH) {
		_corsHeaders = requestH;
		return CORS.makeCORS(Response.ok(), requestH);
	}
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(@HeaderParam("User") String User,
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) {
			   
		System.out.println("User: " + User);
		String uploadPath, savePath; //uploadCode;
		String INPUT_FILE = "tempFileLocation";
		//String INPUT_FILE_UploadCode = "FileUpload";
			
		uploadPath = GetProperty.getParam("location", INPUT_FILE);
		savePath = GetProperty.getParam("savePath", INPUT_FILE);
					
		String uploadedFileLocation = uploadPath + fileDetail.getFileName();
		
		String newName = fileDetail.getFileName();
		newName = newName.replaceAll(" ", "_");
		newName = User+"_"+newName;
		File newFile = new File(uploadPath +newName);
		
		
		uploadedFileLocation = uploadPath + newFile.getName();

		//System.out.println("saving the file");
		// saving the file
		writeToFile(uploadedInputStream, uploadedFileLocation);
			
		//System.out.println("Generating Thumbnail");
		// Generating Thumbnail
		String ThumbnailFilename = Thumbnail.Generate_Thumbnail(uploadPath, newFile.getName());

		//System.out.println("Duration");
		//Get the duration of the video
		long Duration = getDuration(uploadedFileLocation);
			
		//System.out.println("Extension");
		//String ext = FilenameUtils.getExtension(newFile.getName());
		System.out.println("DB");
		
		String Codec = VideoInfo.videoInfo(uploadedFileLocation);
		int UserId = Java2MySql.getUserId(User);
		String ID = Java2MySql.VideoUpdate(savePath+newFile.getName(), Codec, ThumbnailFilename, Duration, UserId);
			
		//String output = "<h1>ClViTra</h1> <p style=\"color:green\"> Upload Successful!</p>";
		//output += VideosDisplay();

		if(Codec.equals("h264"))
		{
			System.out.println("FileUpload -- ext==MP4");
			ObjectStore ob = new ObjectStore();
		   	String URI = ob.ObjectStoreStart(uploadPath + newFile.getName());
		   	Java2MySql.VideoUpdate(ID, newFile.getName(),URI);
		   	File inputfile = new File(uploadPath+newFile.getName());
		   	inputfile.setWritable(true);
    		boolean b = inputfile.delete();
		}
		else
		{
			try {
				RabbitMQSend.send(ID);
				RabbitMQReceive.recv();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		//uploadCode = "<h1>ClViTra</h1> <p style=\"color:green\"> Upload Successful!</p>";
		//uploadCode += GetProperty.getParam("uploadCode", INPUT_FILE_UploadCode);
		
		//ResponseBuilder x = Response.temporaryRedirect(location);
		ResponseBuilder x = Response.status(200).entity(ID);
		return CORS.makeCORS(x, _corsHeaders);
	}
	
	// save uploaded file to new location
	private void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) {

		try {
			
			OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
			int read = 0;
			byte[] bytes = new byte[1024];

			out = new FileOutputStream(new File(uploadedFileLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
		
	private long getDuration(String movie) {
			
		IContainer container = IContainer.make();
		//IURLProtocolHandler movie = null;
		if (container.open(movie, IContainer.Type.READ, null) < 0) {
			throw new RuntimeException("Cannot open '" + movie + "'");
		}
		return container.getDuration() / 1000000;
	}
}