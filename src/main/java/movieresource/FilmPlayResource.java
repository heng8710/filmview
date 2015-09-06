package movieresource;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import movieservice.HistoryCookieService;

import org.jsoup.Jsoup;

import render.PlayFilm;
import tools.SqliteHelper;
import tools.TypeHelper;
import uuid.MovieUUIDHelper;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import dao.YoukuHKFreeFilmDao;
import filter.HistoryCookieFilter;

@Path("play")
public class FilmPlayResource {

	
	@GET
	@Path("/yk/{type}/{id}")
	@Produces("text/html")
	public Response doGet(@PathParam("type") final String type, @PathParam("id") final String uuid
			,@Context final HttpServletRequest request
			) throws Exception{
		//过滤id
		final Long id = MovieUUIDHelper.id(uuid);
		if(id == null){
			return Response.status(404).entity(String.format("id=[%s]不正确", uuid)).build();
		}
		if(!TypeHelper.isTypeRight(type)){
			return Response.status(404).entity(String.format("类型=[%s]不正确", type)).build();
		}
		final String targetSqliteFile = SqliteHelper.getTargetSqliteFile("yk_sqlite_folder", type);
		final List<Map<String, Object>> li = YoukuHKFreeFilmDao.getBy(String.format(" id = %s and (inactive != 1 or inactive is null)",id), null, targetSqliteFile,"/movie/play/yk/"+type+"/%s",  "/movie/coverimg/yk/"+type+"/%s");
		if(li != null && li.size() == 1){
			final Map<String, Object> movie = li.get(0);
			final Map<String, Object> features = (Map<String, Object>)movie.get("features");
			features.put("flash", Strings.nullToEmpty((String)features.get("flash")));
			
			if(Strings.isNullOrEmpty((String)features.get("flash"))){
				features.put("flash", "");
			}else{
				final String flash = (String)features.get("flash");
				if(flash.indexOf('&') > -1){
					features.put("flash", flash + "&isAutoPlay=true&showAd=0");
				}else{
					features.put("flash", flash + "&isAutoPlay=true&showAd=0");
				}
//				final URIBuilder ub = new URIBuilder(flash);
//				ub.addParameter("isAutoPlay", "true");//isAutoPlay=true&showAd=0
//				ub.addParameter("showAd", "0");
//				features.put("flash", ub.build().toString());
			}
			final String common = (String)features.get("common");
			if(Strings.isNullOrEmpty(common)){
				features.put("iframe", "");
			}else{
				//<iframe height=498 width=510 src=\"http://player.youku.com/embed/XNTAwNDExOTIw\" frameborder=0 allowfullscreen></iframe>
				final String iframe = Jsoup.parseBodyFragment(common).select("iframe").attr("src");
				if(iframe.indexOf('&') > -1){
					features.put("iframe", iframe + "&isAutoPlay=true&showAd=0");
				}else{
					features.put("iframe", iframe + "&isAutoPlay=true&showAd=0");
				}
//				final URIBuilder ub = new URIBuilder(iframe);
//				ub.addParameter("isAutoPlay", "true");//isAutoPlay=true&showAd=0
//				ub.addParameter("showAd", "0");
//				features.put("iframe", ub.build().toString());
//				features.put("iframe", Strings.nullToEmpty(iframe));
			}
			final String newHistoryCookieVal = HistoryCookieService.updateCookie(request, String.format("/movie/play/yk/%s/%s",type, uuid), (String)movie.get("title"));
			//request.getHeader("Host")
			final NewCookie cookie = new NewCookie(HistoryCookieService.HISTORY_COOKIE_NAME, newHistoryCookieVal, "/", null , NewCookie.DEFAULT_VERSION, null, 30*24* 60*60, new Date(System.currentTimeMillis() + 30*24* 60*60* 1000L), false, true);
			final List<Map<String,String>> historyCookieVal = HistoryCookieService.getCookie_2_2(request);
			final byte[] bs = PlayFilm.render(movie, type, uuid, historyCookieVal);
			return Response.ok(bs).cookie(cookie).build();
		}
		return Response.seeOther(URI.create("list/"+type)).build();
	}
	
	
}
