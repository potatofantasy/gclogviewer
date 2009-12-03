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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Analyze gc log to GCLogData
 * 
 * TODO: Refator to Strategy pattern,so can be extended to support multi jvm version and other jvm(like ibm)
 * 
 * @author <a href="mailto:bluedavy@gmail.com">BlueDavy</a>
 */
public class GCLogAnalyze {

	private static final String APPSTOP_WORD_PREFIX="Total time ";
	
	private static final int APPSTOP_WORD_PREFIX_LEN=
		"Total time for which application threads were stopped: ".length();
	
	private static final int APPSTOP_WORD_SUFFIX_LEN=" seconds".length();
	
	private static final String FULLGC_KEYWORD="Full GC";
	
	private static final String MINORGC_KEYWORD1="ParNew";
	
	private static final String MINORGC_KEYWORD2="PSYoungGen";
	
	private static final String CMSGC_KEYWORD="CMS-";
	
	private static final String CMSGC_FAIL1="promotion failed";
	
	private static final String CMSGC_FAIL2="concurrent mode failure";
	
	public static final String CMSGC_TYPE="CMS";
	
	public static final String PARAGC_TYPE="Parallel";
	
	public GCLogData analysis(String fileName) throws Exception{
		BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))));
		String line=null;
		GCLogData data=new GCLogData();
		double sumAppStoppedTime=0;
		double maxAppStoppedTime=0;
		int cmsGCFail1Count=0,cmsGCFail2Count=0;
		double maxGCLogTime=0;
		while((line=reader.readLine())!=null){
			line=line.trim();
			// Full GC
			if(line.indexOf(FULLGC_KEYWORD)!=-1){
				double happenTime=Double.parseDouble(line.substring(0,line.indexOf(":")));
				if(happenTime>maxGCLogTime)
					maxGCLogTime=happenTime;
				if(line.indexOf("real=")==-1){
					System.out.println("ignore full gc info,maybe because it's not complete,line info: "+line);
				}
				else{
					String consumeTime=line.substring(line.indexOf("real=")+5,line.length()-" secs]".length());
					int memoryChangeInfoBegin=0;
					if(line.indexOf(CMSGC_TYPE)!=-1){
						memoryChangeInfoBegin=line.indexOf("]")+1;
						data.setGCType(CMSGC_TYPE+" GC");
					}
					else{
						memoryChangeInfoBegin=line.indexOf("]", line.indexOf("]")+1)+1;
						data.setGCType(PARAGC_TYPE+" GC");
					}
					int memoryChangeInfoEnd=line.indexOf("(", memoryChangeInfoBegin);
					try{
						String memoryChangeInfo=line.substring(memoryChangeInfoBegin,memoryChangeInfoEnd).trim();
						String[] memoryChangeInfos=memoryChangeInfo.split("->");
						data.getFullGCConsumeTimes().put(String.valueOf(happenTime), consumeTime);
						data.getFullGCMemoryChanges().put(String.valueOf(happenTime), memoryChangeInfos);
					}
					catch(StringIndexOutOfBoundsException e){
						System.out.println("igore gull gc info,maybe because this line info not correct: "+line);
					}
				}
			}
			// Minor GC
			else if((line.indexOf(MINORGC_KEYWORD1)!=-1 || line.indexOf(MINORGC_KEYWORD2)!=-1)){
				// handle promotion failed special
				if(line.indexOf(CMSGC_FAIL1)==-1){
					double happenTime=Double.parseDouble(line.substring(0,line.indexOf(":")));
					if(happenTime>maxGCLogTime)
						maxGCLogTime=happenTime;
					int memoryChangeInfoBegin=line.indexOf("]")+1;
					int memoryChangeInfoEnd=line.indexOf(" secs]", memoryChangeInfoBegin);
					String memoryChangeInfo=line.substring(memoryChangeInfoBegin,memoryChangeInfoEnd).trim();
					String consumeTime=(memoryChangeInfo.split(",")[1]).trim();
					memoryChangeInfo=memoryChangeInfo.split(",")[0];
					memoryChangeInfo=memoryChangeInfo.substring(0,memoryChangeInfo.indexOf("("));
					String[] memoryChangeInfos=memoryChangeInfo.split("->");
					data.getMinorGCConsumeTimes().put(String.valueOf(happenTime), consumeTime);
					data.getMinorGCMemoryChanges().put(String.valueOf(happenTime), memoryChangeInfos);
				}
			}
			// CMS GC
			else if(line.indexOf(CMSGC_KEYWORD)!=-1){
				
			}
			// Application stopped time
			else if(line.startsWith(APPSTOP_WORD_PREFIX)){
				double stopTime=Double.parseDouble(line.substring(APPSTOP_WORD_PREFIX_LEN,line.length()-APPSTOP_WORD_SUFFIX_LEN));
				sumAppStoppedTime+=stopTime;
				if(stopTime>maxAppStoppedTime)
					maxAppStoppedTime=stopTime;
			}
			
			if(line.indexOf(CMSGC_FAIL1)!=-1){
				cmsGCFail1Count++;
			}
			
			if(line.indexOf(CMSGC_FAIL2)!=-1){
				cmsGCFail2Count++;
			}
		}
		data.setSumAppStoppedTime(sumAppStoppedTime);
		data.setMaxAppStoppedTime(maxAppStoppedTime);
		data.setCmsGCFail1Count(cmsGCFail1Count);
		data.setCmsGCFail2Count(cmsGCFail2Count);
		data.setGcLogTime(maxGCLogTime);
		return data;
	}
	
	class GCLogData{
	
		// key: time  value: consumetime
		private Map<String, String> minorGCConsumeTimes=new HashMap<String, String>();
		
		// key: time value: String[0]: memoryBefore String[1]: memoryAfter
		private Map<String, String[]> minorGCMemoryChanges=new HashMap<String, String[]>();
		
		// key: time  value: consumetime
		private Map<String, String> fullGCConsumeTimes=new HashMap<String, String>();
		
		// key: time value: String[0]: memoryBefore String[1]: memoryAfter
		private Map<String, String[]> fullGCMemoryChanges=new HashMap<String, String[]>();
		
		// key: time  value: consumetime
		private Map<String, String> cmsGCConsumeTimes=new HashMap<String, String>();
		
		private double sumAppStoppedTime;
		
		private double maxAppStoppedTime;
		
		private int cmsGCFail1Count=0;
		
		private int cmsGCFail2Count=0;
		
		private double gcLogTime=0;
		
		private DecimalFormat doubleformat=new DecimalFormat("#0.00");

		public Map<String, String> getMinorGCConsumeTimes() {
			return minorGCConsumeTimes;
		}

		public Map<String, String[]> getMinorGCMemoryChanges() {
			return minorGCMemoryChanges;
		}

		public Map<String, String> getFullGCConsumeTimes() {
			return fullGCConsumeTimes;
		}

		public Map<String, String[]> getFullGCMemoryChanges() {
			return fullGCMemoryChanges;
		}

		public Map<String, String> getCmsGCConsumeTimes() {
			return cmsGCConsumeTimes;
		}

		public String getSumAppStoppedTime() {
			return doubleformat.format(sumAppStoppedTime)+" seconds";
		}

		public String getMaxAppStoppedTime() {
			return doubleformat.format(maxAppStoppedTime)+" seconds";
		}

		public void setMinorGCConsumeTimes(Map<String, String> minorGCConsumeTimes) {
			this.minorGCConsumeTimes = minorGCConsumeTimes;
		}

		public void setMinorGCMemoryChanges(Map<String, String[]> minorGCMemoryChanges) {
			this.minorGCMemoryChanges = minorGCMemoryChanges;
		}

		public void setFullGCConsumeTimes(Map<String, String> fullGCConsumeTimes) {
			this.fullGCConsumeTimes = fullGCConsumeTimes;
		}

		public void setFullGCMemoryChanges(Map<String, String[]> fullGCMemoryChanges) {
			this.fullGCMemoryChanges = fullGCMemoryChanges;
		}

		public void setCmsGCConsumeTimes(Map<String, String> cmsGCConsumeTimes) {
			this.cmsGCConsumeTimes = cmsGCConsumeTimes;
		}

		public void setSumAppStoppedTime(double sumAppStoppedTime) {
			this.sumAppStoppedTime = sumAppStoppedTime;
		}

		public void setMaxAppStoppedTime(double maxAppStoppedTime) {
			this.maxAppStoppedTime = maxAppStoppedTime;
		}

		public int getCmsGCFail1Count() {
			return cmsGCFail1Count;
		}

		public int getCmsGCFail2Count() {
			return cmsGCFail2Count;
		}

		public void setCmsGCFail1Count(int cmsGCFail1Count) {
			this.cmsGCFail1Count = cmsGCFail1Count;
		}

		public void setCmsGCFail2Count(int cmsGCFail2Count) {
			this.cmsGCFail2Count = cmsGCFail2Count;
		}

		public String getGcLogTime() {
			return doubleformat.format(gcLogTime)+" s";
		}

		public void setGcLogTime(double gcLogTime) {
			this.gcLogTime = gcLogTime;
		}

		public String getThroughput() {
			return doubleformat.format(sumAppStoppedTime*100/gcLogTime)+"%";
		}
		
		public int getFullGCCount(){
			return fullGCConsumeTimes.size();
		}
		
		public String getFullGCConsumeTime(){
			double consumeTimes=0;
			for (String consumeTime : fullGCConsumeTimes.values()) {
				consumeTimes+=Double.parseDouble(consumeTime);
			}
			return doubleformat.format(consumeTimes)+" s";
		}
		
		public String getAvgFullGCConsumeTime(){
			double consumeTimes=0;
			for (String consumeTime : fullGCConsumeTimes.values()) {
				consumeTimes+=Double.parseDouble(consumeTime);
			}
			return doubleformat.format(consumeTimes/getFullGCCount())+" s";
		}
		
		public int getMinorGCCount(){
			return minorGCConsumeTimes.size();
		}
		
		public String getMinorGCConsumeTime(){
			double consumeTimes=0;
			for (String consumeTime : minorGCConsumeTimes.values()) {
				consumeTimes+=Double.parseDouble(consumeTime);
			}
			return doubleformat.format(consumeTimes)+" s";
		}
		
		public String getAvgMinorGCConsumeTime(){
			double consumeTimes=0;
			for (String consumeTime : minorGCConsumeTimes.values()) {
				consumeTimes+=Double.parseDouble(consumeTime);
			}
			return doubleformat.format(consumeTimes/getMinorGCCount())+" s";
		}
		
		public String getAvgStopTime(){
			return doubleformat.format(sumAppStoppedTime*60*1000/gcLogTime)+" ms/m";
		}
		
		private String gcType;
		
		public String getGCType(){
			return gcType;
		}
		
		public void setGCType(String gcType){
			this.gcType=gcType;
		}
	
	}
	
}
