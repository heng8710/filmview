package app;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;


//@ApplicationPath("")
public class MyApplication  extends ResourceConfig{
	
	 public MyApplication() {
	        packages("resource");
	        
	        //referer filter
	        
	        //cache headers
	        
	    }
}
