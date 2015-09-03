package tools;

import com.google.common.base.CharMatcher;

public class TypeHelper {

	
	public static boolean isTypeRight(final String type){
		final CharMatcher cm = CharMatcher.JAVA_LETTER.or(CharMatcher.JAVA_DIGIT).or(CharMatcher.anyOf("_-"));//点【.】应该是不可以 的
		return cm.matchesAllOf(type);
	}
	
	
	
	public static String typeAlias(final String rawType){
		switch(rawType){
		case "hk": return "香港";
		case "dalu": return "大陆";
		case "tw": return "台湾";
		case "hanguo": return "韩国";
		case "fr": return "法国";
		case "tai": return "泰国";
		case "usa": return "美国";
		default: return rawType;
	}
}
}
