package crawer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import jersey.repackaged.com.google.common.collect.Maps;

import org.apache.commons.codec.Charsets;
import org.glassfish.jersey.client.ClientConfig;
import org.w3c.dom.Document;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlListItem;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * http://list.iqiyi.com/www/1/-------------【4或者11】-【页数，1到30】-1-iqiyi--.html
 * 4:表示按更新时间排序
 * 11：表示按热闹程序排序
 * @author heng
 *
 */
public class _AiqiyiAll2 {

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
	
	
	public static List<HtmlListItem> li(final int pageNum){
		try(final WebClient c = new WebClient(BrowserVersion.CHROME)){
			final String targetUrl = String.format("http://list.iqiyi.com/www/1/-------------4-%s-1-iqiyi--.html", pageNum);
			final String refer = (pageNum > 1) ? String.format("http://list.iqiyi.com/www/1/-------------4-%s-1-iqiyi--.html", pageNum-1) : "http://www.iqiyi.com/dianying/";
	//		final Response r = client.target(targetUrl)
	//			.request("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
	//			.header("Referer", refer)
	//			.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36")
	//			.get();
			c.addRequestHeader("Context-Type", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			c.addRequestHeader("Referer", refer);
		    final HtmlPage p;
			try {
				p = c.getPage(targetUrl);
			} catch (FailingHttpStatusCodeException | IOException e) {
				throw new IllegalStateException("访问代理页面失败，可能是页面有问题了", e);
			}
		    final List<HtmlListItem> lx = (List<HtmlListItem>)p.getByXPath("//div[@class='wrapper-cols']//li");
	    	if(lx == null || lx.size() <= 0){
	    		throw new IllegalStateException("找不到页面中的li信息了：【//div[@class='wrapper-cols']//li】");
	    	}else{
	    		return lx;
	    	}
		}catch(final Exception e){
			throw new IllegalStateException(e);
		}
	}
	
	public static List<Map<String, Object>> parseLis(final List<HtmlListItem> lis){
		final List<Map<String, Object>> r = Lists.newArrayList();
		for(final HtmlListItem li: lis){
			final Map<String, Object> m = Maps.newHashMap();
//			Object o = li.getByXPath("//a/@href");
//			System.out.println(o);
			HtmlDivision picDiv = (HtmlDivision)li.getElementsByAttribute("div", "class", "site-piclist_pic").get(0);
			HtmlAnchor a = (HtmlAnchor)picDiv.getElementsByTagName("a").get(0);
			String href = a.getAttribute("href");
			m.put("url", href);
			String title = a.getAttribute("title");
			m.put("title", title);
//			String href = a.getAttribute("href");
			
			String img = picDiv.getElementsByTagName("img").get(0).getAttribute("src");
			
			m.put("img", img);
			
			String time = picDiv.getElementsByAttribute("div", "class", "wrapper-listTitle").get(0).asText();
			m.put("time", time);
			
			HtmlDivision infoDiv = (HtmlDivision)li.getElementsByAttribute("div", "class", "site-piclist_info").get(0);
			String info = infoDiv.asText();
			m.put("info", info);
			
			r.add(m);
		}
		return r;
	}
	
	public static void main(String... args) throws Exception{
//		final byte[] htmlBytes = getPage(1);
//		final List<String> liStrList = cleanRawHtml(htmlBytes);
////		pa(htmlBytes);
//		parseLi(liStrList);
		
		List<HtmlListItem> lis = li(1);
//		System.out.println(lis);
		parseLis(lis);
	}
	
	
	public static List<Map<String, Object>> get(final int pageNum){
		final String sql = "select * from MOVIE where page_num = ?";
		try(final Connection  conn = sqlitetools.SqliteTools.conn("F:/sqlite/movie/aiqiyi/orderbytime.sqlite"); final PreparedStatement ps = conn.prepareStatement(sql);){
			ps.setInt(1, pageNum);
			final ResultSet rs = ps.executeQuery();
			final List<Map<String, Object>> r = Lists.newArrayList();
			for(;rs.next();){
				final Map<String, Object> i = Maps.newHashMap();
				final String url = rs.getString("url");
				final String title = rs.getString("title");
				final String coverImg = rs.getString("cover_img");
				final String movieTime = rs.getString("movie_time");
				final String info = rs.getString("info");
				
				i.put("url", url);
				i.put("title", title);
				final String proxyImgUrl = "/filmview/proxy?url="+ URLEncoder.encode(coverImg, "utf-8");
				i.put("img", proxyImgUrl);
				i.put("time", movieTime);
				i.put("info", info);
				
				r.add(i);
			}
			return r;
		}catch(final Exception e){
			throw new IllegalStateException(e);
		}
	}
}
