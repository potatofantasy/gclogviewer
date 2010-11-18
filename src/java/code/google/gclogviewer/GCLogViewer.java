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

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.jfree.ui.RectangleInsets;

/**
 * GCLogViewer,create shell/generate chart graph/handle ui event
 * 
 * @author <a href="mailto:bluedavy@gmail.com">BlueDavy</a>
 */
public class GCLogViewer {

	private static final String[] FILTER_NAMES={"GC Log File (*.log)","All Files (*.*)"};
	private static final String[] FILTER_EXTS={"*.log","*.*"};
	
	private static final String SHELL_TITLE="GCLogViewer";
	
	private Shell shell = null;
	private Menu menuBar,fileMenu,toolsMenu;
	private MenuItem fileMenuHeader,toolsMenuItem,fileOpenMenuItem;
	private MenuItem memoryLeakDetectionMenuItem,gcTuningMenuItem;
	private MenuItem exitMenuItem;
	private Group summary = null,gcTrendGroup = null, memoryTrendGroup=null;
	private Label runtimedataLabel;
	private Label gctypedataLabel;
	private Label throughputdataLabel;
	private Label ygcDataLabel,ygctDataLabel,avgYGCTDataLabel,avgYGCRateDataLabel;
	private Label fgcDataLabel,fgctDataLabel,avgFGCTDataLabel,avgFGCRateDataLabel;
	private Label cmsgcDataLabel,cmsgctDataLabel,avgCMSGCTDataLabel,avgCMSGCRateDataLabel;
	private ChartComposite gcTrendChart=null ,memoryTrendChart=null;
	private ProgressBar bar;

