package de.dbis.services;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.multipart.FormDataParam;

import de.dbis.slave.RabbitMQReceive;

@Path("/config")
public class Configuration {
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response MasterSlaveConfig(@FormDataParam("func") String func)
	{
		String status="Master";
		if (func.equals("Master"))
			status = "<html><body> Configuration set as MASTER! </body></html>";
		
		else if(func.equals("Slave")) {
			RabbitMQReceive.recv();
			status = "<html><body> Configuration set as SLAVE! </body></html>";
		}
		
		System.out.println(func);
		return Response.status(200).entity(status).build();
	}
}
