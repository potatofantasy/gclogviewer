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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Analyze gc log to GCLogData
 * 
 * @author <a href="mailto:bluedavy@gmail.com">bluedavy</a>
 */
public class GCLogAnalyze {

	private static final List<String> IGNORE_KEYWORDS=new ArrayList<String>();
	
	static{
		// CMS Concurrent Time not need calculate
		IGNORE_KEYWORDS.add("CMS-concurrent-");
		IGNORE_KEYWORDS.add("CMS: abort preclean due");
	}
	
	public GCLogData analysis(String fileName) throws Exception{
		BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))));
		String line=null;
		GCLogData data=new GCLogData();
		String runtime=null;
		boolean needPrint = true;
		OneLineGCData previousData=null;
		while((line=reader.readLine())!=null){
			line=line.trim();
			GCLogParser parser=getParser(line);
			if(parser == null){
				for (String ignoreKeyword : IGNORE_KEYWORDS) {
					if(line.indexOf(ignoreKeyword)!=-1){
						needPrint=false;
					}
				}
				if(needPrint){
					System.err.println("Cannot find parser for line: "+line);
				}
				continue;
			}
			OneLineGCData onelineGCData=parser.parse(line);
			runtime=onelineGCData.getOccurTime();
			if(runtime==null){
				// try to read next line
				String nextLine=reader.readLine();
				line+=nextLine;
				onelineGCData=parser.parse(line);
				runtime=onelineGCData.getOccurTime();
				if(runtime==null){
					System.err.println("Runtime is null: "+line);
					continue;
				}
			}
			if(parser.isYGC()){
				data.getYGCPauseTimes().put(runtime,onelineGCData.getPauseTime());
				data.getYGCMemoryChanges().put(runtime, onelineGCData.getMemoryChangeInfo());
				if(data.getGCType()==null)
					data.setGCType(parser.getGCDescription());
				if(previousData==null){
					data.setDataForLDSAndPTOS(onelineGCData.getHeapMemoryAfter(), onelineGCData.getMemoryChangeInfo()[1], "0", "0",runtime);
				}
				else{
					if(previousData.isYGCData())
						data.setDataForLDSAndPTOS(onelineGCData.getHeapMemoryAfter(), onelineGCData.getMemoryChangeInfo()[1], previousData.getHeapMemoryAfter(), previousData.getMemoryChangeInfo()[1],runtime);
					else
						data.setDataForLDSAndPTOS(onelineGCData.getHeapMemoryAfter(), onelineGCData.getMemoryChangeInfo()[1], previousData.getHeapMemoryAfter(), "0",runtime);
				}
				previousData = onelineGCData;
			}
			else if(parser.isCMSGC()){
				data.getCMSGCPauseTimes().put(runtime,onelineGCData.getPauseTime());
				data.getCMSGCMemoryChanges().put(runtime, onelineGCData.getMemoryChangeInfo());
				data.setGCType(parser.getGCDescription());
			}
			else{
				data.getFGCPauseTimes().put(runtime,onelineGCData.getPauseTime());
				data.getFGCMemoryChanges().put(runtime, onelineGCData.getMemoryChangeInfo());
				data.setGCType(parser.getGCDescription());
				previousData = onelineGCData;
			}
		}
		data.setRuntime(runtime);
		return data;
	}
	
	private GCLogParser getParser(String line){
		for(String gcLogKeyword: GCLogKeywordsAndParsers.getKeywords()){
			if(line.indexOf(gcLogKeyword)!=-1){
				return GCLogKeywordsAndParsers.getParser(gcLogKeyword);
			}
		}
		return null;
	}
	
}
