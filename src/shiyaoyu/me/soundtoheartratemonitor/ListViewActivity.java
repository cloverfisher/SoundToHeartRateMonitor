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
import android.util.Log;
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
		NUM_DURATION = ((Double)timelist.get(timelist.size()-1))/NUM_SERIES_PER_PLOT;
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

//	            ArrayList<Double> timearr = new ArrayList<Double>();
//	            ArrayList<Integer> bpmarr = new ArrayList<Integer>();
	            
//	            Vector<Double> v2 = new Vector<Double>();
//	            XYSeries series2 = new SimpleXYSeries(v2, null, null);
	            
	            ArrayList<Integer> tempList = new ArrayList<Integer>();
	            tempList.add(0);
		            for(int i = 0,k=0; i<timelist.size();i++ )
		            {
		            	double temptime = (Double)timelist.get(i);
		            	if((temptime>=NUM_DURATION*k) && (temptime<=NUM_DURATION*(k+1)))
		            	{
//		                	timearr.add(temptime);
//		                	bpmarr.add((Integer)bpmlist.get(i));
		            	}
		            	else {
							k++;
							tempList.add(i);
						}
		
		            }
		        tempList.add(timelist.size()-1);
		        Log.e("TAG", "NUM_DURATION:" + NUM_DURATION);
		        Log.e("TAG", "k:" + tempList.size());
		        XYSeries[] series2 ;
		        series2 = new SimpleXYSeries[NUM_SERIES_PER_PLOT];
		        for(int k =0; k < NUM_SERIES_PER_PLOT; k++)
		        {
		        	Log.e("ysy", "time" + timelist.get(tempList.get(k)) + "~" + timelist.get(tempList.get(k+1)));
		        	series2[k] = new SimpleXYSeries(timelist.subList(tempList.get(k), tempList.get(k+1)), bpmlist.subList(tempList.get(k), tempList.get(k+1)), "S" + k);
	           //     XYSeries series = new SimpleXYSeries(timelist.subList(tempList.get(k), tempList.get(k+1)), bpmlist.subList(tempList.get(k), tempList.get(k+1)), "S" + k);
	            //    p.addSeries(series, new LineAndPointFormatter(Color.BLUE,null, null, null));  
		        	 p.addSeries(series2[k], new LineAndPointFormatter(Color.BLUE,null, null, null));  
		        }

		        /*
	            for (int k = 0; k < NUM_SERIES_PER_PLOT; k++) {
	                ArrayList<Number> nums = new ArrayList<Number>();
	                for (int j = 0; j < NUM_POINTS_PER_SERIES; j++) {
	                    nums.add(generator.nextFloat());
	                }

	                double rl = Math.random();
	                double gl = Math.random();
	                double bl = Math.random();

	                double rp = Math.random();
	                double gp = Math.random();
	                double bp = Math.random();

	                XYSeries series = new SimpleXYSeries(nums, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "S" + k);
	                p.addSeries(series, new LineAndPointFormatter(
	                        Color.rgb(new Double(rl * 255).intValue(), new Double(gl * 255).intValue(), new Double(bl * 255).intValue()),
	                        Color.rgb(new Double(rp * 255).intValue(), new Double(gp * 255).intValue(), new Double(bp * 255).intValue()),
	                        null, null));
	            }
	            */
	            
	            p.redraw();
	            return v;
		}
		
		
		
	}

	
	
	
}
