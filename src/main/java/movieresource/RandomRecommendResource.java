package movieresource;

import java.sql.Connection;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import jersey.repackaged.com.google.common.base.Joiner;
import jersey.repackaged.com.google.common.collect.Lists;
import jersey.repackaged.com.google.common.collect.Maps;
import jersey.repackaged.com.google.common.collect.Sets;
import render.ListFilm;
import sqlitetools.SqliteTools;
import tools.SqliteHelper;
import uuid.MovieUUIDHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;


@Path("recommend/random")
public class RandomRecommendResource {
	static final Logger logger  = Logger.getLogger("global");

	static final int NUMBER_PER_TYPE = 10;
	
	static final String[] TYPES = new String[]{"hk", "dalu", "tw", "hanguo", "tai", "usa", "uk", "fr"};
	static final Map<String, String> TYPE_RANGE = new HashMap<String, String>(){{
			put("hk", "1-1218");//
			put("dalu", "1-1218");
			put("tw", "1-686");
			put("hanguo", "1-1218");
			put("tai", "1-512");
			put("usa", "1-1218");
			put("uk", "1-1218");
			put("fr", "1-1218");
	}};
	
	@GET
	@Produces("text/html")
	public Response yk( ) throws Exception{
		final LinkedHashMap<String, String> randomTypeMap = new PeriodRandomItem(System.currentTimeMillis()).randomTypeMap;//getRandom();
		final List<Map<String, Object>> all = Lists.newArrayList();
		for(final Entry<String, String> entry: randomTypeMap.entrySet()){
			final String playUrlFormatter = "/movie/play/yk/"+entry.getKey()+"/%s";
			final String coverimgUrlFormatter = "/movie/coverimg/yk/"+entry.getKey()+"/%s";
			try(final Connection conn = SqliteTools.conn( SqliteHelper.getTargetSqliteFile("yk_sqlite_folder", entry.getKey()))){
				//ids不可能为空的
				final List<Map<String, String>> rows = SqliteTools.getAll_2(conn, "movie", String.format("id in (%s) and (inactive != 1 or inactive is null)", entry.getValue()), null, null, null);
				rows.forEach(row->{
					try {
						final Map<String, Object> i = Maps.newHashMap();
						final String movieId = row.get("id");
						final String url = String.format(playUrlFormatter, MovieUUIDHelper.uuid(Long.valueOf(movieId)));
						i.put("url", url);
						i.put("title", row.get("movie_name"));
						final String proxyImgUrl = String.format(coverimgUrlFormatter, MovieUUIDHelper.uuid(Long.valueOf(movieId)));
						i.put("img", proxyImgUrl);
						i.put("time", "");
						i.put("info", row.get("actors"));
						final Map<String, Object> features = new ObjectMapper().readValue(row.get("features"), Map.class);
						i.put("features", features);
						all.add(i);
					} catch (Exception e) {
						logger.log(Level.SEVERE, String.format("添加项=[%s]失败", row), e);
					}
				});
			}catch(final Exception e){
				logger.log(Level.SEVERE, String.format("获取随机资源资源，查询type=[%s]时出错", entry.getKey()), e);
			}
		}
		
		final byte[] bs = ListFilm.render( all, "随机推荐电影", 1, 1 , "/movie/recommend/random"); 
		return Response.ok(bs).build();
	}
	
	
	
	
	
	
	
	static final AtomicReference<PeriodRandomItem> random = new AtomicReference<>();
	static LinkedHashMap<String, String> getRandom(){
		final long now = System.currentTimeMillis();
		PeriodRandomItem r = random.get();
		if(r == null){
			r = new PeriodRandomItem(now);
			random.set(r);
			return r.randomTypeMap;
		}
		if(r.isTimeInRange(now)){
			return r.randomTypeMap;
		}
		r = new PeriodRandomItem(now);
		random.set(r);
		return r.randomTypeMap;
	}
	
	/**
	 * 在一定的范围之内
	 */
	final static class PeriodRandomItem{
		/**
		 * 整点
		 */
		final long time;
		
		final LinkedHashMap<String, String> randomTypeMap;
		
		PeriodRandomItem(final long time) {
			final Calendar c = Calendar.getInstance();
			c.setTimeInMillis(time);
//			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			this.time = (c.getTimeInMillis()/1000L)*1000L;
			final Random random = new Random(this.time);
			final List<String> ttypes = Lists.newArrayList(TYPES);
			final LinkedHashMap<String, String> randomTypeMap = Maps.newLinkedHashMap();
			Collections.shuffle(ttypes);
			ttypes.forEach(s->{
				final Iterator<String> it = Splitter.on('-').omitEmptyStrings().trimResults().split(TYPE_RANGE.get(s)).iterator();
				final int left = Integer.valueOf(it.next());
				final int right = Integer.valueOf(it.next());
				final int width = right - left + 1;
				final Set<Integer> idSet = Sets.newHashSet();
				for(;;){
					if(idSet.size() >= NUMBER_PER_TYPE){
						//!!!!!!一定保证要大于。。。。。
						break;
					}
					idSet.add( left + random.nextInt(width));
				}
				randomTypeMap.put(s, Joiner.on(',').join(idSet));
			});
			this.randomTypeMap = randomTypeMap;
		}
		
		boolean isTimeInRange(final long otherTime){
			final Calendar c = Calendar.getInstance();
			c.setTimeInMillis(otherTime);
//			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			return (c.getTimeInMillis()/1000L)*1000L  == time;
		}
		
	} 
}
