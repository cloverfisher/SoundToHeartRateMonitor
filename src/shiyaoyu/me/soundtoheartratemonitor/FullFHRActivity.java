package shiyaoyu.me.soundtoheartratemonitor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.androidplot.util.Redrawer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

public class FullFHRActivity extends Activity implements OnTouchListener{

	 private XYPlot fhrPlot = null; 
	 private SimpleXYSeries fullFHRSeries = null;
	 Redrawer redrawer;
	 private PointF minXY;
	 private PointF maxXY;
	 static final int HISTORY_SIZE = 120;
	 static final String LOG_E = "ysy_full_fhr";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_fullfhr);
		
		Intent intent = getIntent() ;
		ArrayList timelist = intent.getParcelableArrayListExtra("time");
		ArrayList bpmlist = intent.getParcelableArrayListExtra("bpm");
		fullFHRSeries = new SimpleXYSeries(timelist,bpmlist,"fhr");


//		for(int i = 0; i < timelist.size();i++)
//		{
//			Log.e("ysy", "bpm:" + bpmlist.get(i));
//			fullFHRSeries.addLast((Double)timelist.get(i), (Double)bpmlist.get(i));
//		}
		Log.e("LOG_E", "time size:"+ timelist.size());
		/*plot part*/
		//TODO plot
		plotConfiguration();
		fhrPlot.setOnTouchListener(this);
		fhrPlot.addSeries(fullFHRSeries,  new LineAndPointFormatter(
                Color.rgb(50, 50, 50), null, null, null));
		
		fhrPlot.redraw();
		fhrPlot.calculateMinMaxVals();
	    minXY=new PointF(fhrPlot.getCalculatedMinX().floatValue(),fhrPlot.getCalculatedMinY().floatValue());
	    maxXY=new PointF(fhrPlot.getCalculatedMaxX().floatValue(),fhrPlot.getCalculatedMaxY().floatValue());
		
