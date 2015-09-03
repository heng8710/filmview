package resource;

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

import tools.SqliteHelper;
import tools.TypeHelper;
import uuid.MovieUUIDHelper;

import com.google.common.base.Strings;

import dao.YoukuHKFreeFilmDao;

@Path("play")
public class FilmPlayResource {

	
	@GET
	@Path("/yk/{type}/{id}")
	@Produces("text/html")
	public Response doGet(@PathParam("type") final String type, @PathParam("id") final String uuid) throws Exception{
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
			final String common = (String)features.get("common");
			if(!Strings.isNullOrEmpty(common)){
				//<iframe height=498 width=510 src=\"http://player.youku.com/embed/XNTAwNDExOTIw\" frameborder=0 allowfullscreen></iframe>
				final String iframe = Jsoup.parseBodyFragment(common).select("iframe").attr("src");
				features.put("iframe", Strings.nullToEmpty(iframe));
			}else{
				features.put("iframe", "");
			}
			final byte[] bs = PlayFilm.render(movie, type, uuid);
			return Response.ok(bs).build();
		}
		return Response.seeOther(URI.create("list/"+type)).build();
	}
	
	
}