	public static void main(String[] args) {
		Display display = Display.getDefault();
		GCLogViewer thisClass = new GCLogViewer();
		thisClass.createShell();
		thisClass.shell.open();

		while (!thisClass.shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private void createShell() {
		shell = new Shell(SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
		shell.setText(SHELL_TITLE);
		shell.setBackground(new Color(Display.getCurrent(), 255, 255, 255));
		shell.setMaximized(false);
		shell.setToolTipText("A free open source tool to visualize data produced by the Java VM options -Xloggc:<file>");
		Monitor primary = shell.getDisplay().getPrimaryMonitor ();
		Rectangle bounds = primary.getBounds ();
		shell.setSize(new Point(bounds.width-100, bounds.height-100));
		Rectangle rect = shell.getBounds ();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation (x, y);
		GridLayout layout=new GridLayout();
		layout.numColumns=1;
		shell.setLayout(layout);
		
		menuBar = new Menu(shell, SWT.BAR);
		fileMenuHeader = new MenuItem(menuBar,SWT.CASCADE);
		fileMenuHeader.setText("&File");
		toolsMenuItem = new MenuItem(menuBar,SWT.CASCADE);
		toolsMenuItem.setText("&Tools");
		exitMenuItem = new MenuItem(menuBar,SWT.CASCADE);
		exitMenuItem.setText("&Exit");
		exitMenuItem.addSelectionListener(new ExitListener());
		
		fileMenu = new Menu(shell, SWT.DROP_DOWN);
		fileMenuHeader.setMenu(fileMenu);
		
		fileOpenMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		fileOpenMenuItem.setText("&Open log file...");
		fileOpenMenuItem.addSelectionListener(new OpenFileListener());
		
		toolsMenu = new Menu(shell,SWT.DROP_DOWN);
		toolsMenuItem.setMenu(toolsMenu);
		
		memoryLeakDetectionMenuItem = new MenuItem(toolsMenu,SWT.PUSH);
		memoryLeakDetectionMenuItem.setText("Memory Leak Detection");
		gcTuningMenuItem = new MenuItem(toolsMenu, SWT.PUSH);
		gcTuningMenuItem.setText("GC Tuning");
		
		shell.setMenuBar(menuBar);
		
		createSummary();
		createGCTrendGroup();
		createMemoryTrendGroup();
		createProgressBar();
		
		// Info Grid
		GridData infoGrid=new GridData(GridData.FILL_BOTH);
		final Label runtimeLabel=new Label(summary,SWT.NONE);
		runtimeLabel.setText("Run time: ");
		runtimeLabel.setLayoutData(infoGrid);
		runtimedataLabel=new Label(summary,SWT.NONE);
		runtimedataLabel.setText("xxx seconds");
		runtimedataLabel.setLayoutData(infoGrid);
		final Label gctypeLabel=new Label(summary,SWT.NONE);
		gctypeLabel.setText("GC Type: ");
		gctypeLabel.setLayoutData(infoGrid);
		gctypedataLabel=new Label(summary,SWT.NONE);
		gctypedataLabel.setText("xxx");
		gctypedataLabel.setLayoutData(infoGrid);
		final Label throughputLabel=new Label(summary,SWT.NONE);
		throughputLabel.setText("Throughput: ");
		throughputLabel.setLayoutData(infoGrid);
		throughputdataLabel=new Label(summary,SWT.NONE);
		throughputdataLabel.setText("xx%");
		throughputdataLabel.setLayoutData(infoGrid);
		final Label emptyLabel=new Label(summary,SWT.NONE);
		emptyLabel.setText(" ");
		emptyLabel.setLayoutData(infoGrid);
		final Label emptyDataLabel=new Label(summary,SWT.NONE);
		emptyDataLabel.setText(" ");
		emptyDataLabel.setLayoutData(infoGrid);
		
		// YGC Grid
		GridData ygcInfoGrid=new GridData(GridData.FILL_BOTH);
		final Label ygcLabel=new Label(summary,SWT.NONE);
		ygcLabel.setText("YGC: ");
		ygcLabel.setLayoutData(ygcInfoGrid);
		ygcDataLabel=new Label(summary,SWT.NONE);
		ygcDataLabel.setText("xxx");
		ygcDataLabel.setLayoutData(ygcInfoGrid);
		final Label ygctLabel=new Label(summary,SWT.NONE);
		ygctLabel.setText("YGCT: ");
		ygctLabel.setLayoutData(ygcInfoGrid);
		ygctDataLabel=new Label(summary,SWT.NONE);
		ygctDataLabel.setText("xxx seconds");
		ygctDataLabel.setLayoutData(ygcInfoGrid);
		final Label avgYGCTLabel=new Label(summary,SWT.NONE);
		avgYGCTLabel.setText("Avg YGCT: ");
		avgYGCTLabel.setLayoutData(ygcInfoGrid);
		avgYGCTDataLabel=new Label(summary,SWT.NONE);
		avgYGCTDataLabel.setText("xxx seconds");
		avgYGCTDataLabel.setLayoutData(ygcInfoGrid);
		final Label avgYGCRateLabel=new Label(summary,SWT.NONE);
		avgYGCRateLabel.setText("Avg YGCRate: ");
		avgYGCRateLabel.setLayoutData(ygcInfoGrid);
		avgYGCRateDataLabel=new Label(summary,SWT.NONE);
		avgYGCRateDataLabel.setText("xxx seconds");
		avgYGCRateDataLabel.setLayoutData(ygcInfoGrid);
		
		// CMS Grid
		GridData cmsgcInfoGrid=new GridData(GridData.FILL_BOTH);
		cmsgcInfoGrid.exclude=true;
		final Label cmsgcLabel=new Label(summary,SWT.NONE);
		cmsgcLabel.setText("CMSGC: ");
		cmsgcLabel.setLayoutData(cmsgcInfoGrid);
		cmsgcDataLabel=new Label(summary,SWT.NONE);
		cmsgcDataLabel.setText("xxx");
		cmsgcDataLabel.setLayoutData(cmsgcInfoGrid);
		final Label cmsgctLabel=new Label(summary,SWT.NONE);
		cmsgctLabel.setText("CMSGCT: ");
		cmsgctLabel.setLayoutData(cmsgcInfoGrid);
		cmsgctDataLabel=new Label(summary,SWT.NONE);
		cmsgctDataLabel.setText("xxx seconds");
		cmsgctDataLabel.setLayoutData(cmsgcInfoGrid);
		final Label avgCMSGCTLabel=new Label(summary,SWT.NONE);
		avgCMSGCTLabel.setText("Avg CMSGCT: ");
		avgCMSGCTLabel.setLayoutData(cmsgcInfoGrid);
		avgCMSGCTDataLabel=new Label(summary,SWT.NONE);
		avgCMSGCTDataLabel.setText("xxx seconds");
		avgCMSGCTDataLabel.setLayoutData(cmsgcInfoGrid);
		final Label avgCMSGCRateLabel=new Label(summary,SWT.NONE);
		avgCMSGCRateLabel.setText("Avg CMSGCRate: ");
		avgCMSGCRateLabel.setLayoutData(cmsgcInfoGrid);
		avgCMSGCRateDataLabel=new Label(summary,SWT.NONE);
		avgCMSGCRateDataLabel.setText("xxx seconds");
		avgCMSGCRateDataLabel.setLayoutData(cmsgcInfoGrid);
		
		// FGC Grid
		GridData fgcInfoGrid=new GridData(GridData.FILL_BOTH);
		final Label fgcLabel=new Label(summary,SWT.NONE);
		fgcLabel.setText("FGC: ");
		fgcLabel.setLayoutData(fgcInfoGrid);
		fgcDataLabel=new Label(summary,SWT.NONE);
		fgcDataLabel.setText("xxx");
		fgcDataLabel.setLayoutData(fgcInfoGrid);
		final Label fgctLabel=new Label(summary,SWT.NONE);
		fgctLabel.setText("FGCT: ");
		fgctLabel.setLayoutData(fgcInfoGrid);
		fgctDataLabel=new Label(summary,SWT.NONE);
		fgctDataLabel.setText("xxx seconds");
		fgctDataLabel.setLayoutData(fgcInfoGrid);
		final Label avgFGCTLabel=new Label(summary,SWT.NONE);
		avgFGCTLabel.setText("Avg FGCT: ");
		avgFGCTLabel.setLayoutData(fgcInfoGrid);
		avgFGCTDataLabel=new Label(summary,SWT.NONE);
		avgFGCTDataLabel.setText("xxx seconds");
		avgFGCTDataLabel.setLayoutData(fgcInfoGrid);
		final Label avgFGCRateLabel=new Label(summary,SWT.NONE);
		avgFGCRateLabel.setText("Avg FGCRate: ");
		avgFGCRateLabel.setLayoutData(fgcInfoGrid);
		avgFGCRateDataLabel=new Label(summary,SWT.NONE);
		avgFGCRateDataLabel.setText("xxx seconds");
		avgFGCRateDataLabel.setLayoutData(fgcInfoGrid);
		
	}
	
	private void createSummary() {
		summary = new Group(shell, SWT.NONE);
		summary.setText("Summary");
		GridData grid=new GridData(GridData.FILL_BOTH);
		grid.heightHint=shell.getDisplay().getBounds().height/5;
		summary.setLayoutData(grid);
		GridLayout layout=new GridLayout();
		layout.numColumns=8;
		layout.makeColumnsEqualWidth=true;
		summary.setLayout(layout);
	}
	
	private void createGCTrendGroup() {
		gcTrendGroup = new Group(shell, SWT.NONE);
		gcTrendGroup.setText("GC Trend");
		GridData grid=new GridData(GridData.FILL_BOTH);
		grid.heightHint=shell.getDisplay().getBounds().height*4/10;
		gcTrendGroup.setLayoutData(grid);
		gcTrendGroup.setLayout(new GridLayout());
	}
	
	private void createMemoryTrendGroup() {
		memoryTrendGroup = new Group(shell, SWT.NONE);
		memoryTrendGroup.setText("Memory Trend");
		GridData grid=new GridData(GridData.FILL_BOTH);
		grid.heightHint=shell.getDisplay().getBounds().height*4/10;
		memoryTrendGroup.setLayoutData(grid);
		memoryTrendGroup.setLayout(new GridLayout());
	}
	
	private void createProgressBar(){
		bar=new ProgressBar(shell,SWT.NONE | SWT.SMOOTH);
		bar.setMinimum(0);
		bar.setMaximum(100);
		GridData grid=new GridData(GridData.FILL_BOTH);
		grid.heightHint=shell.getDisplay().getBounds().height/10;
		grid.exclude=true;
		bar.setLayoutData(grid);
	}
	
	private JFreeChart createGCTrendChart(GCLogData data) {
    	XYDataset gcTrendDataset = createGCTrendDataset(data);
        JFreeChart chart = ChartFactory.createXYLineChart(
            "GC Trend", 
            "Time(S)", 
            "Pause Time(ms)",
            gcTrendDataset, 
            PlotOrientation.VERTICAL,
            true, 
            true, 
            false
        );
        chart.setBackgroundPaint( java.awt.Color.white );
        chart.setBorderVisible( true );
        chart.setBorderPaint( java.awt.Color.BLACK );
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(java.awt.Color.lightGray);
        plot.setDomainGridlinePaint(java.awt.Color.white);
        plot.setRangeGridlinePaint(java.awt.Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.getRangeAxis().setFixedDimension(15.0);
        return chart;
    }
    
    private XYDataset createGCTrendDataset(GCLogData data) {
    	XYSeries ygcSeries=new XYSeries("YGC");
        Map<String, String> ygcPauseTimes=data.getYGCPauseTimes();
        DecimalFormat timeformat=new DecimalFormat("#0.00");
        DecimalFormat doubleformat=new DecimalFormat("#0.0000");
        for (Entry<String, String> entry : ygcPauseTimes.entrySet()) {
        	double happenTime=Double.parseDouble(timeformat.format(Double.parseDouble(entry.getKey())));
        	double pauseTime=Double.parseDouble(doubleformat.format(Double.parseDouble(entry.getValue())))*1000;
        	ygcSeries.add(happenTime,pauseTime,true);
		}
        XYSeries fgcSeries=new XYSeries("FGC");
        Map<String, String> fgcPauseTimes=data.getFGCPauseTimes();
        for (Entry<String, String> entry : fgcPauseTimes.entrySet()) {
        	double happenTime=Double.parseDouble(timeformat.format(Double.parseDouble(entry.getKey())));
        	double pauseTime=Double.parseDouble(doubleformat.format(Double.parseDouble(entry.getValue())))*1000;
        	fgcSeries.add(happenTime,pauseTime,true);
		}
        XYSeries cmsgcSeries=new XYSeries("CMSGC");
        Map<String, String> cmsgcPauseTimes=data.getCMSGCPauseTimes();
        for (Entry<String, String> entry : cmsgcPauseTimes.entrySet()) {
        	double happenTime=Double.parseDouble(timeformat.format(Double.parseDouble(entry.getKey())));
        	double pauseTime=Double.parseDouble(doubleformat.format(Double.parseDouble(entry.getValue())))*1000;
        	cmsgcSeries.add(happenTime,pauseTime,true);
		}
        XYSeriesCollection dataset = new XYSeriesCollection();
        if(fgcPauseTimes.size()>0)
        	dataset.addSeries(fgcSeries);
        if(cmsgcPauseTimes.size()>0)
        	dataset.addSeries(cmsgcSeries);
        dataset.addSeries(ygcSeries);
        return dataset;
    }
    
    /**
     * create Memory Trend Chart
     */
    private JFreeChart createMemoryTrendChart(GCLogData data) {
    	XYDataset memoryTrendDataset = createMemoryTrendDataset(data);
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Memory Trend", 
            "Time(S)", 
            "Memory Change(K)",
            memoryTrendDataset, 
            PlotOrientation.VERTICAL,
            true, 
            true, 
            false
        );
        chart.setBackgroundPaint( java.awt.Color.white );
        chart.setBorderVisible( true );
        chart.setBorderPaint( java.awt.Color.BLACK );
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(java.awt.Color.lightGray);
        plot.setDomainGridlinePaint(java.awt.Color.white);
        plot.setRangeGridlinePaint(java.awt.Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.getRangeAxis().setFixedDimension(15.0);              
        return chart;
    }
    
    /**
     * create Memory Trend Dataset
     */
    private XYDataset createMemoryTrendDataset(GCLogData data) {
    	XYSeries ygcSeries=new XYSeries("YGC");
        Map<String, String[]> ygcMemoryChanges=data.getYGCMemoryChanges();
        DecimalFormat doubleformat=new DecimalFormat("#0.00");
        for (Entry<String, String[]> entry : ygcMemoryChanges.entrySet()) {
        	String beginMemoryInfo=entry.getValue()[0];
        	String endMemoryInfo=entry.getValue()[1];
        	String happenTime=entry.getKey();
        	double beginMemory=Double.parseDouble(beginMemoryInfo);
        	double endMemory=Double.parseDouble(endMemoryInfo);
			double beginTime=Double.parseDouble(doubleformat.format(Double.parseDouble(happenTime)));
			ygcSeries.add(beginTime, beginMemory, true);
        	double pauseTime=Double.parseDouble(doubleformat.format(Double.parseDouble(data.getYGCPauseTimes().get(happenTime))));
        	ygcSeries.add(pauseTime+beginTime,endMemory,true);
		}
        XYSeries fgcSeries=new XYSeries("FGC");
        Map<String, String[]> fgcMemoryChanges=data.getFGCMemoryChanges();
        for (Entry<String, String[]> entry : fgcMemoryChanges.entrySet()) {
        	String beginMemoryInfo=entry.getValue()[0];
        	String endMemoryInfo=entry.getValue()[1];
        	String happenTime=entry.getKey();
        	double beginMemory=Double.parseDouble(beginMemoryInfo);
        	double endMemory=Double.parseDouble(endMemoryInfo);
			double beginTime=Double.parseDouble(doubleformat.format(Double.parseDouble(happenTime)));
			fgcSeries.add(beginTime, beginMemory, true);
        	double pauseTime=Double.parseDouble(doubleformat.format(Double.parseDouble(data.getFGCPauseTimes().get(happenTime))));
        	fgcSeries.add(pauseTime+beginTime,endMemory,true);
		}
        XYSeries cmsgcSeries=new XYSeries("CMSGC");
        Map<String, String[]> cmsgcMemoryChanges=data.getCMSGCMemoryChanges();
        for (Entry<String, String[]> entry : cmsgcMemoryChanges.entrySet()) {
        	String beginMemoryInfo=entry.getValue()[0];
        	String endMemoryInfo=entry.getValue()[1];
        	String happenTime=entry.getKey();
        	double beginMemory=Double.parseDouble(beginMemoryInfo);
        	double endMemory=Double.parseDouble(endMemoryInfo);
			double beginTime=Double.parseDouble(doubleformat.format(Double.parseDouble(happenTime)));
			cmsgcSeries.add(beginTime, beginMemory, true);
        	double pauseTime=Double.parseDouble(doubleformat.format(Double.parseDouble(data.getCMSGCPauseTimes().get(happenTime))));
        	cmsgcSeries.add(pauseTime+beginTime,endMemory,true);
		}
        XYSeriesCollection dataset = new XYSeriesCollection();
        if(fgcMemoryChanges.size()>0){
        	dataset.addSeries(fgcSeries);
        }
        if(cmsgcMemoryChanges.size()>0){
        	dataset.addSeries(cmsgcSeries);
        }
        dataset.addSeries(ygcSeries);
        return dataset;

    }
	
	class OpenFileListener extends SelectionAdapter{

		private class WatchChartProgress implements ChartProgressListener {
			public void chartProgress(ChartProgressEvent event) {
				switch (event.getType()) {
					case ChartProgressEvent.DRAWING_STARTED:
						bar.setSelection(bar.getSelection()+5);
						break;
					case ChartProgressEvent.DRAWING_FINISHED:
						bar.setSelection(bar.getSelection()+30);
						break;
					default:
						break;
				}
			}
		}

		public void widgetSelected(SelectionEvent event) {
			FileDialog dialog=new FileDialog(shell,SWT.OPEN);
			dialog.setFilterNames(FILTER_NAMES);
			dialog.setFilterExtensions(FILTER_EXTS);
			final String fileName=dialog.open();
			if((fileName!=null)&&(!"".equals(fileName))){
				Display.getDefault().syncExec(new Runnable(){
					public void run() {
						shell.setText(SHELL_TITLE+": "+fileName);
						((GridData)bar.getLayoutData()).exclude=false;
						shell.layout();
					}
				});
				new Thread(new Runnable(){

					public void run() {
						GCLogAnalyze analyze=new GCLogAnalyze();
						try {
							final GCLogData data=analyze.analysis(fileName);
							Display.getDefault().asyncExec(new Runnable(){
								public void run() {
									bar.setSelection(5);
								}
							});
							final JFreeChart chart=createGCTrendChart(data);
							Display.getDefault().asyncExec(new Runnable(){
								public void run() {
									bar.setSelection(10);
								}
							});
							final JFreeChart chart2=createMemoryTrendChart(data);
							Display.getDefault().asyncExec(new Runnable(){
								public void run() {
									bar.setSelection(15);
								}
							});
							chart.addProgressListener(new WatchChartProgress());
							chart2.addProgressListener(new WatchChartProgress());
							Display.getDefault().asyncExec(new Runnable(){

								public void run() {
									runtimedataLabel.setText(data.getRuntime());
									bar.setSelection(16);
									gctypedataLabel.setText(data.getGCType());
									bar.setSelection(17);
									throughputdataLabel.setText(data.getThroughput());
									bar.setSelection(18);
									ygcDataLabel.setText(String.valueOf(data.getYGC()));
									bar.setSelection(22);
									ygctDataLabel.setText(data.getYGCT());
									bar.setSelection(23);
									avgYGCTDataLabel.setText(data.getAvgYGCT());
									bar.setSelection(24);
									avgYGCRateDataLabel.setText(data.getAvgYGCRate());
									fgcDataLabel.setText(String.valueOf(data.getFGC()));
									bar.setSelection(25);
									fgctDataLabel.setText(data.getFGCT());
									bar.setSelection(26);
									avgFGCTDataLabel.setText(data.getAvgFGCT());
									avgFGCRateDataLabel.setText(data.getAvgFGCRate());
									bar.setSelection(27);
									if(data.getCMSGC()>0){
										cmsgcDataLabel.setText(String.valueOf(data.getCMSGC()));
										cmsgctDataLabel.setText(data.getCMSGCT());
										avgCMSGCTDataLabel.setText(data.getAvgCMSGCT());
										avgCMSGCRateDataLabel.setText(data.getAvgCMSGCRate());
										((GridData)cmsgcDataLabel.getLayoutData()).exclude=false;
										summary.layout();
									}
									else{
										((GridData)cmsgcDataLabel.getLayoutData()).exclude=true;
										summary.layout();
									}
									bar.setSelection(30);
									
									if(gcTrendChart==null){
										gcTrendChart=new GCLogChartComposite(gcTrendGroup,SWT.NONE,chart,true);
										GridData grid=new GridData(GridData.FILL_BOTH);
										gcTrendChart.setLayoutData(grid);
									}
									else{
										gcTrendChart.setChart(chart);
									}
									gcTrendChart.pack();
									gcTrendGroup.layout();
									if(memoryTrendChart==null){
										memoryTrendChart=new GCLogChartComposite(memoryTrendGroup,SWT.NONE,chart2,true);
										GridData grid=new GridData(GridData.FILL_BOTH);
										memoryTrendChart.setLayoutData(grid);
									}
									else{
										memoryTrendChart.setChart(chart2);
									}
									memoryTrendChart.pack();
									memoryTrendGroup.layout();
								}
							});
						} 
						catch (final Exception e) {
							Display.getDefault().asyncExec(new Runnable(){
								public void run() {
									MessageBox messageBox = new MessageBox(shell, SWT.ERROR | SWT.OK);
									messageBox.setText(e.toString());
									StringBuilder errorString=new StringBuilder("Pls visit GCLogViewer website to feedback this exception,error Details: \r\n");
									StackTraceElement[] eles=e.getStackTrace();
									for (int i = eles.length-1; i > eles.length-5; i--) {
										errorString.append(eles[i]);
										errorString.append("\r\n");
									}
									messageBox.setMessage(errorString.toString());
							        messageBox.open();
								}
							});
							e.printStackTrace();
						}
					}
					
				}).start();
			}
		}
		
	}
	
	class ExitListener extends SelectionAdapter {
	    
		public void widgetSelected(SelectionEvent event) {
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
	        messageBox.setText("Warning");
	        messageBox.setMessage("Are you sure exit?");
	        int buttonID = messageBox.open();
	        switch(buttonID) {
	          case SWT.OK:
	        	shell.close();
	  	    	Display.getDefault().dispose();
	          case SWT.CANCEL:
	            break;
	        }
	    }
	}

}