package render;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import conf.GlobalSetting;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

public final class CommonFMRender {

	public static byte[] render(final Map<String, ? extends Object> movie , final String template) throws Exception{
		final Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
		cfg.setDirectoryForTemplateLoading(new File(GlobalSetting.getStringByPath("freemarker_dir")));
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		final Template temp = cfg.getTemplate(template);
		final ByteArrayOutputStream o = new ByteArrayOutputStream();
		final Writer out = new OutputStreamWriter(o);
		temp.process(movie, out);
		return o.toByteArray();
	}
}
