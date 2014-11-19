package shiyaoyu.me.soundtoheartratemonitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.androidplot.Plot;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

public class ListViewActivity extends Activity {
    private static final int NUM_PLOTS = 10;
    private static final int NUM_POINTS_PER_SERIES = 10;
    private static final int NUM_SERIES_PER_PLOT = 3;
    
    double NUM_DURATION = 60; //every graph view 60s;
    private ListView plotListView;
    
    
	ArrayList timelist;
	ArrayList bpmlist;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_listview);
		
		Intent intent = getIntent() ;
		 timelist = intent.getParcelableArrayListExtra("time");
		 bpmlist = intent.getParcelableArrayListExtra("bpm");
		NUM_DURATION = NUM_DURATION/NUM_SERIES_PER_PLOT;
		ListView plotListView = (ListView)findViewById(R.id.listviewplot);
		
		
		plotListView.setAdapter(new MyAdapter(getApplicationContext(), R.layout.listview_item, null));
	}
	
	class MyAdapter extends ArrayAdapter<View>
	{

		public MyAdapter(Context context, int resource, List<View> views) {
			super(context, resource, views);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return NUM_SERIES_PER_PLOT;

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
		       LayoutInflater inf = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	            View v = convertView;
	            if (v == null) {
	                v = inf.inflate(R.layout.listview_item, parent, false);
	            }

	            XYPlot p = (XYPlot) v.findViewById(R.id.listplotitem);
	           
	        //    p.setMargins(20, 20, 20, 20);
	    	//	p.getBackgroundPaint().setColor(Color.WHITE);

	            Random generator = new Random();

	            p.setTitle("plot" + position);

	            ArrayList<Double> timearr = new ArrayList<Double>();
	            ArrayList<Integer> bpmarr = new ArrayList<Integer>();
	            
//	            Vector<Double> v2 = new Vector<Double>();
//	            XYSeries series2 = new SimpleXYSeries(v2, null, null);
	            
	            for (int k = 0; k < NUM_SERIES_PER_PLOT; k++) {
		            for(int i = 0; i<timelist.size();i++ )
		            {
		            	double temptime = (Double)timelist.get(i);
		            	if(temptime>NUM_DURATION*k && temptime<=NUM_DURATION*(k+1))
		            	{
		                	timearr.add(temptime);
		                	bpmarr.add((Integer)bpmlist.get(i));
		            	}
		
		            }
	            	
	                XYSeries series = new SimpleXYSeries(timearr, bpmarr, "S" + k);
	                p.addSeries(series, new LineAndPointFormatter(
	                        Color.BLUE,null, null, null));
	            }
	            p.redraw();
	            return v;
		}
		
		
		
	}

	
	
	
}
