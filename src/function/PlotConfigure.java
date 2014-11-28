package function;

import java.text.DecimalFormat;

import android.graphics.Color;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.XYPlot;

public class PlotConfigure {

	
	public static void plotConfiguration(XYPlot fhrPlot,int domainLeft,int domainRight)
	{
		int domainsize = domainRight - domainLeft;
		fhrPlot.getGraphWidget().setMargins(10, 10, 10, 10);
		fhrPlot.getGraphWidget().getBackgroundPaint().setColor(Color.WHITE);
		fhrPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
		fhrPlot.getGraphWidget().getDomainGridLinePaint().setColor(Color.RED);
		fhrPlot.getGraphWidget().getRangeGridLinePaint().setColor(Color.RED);
		fhrPlot.getGraphWidget().getRangeLabelPaint().setTextSize(20);//.setColor(Color.TRANSPARENT);
		fhrPlot.getGraphWidget().getRangeLabelPaint().setColor(Color.RED);
		fhrPlot.getGraphWidget().getDomainLabelPaint().setTextSize(20);
		fhrPlot.getGraphWidget().getDomainLabelPaint().setColor(Color.RED);

		fhrPlot.setRangeBoundaries(30,240,BoundaryMode.FIXED);
		fhrPlot.setDomainBoundaries(domainLeft, domainRight, BoundaryMode.FIXED);	
		fhrPlot.setDomainStepValue(domainsize/10+1);
		fhrPlot.setRangeStepValue(22);
		fhrPlot.setTicksPerRangeLabel(3);
		fhrPlot.setTicksPerDomainLabel(6);
		fhrPlot.getGraphWidget().getDomainSubGridLinePaint().setColor(Color.rgb(220,175 ,175));
		fhrPlot.getGraphWidget().getRangeSubGridLinePaint().setColor(Color.rgb(220,175 , 175));
		fhrPlot.setDomainLabel("time");
	//	fhrPlot.getDomainLabelWidget().pack();
		fhrPlot.setRangeLabel("bpm");
	//    fhrPlot.getRangeLabelWidget().pack();	
	    
	    fhrPlot.setRangeValueFormat(new DecimalFormat("#"));	
	    fhrPlot.setDomainValueFormat(new DecimalFormat("#"));
	}
}
