package code.google.gclogviewer;

import junit.framework.TestCase;
import code.google.gclogviewer.parser.CMSFailedFGCLogParser;
import code.google.gclogviewer.parser.CMSInitialMarkFGCLogParser;
import code.google.gclogviewer.parser.CMSRemarkFGCLogParser;
import code.google.gclogviewer.parser.ParFGCLogParser;
import code.google.gclogviewer.parser.ParallelFGCLogParser;
import code.google.gclogviewer.parser.ParallelYGCLogParser;
import code.google.gclogviewer.parser.ParnewYGCLogParser;

public class ParserTest extends TestCase{

	public void testParallelYGCParser() throws Exception {
		String line="2010-11-09T08:04:37.037+0800: 24.791: [GC [PSYoungGen: 462628K->62805K(512000K)] 462628K->62805K(1470464K), 0.0817750 secs] [Times: user=0.14 sys=0.15, real=0.08 secs]";
		GCLogParser parser=new ParallelYGCLogParser();
		OneLineGCData data = parser.parse(line);
		assertEquals("2010-11-09T08:04:37", data.getOccurDateTime());
		assertEquals("24.791",data.getOccurTime());
		assertEquals("0.0817750",data.getPauseTime());
		assertEquals("462628",data.getMemoryChangeInfo()[0]);
		assertEquals("62805",data.getMemoryChangeInfo()[1]);
		line="24.791: [GC [PSYoungGen: 462628K->62805K(512000K)] 462628K->62805K(1470464K), 0.0817750 secs] [Times: user=0.14 sys=0.15, real=0.08 secs]";
		data = parser.parse(line);
		assertNull(data.getOccurDateTime());
		assertEquals("24.791",data.getOccurTime());
		assertEquals("0.0817750",data.getPauseTime());
		assertEquals("462628",data.getMemoryChangeInfo()[0]);
		assertEquals("62805",data.getMemoryChangeInfo()[1]);
	}
	
	public void testParnewYGCParser() throws Exception {
		String line="2010-11-10T17:03:49.068+0800: 125.294: [GC 125.294: [ParNew: 558615K->13855K(563200K), 0.0177710 secs] 739960K->195200K(1624064K), 0.0179390 secs] [Times: user=0.05 sys=0.00, real=0.02 secs]";
		GCLogParser parser=new ParnewYGCLogParser();
		OneLineGCData data = parser.parse(line);
		assertEquals("2010-11-10T17:03:49", data.getOccurDateTime());
		assertEquals("125.294",data.getOccurTime());
		assertEquals("0.0179390",data.getPauseTime());
		assertEquals("558615",data.getMemoryChangeInfo()[0]);
		assertEquals("13855",data.getMemoryChangeInfo()[1]);
		line="125.294: [GC 125.294: [ParNew: 558615K->13855K(563200K), 0.0177710 secs] 739960K->195200K(1624064K), 0.0179390 secs] [Times: user=0.05 sys=0.00, real=0.02 secs]";
		data = parser.parse(line);
		assertNull(data.getOccurDateTime());
		assertEquals("125.294",data.getOccurTime());
		assertEquals("0.0179390",data.getPauseTime());
		assertEquals("558615",data.getMemoryChangeInfo()[0]);
		assertEquals("13855",data.getMemoryChangeInfo()[1]);
	}
	
	public void testParallelFGCParser() throws Exception {
		String line="2010-11-09T18:45:52.548+0800: 38500.303: [Full GC [PSYoungGen: 42686K->0K(512000K)] [PSOldGen: 958137K->140581K(958464K)] 1000824K->140581K(1470464K) [PSPermGen: 52608K->52608K(131072K)], 0.4473140 secs] [Times: user=0.45 sys=0.00, real=0.44 secs]";
		GCLogParser parser=new ParallelFGCLogParser();
		OneLineGCData data = parser.parse(line);
		assertEquals("2010-11-09T18:45:52", data.getOccurDateTime());
		assertEquals("38500.303",data.getOccurTime());
		assertEquals("0.4473140",data.getPauseTime());
		assertEquals("1000824",data.getMemoryChangeInfo()[0]);
		assertEquals("140581",data.getMemoryChangeInfo()[1]);
		line="38500.303: [Full GC [PSYoungGen: 42686K->0K(512000K)] [PSOldGen: 958137K->140581K(958464K)] 1000824K->140581K(1470464K) [PSPermGen: 52608K->52608K(131072K)], 0.4473140 secs] [Times: user=0.45 sys=0.00, real=0.44 secs]";
		data = parser.parse(line);
		assertNull(data.getOccurDateTime());
		assertEquals("38500.303",data.getOccurTime());
		assertEquals("0.4473140",data.getPauseTime());
		assertEquals("1000824",data.getMemoryChangeInfo()[0]);
		assertEquals("140581",data.getMemoryChangeInfo()[1]);
	}
	
