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
