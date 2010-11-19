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
 * parse ParNew YGC log, -XX:+UseConcMarkSweepGC
 * 
 * 	Log example:
 *   2010-11-10T17:03:49.068+0800: 125.294: [GC 125.294: [ParNew: 558615K->13855K(563200K), 0.0177710 secs] 739960K->195200K(1624064K), 0.0179390 secs] [Times: user=0.05 sys=0.00, real=0.02 secs] 	
 *   125.294: [GC 125.294: [ParNew: 558615K->13855K(563200K), 0.0177710 secs] 739960K->195200K(1624064K), 0.0179390 secs] [Times: user=0.05 sys=0.00, real=0.02 secs]
 *
 * @author <a href="mailto:bluedavy@gmail.com">bluedavy</a>
 */
public class ParnewYGCLogParser extends CommonLogParser implements GCLogParser {

	private static final Pattern pattern1=Pattern.compile("([0-9-T:]+)..*: ([0-9.]+):.*ParNew: ([0-9]+)K->([0-9]+)K\\(.*, ([0-9.]+) secs");
	private static final Pattern pattern2=Pattern.compile("([0-9.]+):.*ParNew: ([0-9]+)K->([0-9]+)K\\(.*, ([0-9.]+) secs");
	
	public OneLineGCData parse(String lineInfo) throws Exception {
		return parse(lineInfo, pattern1, pattern2);
	}

	public boolean isYGC() {
		return true;
	}

	public String getGCDescription() {
		return "ParNewGC";
	}

	public boolean isCMSGC() {
		return true;
	}

}
