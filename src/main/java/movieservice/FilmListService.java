package movieservice;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.Range;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;

import jersey.repackaged.com.google.common.base.Joiner;
import jersey.repackaged.com.google.common.collect.Lists;
import jersey.repackaged.com.google.common.collect.Maps;
import jersey.repackaged.com.google.common.collect.Sets;
import sqlitetools.SqliteTools;
import tools.SqliteHelper;
import tools.TypeHelper;
import dao.YoukuHKFreeFilmDao;

public class FilmListService {
	
	private static Logger logger = Logger.getLogger("global");

	public static Map<String, Object> doGetByTypeAndPageNum(final String type, final Integer pageNum, final String sqliteFilePath) throws Exception{
		final List<Map<String, Object>> li = YoukuHKFreeFilmDao.getBy(String.format(" page_num = %s and (inactive != 1 or inactive is null)",pageNum), " order by weight desc, id asc ",  sqliteFilePath, "/movie/play/yk/"+type+"/%s",  "/movie/coverimg/yk/"+type+"/%s");
	//	final byte[] bs = ListFilm.render(li, TypeHelper.typeAlias(type)+"电影", pageNum, maxPageNum/*这个是要改的，要看对方的资源是否是变化 了*/, "/movie/list/yk/"+type+"/%s");
		final Map<String, Object> r = Maps.newHashMap();
		r.put("movies", li);
		r.put("type", type);
		r.put("type_alias", TypeHelper.typeAlias(type) + "电影");
		r.put("num", li.size());
		r.put("page_num", pageNum);
		return r;
	}
	
	
	
	/**
	 * @param type
	 * @param ids： 格式 是这样的：【1,8,11-777】
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String, Object>> doGetByIds(final String type, final Set<String> ids) throws Exception{
		if(ids.size() == 0){
			return Lists.newArrayList();
		}
		final String sqliteFilePath = SqliteHelper.getTargetSqliteFile("yk_sqlite_folder", type);
		final Set<Integer> underIds = Sets.newHashSet(); 
		ids.forEach(s->{
			if(s.indexOf('-') == -1){
				underIds.add(Integer.valueOf(s));
			}else{
				//必须是这种格式【22-998】
				final Iterator<String> it = Splitter.on('-').omitEmptyStrings().trimResults().split(s).iterator();
				//用range的好处是上下届颠倒了，也会自动修正。
				final Range<Integer> r = Range.between(Integer.valueOf(it.next()), Integer.valueOf(it.next()));
				for(int i=r.getMinimum(); i< r.getMaximum(); i++){
					underIds.add(i);
				}
			}
		});
		final String idsStr = Joiner.on(',').join(underIds);
		final List<Map<String, Object>> li = YoukuHKFreeFilmDao.getBy(String.format(" id in (%s) and (inactive != 1 or inactive is null)",idsStr), null,  sqliteFilePath, "/movie/play/yk/"+type+"/%s",  "/movie/coverimg/yk/"+type+"/%s");
//		final Map<String, Object> r = Maps.newHashMap();
//		r.put("movies", li);
//		r.put("type", type);
//		r.put("type_alias", TypeHelper.typeAlias(type) + "电影");
//		r.put("num", li.size());
		return li;
		
	}
	
	
	
	public static List<Map<String, String>> doGetByIds_2(final String type, final Set<String> ids, final String[] returnColumnNames) throws Exception{
		if(ids.size() == 0){
			return Lists.newArrayList();
		}
		final String sqliteFilePath = SqliteHelper.getTargetSqliteFile("yk_sqlite_folder", type);
		final Set<Integer> underIds = Sets.newHashSet(); 
		ids.forEach(s->{
			if(s.indexOf('-') == -1){
				underIds.add(Integer.valueOf(s));
			}else{
				//必须是这种格式【22-998】
				final Iterator<String> it = Splitter.on('-').omitEmptyStrings().trimResults().split(s).iterator();
				//用range的好处是上下届颠倒了，也会自动修正。
				final Range<Integer> r = Range.between(Integer.valueOf(it.next()), Integer.valueOf(it.next()));
				for(int i=r.getMinimum(); i< r.getMaximum(); i++){
					underIds.add(i);
				}
			}
		});
		final String idsStr = Joiner.on(',').join(underIds);
		try(final Connection  conn = sqlitetools.SqliteTools.conn(sqliteFilePath/*"F:/sqlite/movie/aiqiyi/orderbytime.sqlite"*/)){
			return  SqliteTools.getAllByColumnNames_2(conn, "movie", returnColumnNames, String.format(" id in (%s) and (inactive != 1 or inactive is null)",idsStr), null, null, null);
		}catch(final Exception e){
//			logger.log(Level.SEVERE, "查询movie出错", e);
			throw new IllegalStateException("查询movie出错", e);
		}
	}
}
