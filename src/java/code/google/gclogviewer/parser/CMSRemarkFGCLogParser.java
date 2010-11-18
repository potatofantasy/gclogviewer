package code.google.gclogviewer.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import code.google.gclogviewer.GCLogParser;
import code.google.gclogviewer.OneLineGCData;

/**
 * parse CMS Remark log, -XX:+UseConcMarkSweepGC
 * 
 * 	Log example:
 * 	2010-11-11T13:53:18.634+0800: 75094.860: [GC[YG occupancy: 259902 K (563200 K)]75094.860: [Rescan (parallel) , 0.0586970 secs]75094.919: [weak refs processing, 0.0168870 secs]75094.936: [class unloading, 0.0413000 secs]75094.977: [scrub symbol & string tables, 0.0115240 secs] [1 CMS-remark: 869959K(1060864K)] 1129862K(1624064K), 0.1832670 secs] [Times: user=0.23 sys=0.00, real=0.18 secs]  
 *  75094.860: [GC[YG occupancy: 259902 K (563200 K)]75094.860: [Rescan (parallel) , 0.0586970 secs]75094.919: [weak refs processing, 0.0168870 secs]75094.936: [class unloading, 0.0413000 secs]75094.977: [scrub symbol & string tables, 0.0115240 secs] [1 CMS-remark: 869959K(1060864K)] 1129862K(1624064K), 0.1832670 secs] [Times: user=0.23 sys=0.00, real=0.18 secs]
 *
 * @author <a href="mailto:bixuan@taobao.com">bixuan</a>
 */
public class CMSRemarkFGCLogParser implements GCLogParser {

	private static final Pattern pattern1=Pattern.compile("([0-9-T:]+)..*: ([0-9.]+):.*CMS-remark: ([0-9]+)K\\(.*, ([0-9.]+) secs");
	private static final Pattern pattern2=Pattern.compile("([0-9.]+):.*CMS-remark: ([0-9]+)K\\(.*, ([0-9.]+) secs");
	
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
