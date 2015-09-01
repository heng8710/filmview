package resource;

import java.net.URI;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import jersey.repackaged.com.google.common.collect.Maps;
import list.CommonFMRender;
import my.http.MyHttpGet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import sqlitetools.SqliteTools;
import uuid.MovieUUIDHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

import conf.GlobalSetting;


@Path("/edit")
public class FilmEditResource {

	@POST
	@Path("youku_play_url")
	@Produces("text/html")
	public Response post(@PathParam("type") final String type, @PathParam("id") final String qid, @PathParam("youku_play_url") final String youkuPlayUrl) throws Exception{
		//检查
		final String sqliteFile = sqliteFile(type);
		try(final Connection conn = SqliteTools.conn(sqliteFile)){
			final Map<String, String> row = SqliteTools.getOne_2(conn, "movie", String.format(" id = %s and (inactive != 1 or inactive is null)",qid), null, null);
			if(row == null || row.size() == 0){
				return Response.status(404).entity(String.format("找不到id=[%s]的记录", qid)).build();
			}
			final Map<String, String> moviePlayUrlMap = parseMoviePlayUrl(youkuPlayUrl);
			if(moviePlayUrlMap != null && moviePlayUrlMap.size() > 0){
				SqliteTools.update(conn, "movie", " id = "+ qid, new Object[]{new ObjectMapper().writeValueAsString(moviePlayUrlMap)}, new String[]{"features"});
			}
			return Response.seeOther(URI.create(String.format("/movie/edit/%s?id=%s", type, qid))).build();
		}catch (final Exception e) {
			return Response.serverError().entity(e).build();
		}
	}
	
	
	
	
	static Map<String, String> parseMoviePlayUrl(final String moviePlayUrl) throws Exception{
		final Map<String, String> palyUrls = Maps.newHashMap();
		final byte[] moviePlayPage = MyHttpGet.httpGet(moviePlayUrl);
		final Document doc = Jsoup.parse(new String(moviePlayPage, Charset.forName("utf-8")));
		final Elements embed = doc.select("#link3");
		if(embed.size() == 1){
			palyUrls.put("embed", embed.get(0).attr("value"));
		}
		
		final Elements direct = doc.select("#link1");
		if(direct.size() == 1){
			palyUrls.put("direct", direct.get(0).attr("value"));
		}
		
		final Elements flash = doc.select("#link2");
		if(flash.size() == 1){
			palyUrls.put("flash", flash.get(0).attr("value"));
		}
		
		final Elements common = doc.select("#link4");
		if(common.size() == 1){
			palyUrls.put("common", common.get(0).attr("value"));
		}
		return palyUrls;
	}
	
	
	
	
	@GET
	@Path("youku_play_url/{type}")
	@Produces("text/html")
	public Response get(@PathParam("type") final String type, @QueryParam("id") final String qid, @QueryParam("uuid") final String quuid) throws Exception{
		//检查
		final String sqliteFile = sqliteFile(type);
		final Long id = Strings.isNullOrEmpty(qid)? MovieUUIDHelper.id(quuid): Long.valueOf(qid);
		if(id == null){
			return Response.status(404).entity(String.format("uuid=[%s], id=[%s]，都不正确", quuid, qid)).build();
		}
		try(final Connection conn = SqliteTools.conn(sqliteFile)){
			final Map<String, String> row = SqliteTools.getOne_2(conn, "movie", String.format(" id = %s and (inactive != 1 or inactive is null)",id), null, null);
			if(row == null || row.size() == 0){
				return Response.status(404).entity(String.format("找不到id=[%s]的记录", qid)).build();
			}
			for(final Entry<String,String> entry: row.entrySet()){
				final String key = entry.getKey();
				final String val = entry.getValue();
				if(val == null){
					row.put(key, "");
				}
			}
			row.put("uuid", MovieUUIDHelper.uuid(id));
			row.put("type", type);
			return Response.ok(CommonFMRender.render(row, "list/edit.html")).build();
		}catch (final Exception e) {
			return Response.serverError().entity(e).build();
		}
	}
	
	
	
	static String sqliteFile(final String type){
		final String sqliteFile;
		switch(type){
			case "hk":{
				sqliteFile = GlobalSetting.getStringByPath("yk_hk_sqlite_file");
				break;
			}
			case "dalu":{
				sqliteFile = GlobalSetting.getStringByPath("yk_dalu_sqlite_file");
				break;
			}
			case "tw":{
				sqliteFile = GlobalSetting.getStringByPath("yk_tw_sqlite_file");
				break;
			}
			default: throw new IllegalArgumentException();
		}
		return sqliteFile;
	}
}
