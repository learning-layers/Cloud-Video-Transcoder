package de.dbis.util;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

public class CORS {
	
	public static Response makeCORS(ResponseBuilder res, String returnMethod) {
		Response.ResponseBuilder rb = res.header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS, DELETE");//.header("Access-Control-Allow-Headers", "X-Requested-With");

		if (!"".equals(returnMethod)) {
			rb.header("Access-Control-Allow-Headers", returnMethod);
		}

		return rb.build();
	}
}
