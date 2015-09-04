package app;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

import filter.SearchRequestFilter;


@ApplicationPath("movie")
public class MovieApplication extends ResourceConfig {
    public MovieApplication() {
        packages("movieresource");
        register(SearchRequestFilter.class);
        
        //referer filter
        
        //cache headers
        
    }
}