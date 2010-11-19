/**
 * GCLogViewer
 * 
 * A free open source tool to visualize data produced by the Java VM options -Xloggc:<file> or jstat > <file>.
 * 
 * Code license:	Apache License 2.0
 * 
 * http://code.google.com/p/gclogviewer
 */
package code.google.gclogviewer.parser;

import java.util.regex.Pattern;

import code.google.gclogviewer.GCLogParser;
import code.google.gclogviewer.OneLineGCData;

/**
 * parse CMS Failed FGC log, -XX:+UseConcMarkSweepGC
 * 
 * 	Log example:
 * 	2010-11-11T16:11:11.948+0800: 83368.174: [GC 83368.175: [ParNew (promotion failed): 563061K->563061K(563200K), 0.1841480 secs]83368.359: [CMS: 693918K->226725K(1060864K), 1.7836900 secs] 1215048K->226725K(1624064K), 1.9682580 secs] [Times: user=2.06 sys=0.09, real=1.97 secs] 
 *  83368.174: [GC 83368.175: [ParNew (promotion failed): 563061K->563061K(563200K), 0.1841480 secs]83368.359: [CMS: 693918K->226725K(1060864K), 1.7836900 secs] 1215048K->226725K(1624064K), 1.9682580 secs] [Times: user=2.06 sys=0.09, real=1.97 secs]
 *
 * @author <a href="mailto:bluedavy@gmail.com">bluedavy</a>
 */
public class CMSFailedFGCLogParser extends CommonLogParser implements GCLogParser {

	private static final Pattern pattern1=Pattern.compile("([0-9-T:]+)..*: ([0-9.]+):.*CMS.*secs] ([0-9]+)K->([0-9]+)K\\(.*, ([0-9.]+) secs");
	private static final Pattern pattern2=Pattern.compile("([0-9.]+):.*CMS.*secs] ([0-9]+)K->([0-9]+)K\\(.*, ([0-9.]+) secs");
	
	public OneLineGCData parse(String lineInfo) throws Exception {
		return parse(lineInfo, pattern1, pattern2);
	}

	public boolean isYGC() {
		return false;
	}

	public String getGCDescription() {
		return "ConcMarkSweepGC";
	}

	public boolean isCMSGC() {
		return false;
	}

}
