package crawer;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import jersey.repackaged.com.google.common.collect.Lists;

import org.apache.commons.codec.Charsets;
import org.glassfish.jersey.client.ClientConfig;
import org.w3c.dom.Document;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * http://list.iqiyi.com/www/1/-------------【4或者11】-【页数，1到30】-1-iqiyi--.html
 * 4:表示按更新时间排序
 * 11：表示按热闹程序排序
 * @author heng
 *
 */
public class _AiqiyiAll {

	public static byte[] getPage(final int pageNum){
		ClientConfig clientConfig = new ClientConfig();
//		clientConfig.register(MyClientResponseFilter.class);
//		clientConfig.register(new AnotherClientFilter());
		Client client = ClientBuilder.newClient(clientConfig);
//		client.register(ThirdClientFilter.class);
//		Configuration newConfiguration = client.getConfiguration();
		final String targetUrl = String.format("http://list.iqiyi.com/www/1/-------------4-%s-1-iqiyi--.html", pageNum);
		final String refer = (pageNum > 1) ? String.format("http://list.iqiyi.com/www/1/-------------4-%s-1-iqiyi--.html", pageNum-1) : "http://www.iqiyi.com/dianying/";
		final Response r = client.target(targetUrl)
			.request("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
			.header("Referer", refer)
			.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36")
			.get();
		if(r.getStatus() < 200 || r.getStatus() >= 300){
			throw new IllegalStateException(String.format("访问aiqiyi电影=[%s]失败, 返回结果状态=[%s]", targetUrl, r.getStatus()));
		}
		final byte[] html = r.readEntity(byte[].class);
//		System.out.println(new String(html, Charsets.UTF_8));
		return html;
	}
	
	
	/**
	 * 对方做了保护，不是正常的xml格式，这里直接使用正则，截取需要的xml部分出来始可
	 * @param htmlBytes
	 */
	static List<String> cleanRawHtml(final byte[] htmlBytes){
		final String rawStr = new String(htmlBytes, Charsets.UTF_8);
//		final Pattern p = Pattern.compile("<div%sclass%s=%s\"wrapper-cols\"%s>", Pattern.CASE_INSENSITIVE);
//		final Pattern p = Pattern.compile("\"wrapper-cols\"", Pattern.CASE_INSENSITIVE);
//		final int start = p.matcher(rawStr).find().start();
//		System.out.println(rawStr.substring(start, start + 10) );
//		System.out.println(p.matcher(rawStr).find());//true
		
		final List<String> liStrList = Lists.newArrayList();
		final int wrapper_cols_start = rawStr.indexOf("\"wrapper-cols\"");
		final int left = rawStr.indexOf("<ul", wrapper_cols_start);
		final int right = rawStr.indexOf("</ul", left);
		int curStart = wrapper_cols_start;
		int curEnd = curStart;
		for(;;){
			curStart = rawStr.indexOf("<li", curEnd);
			curEnd = rawStr.indexOf("</li", curEnd +1);
			if((curStart < 0 || curStart < left || curStart > right)
					||(curEnd < 0 || curEnd < left || curEnd > right)){
				break;
			}
			if(left < curStart && left < curEnd && curStart < right && curEnd < right){
				liStrList.add(rawStr.substring(curStart, curEnd) + "</li>");
			}
		}
//		System.out.println(rawStr.substring(jianQuoHaoStart, jianQuoHaoEnd+ 1));
		return liStrList;
	}
	
	static void parseLi(List<String> liStrList) throws Exception{
		final List<Map<String, Object>> r = Lists.newArrayList();
		for(final String s: liStrList){
			// 解析文件，生成document对象
			final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			//&amp;的问题以后再说
			final Document document = builder.parse(new ByteArrayInputStream(s.replaceAll("&", "&amp;").getBytes("utf-8")));
			// 生成XPath对象
			final XPath xpath = XPathFactory.newInstance().newXPath();
			
			// 获取节点值
			final String href = (String) xpath.evaluate("/li/div[@class='site-piclist_pic']//a/@href", document,XPathConstants.STRING);
			final String title = (String) xpath.evaluate("/li/div[@class='site-piclist_pic']//a/@title", document,XPathConstants.STRING);
			final String img = (String) xpath.evaluate("/li/div[@class='site-piclist_pic']//img/@src", document,XPathConstants.STRING);
			System.out.println(href);
			System.out.println(title);
			System.out.println(img);
			
			final String time = (String) xpath.evaluate("/li/div[@class='site-piclist_pic']//div[@class='wrapper-listTitle']//p[@class='textOverflow']/text()", document,XPathConstants.STRING);
			
//			final String time = (String) xpath.evaluate("/li/div[@class='site-piclist_info']//div[@class='wrapper-listTitle']//p[@class='textOverflow']/text()", document,XPathConstants.STRING);
			System.out.println(time.trim());
			
//			for(int i=0;i<nList.getLength();i++){
//				String s = nList.item(i).toString();
//			}
//			System.out.println(nList);
		}
		
	}
	
	static void pa(final byte[] htmlBytes) throws Exception{
//		final Parser parser = 
		XmlMapper xml = new XmlMapper();
		
		Map<String, Object> m = xml.readValue(htmlBytes, Map.class);
		System.out.println(m);
	}
	
	public static void main(String... args) throws Exception{
		final byte[] htmlBytes = getPage(1);
		final List<String> liStrList = cleanRawHtml(htmlBytes);
//		pa(htmlBytes);
		parseLi(liStrList);
	}
}
