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

import java.io.File;
import java.util.BitSet;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
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

import code.google.gclogviewer.gui.GCLogChartComposite;
import code.google.gclogviewer.parser.GCDataStore;
import code.google.gclogviewer.parser.GCParserDriver;
import code.google.gclogviewer.parser.GCStats;
import code.google.gclogviewer.parser.GCType;

/**
 * GCLogViewer,create shell/generate graph/handle ui event
 * 
 * TODO: refator to more small classes
 * 
 * @author <a href="mailto:bluedavy@gmail.com">BlueDavy</a>
 */
public class GCLogViewer
{

    private static final String[] FILTER_NAMES =
    { "GC Log File (*.log)", "All Files (*.*)" };
    private static final String[] FILTER_EXTS =
    { "*.log", "*.*" };

    private static final String SHELL_TITLE = "GCLogViewer";

    private Shell shell = null;
    private Menu menuBar, fileMenu;
    private MenuItem fileMenuHeader, fileOpenMenuItem, exitMenuItem;
    private Group summary = null, charGroup = null, minorGCGroup = null, fullGCGroup = null, cmsGCGroup = null;
    private Label runtimedataLabel;
    private Label gctypedataLabel;
    private Label throughputdataLabel;
    private Label sumAppStopDataLabel;
    private Label maxAppStopDataLabel;
    private Label avgAppStopDataLabel;
    private Label minorGCTimesDataLabel;
    private Label minorGCConsumeTimesDataLabel;
    private Label avgMinorGCConsumeTimeDataLabel;
    private Label fullGCTimesDataLabel;
    private Label fullGCConsumeTimesDataLabel;
    private Label avgFullGCConsumeTimeDataLabel;
    private ChartComposite minorGCChart = null, fullGCChart = null, cmsGCChart = null;
    private Label cmsFail1DataLabel;
    private Label cmsFail2DataLabel;
    private Label cmsFail1Label;
    private Label cmsFail2Label;
    private ProgressBar bar;

    /**
     * @param args
     */
    public static void main(String[] args)
    {
	Display display = Display.getDefault();
	GCLogViewer thisClass = new GCLogViewer();
	thisClass.createShell();
	thisClass.shell.open();

	while (!thisClass.shell.isDisposed())
	{
	    if (!display.readAndDispatch())
		display.sleep();
	}
	display.dispose();
    }

