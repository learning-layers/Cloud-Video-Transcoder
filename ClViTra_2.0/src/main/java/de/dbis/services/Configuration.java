package de.dbis.services;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.multipart.FormDataParam;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import de.dbis.slave.RabbitMQReceive;

/**
 * 
 * Configures the machine to work as a Master or Slave.
 *
 */
@Api(value = "/config", description = "Set the system as Master or Slave")
@Path("/config")
public class Configuration {
	
	/**
	 * Configures the application as Master or Slave.
	 * @param func it can be "slave" or "master" 
	 * @return javax.ws.rs.core.Response
	 */
	@POST
	@ApiOperation(value = "Set the system as Master or Slave", response = Configuration.class)
	@ApiResponses(value = {
	  @ApiResponse(code = 200, message = "Success"),
	})
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
