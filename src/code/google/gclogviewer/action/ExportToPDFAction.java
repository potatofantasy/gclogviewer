package code.google.gclogviewer.action;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.jfree.chart.JFreeChart;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import code.google.gclogviewer.parser.GCStats;
import code.google.gclogviewer.parser.GCType;

public class ExportToPDFAction implements Action
{

    private String exportPath;
    private GCStats gcStats;
    private JFreeChart fullGCChart;
    private JFreeChart minorGCChart;
    private JFreeChart cmsGCChart;

    @Override
    public void execute() throws Exception
    {
	OutputStream out = new FileOutputStream(exportPath);

	Document document = new Document(PageSize.A4);
	PdfWriter writer = PdfWriter.getInstance(document, out);
	document.addAuthor("GCLogViewer");
	document.addSubject("gc log");
	document.open();
	
	PdfPTable summaryTable = new PdfPTable(6);
	summaryTable.setTotalWidth(document.getPageSize().getHeight());
	
	PdfPCell header = new PdfPCell(new Paragraph("Summary"));
	header.setColspan(6);
	summaryTable.addCell(header);

	// col 1
	summaryTable.addCell("Rum time:");
	summaryTable.addCell(gcStats.getGCLastLogTime());
	summaryTable.addCell("GC Type:");
	summaryTable.addCell(gcStats.getGCType());
	summaryTable.addCell("Throughput:");
	summaryTable.addCell(gcStats.getThroughput());

	// col2
	summaryTable.addCell("Sum App Stopped Time:");
	summaryTable.addCell(gcStats.getSumAppStoppedTime());
	summaryTable.addCell("Max App Stopped Time:");
	summaryTable.addCell(gcStats.getMaxAppStoppedTime());
	summaryTable.addCell("Avg App Stopped Time:");
	summaryTable.addCell(gcStats.getAvgStopTime());

	// col3
	summaryTable.addCell("MinorGC times:");
	summaryTable.addCell(String.valueOf(gcStats.getMinorGCCount()));
	summaryTable.addCell("Consume Time:");
	summaryTable.addCell(gcStats.getMinorGCTotalConsumeTime());
	summaryTable.addCell("Avg Consume Time:");
	summaryTable.addCell(gcStats.getAvgMinorGCConsumeTime());

	// col3
	summaryTable.addCell("FullGC times: ");
	summaryTable.addCell(String.valueOf(gcStats.getFullGCCount()));
	summaryTable.addCell("Consume Time: ");
	summaryTable.addCell(gcStats.getFullGCConsumeTime());
	summaryTable.addCell("Avg Consume Time: ");
	summaryTable.addCell(gcStats.getAvgFullGCConsumeTime());

	// col4
	if (gcStats.getGcType() == GCType.CONC_MARK_SWEEP_GC)
	{
	    summaryTable.addCell("promotion failed: ");
	    summaryTable.addCell(String.valueOf(gcStats.getCmsGCPromotionFailedCount()));
	    summaryTable.addCell("concurrent mode fail: ");
	    summaryTable.addCell(String.valueOf(gcStats.getCmsGCConcurrentModeFailedCount()));
	    summaryTable.addCell("");
	    summaryTable.addCell("");
	}
	document.add(summaryTable);

	PdfContentByte pdfContentByte = writer.getDirectContent();
	Graphics2D graphics2D = pdfContentByte.createGraphics(document.getPageSize().getWidth(), 620, new DefaultFontMapper());
	Rectangle2D rectangle2D = new Rectangle2D.Double(0, 0, document.getPageSize().getWidth(), 200);
	minorGCChart.draw(graphics2D, rectangle2D);
	rectangle2D = new Rectangle2D.Double(0, 210, document.getPageSize().getWidth(), 200); 
	fullGCChart.draw(graphics2D, rectangle2D);
	if (gcStats.getGcType() == GCType.CONC_MARK_SWEEP_GC)
	{
	    rectangle2D = new Rectangle2D.Double(0, 420, document.getPageSize().getWidth(), 200); 
	    cmsGCChart.draw(graphics2D, rectangle2D);
	}
	
	graphics2D.dispose();
	
	document.close();
	out.close();
    }

    public GCStats getGcStats()
    {
	return gcStats;
    }

    public void setGcStats(GCStats gcStats)
    {
	this.gcStats = gcStats;
    }

    public JFreeChart getMinorGCChart()
    {
	return minorGCChart;
    }

    public void setMinorGCChart(JFreeChart minorGCChart)
    {
	this.minorGCChart = minorGCChart;
    }

    public JFreeChart getCmsGCChart()
    {
	return cmsGCChart;
    }

    public void setCmsGCChart(JFreeChart cmsGCChart)
    {
	this.cmsGCChart = cmsGCChart;
    }

    public String getExportPath()
    {
	return exportPath;
    }

    public void setExportPath(String exportPath)
    {
	this.exportPath = exportPath;
    }

    public JFreeChart getFullGCChart()
    {
	return fullGCChart;
    }

    public void setFullGCChart(JFreeChart fullGCChart)
    {
	this.fullGCChart = fullGCChart;
    }

}
