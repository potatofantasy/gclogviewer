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
 * parse Parallel YGC log, -XX:+UseParallelGC
 * 
 * 	Log example:
 *   2010-11-09T08:04:37.037+0800: 24.791: [GC [PSYoungGen: 462628K->62805K(512000K)] 462628K->62805K(1470464K), 0.0817750 secs] [Times: user=0.14 sys=0.15, real=0.08 secs]	
 *   24.791: [GC [PSYoungGen: 462628K->62805K(512000K)] 462628K->62805K(1470464K), 0.0817750 secs] [Times: user=0.14 sys=0.15, real=0.08 secs]
 *
 * @author <a href="mailto:bluedavy@gmail.com">bluedavy</a>
 */
public class ParallelYGCLogParser extends CommonYGCLogParser implements GCLogParser {

	private static final Pattern pattern1=Pattern.compile("([0-9-T:]+)..*: ([0-9.]+):.*PSYoungGen: ([0-9]+)K->([0-9]+)K\\(.*->([0-9]+)K\\(.*, ([0-9.]+) secs");
	private static final Pattern pattern2=Pattern.compile("([0-9.]+):.*PSYoungGen: ([0-9]+)K->([0-9]+)K\\(.*->([0-9]+)K\\(.*, ([0-9.]+) secs");
	
	public OneLineGCData parse(String lineInfo) throws Exception {
		return parse(lineInfo, pattern1, pattern2);
	}

	public boolean isYGC() {
		return true;
	}

	public String getGCDescription() {
		return "ParallelGC";
	}

	public boolean isCMSGC() {
		return false;
	}

}
