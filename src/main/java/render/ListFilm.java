package render;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import jersey.repackaged.com.google.common.collect.Lists;
import jersey.repackaged.com.google.common.collect.Maps;

import org.apache.commons.lang3.Range;

import conf.GlobalSetting;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

public class ListFilm {

	
	public static byte[] render(final List<Map<String, Object>> movies, final String title, final Integer pageNum, final int maxPage/*必须大于10*/, final String pagingUrlFormatter) throws Exception{
		final Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
		cfg.setDirectoryForTemplateLoading(new File((String)GlobalSetting.getByPath("freemarker_dir")));
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		
		final Template temp = cfg.getTemplate("list/list.html");
		final ByteArrayOutputStream o = new ByteArrayOutputStream();
		final Writer out = new OutputStreamWriter(o);
		
		final Map<String, Object> root = Maps.newHashMap();
		root.put("movies", movies);
		root.put("pages", pages(pageNum, maxPage/*这个是要改的，要看对方的资源是否是变化 了*/, pagingUrlFormatter));
		root.put("nextPage", pageNum < maxPage ? String.format(pagingUrlFormatter, pageNum+1): String.format(pagingUrlFormatter, pageNum));
		root.put("currentPage", pageNum);
		root.put("maxPage", maxPage);
		root.put("title", title);
		temp.process(root, out);
		return o.toByteArray();
	}
	
	static List<Map<String, Object>> pages(final int curPage, final int maxPage/*必须大于10*/, final String pagingUrlFormatter){
		final List<Map<String, Object>> lp = Lists.newArrayList();
		if(maxPage > 10){
			final Range<Integer> range;
			if(curPage + 5 <= maxPage){
				range = Range.between((curPage-4>0)? curPage-4: 1, (curPage-4>0)? curPage+5: 10);
			}else{
				range = Range.between(maxPage - 9, maxPage);
			}
			for(int index=range.getMinimum(); index <= range.getMaximum(); index++){
				Map<String, Object> pm = Maps.newHashMap();
				pm.put("active", (curPage == index)? 1 : 0);
				pm.put("url", String.format(pagingUrlFormatter, index));
				pm.put("num", index);
				lp.add(pm);
			}
		}else{
			for(int index=1; index <= maxPage; index++){
				Map<String, Object> pm = Maps.newHashMap();
				pm.put("active", (curPage == index)? 1 : 0);
				pm.put("url", String.format(pagingUrlFormatter, index));
				pm.put("num", index);
				lp.add(pm);
			}
		}
		return lp;
	}
	
	public static void main(String... args) throws Exception{
//		sth(1, 30, "/filmview/list/%s");
	}
}
