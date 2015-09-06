package render;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import jersey.repackaged.com.google.common.collect.Maps;
import conf.GlobalSetting;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

public class PlayFilm {

	
	public static byte[] render(final Map<String, Object> movie , final String type, final String uuid, final List<Map<String,String>> historyCookieVal) throws Exception{
		final Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
		cfg.setDirectoryForTemplateLoading(new File(GlobalSetting.getStringByPath("freemarker_dir")));
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		
		final Template temp = cfg.getTemplate("list/item.html");
		final ByteArrayOutputStream o = new ByteArrayOutputStream();
		final Writer out = new OutputStreamWriter(o);
		
		
		final Map<String, Object> root = Maps.newHashMap();
		root.put("movie", movie);
		root.put("type", type);
		root.put("uuid", uuid);
		root.put("historys", historyCookieVal);
		
//		root.put("pages", pages(pageNum, maxPage/*这个是要改的，要看对方的资源是否是变化 了*/, pagingUrlFormatter));
//		root.put("nextPage", pageNum < maxPage ? String.format(pagingUrlFormatter, pageNum+1): String.format(pagingUrlFormatter, pageNum));
		temp.process(root, out);
		return o.toByteArray();
	}
	
}
