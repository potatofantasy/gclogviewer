/**
 * High-Speed Service Framework (HSF)
 * 
 * www.taobao.com
 * 	(C) ÌÔ±¦(ÖÐ¹ú) 2003-2008
 */
package code.google.gclogviewer;

/**
 * Parse GC Log to GCLogData
 *
 * @author <a href="mailto:bluedavy@gmail.com">bluedavy</a>
 */
public interface GCLogParser {
	
	public OneLineGCData parse(String lineInfo) throws Exception;
	
	public boolean isYGC();
	
	public boolean isCMSGC();
	
	public String getGCDescription();
	
}