//		
//		fhrPlot.setRangeBoundaries(0,500,BoundaryMode.FIXED);
//		fhrPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);
	//	fullFHRSeries = new SimpleXYSeries(timelist, bpmlist, "FHR");
	
	}
	
	 // Definition of the touch states
    static final int NONE = 0;
    static final int ONE_FINGER_DRAG = 1;
    static final int TWO_FINGERS_DRAG = 2;
    int mode = NONE;
 
    PointF firstFinger;
    float lastScrolling;
    float distBetweenFingers;
    float lastZooming;
    boolean stopThread = false;
    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: // Start gesture
                firstFinger = new PointF(event.getX(), event.getY());
                mode = ONE_FINGER_DRAG;
                stopThread = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_POINTER_DOWN: // second finger
                distBetweenFingers = spacing(event);
                // the distance check is done to avoid false alarms
                if (distBetweenFingers > 5f) {
                    mode = TWO_FINGERS_DRAG;
                }
                break;
            case MotionEvent.ACTION_MOVE:
            	Log.e(LOG_E, "mode:" + mode);
            	Log.e(LOG_E, "minxy:" + minXY.x + "maxxy:" + maxXY.y);
                if (mode == ONE_FINGER_DRAG) {
                    PointF oldFirstFinger = firstFinger;
                    firstFinger = new PointF(event.getX(), event.getY());
                    scroll(oldFirstFinger.x - firstFinger.x);
                    fhrPlot.setDomainBoundaries(minXY.x, maxXY.x,
                            BoundaryMode.FIXED);
                    fhrPlot.redraw();

                } else if (mode == TWO_FINGERS_DRAG) {
                    float oldDist = distBetweenFingers;
                    distBetweenFingers = spacing(event);
                    zoom(oldDist / distBetweenFingers);
                    fhrPlot.setDomainBoundaries(minXY.x, maxXY.x,
                            BoundaryMode.FIXED);
                    fhrPlot.redraw();
                }
                break;
        }
        return true;
    }
 
    private void zoom(float scale) {
        float domainSpan = maxXY.x - minXY.x;
        float domainMidPoint = maxXY.x - domainSpan / 2.0f;
        float offset = domainSpan * scale / 2.0f;

        minXY.x = domainMidPoint - offset;
        maxXY.x = domainMidPoint + offset;

//        minXY.x = Math.min(minXY.x, fullFHRSeries.getX(fullFHRSeries.size() - 3)
//                .floatValue());
//        maxXY.x = Math.max(maxXY.x, fullFHRSeries.getX(1).floatValue());
        clampToDomainBounds(domainSpan);
    }
    
    private void scroll(float pan) {
        float domainSpan = maxXY.x - minXY.x;
        float step = domainSpan / fhrPlot.getWidth();
        float offset = pan * step;
        minXY.x = minXY.x + offset;
        maxXY.x = maxXY.x + offset;
        clampToDomainBounds(domainSpan);
    }
    
    private void clampToDomainBounds(float domainSpan) {
        float leftBoundary = fullFHRSeries.getX(0).floatValue();
        float rightBoundary = fullFHRSeries.getX(fullFHRSeries.size() - 1).floatValue();
        if(minXY.x<0)
        {
        	minXY.x = 0;
        	maxXY.x = domainSpan;
        }
//        if(minXY.x<leftBoundary)
//        {
//        	minXY.x = leftBoundary;
//        	maxXY.x = leftBoundary+domainSpan; 
//        }
//        else if (maxXY.x>rightBoundary)
//        {
//        	maxXY.x = rightBoundary;
//        	minXY.x = rightBoundary-domainSpan;
//        }
        // enforce left scroll boundary:
//        if (minXY.x < leftBoundary) {
//            minXY.x = leftBoundary;
//            maxXY.x = leftBoundary + domainSpan;
//        } else if (maxXY.x > fullFHRSeries.getX(fullFHRSeries.size() - 1).floatValue()) {
//            maxXY.x = rightBoundary;
//            minXY.x = rightBoundary - domainSpan;
//        }
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }
 

    
    private void plotConfiguration()
    {
    	fhrPlot = (XYPlot)findViewById(R.id.fullplot);		
    	//	fhrPlot.setDomainBoundaries(-1, 1, BoundaryMode.FIXED);
    	//	fhrPlot.getGraphWidget().get`
    	fhrPlot.getGraphWidget().setMargins(20, 20, 20, 20);
    		fhrPlot.getGraphWidget().getBackgroundPaint().setColor(Color.WHITE);
    		fhrPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
    		fhrPlot.getGraphWidget().getDomainGridLinePaint().setColor(Color.RED);
    		fhrPlot.getGraphWidget().getRangeGridLinePaint().setColor(Color.RED);
    		fhrPlot.getGraphWidget().getRangeLabelPaint().setTextSize(20);//.setColor(Color.TRANSPARENT);
    		fhrPlot.getGraphWidget().getRangeLabelPaint().setColor(Color.RED);
    		fhrPlot.getGraphWidget().getDomainLabelPaint().setTextSize(20);//.setColor(Color.TRANSPARENT);
    		fhrPlot.getGraphWidget().getDomainLabelPaint().setColor(Color.RED);
    		//	fhrPlot.setBackgroundColor( Color.WHITE);

    		fhrPlot.setRangeBoundaries(0,240,BoundaryMode.FIXED);
    		fhrPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);

    	//	fhrPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
    		fhrPlot.setDomainStepValue(19);//domainbound / (steplong/tick) + 1
    		fhrPlot.setTicksPerDomainLabel(6);
    		fhrPlot.setRangeStepValue(25);
    		fhrPlot.setTicksPerRangeLabel(3);

    		fhrPlot.getGraphWidget().getDomainSubGridLinePaint().setColor(Color.rgb(220,175 ,175));
    		fhrPlot.getGraphWidget().getRangeSubGridLinePaint().setColor(Color.rgb(220,175 , 175));
    	//	fhrPlot.getGraphWidget().getDomainLabelPaint().
    		fhrPlot.setDomainLabel("time");
    		fhrPlot.getDomainLabelWidget().pack();
    		fhrPlot.setRangeLabel("bpm");
    	    fhrPlot.getRangeLabelWidget().pack();	
    	    
    	    fhrPlot.setRangeValueFormat(new DecimalFormat("#"));
//    	    fhrPlot.setDomainValueFormat(new DecimalFormat("#.#"));
    }
	
}