	public void testParallelOldFGCParser() throws Exception {
		String line="2010-11-09T18:45:52.548+0800: 38500.303: [Full GC [PSYoungGen: 42686K->0K(512000K)] [ParOldGen: 958137K->140581K(958464K)] 1000824K->140581K(1470464K) [PSPermGen: 52608K->52608K(131072K)], 0.4473140 secs] [Times: user=0.45 sys=0.00, real=0.44 secs]";
		GCLogParser parser=new ParFGCLogParser();
		OneLineGCData data = parser.parse(line);
		assertEquals("2010-11-09T18:45:52", data.getOccurDateTime());
		assertEquals("38500.303",data.getOccurTime());
		assertEquals("0.4473140",data.getPauseTime());
		assertEquals("1000824",data.getMemoryChangeInfo()[0]);
		assertEquals("140581",data.getMemoryChangeInfo()[1]);
		line="38500.303: [Full GC [PSYoungGen: 42686K->0K(512000K)] [ParOldGen: 958137K->140581K(958464K)] 1000824K->140581K(1470464K) [PSPermGen: 52608K->52608K(131072K)], 0.4473140 secs] [Times: user=0.45 sys=0.00, real=0.44 secs]";
		data = parser.parse(line);
		assertNull(data.getOccurDateTime());
		assertEquals("38500.303",data.getOccurTime());
		assertEquals("0.4473140",data.getPauseTime());
		assertEquals("1000824",data.getMemoryChangeInfo()[0]);
		assertEquals("140581",data.getMemoryChangeInfo()[1]);
	}
	
	public void testCMSFailedFGCParser() throws Exception {
		String line="2010-11-11T16:11:11.948+0800: 83368.174: [GC 83368.175: [ParNew (promotion failed): 563061K->563061K(563200K), 0.1841480 secs]83368.359: [CMS: 693918K->226725K(1060864K), 1.7836900 secs] 1215048K->226725K(1624064K), 1.9682580 secs] [Times: user=2.06 sys=0.09, real=1.97 secs]";
		GCLogParser parser=new CMSFailedFGCLogParser();
		OneLineGCData data = parser.parse(line);
		assertEquals("2010-11-11T16:11:11", data.getOccurDateTime());
		assertEquals("83368.174",data.getOccurTime());
		assertEquals("1.9682580",data.getPauseTime());
		assertEquals("1215048",data.getMemoryChangeInfo()[0]);
		assertEquals("226725",data.getMemoryChangeInfo()[1]);
		line="83368.174: [GC 83368.175: [ParNew (promotion failed): 563061K->563061K(563200K), 0.1841480 secs]83368.359: [CMS: 693918K->226725K(1060864K), 1.7836900 secs] 1215048K->226725K(1624064K), 1.9682580 secs] [Times: user=2.06 sys=0.09, real=1.97 secs]";
		data = parser.parse(line);
		assertNull(data.getOccurDateTime());
		assertEquals("83368.174",data.getOccurTime());
		assertEquals("1.9682580",data.getPauseTime());
		assertEquals("1215048",data.getMemoryChangeInfo()[0]);
		assertEquals("226725",data.getMemoryChangeInfo()[1]);
	}
	
	public void testCMSInitialMarkFGCParser() throws Exception {
		String line="2010-11-11T13:53:14.724+0800: 75090.950: [GC [1 CMS-initial-mark: 869925K(1060864K)] 874081K(1624064K), 0.0069590 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]";
		GCLogParser parser=new CMSInitialMarkFGCLogParser();
		OneLineGCData data = parser.parse(line);
		assertEquals("2010-11-11T13:53:14", data.getOccurDateTime());
		assertEquals("75090.950",data.getOccurTime());
		assertEquals("0.0069590",data.getPauseTime());
		assertEquals("869925",data.getMemoryChangeInfo()[0]);
		assertEquals("869925",data.getMemoryChangeInfo()[1]);
		line="75090.950: [GC [1 CMS-initial-mark: 869925K(1060864K)] 874081K(1624064K), 0.0069590 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]";
		data = parser.parse(line);
		assertNull(data.getOccurDateTime());
		assertEquals("75090.950",data.getOccurTime());
		assertEquals("0.0069590",data.getPauseTime());
		assertEquals("869925",data.getMemoryChangeInfo()[0]);
		assertEquals("869925",data.getMemoryChangeInfo()[1]);
	}
	
	public void testCMSRemarkFGCParser() throws Exception {
		String line="2010-11-11T13:53:18.634+0800: 75094.860: [GC[YG occupancy: 259902 K (563200 K)]75094.860: [Rescan (parallel) , 0.0586970 secs]75094.919: [weak refs processing, 0.0168870 secs]75094.936: [class unloading, 0.0413000 secs]75094.977: [scrub symbol & string tables, 0.0115240 secs] [1 CMS-remark: 869959K(1060864K)] 1129862K(1624064K), 0.1832670 secs] [Times: user=0.23 sys=0.00, real=0.18 secs]";
		GCLogParser parser=new CMSRemarkFGCLogParser();
		OneLineGCData data = parser.parse(line);
		assertEquals("2010-11-11T13:53:18", data.getOccurDateTime());
		assertEquals("75094.860",data.getOccurTime());
		assertEquals("0.1832670",data.getPauseTime());
		assertEquals("869959",data.getMemoryChangeInfo()[0]);
		assertEquals("869959",data.getMemoryChangeInfo()[1]);
		line="75094.860: [GC[YG occupancy: 259902 K (563200 K)]75094.860: [Rescan (parallel) , 0.0586970 secs]75094.919: [weak refs processing, 0.0168870 secs]75094.936: [class unloading, 0.0413000 secs]75094.977: [scrub symbol & string tables, 0.0115240 secs] [1 CMS-remark: 869959K(1060864K)] 1129862K(1624064K), 0.1832670 secs] [Times: user=0.23 sys=0.00, real=0.18 secs]";
		data = parser.parse(line);
		assertNull(data.getOccurDateTime());
		assertEquals("75094.860",data.getOccurTime());
		assertEquals("0.1832670",data.getPauseTime());
		assertEquals("869959",data.getMemoryChangeInfo()[0]);
		assertEquals("869959",data.getMemoryChangeInfo()[1]);
	}

}
