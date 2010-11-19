/**
 * GCLogViewer
 * 
 * A free open source tool to visualize data produced by the Java VM options -Xloggc:<file> or jstat > <file>.
 * 
 * Code license:	Apache License 2.0
 * 
 * http://code.google.com/p/gclogviewer
 */
package code.google.gclogviewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import code.google.gclogviewer.parser.CMSFailedFGCLogParser;
import code.google.gclogviewer.parser.CMSInitialMarkFGCLogParser;
import code.google.gclogviewer.parser.CMSRemarkFGCLogParser;
import code.google.gclogviewer.parser.ParFGCLogParser;
import code.google.gclogviewer.parser.ParallelFGCLogParser;
import code.google.gclogviewer.parser.ParallelYGCLogParser;
import code.google.gclogviewer.parser.ParnewYGCLogParser;

/**
 * @author <a href="mailto:bluedavy@gmail.com">bluedavy</a>
 */
public class GCLogKeywordsAndParsers {

	private static List<String> keywords=new ArrayList<String>();
	
	private static Map<String, GCLogParser> parsers=new HashMap<String, GCLogParser>();
	
	static{
		// first FGC keyword,just because when fgc,ygc keyword will also exists
		for(FGCType type: FGCType.values()){
			keywords.add(type.getKeyword());
		}
		// then YGC keyword
		for(YGCType type: YGCType.values()){
			keywords.add(type.getKeyword());
		}
		parsers.put(YGCType.PARALLEL.getKeyword(), new ParallelYGCLogParser());
		parsers.put(YGCType.PARNEW.getKeyword(), new ParnewYGCLogParser());
		parsers.put(FGCType.PARALLEL.getKeyword(), new ParallelFGCLogParser());
		parsers.put(FGCType.PAR.getKeyword(), new ParFGCLogParser());
		parsers.put(FGCType.CMSINITIALMARK.getKeyword(), new CMSInitialMarkFGCLogParser());
		parsers.put(FGCType.CMSREMARK.getKeyword(), new CMSRemarkFGCLogParser());
		parsers.put(FGCType.CMSFailed.getKeyword(), new CMSFailedFGCLogParser());
	}
	
	public static List<String> getKeywords(){
		return keywords;
	}
	
	public static GCLogParser getParser(String gcLogKeyword){
		return parsers.get(gcLogKeyword);
	}
	
	public enum FGCType{
		// Serial
		SERIAL("Tenured"),
		// Parallel
		PARALLEL("PSOldGen"),PAR("ParOldGen"),
		// CMS
		CMSINITIALMARK("CMS-initial-mark"),CMSREMARK("CMS-remark"),CMSFailed("ParNew (promotion failed)");
		private String keyword;
		FGCType(String keyword){
			this.keyword=keyword;
		}
		public String getKeyword(){
			return keyword;
		}
	}
	
	public enum YGCType{
		// Serial
		SERIAL("DefNew"),
		// Parallel
		PARALLEL("PSYoungGen"),
		// CMS
		PARNEW("ParNew");
		private String keyword;
		YGCType(String keyword){
			this.keyword=keyword;
		}
		public String getKeyword(){
			return keyword;
		}
	}
	
}
