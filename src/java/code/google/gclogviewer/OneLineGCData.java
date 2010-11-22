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

/**
 * extract One Line data to OneLineGCData Object 
 *
 * @author <a href="mailto:bluedavy@gmail.com">bluedavy</a>
 */
public class OneLineGCData {

	// int[0]: memoryBeforeGC int[1]: memoryAfterGC
	private String[] memoryChangeInfo=new String[2];
	
	// HeapMemoryAfter
	private String heapMemoryAfter;

	// gc occur time
	private String occurTime;
	
	// gc occur dateTime
	private String occurDateTime;
	
	// gc pause time
	private String pauseTime;
	
	private boolean isYGCData;
	
	public boolean isYGCData() {
		return isYGCData;
	}

	public void setYGCData(boolean isYGCData) {
		this.isYGCData = isYGCData;
	}

	public String getOccurDateTime() {
		return occurDateTime;
	}

	public void setOccurDateTime(String occurDateTime) {
		this.occurDateTime = occurDateTime;
	}

	public String[] getMemoryChangeInfo() {
		return memoryChangeInfo;
	}

	public String getOccurTime() {
		return occurTime;
	}

	public String getPauseTime() {
		return pauseTime;
	}

	public void setMemoryChangeInfo(String[] memoryChangeInfo) {
		this.memoryChangeInfo = memoryChangeInfo;
	}

	public void setOccurTime(String occurTime) {
		this.occurTime = occurTime;
	}

	public void setPauseTime(String pauseTime) {
		this.pauseTime = pauseTime;
	}
	
	public String getHeapMemoryAfter() {
		return heapMemoryAfter;
	}

	public void setHeapMemoryAfter(String heapMemoryAfter) {
		this.heapMemoryAfter = heapMemoryAfter;
	}
	
}
