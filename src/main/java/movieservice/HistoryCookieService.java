package movieservice;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import jersey.repackaged.com.google.common.collect.Lists;
import sqlitetools.SqliteTools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CharMatcher;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.LinkedHashMultimap;

import conf.GlobalSetting;


public class HistoryCookieService {
	
	final static Logger logger = Logger.getLogger("global");

	public final static String HISTORY_COOKIE_NAME = "history_cookie_name";
	final static CharMatcher cookieNameCharMatcher = CharMatcher.JAVA_LOWER_CASE.or(CharMatcher.DIGIT);
	
	private static String cookieKey(final HttpServletRequest request){
		String cookieVal = null;
		if(request.getCookies() != null){
			for(Cookie cookie: request.getCookies()){
				if(Objects.equal(HISTORY_COOKIE_NAME, cookie.getName())){
//					System.out.println("resource  >"+cookie.getValue());
					cookieVal = cookie.getValue();
					break;
				}
			}
		}
		if(Strings.isNullOrEmpty(cookieVal)){
			return UUID.randomUUID().toString().replaceAll("-", "");
		}
		if(cookieVal.length() != 32 || !cookieNameCharMatcher.matchesAllOf(cookieVal)){
			return UUID.randomUUID().toString().replaceAll("-", "");
		}
		return cookieVal;
	}
	
	final static int MAX_HISTORY_NUMBER = 10;
	
	
	public synchronized static String getCookie(final HttpServletRequest request){
		final String cookieKey = cookieKey(request);
		final Connection conn = conn();
		try{
			final Map<String, String> row = SqliteTools.getOneByColumnNames_2(conn, "cookie", new String[]{"id", "value"}, String.format("key = '%s'", cookieKey) , null, null);
			if(row != null && row.size() > 0){
				final String dbVal = row.get("value");
				return dbVal;
			}else{
				return null;
			}
		}catch(final Exception e){
			connForceNew();
			throw new IllegalStateException("在history_cookie_sqlite_file查询cookie时出错了", e);
		}
	}
	
	
	/**
	 * {@link #getCookie(HttpServletRequest)} 一样，只是返回值格式 不一样。
	 * 不抛出异常。
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public synchronized static List<Map<String, String>> getCookie_2(final HttpServletRequest request){
		try {
			final String rawStr = getCookie(request);
			if(Strings.isNullOrEmpty(rawStr)){
				return Lists.newArrayList();
			}
			final ObjectMapper mapper = new ObjectMapper();
			final List<Map<String, String>> raw = mapper.readValue(rawStr, List.class);
			return raw;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "解析history_cookie 的value失败", e);
			return Lists.newArrayList();
		}
	}
	
	
	
	public synchronized static List<Map<String, String>> getCookie_2_2(final HttpServletRequest request){
		final List<Map<String, String>> raw = getCookie_2(request);
		final List<Map<String, String>> r = Lists.newArrayListWithExpectedSize(raw.size());
		raw.forEach(map->{
			map.forEach((url, title)->{//！！要确保只有一个才行
				r.add(ImmutableMap.of("url", Strings.nullToEmpty(url), "title", Strings.nullToEmpty(title)));
			});
		});
		return r;
	}
	
	public synchronized static String updateCookie(final HttpServletRequest request, final String url, final String title){
		final String cookieKey = cookieKey(request);
		final Connection conn = conn();
		try{
			final Map<String, String> newItem = ImmutableMap.of(url, title);
			final ObjectMapper mapper = new ObjectMapper();
//			final String newItem = mapper.writeValueAsString(); //格式在这里
			final Map<String, String> row = SqliteTools.getOneByColumnNames_2(conn, "cookie", new String[]{"id", "value"}, String.format("key = '%s'", cookieKey) , null, null);
			
			final String r;
			if(row != null && row.size() > 0){
				final String dbVal = row.get("value");
				final List<Map<String, String>> list;
				if(Strings.isNullOrEmpty(dbVal)){
					list = Lists.newLinkedList();
				}else{
					list = mapper.readValue(dbVal, List.class);
				}
				for(final Iterator<Map<String, String>> it=list.iterator();it.hasNext();){
					final Map<String, String> item = it.next();
					for(final Entry<String, String> entry: item.entrySet()){
						if(Objects.equal(entry.getKey(), url)){//这里比较不需要格式
							it.remove();
							break;
						}
					}
				}
				if(list.size() > 0){
					list.add(0, newItem);
					r = mapper.writeValueAsString(list.subList(0, Math.min(MAX_HISTORY_NUMBER, list.size())));
				}else{
					r = mapper.writeValueAsString(Lists.newArrayList(newItem));
				}
				final String id = row.get("id");
				SqliteTools.update(conn, "cookie", String.format("id=%s", id), ImmutableMap.of("value", r));
			}else{
				r = mapper.writeValueAsString(Lists.newArrayList(newItem));
				SqliteTools.insert(conn, "cookie", ImmutableMap.of("key", cookieKey, "value", r, "create_time", System.currentTimeMillis()/1000L));
			}
			return cookieKey;
		}catch(final Exception e){
//			logger.log(Level.SEVERE, "在history_cookie_sqlite_file查询cookie时出错了", e);
			connForceNew();
			throw new IllegalStateException("在history_cookie_sqlite_file查询cookie时出错了", e);
		}
	}
	
	static ConnHolder connForceNew(){
		ConnHolder conn = connHolder;
		if(conn != null){
			try {
				conn.conn.close();
			} catch (final SQLException e) {
				logger.log(Level.SEVERE, "关闭旧的sqlite conn失败", e);
			}
		}
		conn = new ConnHolder();
		connHolder = conn;
		return conn;
	}
	

	static Connection conn(){
		ConnHolder conn = connHolder;
		if(conn == null || System.currentTimeMillis() - conn.time > 10*60*1000L/*10分钟*/){
			conn = connForceNew();
		}
		return conn.conn;
	}
	static ConnHolder connHolder;
	
	
	final static String createTableSql = "CREATE TABLE  IF NOT EXISTS 'cookie' ("
							+"'id'  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
							+"'key'  TEXT(100),"
							+"'value'  TEXT(2000),"
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
	
	
}
