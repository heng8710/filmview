package resource;

import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import list.ListFilm;
import conf.GlobalSetting;
import dao.YoukuHKFreeFilmDao;

@Path("list")
public class FilmListResource {

	
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
	@Path("/yk/hk/{pageNum}")
	@Produces("text/html")
	public Response yk_hk_1(@PathParam("pageNum") final Integer pageNum) throws Exception{
		final String sqliteFilePath = (String)GlobalSetting.getByPath("yk_hk_sqlite_file");
		if(!Paths.get(sqliteFilePath).toFile().isFile()){
			throw new IllegalStateException(String.format("sqlite文件=[%s]不存在", sqliteFilePath));
		}
		
		final int maxPageNum = YoukuHKFreeFilmDao.getMaxPageNum(sqliteFilePath);
		if(maxPageNum < 1){
			return Response.serverError().build();
		}
		if(pageNum > maxPageNum){
			return Response.seeOther(URI.create("list/yk/hk/"+ maxPageNum)).build();
		}
		if(pageNum < 1){
			return Response.seeOther(URI.create("list/yk/hk/"+ 1)).build();
		}
		final List<Map<String, Object>> li = YoukuHKFreeFilmDao.getBy(String.format(" page_num = %s and (inactive != 1 or inactive is null)",pageNum), " order by id asc , weight desc ",  sqliteFilePath,"/movie/play/yk/hk/%s",  "/movie/coverimg/yk/hk/%s");
		
		final byte[] bs = ListFilm.render(li,"香港电影", pageNum, maxPageNum/*这个是要改的，要看对方的资源是否是变化 了*/, "/movie/list/yk/hk/%s");
		return Response.ok(bs).build();
	}
	
	
	@GET
	@Path("/yk/dalu/{pageNum}")
	@Produces("text/html")
	public Response yk_dalu_1(@PathParam("pageNum") final Integer pageNum) throws Exception{
		final String sqliteFilePath = (String)GlobalSetting.getByPath("yk_dalu_sqlite_file");
		if(!Paths.get(sqliteFilePath).toFile().isFile()){
			throw new IllegalStateException(String.format("sqlite文件=[%s]不存在", sqliteFilePath));
		}
		
		final int maxPageNum = YoukuHKFreeFilmDao.getMaxPageNum(sqliteFilePath);
		if(maxPageNum < 1){
			return Response.serverError().build();
		}
		if(pageNum > maxPageNum){
			return Response.seeOther(URI.create("list/yk/dalu/"+ maxPageNum)).build();
		}
		if(pageNum < 1){
			return Response.seeOther(URI.create("list/yk/dalu/"+ 1)).build();
		}
		final List<Map<String, Object>> li = YoukuHKFreeFilmDao.getBy(String.format(" page_num = %s and (inactive != 1 or inactive is null)",pageNum), " order by id asc , weight desc ", sqliteFilePath,"/movie/play/yk/dalu/%s",  "/movie/coverimg/yk/dalu/%s");
		
		final byte[] bs = ListFilm.render(li, "大陆电影", pageNum, maxPageNum/*这个是要改的，要看对方的资源是否是变化 了*/, "/movie/list/yk/dalu/%s");
		return Response.ok(bs).build();
	}
	
	
	
	@GET
	@Path("/yk/tw/{pageNum}")
	@Produces("text/html")
	public Response yk_taiwan_1(@PathParam("pageNum") final Integer pageNum) throws Exception{
		final String sqliteFilePath = (String)GlobalSetting.getByPath("yk_tw_sqlite_file");
		if(!Paths.get(sqliteFilePath).toFile().isFile()){
			throw new IllegalStateException(String.format("sqlite文件=[%s]不存在", sqliteFilePath));
		}
		
		final int maxPageNum = YoukuHKFreeFilmDao.getMaxPageNum(sqliteFilePath);
		if(maxPageNum < 1){
			return Response.serverError().build();
		}
		if(pageNum > maxPageNum){
			return Response.seeOther(URI.create("list/yk/tw/"+ maxPageNum)).build();
		}
		if(pageNum < 1){
			return Response.seeOther(URI.create("list/yk/tw/"+ 1)).build();
		}
		final List<Map<String, Object>> li = YoukuHKFreeFilmDao.getBy(String.format(" page_num = %s and (inactive != 1 or inactive is null)",pageNum), " order by id asc , weight desc ",  sqliteFilePath,"/movie/play/yk/tw/%s",  "/movie/coverimg/yk/tw/%s");
		
		final byte[] bs = ListFilm.render(li,"台湾电影", pageNum, maxPageNum/*这个是要改的，要看对方的资源是否是变化 了*/, "/movie/list/yk/tw/%s");
		return Response.ok(bs).build();
	}
	
	
	@GET
	@Path("/yk/hk")
	public Response yk_hk_2( ) throws Exception{
		return Response.seeOther(URI.create("list/yk/hk/1")).build();
	}
	
	@GET
	@Path("/yk/dalu")
	public Response yk_dalu_2( ) throws Exception{
		return Response.seeOther(URI.create("list/yk/dalu/1")).build();
	}
	
	@GET
	@Path("/yk/tw")
	public Response yk_taiwan_2( ) throws Exception{
		return Response.seeOther(URI.create("list/yk/tw/1")).build();
	}
	
	@GET
	@Path("/yk")
	public Response yk_hk_3( ) throws Exception{
		return Response.seeOther(URI.create("list/yk/hk")).build();
	}
	
	
	@GET
	public Response yk_hk_4( ) throws Exception{
		return Response.seeOther(URI.create("list/yk")).build();
	}
	
	
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
