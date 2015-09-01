package dao;

import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import jersey.repackaged.com.google.common.collect.Lists;
import jersey.repackaged.com.google.common.collect.Maps;

public class AiqiyiFilmDao {
	
	
	public static List<Map<String, Object>> get(final int pageNum, final String sqliteFilePath){
		final String sql = "select * from MOVIE where page_num = ?";
		try(final Connection  conn = sqlitetools.SqliteTools.conn(sqliteFilePath/*"F:/sqlite/movie/aiqiyi/orderbytime.sqlite"*/); final PreparedStatement ps = conn.prepareStatement(sql);){
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
				final String proxyImgUrl = "/movie/proxy?url="+ URLEncoder.encode(coverImg, "utf-8");
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
