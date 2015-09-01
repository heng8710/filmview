package uuid;

import conf.GlobalSetting;
import sqlitetools.idmapper.UUIDMapper;

public final class MovieUUIDHelper {

	private MovieUUIDHelper() {
	}

	static final UUIDMapper uuidMapper = new UUIDMapper(GlobalSetting.getStringByPath("movie_id_mapper_sqlite_file")); 
	
	
	public static Long id(final String uuid){
		return uuidMapper.id(uuid);
	}
	
	public static String uuid(final long id){
		return uuidMapper.uuid(id);
	}
	
	public static void main(String... args){
		long start = 1001;
		long end = 2000;
		
		uuidMapper.gen(start, end);
	}
	
	
	
}
