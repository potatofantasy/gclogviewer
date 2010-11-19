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

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.JFreeChart;
import org.jfree.experimental.chart.swt.ChartComposite;

/**
 * override mouseDown event,disable redraw where mousedown,so performance will be a little well
 * 
 * @author <a href="mailto:bluedavy@gmail.com">bluedavy</a>
 */
public class GCLogChartComposite extends ChartComposite {

	public GCLogChartComposite(Composite comp, int style, JFreeChart chart,boolean useBuffer) {
		super(comp, style, chart,DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_MINIMUM_DRAW_WIDTH,DEFAULT_MINIMUM_DRAW_HEIGHT,DEFAULT_MAXIMUM_DRAW_WIDTH,DEFAULT_MAXIMUM_DRAW_HEIGHT,true, false,true,true,true,true);
	}

	@Override
	public void mouseDown(MouseEvent event) {
		// Do Nothing
	}
	
}
