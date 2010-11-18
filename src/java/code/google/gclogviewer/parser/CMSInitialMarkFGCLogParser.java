package code.google.gclogviewer.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import code.google.gclogviewer.GCLogParser;
import code.google.gclogviewer.OneLineGCData;

/**
 * parse CMS InitialMark log, -XX:+UseConcMarkSweepGC
 * 
 * 	Log example:
 * 	2010-11-11T13:53:14.724+0800: 75090.950: [GC [1 CMS-initial-mark: 869925K(1060864K)] 874081K(1624064K), 0.0069590 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]  
 *  75090.950: [GC [1 CMS-initial-mark: 869925K(1060864K)] 874081K(1624064K), 0.0069590 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
 *
 * @author <a href="mailto:bixuan@taobao.com">bixuan</a>
 */
public class CMSInitialMarkFGCLogParser implements GCLogParser {

	private static final Pattern pattern1=Pattern.compile("([0-9-T:]+)..*: ([0-9.]+):.*CMS-initial-mark: ([0-9]+)K\\(.*, ([0-9.]+) secs");
	private static final Pattern pattern2=Pattern.compile("([0-9.]+):.*CMS-initial-mark: ([0-9]+)K\\(.*, ([0-9.]+) secs");
	
	public OneLineGCData parse(String lineInfo) throws Exception {
		return parse(lineInfo, pattern1, pattern2);
	}

	public boolean isYGC() {
		return false;
	}

	public String getGCDescription() {
		return "ConcMarkSweepGC";
	}

	private OneLineGCData parse(String lineInfo,Pattern pattern1,Pattern pattern2) throws Exception {
		OneLineGCData data=new OneLineGCData();
		boolean result=formData(lineInfo, data, pattern1);
		if(!result){
			result=formData(lineInfo, data, pattern2);
			if(!result){
				System.err.println("parse line failed: "+lineInfo);
			}
		}
		return data;
	}
	
	private boolean formData(String lineInfo,OneLineGCData data,Pattern pattern){
		boolean result=false;
		Matcher match = pattern.matcher(lineInfo);
		String[] memoryChanges=new String[2];
		if (match.find()) {
			// because cms cannot get memory change info
			if(match.groupCount() == 3){
				data.setOccurTime(match.group(1));
				memoryChanges[0]=match.group(2);
				memoryChanges[1]=match.group(2);
				data.setMemoryChangeInfo(memoryChanges);
				data.setPauseTime(match.group(3));
			}
			else if(match.groupCount() == 4){
				data.setOccurDateTime(match.group(1));
				data.setOccurTime(match.group(2));
				memoryChanges[0]=match.group(3);
				memoryChanges[1]=match.group(3);
				data.setMemoryChangeInfo(memoryChanges);
				data.setPauseTime(match.group(4));
			}
			result=true;
        }
		return result;
	}

	public boolean isCMSGC() {
		return true;
	}
	
}
