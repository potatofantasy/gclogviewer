/**
 * High-Speed Service Framework (HSF)
 * 
 * www.taobao.com
 * 	(C) ÌÔ±¦(ÖÐ¹ú) 2003-2008
 */
package code.google.gclogviewer;

/**
 * extract One Line data to OneLineGCData Object 
 *
 * @author <a href="mailto:bixuan@taobao.com">bixuan</a>
 */
public class OneLineGCData {

	// int[0]: memoryBeforeGC int[2]: memoryAfterGC
	private String[] memoryChangeInfo=new String[2];
	
	// gc occur time
	private String occurTime;
	
	// gc occur dateTime
	private String occurDateTime;
	
	// gc pause time
	private String pauseTime;
	
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
	
}
