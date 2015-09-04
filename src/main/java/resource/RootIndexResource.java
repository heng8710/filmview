package resource;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;


@Path("")
public class RootIndexResource {
	

		
	@GET
	@Produces("text/html")
	public Response doGet() throws Exception{
		return Response.seeOther(URI.create("/movie/recommend/random")).build();
	}
	
	
	
}
