package resource;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import list.PlayFilm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import uuid.MovieUUIDHelper;

import com.google.common.base.Strings;

import conf.GlobalSetting;
import dao.YoukuHKFreeFilmDao;

@Path("play")
public class FilmPlayResource {

	
//	@GET
//	@Path("/aiqiyi/updatetime/{pageNum}")
//	@Produces("text/html")
//	public Response updatetime1(@PathParam("pageNum") final Integer pageNum) throws Exception{
//		final List<Map<String, Object>> li = AiqiyiFilmDao.get(pageNum, (String)GlobalSetting.getByPath("orderbyupdatetime_sqlite_file"));
//		final byte[] bs = ListFilm.sth(li, pageNum, 30/*这个是要改的，要看对方的资源是否是变化 了*/, "/filmview/list/aiqiyi/updatetime/%s");
////		System.out.println(new String(bs, Charsets.UTF_8));
//		return Response.ok(bs).build();
//	}
	
	@GET
	@Path("/yk/hk/{id}")
	@Produces("text/html")
	public Response hk(@PathParam("id") final String uuid) throws Exception{
		//过滤id
		final Long id = MovieUUIDHelper.id(uuid);
		if(id == null){
			return Response.status(404).build();
		}
		final List<Map<String, Object>> li = YoukuHKFreeFilmDao.getBy(String.format(" id = %s and (inactive != 1 or inactive is null)",id), null, (String)GlobalSetting.getByPath("yk_hk_sqlite_file"),"/movie/play/yk/hk/%s",  "/movie/coverimg/yk/hk/%s");
		if(li != null && li.size() == 1){
			final Map<String, Object> movie = li.get(0);
			final Map<String, Object> features = (Map<String, Object>)movie.get("features");
			features.put("flash", Strings.nullToEmpty((String)features.get("flash")));
			final String common = (String)features.get("common");
			if(!Strings.isNullOrEmpty(common)){
				//<iframe height=498 width=510 src=\"http://player.youku.com/embed/XNTAwNDExOTIw\" frameborder=0 allowfullscreen></iframe>
				final String iframe = Jsoup.parseBodyFragment(common).select("iframe").attr("src");
				features.put("iframe", Strings.nullToEmpty(iframe));
			}else{
				features.put("iframe", "");
			}
			final byte[] bs = PlayFilm.render(movie);
			return Response.ok(bs).build();
		}
		return Response.seeOther(URI.create("list/yk")).build();
	}
	
	
	@GET
	@Path("/yk/dalu/{id}")
	@Produces("text/html")
	public Response dalu(@PathParam("id") final String uuid) throws Exception{
		//过滤id
		final Long id = MovieUUIDHelper.id(uuid);
		if(id == null){
			return Response.status(404).build();
		}
		final List<Map<String, Object>> li = YoukuHKFreeFilmDao.getBy(String.format(" id = %s and (inactive != 1 or inactive is null)",id), null, (String)GlobalSetting.getByPath("yk_dalu_sqlite_file"),"/movie/play/yk/dalu/%s",  "/movie/coverimg/yk/dalu/%s");
		if(li != null && li.size() == 1){
			final Map<String, Object> movie = li.get(0);
			final Map<String, Object> features = (Map<String, Object>)movie.get("features");
			features.put("flash", Strings.nullToEmpty((String)features.get("flash")));
			final String common = (String)features.get("common");
			if(!Strings.isNullOrEmpty(common)){
				//<iframe height=498 width=510 src=\"http://player.youku.com/embed/XNTAwNDExOTIw\" frameborder=0 allowfullscreen></iframe>
				final String iframe = Jsoup.parseBodyFragment(common).select("iframe").attr("src");
				features.put("iframe", Strings.nullToEmpty(iframe));
			}else{
				features.put("iframe", "");
			}
			final byte[] bs = PlayFilm.render(movie);
			return Response.ok(bs).build();
		}
		return Response.seeOther(URI.create("list/dalu")).build();
	}
	
	
	
	@GET
	@Path("/yk/tw/{id}")
	@Produces("text/html")
	public Response tw(@PathParam("id") final String uuid) throws Exception{
		//过滤id
		final Long id = MovieUUIDHelper.id(uuid);
		if(id == null){
			return Response.status(404).build();
		}
		final List<Map<String, Object>> li = YoukuHKFreeFilmDao.getBy(String.format(" id = %s and (inactive != 1 or inactive is null)",id), null, (String)GlobalSetting.getByPath("yk_tw_sqlite_file"),"/movie/play/yk/tw/%s",  "/movie/coverimg/yk/tw/%s");
		if(li != null && li.size() == 1){
			final Map<String, Object> movie = li.get(0);
			final Map<String, Object> features = (Map<String, Object>)movie.get("features");
			features.put("flash", Strings.nullToEmpty((String)features.get("flash")));
			final String common = (String)features.get("common");
			if(!Strings.isNullOrEmpty(common)){
				//<iframe height=498 width=510 src=\"http://player.youku.com/embed/XNTAwNDExOTIw\" frameborder=0 allowfullscreen></iframe>
				final String iframe = Jsoup.parseBodyFragment(common).select("iframe").attr("src");
				features.put("iframe", Strings.nullToEmpty(iframe));
			}else{
				features.put("iframe", "");
			}
			final byte[] bs = PlayFilm.render(movie);
			return Response.ok(bs).build();
		}
		return Response.seeOther(URI.create("list/tw")).build();
	}
	
	
//	@GET
//	@Path("/aiqiyi/updatetime")
//	public Response updatetime2( ) throws Exception{
//		return Response.seeOther(URI.create("list/aiqiyi/updatetime/1")).build();
//	}
//	
//	@GET
//	@Path("/aiqiyi")
//	public Response updatetime3( ) throws Exception{
//		return Response.seeOther(URI.create("list/aiqiyi/updatetime/1")).build();
//	}
//	
//	@GET
//	@Path("/yk")
//	public Response yk2( ) throws Exception{
//		return Response.seeOther(URI.create("list/yk/1")).build();
//	}
//	
//	
//	@GET
//	@Path("/aiqiyi/heat/{pageNum}")
//	@Produces("text/html")
//	public Response heat1(@PathParam("pageNum") final Integer pageNum) throws Exception{
//		final List<Map<String, Object>> li = AiqiyiFilmDao.get(pageNum, (String)GlobalSetting.getByPath("orderbyheat_sqlite_file"));
//		final byte[] bs = ListFilm.sth(li, pageNum, 30/*这个是要改的，要看对方的资源是否是变化 了*/, "/filmview/list/aiqiyi/heat/%s");
////		System.out.println(new String(bs, Charsets.UTF_8));
//		return Response.ok(bs).build();
//	}
//	
//	
//	@GET
//	@Path("/aiqiyi/heat")
//	public Response heat1( ) throws Exception{
//		return Response.seeOther(URI.create("list/aiqiyi/heat/1")).build();
//	}
}
