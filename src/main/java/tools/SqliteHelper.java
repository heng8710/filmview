package tools;

import java.io.File;
import java.nio.file.Paths;

import com.google.common.base.Strings;

import conf.GlobalSetting;
import jersey.repackaged.com.google.common.base.Objects;

public class SqliteHelper {

	
	
	public static File newestFolder(final File folder, final File foundFile, final String foundName){
		File targetSqliteFile = foundFile;
		String targetSqliteFileName = foundName;
		for(final File sqliteFile: folder.listFiles()){
			if(sqliteFile.isDirectory()){
				final String newName = sqliteFile.getName();
				if(targetSqliteFileName == null){
					targetSqliteFileName = newName;
					targetSqliteFile = sqliteFile;
					continue;
				}
				if(targetSqliteFileName.compareTo(newName) < 0){//比较部分
					targetSqliteFileName = newName;
					targetSqliteFile = sqliteFile;
					//不continue，还要搜索子目录
				}
				final File subFile = newestFolder(sqliteFile, targetSqliteFile, targetSqliteFileName);
				if(!Objects.equal(subFile, targetSqliteFile)){
					targetSqliteFile = subFile;
					targetSqliteFileName = subFile.getName();
					continue;
				}
			}
		}
		return targetSqliteFile;
	}
	
	public static File newestFile(final File folder, final File foundFile, final String foundName){
		File targetSqliteFile = foundFile;
		String targetSqliteFileName = foundName;
		for(final File sqliteFile: folder.listFiles()){
			if(sqliteFile.isDirectory()){//递归深度优先
				final File subFile = newestFile(sqliteFile, targetSqliteFile, targetSqliteFileName);
				if(!Objects.equal(subFile, targetSqliteFile)){
					targetSqliteFile = subFile;
					targetSqliteFileName = subFile.getName();
					continue;
				}
			}
			//以下肯定都是文件了
			final String newName = sqliteFile.getName();
			if(targetSqliteFileName == null){
				targetSqliteFileName = newName;
				targetSqliteFile = sqliteFile;
				continue;
			}
			if(targetSqliteFileName.compareTo(newName) < 0){//比较部分
				targetSqliteFileName = newName;
				targetSqliteFile = sqliteFile;
			}
		}
		return targetSqliteFile;
	}
	
	
	
	public static String getTargetSqliteFile(final String sqliteRootFolder/*"yk_sqlite_folder"*/, final String type){
		final String sqliteRootFolderPath = GlobalSetting.getStringByPath(sqliteRootFolder);
		if(Strings.isNullOrEmpty(sqliteRootFolderPath)){
			throw new IllegalArgumentException(String.format("sqliteRootFolderPath=[%s] 不正确", sqliteRootFolderPath));
		}
		if(!TypeHelper.isTypeRight(type)){
			throw new IllegalArgumentException(String.format("type=[%s] 不正确", type));
		}
		final java.nio.file.Path sqliteFolderPath = Paths.get(sqliteRootFolderPath).resolve(type);
		final File sqliteFolder = sqliteFolderPath.toFile();
		if(!sqliteFolder.exists() || !sqliteFolder.isDirectory()){
			throw new IllegalArgumentException(String.format("sqlite folder=[%s] 不正确", sqliteFolder));
		}
		final File targetSqliteFile = SqliteHelper.newestFile(sqliteFolder, null, null);
		if(targetSqliteFile == null){
			throw new IllegalStateException(String.format("目录=[%s]之下，找不到可用的数据库", sqliteFolderPath));
		}
		final String sqliteFilePath = targetSqliteFile.getAbsolutePath();
		return sqliteFilePath;
	}
	
}
