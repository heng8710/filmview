package app;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

import filter.SearchRequestFilter;


@ApplicationPath("movie")
public class MyApplication extends ResourceConfig {
    public MyApplication() {
        packages("resource");
        register(SearchRequestFilter.class);
        
        //referer filter
        
        //cache headers
        
    }
}