package movieresource;

import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import jersey.repackaged.com.google.common.base.Objects;

import org.glassfish.jersey.client.ClientConfig;

//@Path("proxy")
public class ProxyResource {

	@GET
//	@Path("{pageNum}")
	public Response doGet(@QueryParam("url") String url) throws Exception{
		final String realUrl = URLDecoder.decode(url, "utf-8");
//		System.out.println("ourl="+ url);
//		System.out.println("realUrl=" + realUrl);
//		final byte[] bs = ListFilm.sth(pageNum);
//		System.out.println(new String(bs, Charsets.UTF_8));
		final Response r = get(realUrl);
		return Response.ok(r.readEntity(byte[].class)).type(r.getMediaType()).build();
	}
	
	private static Response get(final String url) throws Exception{
		final ClientConfig clientConfig = new ClientConfig();
//		clientConfig.register(MyClientResponseFilter.class);
//		clientConfig.register(new AnotherClientFilter());
		final Client client = ClientBuilder.newClient(clientConfig);
//		client.register(ThirdClientFilter.class);
//		Configuration newConfiguration = client.getConfiguration();
		final URL u = new URL(url);
		final String referer = u.getProtocol() + "://" + u.getHost() + (Objects.equal(80 , u.getPort())? "": (":"+ u.getPort())) + "/";  
		final Response r = client.target(url)
//			.request("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
			.request()
			.header("Referer", referer)
			.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36")
			.get();
		if(r.getStatus() < 200 || r.getStatus() >= 300){
			throw new IllegalStateException(String.format("访问aiqiyi电影=[%s]失败, 返回结果状态=[%s]", url, r.getStatus()));
		}
//		final byte[] bs = r.readEntity(byte[].class);
//		return bs;
		return r;
	}
}
