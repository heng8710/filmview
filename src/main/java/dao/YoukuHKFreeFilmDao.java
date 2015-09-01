package dao;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import jersey.repackaged.com.google.common.collect.Lists;
import jersey.repackaged.com.google.common.collect.Maps;
import sqlitetools.SqliteTools;
import uuid.MovieUUIDHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

public class YoukuHKFreeFilmDao {
	
	
	
	
//	public static List<Map<String, Object>> get(final int pageNum, final String sqliteFilePath){
//		final String sql = "select * from MOVIE where page_num = ?";
//		try(final Connection  conn = tools.SqliteTools.conn(sqliteFilePath/*"F:/sqlite/movie/aiqiyi/orderbytime.sqlite"*/); final PreparedStatement ps = conn.prepareStatement(sql);){
//			ps.setInt(1, pageNum);
//			final ResultSet rs = ps.executeQuery();
//			final List<Map<String, Object>> r = Lists.newArrayList();
//			for(;rs.next();){
//				final Map<String, Object> i = Maps.newHashMap();
//				final String url = rs.getString("url");
//				final String title = rs.getString("title");
//				final String coverImg = rs.getString("cover_img");
//				final String movieTime = rs.getString("movie_time");
//				final String info = rs.getString("info");
//				
//				i.put("url", url);
//				i.put("title", title);
//				final String proxyImgUrl = "/filmview/proxy?url="+ URLEncoder.encode(coverImg, "utf-8");
//				i.put("img", proxyImgUrl);
//				i.put("time", movieTime);
//				i.put("info", info);
//				
//				r.add(i);
//			}
//			return r;
//		}catch(final Exception e){
//			throw new IllegalStateException(e);
//		}
//	}
	
	
	
	public static int getMaxPageNum(final String sqliteFilePath){
		try(final Connection  conn = sqlitetools.SqliteTools.conn(sqliteFilePath/*"F:/sqlite/movie/aiqiyi/orderbytime.sqlite"*/)){
			final List<String>  query = SqliteTools.getOneByColumnNames_1(conn, "movie", new String[]{"max(page_num)"}, null, null, null);
			if(query.size() > 0){
				return Integer.valueOf(query.get(0));
			}
			throw new IllegalStateException();
		}catch(final Exception e){
			throw new IllegalStateException(e);
		}
	}
	
	public static List<Map<String, Object>> getBy(final String where, final String orderBy, final String sqliteFilePath,  final String moviePlayUrlFormatter, final String coverImgUrlFormatter){
		try(final Connection  conn = sqlitetools.SqliteTools.conn(sqliteFilePath/*"F:/sqlite/movie/aiqiyi/orderbytime.sqlite"*/)){
			final List<Map<String, String>>  query = SqliteTools.getAll_2(conn, "movie", where, orderBy, null, null);
			final List<Map<String, Object>> r = Lists.newArrayList();
			for(final Map<String, String> row: query){
				final Map<String, Object> i = Maps.newHashMap();
				final Map<String, Object> features = new ObjectMapper().readValue(row.get("features"), Map.class);
				final String id = row.get("id");
				String url = String.format(moviePlayUrlFormatter, MovieUUIDHelper.uuid(Long.valueOf(id)));//(String)features.get("direct");
				final String title = row.get("movie_name");
				final String coverImg = row.get("img_url");
				final String movieTime = "";
				final String info = row.get("actors");
				
//				if(Strings.isNullOrEmpty(url)){
//					url = (String)features.get("yk_api_search_link");
//				}
				i.put("url", Strings.nullToEmpty(url));
				
				i.put("title", title);
				final String proxyImgUrl = String.format(coverImgUrlFormatter, MovieUUIDHelper.uuid(Long.valueOf(id)));//"/movie/coverimg/yk/" + id;//"/filmview/proxy?url="+ URLEncoder.encode(coverImg, "utf-8");
				i.put("img", proxyImgUrl);
				i.put("time", movieTime);
				i.put("info", info);
				i.put("features", features);
//				for(final Map<String, Object> entry: li){
//					entry.put("img_url", );
//				}
				r.add(i);
			}
			return r;
		}catch(final Exception e){
			throw new IllegalStateException(e);
		}
	}
}
