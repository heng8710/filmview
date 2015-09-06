package movieresource;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import jersey.repackaged.com.google.common.collect.Lists;
import jersey.repackaged.com.google.common.collect.Maps;
import movieservice.HistoryCookieService;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

import render.ListFilm;
import sqlitetools.SqliteTools;
import tools.SqliteHelper;
import uuid.MovieUUIDHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

import conf.GlobalSetting;


@Path("/search")
public class SearchResource {

	private final static Logger logger = Logger.getLogger("global");
	
	
	static final int NumberPerPage = 30;
	static final int MaxNumber = 1024*1024;
	
	public final static String[] searchTypes = new String[]{"hk", "dalu", "tw", "hanguo", "tai", "usa", "uk", "fr"}; 
	
	@GET
	@Path("/{text}")
	@Produces("text/html")
	public Response get2(@PathParam("text") final String text, @PathParam("pageNum") final int pageNum) throws Exception{
		return Response.seeOther(URI.create("search/"+text+"/1")).build();
	}
	
	
	@GET
	@Path("/{text}/{pageNum}")
	@Produces("text/html")
	public Response get0(@PathParam("text") final String text, @PathParam("pageNum") final int pageNum, @Context final HttpServletRequest request) throws Exception{
		//TODO 限制text
		final List<Map<String, Object>> li = Lists.newArrayList();
		int hits = 0;
		for(final String type: searchTypes){
			final String targetLuceneFolder = Paths.get(GlobalSetting.getStringByPath("lucene_index_root_folder_for_youku")).resolve(type).toFile().getAbsolutePath();
			hits = Math.max(hits, addSearch(li, "/movie/play/yk/"+type+"/%s", "/movie/coverimg/yk/"+type+"/%s", SqliteHelper.getTargetSqliteFile("yk_sqlite_folder", type), targetLuceneFolder, text, pageNum));
		}
//		hits = Math.max(hits, addSearch(li, "/movie/play/yk/hk/%s", "/movie/coverimg/yk/hk/%s", GlobalSetting.getStringByPath("yk_hk_sqlite_file"), GlobalSetting.getStringByPath("hkpinglun_luncene_index_folder"), text, pageNum));
//		hits = Math.max(hits, addSearch(li, "/movie/play/yk/dalu/%s", "/movie/coverimg/yk/dalu/%s", GlobalSetting.getStringByPath("yk_dalu_sqlite_file"), GlobalSetting.getStringByPath("dalupinglun_luncene_index_folder"), text, pageNum));
//		hits = Math.max(hits, addSearch(li, "/movie/play/yk/tw/%s", "/movie/coverimg/yk/tw/%s", GlobalSetting.getStringByPath("yk_tw_sqlite_file"), GlobalSetting.getStringByPath("twpinglun_luncene_index_folder"), text, pageNum));
//		hits = Math.max(hits, addSearch(li, "/movie/play/yk/hanguo/%s", "/movie/coverimg/yk/hanguo/%s", GlobalSetting.getStringByPath("yk_hanguo_sqlite_file"), GlobalSetting.getStringByPath("hanguopinglun_luncene_index_folder"), text, pageNum));
		final List<Map<String,String>> historyCookieVal = HistoryCookieService.getCookie_2_2(request);
		final byte[] bs = ListFilm.render(li, "搜索:"+ text, pageNum, (int)Math.ceil((double)hits/NumberPerPage) , "/movie/search/"+ text + "/%s", historyCookieVal);
		return Response.ok(bs).build();
	}
	

	static Integer addSearch(final List<Map<String, Object>> li, final String playUrlFormatter, final String coverimgUrlFormatter, final String sqliteFile, final String luceneFolder, final String searchText, final int pageNum) throws Exception{
		if(Strings.isNullOrEmpty(sqliteFile)){
			return 0;
		}
		if(!Paths.get(sqliteFile).toFile().exists()){
			return 0;
		}
		final File folder = Paths.get(luceneFolder).toFile();
		if(!folder.exists() || !folder.isDirectory()){
			return 0;
		}
		final Connection twConn = SqliteTools.conn(sqliteFile);
		final Integer[] twIds = doSearch(searchText, pageNum, luceneFolder);
		for(int index=1; index<twIds.length; index++){
			final int id = twIds[index];
			final Map<String, String> row = SqliteTools.getOne_2(twConn, "movie", String.format("id = %s and (inactive != 1 or inactive is null)", id), null, null);
			if(row == null || row.size() == 0){
				continue;
			}
			final Map<String, Object> i = Maps.newHashMap();
			final String movieId = row.get("id");
			final String url = String.format(playUrlFormatter, MovieUUIDHelper.uuid(Long.valueOf(movieId)));
			i.put("url", url);
			i.put("title", row.get("movie_name"));
			final String proxyImgUrl = String.format(coverimgUrlFormatter, MovieUUIDHelper.uuid(Long.valueOf(/*id*/movieId)));
			i.put("img", proxyImgUrl);
			i.put("time", "");
			i.put("info", row.get("actors"));
			final Map<String, Object> features = new ObjectMapper().readValue(row.get("features"), Map.class);
			i.put("features", features);
			li.add(i);
		}
		return twIds[0];
	}
	
	
	
	
	
	
	static Integer[] doSearch(final String searchText, final int pageNum, final String indexPath){
		final Analyzer analyzer = new CJKAnalyzer();
		final QueryParser parser = new QueryParser("title", analyzer);
		final Query query;
		try {
			query = parser.parse(String.format("title:\"%s\" content:\"%s\"", searchText, searchText));
		} catch (final ParseException e) {
//			logger.log(Level.SEVERE,"解析查询语句有问题",  e);
			throw new IllegalArgumentException("解析查询语句有问题", e);
		}
		
		try (final Directory index = new SimpleFSDirectory(Paths.get(indexPath));){
			final DirectoryReader ireader = DirectoryReader.open(index);
			final IndexSearcher isearcher = new IndexSearcher(ireader);
			final TopDocs  td = isearcher.search(query, MaxNumber);
			if(td.totalHits == 0){
				return new Integer[]{0};
			}
			final int maxPage = (int)Math.ceil(((double)td.totalHits / NumberPerPage));
			if(pageNum > maxPage){
				return new Integer[]{td.totalHits};
			}
			final int start = (pageNum -1)* NumberPerPage;
			final int end =  (start + NumberPerPage -1 < td.totalHits)? (start + NumberPerPage -1): (td.totalHits-1);
			
			final Integer[] r = new Integer[end - start + 1 + 1];
			//第一个是查出来的总数
			r[0] = td.totalHits;
			for(int i=start; i<=end; i++){
				r[i+1-start] = Integer.valueOf(isearcher.doc(td.scoreDocs[i].doc).getField("id").stringValue());
			}
			return r;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