    /**
     * This method initializes sShell
     */
    private void createShell()
    {
	shell = new Shell(SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
	shell.setText(SHELL_TITLE);
	shell.setBackground(new Color(Display.getCurrent(), 255, 255, 255));
	shell.setMaximized(false);
	shell.setToolTipText("A free open source tool to visualize data produced by the Java VM options -Xloggc:<file> or jstat > <file>.");
	shell.setSize(new Point(800, 900));
	Monitor primary = shell.getDisplay().getPrimaryMonitor();
	Rectangle bounds = primary.getBounds();
	Rectangle rect = shell.getBounds();
	int x = bounds.x + (bounds.width - rect.width) / 2;
	int y = bounds.y + (bounds.height - rect.height) / 2;
	shell.setLocation(x, y);
	GridLayout layout = new GridLayout();
	layout.numColumns = 1;
	shell.setLayout(layout);

	menuBar = new Menu(shell, SWT.BAR);
	fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
	fileMenuHeader.setText("&File");
	exitMenuItem = new MenuItem(menuBar, SWT.CASCADE);
	exitMenuItem.setText("&Exit");
	exitMenuItem.addSelectionListener(new ExitListener());

	fileMenu = new Menu(shell, SWT.DROP_DOWN);
	fileMenuHeader.setMenu(fileMenu);

	fileOpenMenuItem = new MenuItem(fileMenu, SWT.PUSH);
	fileOpenMenuItem.setText("&Open log file...");
	fileOpenMenuItem.addSelectionListener(new OpenFileListener());

	shell.setMenuBar(menuBar);

	createSummary();
	createChartGroup();
	createProgressBar();

	final Label runtimeLabel = new Label(summary, SWT.NONE);
	runtimeLabel.setText("Run time: ");
	runtimedataLabel = new Label(summary, SWT.NONE);
	runtimedataLabel.setText("xxx seconds");
	final Label gctypeLabel = new Label(summary, SWT.NONE);
	gctypeLabel.setText("GC Type: ");
	GridData gctypeDataGrid = new GridData(GridData.FILL_HORIZONTAL);
	gctypeDataGrid.horizontalSpan = 3;
	gctypedataLabel = new Label(summary, SWT.NONE);
	gctypedataLabel.setText("xxx");
	gctypedataLabel.setLayoutData(gctypeDataGrid);

	final Label throughputLabel = new Label(summary, SWT.NONE);
	throughputLabel.setText("Throughput: ");
	GridData throughputLabelGrid = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
	throughputLabelGrid.verticalSpan = 2;
	throughputLabel.setLayoutData(throughputLabelGrid);
	throughputdataLabel = new Label(summary, SWT.NONE);
	throughputdataLabel.setText("xx%");
	GridData throughputGrid = new GridData(GridData.FILL_BOTH);
	throughputGrid.horizontalSpan = 5;
	throughputGrid.verticalSpan = 2;
	throughputdataLabel.setLayoutData(throughputGrid);

	GridData appStopTimeGrid = new GridData(GridData.FILL_BOTH);
	appStopTimeGrid.verticalSpan = 3;
	final Label sumAppStopLabel = new Label(summary, SWT.NONE);
	sumAppStopLabel.setText("Sum App Stopped Time: ");
	sumAppStopLabel.setLayoutData(appStopTimeGrid);
	sumAppStopDataLabel = new Label(summary, SWT.NONE);
	sumAppStopDataLabel.setText("xxx seconds");
	sumAppStopDataLabel.setLayoutData(appStopTimeGrid);
	final Label maxAppStopLabel = new Label(summary, SWT.NONE);
	maxAppStopLabel.setText("Max App Stopped Time: ");
	maxAppStopLabel.setLayoutData(appStopTimeGrid);
	maxAppStopDataLabel = new Label(summary, SWT.NONE);
	maxAppStopDataLabel.setText("xxx seconds");
	maxAppStopDataLabel.setLayoutData(appStopTimeGrid);
	final Label avgAppStopLabel = new Label(summary, SWT.NONE);
	avgAppStopLabel.setText("Avg App Stopped Time: ");
	avgAppStopLabel.setLayoutData(appStopTimeGrid);
	avgAppStopDataLabel = new Label(summary, SWT.NONE);
	avgAppStopDataLabel.setText("xxx ms/minute");
	avgAppStopDataLabel.setLayoutData(appStopTimeGrid);

	GridData minorGCTimeGrid = new GridData(GridData.FILL_BOTH);
	minorGCTimeGrid.verticalSpan = 4;
	final Label minorGCTimesLabel = new Label(summary, SWT.NONE);
	minorGCTimesLabel.setText("MinorGC times: ");
	minorGCTimesLabel.setLayoutData(minorGCTimeGrid);
	minorGCTimesDataLabel = new Label(summary, SWT.NONE);
	minorGCTimesDataLabel.setText("xxx");
	minorGCTimesDataLabel.setLayoutData(minorGCTimeGrid);
	final Label minorGCConsumeTimesLabel = new Label(summary, SWT.NONE);
	minorGCConsumeTimesLabel.setText("Consume Time: ");
	minorGCConsumeTimesLabel.setLayoutData(minorGCTimeGrid);
	minorGCConsumeTimesDataLabel = new Label(summary, SWT.NONE);
	minorGCConsumeTimesDataLabel.setText("xxx seconds");
	minorGCConsumeTimesDataLabel.setLayoutData(minorGCTimeGrid);
	final Label avgMinorGCConsumeTimeLabel = new Label(summary, SWT.NONE);
	avgMinorGCConsumeTimeLabel.setText("Avg Consume Time: ");
	avgMinorGCConsumeTimeLabel.setLayoutData(minorGCTimeGrid);
	avgMinorGCConsumeTimeDataLabel = new Label(summary, SWT.NONE);
	avgMinorGCConsumeTimeDataLabel.setText("xxx seconds");
	avgMinorGCConsumeTimeDataLabel.setLayoutData(minorGCTimeGrid);

	GridData fullGCTimeGrid = new GridData(GridData.FILL_BOTH);
	fullGCTimeGrid.verticalSpan = 5;
	final Label fullGCTimesLabel = new Label(summary, SWT.NONE);
	fullGCTimesLabel.setText("FullGC times: ");
	fullGCTimesLabel.setLayoutData(fullGCTimeGrid);
	fullGCTimesDataLabel = new Label(summary, SWT.NONE);
	fullGCTimesDataLabel.setText("xxx");
	fullGCTimesDataLabel.setLayoutData(fullGCTimeGrid);
	final Label fullGCConsumeTimesLabel = new Label(summary, SWT.NONE);
	fullGCConsumeTimesLabel.setText("Consume Time: ");
	fullGCConsumeTimesLabel.setLayoutData(fullGCTimeGrid);
	fullGCConsumeTimesDataLabel = new Label(summary, SWT.NONE);
	fullGCConsumeTimesDataLabel.setText("xxx seconds");
	fullGCConsumeTimesDataLabel.setLayoutData(fullGCTimeGrid);
	final Label avgFullGCConsumeTimeLabel = new Label(summary, SWT.NONE);
	avgFullGCConsumeTimeLabel.setText("Avg Consume Time: ");
	avgFullGCConsumeTimeLabel.setLayoutData(fullGCTimeGrid);
	avgFullGCConsumeTimeDataLabel = new Label(summary, SWT.NONE);
	avgFullGCConsumeTimeDataLabel.setText("xxx seconds");
	avgFullGCConsumeTimeDataLabel.setLayoutData(fullGCTimeGrid);

	GridData cmsFailGrid = new GridData(GridData.FILL_BOTH);
	cmsFailGrid.verticalSpan = 6;
	cmsFailGrid.exclude = true;
	cmsFail1Label = new Label(summary, SWT.NONE);
	cmsFail1Label.setText("promotion failed: ");
	cmsFail1Label.setLayoutData(cmsFailGrid);
	cmsFail1DataLabel = new Label(summary, SWT.NONE);
	cmsFail1DataLabel.setText("xxx");
	cmsFail1DataLabel.setLayoutData(cmsFailGrid);
	cmsFail2Label = new Label(summary, SWT.NONE);
	cmsFail2Label.setText("concurrent mode fail: ");
	cmsFail2Label.setLayoutData(cmsFailGrid);
	cmsFail2DataLabel = new Label(summary, SWT.NONE);
	cmsFail2DataLabel.setText("xxx");
	GridData cmsFail2DataGrid = new GridData(GridData.FILL_BOTH);
	cmsFail2DataGrid.horizontalSpan = 3;
	cmsFail2DataGrid.verticalSpan = 6;
	cmsFail2DataGrid.exclude = true;
	cmsFail2DataLabel.setLayoutData(cmsFail2DataGrid);

    }

    /**
     * This method initializes summary
     * 
     */
    private void createSummary()
    {
	summary = new Group(shell, SWT.NONE);
	summary.setText("Summary");
	GridData grid = new GridData(GridData.FILL_BOTH);
	grid.heightHint = (int) (shell.getDisplay().getBounds().height * 21 / 100);
	summary.setLayoutData(grid);
	GridLayout layout = new GridLayout();
	layout.numColumns = 6;
	layout.makeColumnsEqualWidth = true;
	summary.setLayout(layout);
    }

    private void createChartGroup()
    {
	charGroup = new Group(shell, SWT.NONE);
	GridData grid = new GridData(GridData.FILL_BOTH);
	grid.heightHint = (int) (shell.getDisplay().getBounds().height * 70 / 100);

	charGroup.setLayoutData(grid);
	charGroup.setLayout(new FillLayout(SWT.VERTICAL));

	createMinorGCGroup();
	createFullGCGroup();
    }

    private void createMinorGCGroup()
    {
	minorGCGroup = new Group(charGroup, SWT.NONE);
	minorGCGroup.setText("Minor GC Trend");
	minorGCGroup.setLayout(new GridLayout());
    }

    private void createFullGCGroup()
    {
	fullGCGroup = new Group(charGroup, SWT.NONE);
	fullGCGroup.setText("Full GC Trend");
	fullGCGroup.setLayout(new GridLayout());
    }

    private void createProgressBar()
    {
	bar = new ProgressBar(shell, SWT.NONE | SWT.SMOOTH);
	bar.setMinimum(0);
	bar.setMaximum(100);
	GridData grid = new GridData(GridData.FILL_BOTH);
	grid.heightHint = (int) (shell.getDisplay().getBounds().height * 10 / 100);
	grid.exclude = true;
	bar.setLayoutData(grid);
    }

    /**
     * create MinorGC Chart
     */
    private JFreeChart createMinorGCChart(GCStats data)
    {
	XYDataset minorgcDataset = createMinorGCDataset("minor gc infos", data);
	JFreeChart chart = ChartFactory.createXYLineChart("Minor GC Trend", "Time(S)", "Memory Change(K)", minorgcDataset, PlotOrientation.VERTICAL, true, true, false);
	chart.setBackgroundPaint(java.awt.Color.white);
	chart.setBorderVisible(true);
	chart.setBorderPaint(java.awt.Color.BLACK);
	XYPlot plot = (XYPlot) chart.getPlot();
	plot.setBackgroundPaint(java.awt.Color.lightGray);
	plot.setDomainGridlinePaint(java.awt.Color.white);
	plot.setRangeGridlinePaint(java.awt.Color.white);
	plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
	plot.getRangeAxis().setFixedDimension(15.0);
	return chart;
    }

    private JFreeChart createCMSGCChart(GCStats data)
    {
	XYDataset cmsgcDataset = createCMSGCDataset("cms gc infos", data);
	JFreeChart chart = ChartFactory.createXYLineChart("CMS GC Trend", "Time(S)", "Memeory Change(K)", cmsgcDataset, PlotOrientation.VERTICAL, true, true, false);
	chart.setBackgroundPaint(java.awt.Color.white);
	chart.setBorderVisible(true);
	chart.setBorderPaint(java.awt.Color.BLACK);
	XYPlot plot = (XYPlot) chart.getPlot();
	plot.setBackgroundPaint(java.awt.Color.lightGray);
	plot.setDomainGridlinePaint(java.awt.Color.white);
	plot.setRangeGridlinePaint(java.awt.Color.white);
	plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
	plot.getRangeAxis().setFixedDimension(15.0);
	return chart;
    }

    /**
     * create MinorGC Dataset
     */
    private XYDataset createMinorGCDataset(String name, GCStats data)
    {
	XYSeries series = new XYSeries(name);
	Map<Double, Double> timeMap = data.getMinorGCConsumeTimes();
	Map<Double, Double[]> memoeryMap = data.getMinorGCMemoryChanges();

	for (Entry<Double, Double> entry : timeMap.entrySet())
	{
	    double beginTime = entry.getKey();
	    double consumeTime = entry.getValue();
	    Double[] memoeryChanges = memoeryMap.get(beginTime);

	    series.add(beginTime, memoeryChanges[0], true);
	    series.add(consumeTime + beginTime, memoeryChanges[1], true);
	}
	XYSeriesCollection dataset = new XYSeriesCollection();
	dataset.addSeries(series);
	return dataset;
    }

    private XYDataset createCMSGCDataset(String name, GCStats data)
    {
	XYSeries series = new XYSeries(name);
	Map<Double, Double> timeMap = data.getCmsGCConsumeTimes();
	Map<Double, Double[]> memoeryMap = data.getCmsGCMemoryChanges();

	for (Entry<Double, Double> entry : timeMap.entrySet())
	{
	    double beginTime = entry.getKey();
	    double consumeTime = entry.getValue();
	    Double[] memoeryChanges = memoeryMap.get(beginTime);

	    series.add(beginTime, memoeryChanges[0], true);
	    series.add(consumeTime + beginTime, memoeryChanges[1], true);
	}
	XYSeriesCollection dataset = new XYSeriesCollection();
	dataset.addSeries(series);
	return dataset;
    }

    /**
     * create FullGC Chart
     */
    private JFreeChart createFullGCChart(GCStats data)
    {
	XYDataset fullgcDataset = createFullGCDataset("full gc infos", data);
	JFreeChart chart = ChartFactory.createXYLineChart("Full GC Trend", "Time(S)", "Memory Change(K)", fullgcDataset, PlotOrientation.VERTICAL, true, true, false);
	chart.setBackgroundPaint(java.awt.Color.white);
	chart.setBorderVisible(true);
	chart.setBorderPaint(java.awt.Color.BLACK);
	XYPlot plot = (XYPlot) chart.getPlot();
	plot.setBackgroundPaint(java.awt.Color.lightGray);
	plot.setDomainGridlinePaint(java.awt.Color.white);
	plot.setRangeGridlinePaint(java.awt.Color.white);
	plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
	plot.getRangeAxis().setFixedDimension(15.0);
	return chart;
    }

    /**
     * create FullGC Dataset
     */
    private XYDataset createFullGCDataset(String name, GCStats data)
    {
	XYSeries series = new XYSeries(name);

	Map<Double, Double> timeMap = data.getFullGCConsumeTimes();
	Map<Double, Double[]> memoeryMap = data.getFullGCMemoryChanges();

	for (Entry<Double, Double> entry : timeMap.entrySet())
	{
	    double beginTime = entry.getKey();
	    double consumeTime = entry.getValue();
	    Double[] memoeryChanges = memoeryMap.get(beginTime);

	    series.add(beginTime, memoeryChanges[0], true);
	    series.add(consumeTime + beginTime, memoeryChanges[1], true);
	}
	XYSeriesCollection dataset = new XYSeriesCollection();
	dataset.addSeries(series);
	return dataset;

    }

    class OpenFileListener extends SelectionAdapter
    {

	private class WatchChartProgress implements ChartProgressListener
	{
	    public void chartProgress(ChartProgressEvent event)
	    {
		switch (event.getType())
		{
		case ChartProgressEvent.DRAWING_STARTED:
		    bar.setSelection(bar.getSelection() + 5);
		    break;
		case ChartProgressEvent.DRAWING_FINISHED:
		    bar.setSelection(bar.getSelection() + 30);
		    break;
		default:
		    break;
		}
	    }
	}

	public void widgetSelected(SelectionEvent event)
	{
	    FileDialog dialog = new FileDialog(shell, SWT.OPEN);
	    dialog.setFilterNames(FILTER_NAMES);
	    dialog.setFilterExtensions(FILTER_EXTS);
	    final String fileName = dialog.open();
	    if ((fileName != null) && (!"".equals(fileName)))
	    {
		Display.getDefault().syncExec(new Runnable()
		{
		    public void run()
		    {
			shell.setText(SHELL_TITLE + ": " + fileName);
			((GridData) bar.getLayoutData()).exclude = false;
			shell.layout();
		    }
		});
		new Thread(new Runnable()
		{

		    public void run()
		    {
			try
			{

			    BitSet actions = new BitSet();
			    GCParserDriver driver = new GCParserDriver(actions);
			    driver.parse(new File(fileName));
			    final GCDataStore gcData = (GCDataStore) driver.gc_stats();

			    Display.getDefault().asyncExec(new Runnable()
			    {
				public void run()
				{
				    bar.setSelection(5);
				}
			    });
			    final JFreeChart chart = createMinorGCChart(gcData);
			    Display.getDefault().asyncExec(new Runnable()
			    {
				public void run()
				{
				    bar.setSelection(10);
				}
			    });
			    final JFreeChart chart2 = createFullGCChart(gcData);
			    Display.getDefault().asyncExec(new Runnable()
			    {
				public void run()
				{
				    bar.setSelection(15);
				}
			    });

			    final JFreeChart cmsGCThrendChart = createCMSGCChart(gcData);

			    chart.addProgressListener(new WatchChartProgress());
			    chart2.addProgressListener(new WatchChartProgress());
			    cmsGCThrendChart.addProgressListener(new WatchChartProgress());

			    Display.getDefault().asyncExec(new Runnable()
			    {

				public void run()
				{
				    runtimedataLabel.setText(gcData.getGCLastLogTime());
				    bar.setSelection(16);
				    gctypedataLabel.setText(gcData.getGCType());
				    bar.setSelection(17);
				    throughputdataLabel.setText(gcData.getThroughput());
				    bar.setSelection(18);
				    sumAppStopDataLabel.setText(gcData.getSumAppStoppedTime());
				    bar.setSelection(19);
				    maxAppStopDataLabel.setText(gcData.getMaxAppStoppedTime());
				    bar.setSelection(20);
				    avgAppStopDataLabel.setText(gcData.getAvgStopTime());
				    bar.setSelection(21);
				    minorGCTimesDataLabel.setText(String.valueOf(gcData.getMinorGCCount()));
				    bar.setSelection(22);
				    minorGCConsumeTimesDataLabel.setText(gcData.getMinorGCTotalConsumeTime());
				    bar.setSelection(23);
				    avgMinorGCConsumeTimeDataLabel.setText(gcData.getAvgMinorGCConsumeTime());
				    bar.setSelection(24);
				    fullGCTimesDataLabel.setText(String.valueOf(gcData.getFullGCCount()));
				    bar.setSelection(25);
				    fullGCConsumeTimesDataLabel.setText(gcData.getFullGCConsumeTime());
				    bar.setSelection(26);
				    avgFullGCConsumeTimeDataLabel.setText(gcData.getAvgFullGCConsumeTime());
				    bar.setSelection(27);

				    if (gcData.getGcType() == GCType.CONC_MARK_SWEEP_GC)
				    {
					cmsFail1DataLabel.setText(String.valueOf(gcData.getCmsGCPromotionFailedCount()));
					cmsFail2DataLabel.setText(String.valueOf(gcData.getCmsGCConcurrentModeFailedCount()));

					if (cmsGCGroup == null)
					{
					    cmsGCGroup = new Group(charGroup, SWT.NONE);
					    cmsGCGroup.setText("CMS GC Trend");
					    cmsGCGroup.setLayout(new GridLayout());
					}
					
					if (cmsGCChart == null)
					{
					    cmsGCChart = new GCLogChartComposite(cmsGCGroup, SWT.NONE, cmsGCThrendChart, true);
					    GridData grid = new GridData(GridData.FILL_BOTH);
					    cmsGCChart.setLayoutData(grid);
					} else
					{
					    cmsGCChart.setChart(cmsGCThrendChart);
					}

					((GridData) cmsFail1DataLabel.getLayoutData()).exclude = false;
					((GridData) cmsFail2DataLabel.getLayoutData()).exclude = false;

					cmsGCGroup.layout();
					charGroup.layout();
					summary.layout();

				    } else
				    {
					((GridData) cmsFail1DataLabel.getLayoutData()).exclude = true;
					((GridData) cmsFail2DataLabel.getLayoutData()).exclude = true;

					if (cmsGCGroup != null)
					{
					    cmsGCGroup.dispose();
					}

					charGroup.layout();
					summary.layout();
				    }

				    bar.setSelection(30);
				    if (fullGCChart == null)
				    {
					fullGCChart = new GCLogChartComposite(fullGCGroup, SWT.NONE, chart2, true);
					GridData grid = new GridData(GridData.FILL_BOTH);
					fullGCChart.setLayoutData(grid);
				    } else
				    {
					fullGCChart.setChart(chart2);
				    }
				    fullGCChart.pack();
				    fullGCGroup.layout();

				    if (minorGCChart == null)
				    {
					minorGCChart = new GCLogChartComposite(minorGCGroup, SWT.NONE, chart, true);
					GridData grid = new GridData(GridData.FILL_BOTH);
					minorGCChart.setLayoutData(grid);
				    } else
				    {
					minorGCChart.setChart(chart);
				    }
				    minorGCChart.pack();
				    minorGCGroup.layout();

				    shell.layout();
				}

			    });
			} catch (final Exception e)
			{
			    Display.getDefault().asyncExec(new Runnable()
			    {
				public void run()
				{
				    MessageBox messageBox = new MessageBox(shell, SWT.ERROR | SWT.OK);
				    messageBox.setText(e.toString());
				    StringBuilder errorString = new StringBuilder("Pls visit GCLogViewer website to feedback this exception,error Details: \r\n");
				    StackTraceElement[] eles = e.getStackTrace();
				    for (int i = eles.length - 1; i > eles.length - 5; i--)
				    {
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

    class ExitListener extends SelectionAdapter
    {

	public void widgetSelected(SelectionEvent event)
	{
	    MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
	    messageBox.setText("Warning");
	    messageBox.setMessage("Are you sure exit?");
	    int buttonID = messageBox.open();
	    switch (buttonID)
	    {
	    case SWT.OK:
		shell.close();
		Display.getDefault().dispose();
	    case SWT.CANCEL:
		break;
	    }
	}
    }

}