/**
 * High-Speed Service Framework (HSF)
 * 
 * www.taobao.com
 * 	(C) ÌÔ±¦(ÖÐ¹ú) 2003-2008
 */
package name.bluedavy.jvm.gc;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.JFreeChart;
import org.jfree.experimental.chart.swt.ChartComposite;

/**
 * ÃèÊö£º
 *
 * @author <a href="mailto:bixuan@taobao.com">bixuan</a>
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
