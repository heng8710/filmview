package movieresource;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import movieservice.HistoryCookieService;
import render.ListFilm;
import tools.SqliteHelper;
import tools.TypeHelper;
import dao.YoukuHKFreeFilmDao;

@Path("list")
public class FilmListResource {

	final static Logger logger = Logger.getLogger("global");
	
	
	@GET
	@Path("/yk/{type}/{pageNum}")
	public Response doGet4YoukuType(@PathParam("type") final String type, @PathParam("pageNum") final Integer pageNum, @Context final HttpServletRequest request) throws Exception{
		return doGet(type, pageNum, request);
	}
	
	
	private static Response doGet(final String type, final Integer pageNum,
			final HttpServletRequest request
			) throws Exception{
		final String sqliteFilePath = SqliteHelper.getTargetSqliteFile("yk_sqlite_folder", type);
		final int maxPageNum = YoukuHKFreeFilmDao.getMaxPageNum(sqliteFilePath);
		if(maxPageNum < 1){
			return Response.serverError().build();
		}
		if(pageNum > maxPageNum){
			return Response.seeOther(URI.create(String.format("list/yk/%s/%s", type, maxPageNum))).build();
		}
		if(pageNum < 1){
			return Response.seeOther(URI.create(String.format("list/yk/%s/%s", type, 1))).build();
		}
		final List<Map<String, Object>> li = YoukuHKFreeFilmDao.getBy(String.format(" page_num = %s and (inactive != 1 or inactive is null)",pageNum), " order by weight desc, id asc ",  sqliteFilePath, "/movie/play/yk/"+type+"/%s",  "/movie/coverimg/yk/"+type+"/%s");
		final List<Map<String,String>> historyCookieVal = HistoryCookieService.getCookie_2_2(request);
		final byte[] bs = ListFilm.render(li, TypeHelper.typeAlias(type)+"电影", pageNum, maxPageNum/*这个是要改的，要看对方的资源是否是变化 了*/, "/movie/list/yk/"+type+"/%s", historyCookieVal);
		return Response.ok(bs).build();
	}
	
	
	
	
	
	
	
//	private static File newestFile(final File folder, final File foundFile, final String foundName){
//		File targetSqliteFile = foundFile;
//		String targetSqliteFileName = foundName;
//		for(final File sqliteFile: folder.listFiles()){
//			if(sqliteFile.isDirectory()){//递归深度优先
//				final File subFile = newestFile(sqliteFile, targetSqliteFile, targetSqliteFileName);
//				if(!Objects.equal(subFile, targetSqliteFile)){
//					targetSqliteFile = subFile;
//					targetSqliteFileName = subFile.getName();
//					continue;
//				}
//			}
//			//以下肯定都是文件了
//			final String newName = sqliteFile.getName();
//			if(targetSqliteFileName == null){
//				targetSqliteFileName = newName;
//				targetSqliteFile = sqliteFile;
//				continue;
//			}
//			if(targetSqliteFileName.compareTo(newName) < 0){//比较部分
//				targetSqliteFileName = newName;
//				targetSqliteFile = sqliteFile;
//			}
//		}
//		return targetSqliteFile;
//	}
	
	
	@GET
	public Response yk( ) throws Exception{
//		final String[] types = new String[]{"hk", "dalu", "tw", "hanguo", "tai", "usa", "uk", "fr"}; 
//		final String type = types[new Random().nextInt(types.length -1)];
		return Response.seeOther(URI.create("list/yk/"+"hk"/*type*/)).build();
	}
	
	
	@GET
	@Path("/yk/{type}")
	public Response yk2(@PathParam("type") final String type) throws Exception{
		return Response.seeOther(URI.create("list/yk/"+type + "/1")).build();
	}
	
	
}
