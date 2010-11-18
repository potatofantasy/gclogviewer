package code.google.gclogviewer.parser;

import java.util.regex.Pattern;

import code.google.gclogviewer.GCLogParser;
import code.google.gclogviewer.OneLineGCData;

/**
 * parse Parallel FGC log, -XX:+UseParallelGC
 * 
 * 	Log example:
 * 	2010-11-09T18:45:52.548+0800: 38500.303: [Full GC [PSYoungGen: 42686K->0K(512000K)] [PSOldGen: 958137K->140581K(958464K)] 1000824K->140581K(1470464K) [PSPermGen: 52608K->52608K(131072K)], 0.4473140 secs] [Times: user=0.45 sys=0.00, real=0.44 secs] 
 *  38500.303: [Full GC [PSYoungGen: 42686K->0K(512000K)] [PSOldGen: 958137K->140581K(958464K)] 1000824K->140581K(1470464K) [PSPermGen: 52608K->52608K(131072K)], 0.4473140 secs] [Times: user=0.45 sys=0.00, real=0.44 secs]
 *
 * @author <a href="mailto:bixuan@taobao.com">bixuan</a>
 */
public class ParallelFGCLogParser extends CommonLogParser implements GCLogParser {

	private static final Pattern pattern1=Pattern.compile("([0-9-T:]+)..*: ([0-9.]+):.*PSOldGen.*K\\)] ([0-9]+)K->([0-9]+)K\\(.*, ([0-9.]+) secs");
	private static final Pattern pattern2=Pattern.compile("([0-9.]+):.*PSOldGen.*K\\)] ([0-9]+)K->([0-9]+)K\\(.*, ([0-9.]+) secs");
	
	public OneLineGCData parse(String lineInfo) throws Exception {
		return parse(lineInfo, pattern1, pattern2);
	}

	public boolean isYGC() {
		return false;
	}

	public String getGCDescription() {
		return "ParallelGC";
	}

	public boolean isCMSGC() {
		return false;
	}

}
