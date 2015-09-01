package resource;

import java.io.File;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

import uuid.MovieUUIDHelper;

import com.google.common.io.Files;

import conf.GlobalSetting;


@Path("/coverimg")
public class CoverImgResource {

	@GET
	@Path("/{type1}/{type2}/{id}")
	@Produces("image/jpeg")
	public Response coverimg(@PathParam("type1") final String type1, @PathParam("type2") final String type2, @PathParam("id") final String uuid) throws Exception{
		//检查
//		final String folderPath = String.format("f:/film/coverimg/%s/%s/20150826040748/", type1, type2);//type
		final String folder = (String)GlobalSetting.getByPath(String.format("cover_img_folder.%s_%s", type1, type2));
		final Long id = MovieUUIDHelper.id(uuid);
		if(id == null){
			return Response.status(404).build();
		}
		//TODO 检查folder
		if(folder.contains(".")){
			return Response.status(404).build();
		}
		final  File imgFile = Paths.get(folder).resolve(id + ".jpeg").toFile();//检查id格式
		if(imgFile.exists() && imgFile.isFile()){
			final Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MONTH, 1);
			final Date endDate = calendar.getTime();
			final long maxage = (endDate.getTime() - new Date().getTime())/ 1000;
			final CacheControl cc = new CacheControl();
			cc.setMaxAge((int)maxage);
			cc.setMustRevalidate(false);
			cc.setNoCache(false);
			cc.setNoStore(false);
			cc.setPrivate(false);
			cc.setProxyRevalidate(false);
			cc.setSMaxAge((int)maxage);
			
			return Response.ok(Files.toByteArray(imgFile)).cacheControl(cc).expires(endDate).build();
		}
		return Response.serverError().build();
	}
}
