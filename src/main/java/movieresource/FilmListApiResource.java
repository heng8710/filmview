package movieresource;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import jersey.repackaged.com.google.common.base.Joiner;
import jersey.repackaged.com.google.common.collect.Maps;
import jersey.repackaged.com.google.common.collect.Sets;
import movieservice.FilmListService;

import org.apache.commons.lang3.Range;

import tools.SqliteHelper;
import tools.TypeHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import dao.YoukuHKFreeFilmDao;


@Path("/api/list")
public class FilmListApiResource {

	@GET
	@Path("/yk/{type}/{pageNum}")
	@Produces("application/json;charset=utf-8")
	public Response doGet4YoukuType(@PathParam("type") final String type, @PathParam("pageNum") final Integer pageNum) throws Exception{
		return doGetByTypeAndPageNum(type, pageNum);
	}
	
	
	private static Response doGetByTypeAndPageNum(final String type, final Integer pageNum) throws Exception{
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
//		final List<Map<String, Object>> li = YoukuHKFreeFilmDao.getBy(String.format(" page_num = %s and (inactive != 1 or inactive is null)",pageNum), " order by weight desc, id asc ",  sqliteFilePath, "/movie/play/yk/"+type+"/%s",  "/movie/coverimg/yk/"+type+"/%s");
//		final byte[] bs = ListFilm.render(li, TypeHelper.typeAlias(type)+"电影", pageNum, maxPageNum/*这个是要改的，要看对方的资源是否是变化 了*/, "/movie/list/yk/"+type+"/%s");
//		final Map<String, Object> r = Maps.newHashMap();
//		r.put("movies", li);
//		r.put("type", type);
//		r.put("type_alias", TypeHelper.typeAlias(type) + "电影");
//		r.put("num", li.size());
//		r.put("page_num", pageNum);
//		r.put("max_page_num", maxPageNum);
		final Map<String, Object> r = FilmListService.doGetByTypeAndPageNum(type, pageNum, sqliteFilePath);
		r.put("max_page_num", maxPageNum);
		return Response.ok(new ObjectMapper().writeValueAsBytes(r)).build();
	}
	
	
	
	@GET
	@Path("/yk/get_by_ids/{type}/{ids}")
	@Produces("application/json;charset=utf-8")
	public Response getByIdsGet(@PathParam("type") final String type, @PathParam("ids") final String idsStr) throws Exception{
		if(Strings.isNullOrEmpty(idsStr) || !TypeHelper.isTypeRight(type)){
			throw new IllegalArgumentException(String.format("参数不正确，type=[%s], ids=[%s] 不正确", type, idsStr));
		}
		final Set<String> idSet = Sets.newTreeSet(Splitter.on(',').omitEmptyStrings().trimResults().split(idsStr));
		return doGetByIds(type, idSet);
	}
	
	
	public static void main(String...args){
		final Range<Integer> r = Range.between(777, 111);
		System.out.println(r);
	}
	
	@POST
	@Path("/yk/get_by_ids")
	@Produces("application/json;charset=utf-8")
	public Response getByIdsPost(@FormParam("type") final String type, @FormParam("ids") final String idsStr) throws Exception{
		if(Strings.isNullOrEmpty(idsStr) || !TypeHelper.isTypeRight(type)){
			throw new IllegalArgumentException(String.format("参数不正确，type=[%s], ids=[%s] 不正确", type, idsStr));
		}
		final Set<String> idSet = Sets.newTreeSet(Splitter.on(',').omitEmptyStrings().trimResults().split(idsStr));
		return doGetByIds(type, idSet);
	}
	
	/**
	 * @param type
	 * @param ids： 格式 是这样的：【1,8,11-777】
	 * @return
	 * @throws Exception
	 */
	static Response doGetByIds(final String type, final Set<String> ids) throws Exception{
//		if(ids.size() == 0){
//			return Response.ok().build();
//		}
//		final String sqliteFilePath = SqliteHelper.getTargetSqliteFile("yk_sqlite_folder", type);
//		final Set<Integer> underIds = Sets.newHashSet(); 
//		ids.forEach(s->{
//			if(s.indexOf('-') == -1){
//				underIds.add(Integer.valueOf(s));
//			}else{
//				//必须是这种格式【22-998】
//				final Iterator<String> it = Splitter.on('-').omitEmptyStrings().trimResults().split(s).iterator();
//				//用range的好处是上下届颠倒了，也会自动修正。
//				final Range<Integer> r = Range.between(Integer.valueOf(it.next()), Integer.valueOf(it.next()));
//				for(int i=r.getMinimum(); i< r.getMaximum(); i++){
//					underIds.add(i);
//				}
//			}
//		});
//		final String idsStr = Joiner.on(',').join(underIds);
//		final List<Map<String, Object>> li = YoukuHKFreeFilmDao.getBy(String.format(" id in (%s) and (inactive != 1 or inactive is null)",idsStr), null,  sqliteFilePath, "/movie/play/yk/"+type+"/%s",  "/movie/coverimg/yk/"+type+"/%s");
		final List<Map<String, Object>> li = FilmListService.doGetByIds(type, ids);
		final Map<String, Object> r = Maps.newHashMap();
		r.put("movies", li);
		r.put("type", type);
		r.put("type_alias", TypeHelper.typeAlias(type) + "电影");
		r.put("num", li.size());
		return Response.ok(new ObjectMapper().writeValueAsBytes(r)).build();
		
	}
	
}
