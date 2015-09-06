package filter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.PathSegment;

import jersey.repackaged.com.google.common.base.Objects;
import sqlitetools.SqliteTools;

import com.google.common.base.Strings;

import conf.GlobalSetting;

/**
 * 针对播放页面
 */
public class HistoryCookieFilter implements ContainerRequestFilter{
	
	final static Logger logger = Logger.getLogger("global");

	@Context   
    private HttpServletRequest req;
	
	
	synchronized static Connection conn(){
		ConnHolder conn = connHolder;
		if(conn == null || System.currentTimeMillis() - conn.time > 10*60*1000L/*10分钟*/){
			conn = new ConnHolder();
			connHolder = conn;
		}
		return conn.conn;
	}
	static ConnHolder connHolder;
	/**
CREATE TABLE  IF NOT EXISTS 'movie' (
'id'  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
'movie_name'  TEXT(100),
'actors'  TEXT(100),
'jieshaoye'  TEXT(200),
'play_summary'  INTEGER,
'qinxidu'  INTEGER,
'pingfen'  INTEGER,
'img_url'  TEXT(100),
'page_num'  INTEGER,
'features'  TEXT(2000)
)
	 */
	final static String createTableSql = "CREATE TABLE  IF NOT EXISTS 'cookie' ("
							+"'id'  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
							+"'cookie_name'  TEXT(100),"
							+"'cookie_value'  TEXT(100),"
							+"'create_time'  INTEGER"
							+")"; 
	
	final static class ConnHolder{
		final long time;
		final Connection conn;
		ConnHolder() {
			this.time = System.currentTimeMillis();
			final String sqliteRootFolderPath = GlobalSetting.getStringByPath("history_cookie_sqlite_file");
			if(Strings.isNullOrEmpty(sqliteRootFolderPath)){
				throw new IllegalArgumentException(String.format("history_cookie_sqlite_file=[%s] 不正确", sqliteRootFolderPath));
			}
			this.conn = SqliteTools.conn(sqliteRootFolderPath);
			try(final Statement st = this.conn.createStatement()){
				st.execute(createTableSql);
			} catch (final SQLException e) {
				throw new IllegalStateException("初始化history_cookie的表失败", e);
			}
		}
	} 
	
	
	
	public final static String HISTORY_COOKIE_NAME = "history_cookie_name";
	

//	@Override
	public void filter(ContainerRequestContext requestContext,
			ContainerResponseContext responseContext) throws IOException {
		final List<PathSegment>  ps = requestContext.getUriInfo().getPathSegments();
		if(ps.size() > 0 && Objects.equal("play", ps.get(0).getPath())){
//			final String ip = getIpAddr(req);
//			if(counter(ip) > 30){
//				requestContext.abortWith(Response.ok("search limit", MediaType.TEXT_PLAIN).build());
//				return ;
//			}
			final Map<String, Cookie> allCookies = requestContext.getCookies();
			final Cookie historyCookie = allCookies.get(HISTORY_COOKIE_NAME);
			System.out.println("filter-->"+historyCookie);
			if(historyCookie == null){
				final NewCookie nc = new NewCookie(HISTORY_COOKIE_NAME, "1234");
				allCookies.put(HISTORY_COOKIE_NAME, nc);
			}else{
				int newVal = Integer.valueOf(historyCookie.getValue()) + 1;
				final NewCookie nc = new NewCookie(HISTORY_COOKIE_NAME, String.valueOf(newVal));
				allCookies.put(HISTORY_COOKIE_NAME, nc);
			}
		}
	}


	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		// TODO Auto-generated method stub
		final List<PathSegment>  ps = requestContext.getUriInfo().getPathSegments();
		if(ps.size() > 0 && Objects.equal("play", ps.get(0).getPath())){
	//		final String ip = getIpAddr(req);
	//		if(counter(ip) > 30){
	//			requestContext.abortWith(Response.ok("search limit", MediaType.TEXT_PLAIN).build());
	//			return ;
	//		}
			final Map<String, Cookie> allCookies = requestContext.getCookies();
			final Cookie historyCookie = allCookies.get(HISTORY_COOKIE_NAME);
			System.out.println("filter-->"+historyCookie);
			if(historyCookie == null){
				final NewCookie nc = new NewCookie(HISTORY_COOKIE_NAME, "1234");
				allCookies.put(HISTORY_COOKIE_NAME, nc);
			}else{
				int newVal = Integer.valueOf(historyCookie.getValue()) + 1;
				final NewCookie nc = new NewCookie(HISTORY_COOKIE_NAME, String.valueOf(newVal));
				allCookies.put(HISTORY_COOKIE_NAME, nc);
			}
		}
	}
}
