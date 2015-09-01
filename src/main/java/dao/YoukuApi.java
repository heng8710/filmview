package dao;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Random;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;

public class YoukuApi {

	
	public static void main(String... args){
		byte[] bs = searchMovieByName("澳门风云2");
		System.out.println(new String(bs, Charset.forName("utf-8")));
	}
	
	
	public static byte[] searchMovieByName(final String name){
		//https://openapi.youku.com/v2/searches/video/by_keyword.json
		final ClientConfig clientConfig = new ClientConfig();
		final Client client = ClientBuilder.newClient(clientConfig);
//		final String refer = referer("https://openapi.youku.com/v2/searches/video/by_keyword.json");
		final String target = "https://openapi.youku.com/v2/searches/video/by_keyword.json";
		final Response r = client.target(target)
			.queryParam("client_id", "4e275bb143491128")
			.queryParam("keyword", name)
			.queryParam("public_type", "all")
			.queryParam("paid", 0)
			.queryParam("timemore", 60)
			.queryParam("page", 1)
			.queryParam("count", 1)
			.request()
//			.request("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
//			.header("Referer", refer)
//			.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36")
			.get();
		if(r.getStatus() < 200 || r.getStatus() >= 300){
			throw new IllegalStateException(String.format("访问youku电影=[%s]失败, 返回结果状态=[%s]", target, r.getStatus()));
		}
		return r.readEntity(byte[].class);
	}
	
	
	public static String referer(final String target) throws MalformedURLException{
		final URL u = new URL(target);
		final StringBuilder sb = new StringBuilder(u.getProtocol()).append( "://" ).append( u.getHost() ) .append(((u.getPort() == 80 || u.getPort() <= 0)? "" : u.getPort()))  .append("/");   
		final Random r = new Random();
		//30-127
//		for(int i=0; i< 10; i++){
//			sb.append((char)(r.nextInt(97) + 30));
//		}
		return sb.toString();
//		for(int i=0;i<5;i++){
//			//http[s]://xxx.xx.xx/
//			int index = target.indexOf('/', target.indexOf('/', target.indexOf('/')));
//			index += r.nextInt(target.length() - index-2);
//			char c = target.charAt(index);
//			r.ints(randomNumberOrigin, randomNumberBound)
//			c += r.ne
//		}
	
	}
}
