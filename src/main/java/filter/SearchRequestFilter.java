package filter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;

import jersey.repackaged.com.google.common.base.Objects;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class SearchRequestFilter implements ContainerRequestFilter{
	
	final static Logger logger = Logger.getLogger("global");

	@Context   
    private HttpServletRequest req;  
	
	@Override
	public void filter(final ContainerRequestContext requestContext)
			throws IOException {
		final List<PathSegment>  ps = requestContext.getUriInfo().getPathSegments();
		if(ps.size() > 0 && Objects.equal("search", ps.get(0).getPath())){
			final String ip = getIpAddr(req);
			if(counter(ip) > 30){
				requestContext.abortWith(Response.ok("search limit", MediaType.TEXT_PLAIN).build());
				return ;
			}
		}
	}
	
	public static String getIpAddr(final HttpServletRequest request)  {
        String ip  =  request.getHeader( "X-Forwarded-For" );
         if (ip  ==   null   ||  ip.length()  ==   0   ||   "unknown" .equalsIgnoreCase(ip))  {
            ip  =  request.getHeader( "Proxy-Client-IP" );
        }
         if (ip  ==   null   ||  ip.length()  ==   0   ||   "unknown" .equalsIgnoreCase(ip))  {
            ip  =  request.getHeader( "WL-Proxy-Client-IP" );
        }
         if (ip  ==   null   ||  ip.length()  ==   0   ||   "unknown" .equalsIgnoreCase(ip))  {
           ip  =  request.getRemoteAddr();
       }
        return  ip;
   } 
	
	
	static int counter(final String ip){
		try {
			final AtomicInteger counter = cache.get(ip);
			return counter.incrementAndGet();
		} catch (ExecutionException e) {
//			logger.log(Level.SEVERE,"ip counter error", e);
			throw new RuntimeException("ip counter error", e);
		}
	}
	
	//缓存接口这里是LoadingCache，LoadingCache在缓存项不存在时可以自动加载缓存
    final static LoadingCache<String,AtomicInteger> cache
            //CacheBuilder的构造函数是私有的，只能通过其静态方法newBuilder()来获得CacheBuilder的实例
            = CacheBuilder.newBuilder()
            //设置并发级别为8，并发级别是指可以同时写缓存的线程数
            .concurrencyLevel(8)
            //设置写缓存后8秒钟过期
            .expireAfterWrite(60, TimeUnit.SECONDS)
            //设置缓存容器的初始容量为10
            .initialCapacity(10)
            //设置缓存最大容量为100，超过100之后就会按照LRU最近虽少使用算法来移除缓存项
            .maximumSize(100)
//            //设置要统计缓存的命中率
//            .recordStats()
//            //设置缓存的移除通知
//            .removalListener(new RemovalListener<Object, Object>() {
//                @Override
//                public void onRemoval(RemovalNotification<Object, Object> notification) {
//                    System.out.println(notification.getKey() + " was removed, cause is " + notification.getCause());
//                }
//            })
            //build方法中可以指定CacheLoader，在缓存不存在时通过CacheLoader的实现自动加载缓存
            .build(
                    new CacheLoader<String,AtomicInteger>() {
                        @Override
                        public AtomicInteger load(String key) throws Exception {
                            return new AtomicInteger(0);
                        }
                    }
            );
}
