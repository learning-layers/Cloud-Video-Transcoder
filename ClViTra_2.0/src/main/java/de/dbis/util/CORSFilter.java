package de.dbis.util;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CORSFilter implements Filter {
	
	public static String VALID_METHODS = "DELETE, HEAD, GET, OPTIONS, POST, PUT";
	
	public void destroy() {
    
    }
    
    public void doFilter(
			ServletRequest request, ServletResponse response, 
			FilterChain chain) throws IOException, ServletException {

			((HttpServletResponse)response).addHeader("Access-Control-Allow-Origin", "*");
			((HttpServletResponse)response).addHeader("Access-Control-Allow-Methods","GET, POST, DELETE, PUT");
			((HttpServletResponse)response).addHeader("Access-Control-Allow-Headers","Accept, Content-Type, Origin, X-Auth-Token");

			chain.doFilter(request, response);
	}

    public void init(FilterConfig config) throws ServletException {

    }
	
}