package shiyaoyu.me.soundtoheartratemonitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.R.bool;
import android.R.string;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.androidplot.Plot;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import function.PlotConfigure;
import function.RecordThread;

public class ListViewActivity extends Activity {

    private static final int NUM_SERIES_PER_PLOT = 3;
    private static final int NUM_TIME_LENTH_PER_PLOT = 420;
    private static final String LOG_TAG = "ysy_listviewActivity";
    double NUM_DURATION = 60; //every graph view 60s;
    private ListView plotListView;
    XYPlot p[]; 
    
	ArrayList timelist;
	ArrayList bpmlist;
	Button recordButton;
	Boolean recordState = true;
	RecordThread recordThread;
	public TextView beatTextView;
	Handler handler;
	SimpleXYSeries series[] ;
	  Redrawer redrawer;
	public static ListViewActivity lva;
	public Handler getHandler(){
		return this.handler;
		}
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_listview);
		lva = this;
		p = new XYPlot[NUM_SERIES_PER_PLOT];
		  series = new SimpleXYSeries[NUM_SERIES_PER_PLOT];	
		for(int i = 0 ; i < NUM_SERIES_PER_PLOT;i++)
		{
			series[i] = new SimpleXYSeries("FHR");
	//		p[i] = new XYPlot(context, attributes)
		}
		ListView plotListView = (ListView)findViewById(R.id.listviewplot);
		
		
		plotListView.setAdapter(new MyAdapter(getApplicationContext(), R.layout.listview_item, null));
		plotListView.setSelection(2);
		plotListView.setSelection(1);
		plotListView.setSelection(0);
		recordButton = (Button)findViewById(R.id.buttonListRecord);
		
		recordButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(recordState==true)
				{			
					recordState = false;
					recordButton.setText("Stop");
					recordThread = new RecordThread();
					 addNewPlot(p,series); 
					recordThread.start();
					

				    redrawer.start();
				
				}
				else {
					recordState = true;
					recordButton.setText("Record");
					recordThread.setIsRecord(false);
					redrawer.finish();
				}
				
			}
		});
		
		beatTextView = (TextView)findViewById(R.id.tvbeat);
		
		handler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				
				switch (msg.what) {
				case 1:
				{
					beatTextView.setText(""+recordThread.getbeats());
					series[0].addLast(msg.getData().getDouble("time"), msg.getData().getInt("bpm"));
//					for(int i = 0 ; i < NUM_SERIES_PER_PLOT ; i++)
//					{
//						series[i].addLast(msg.getData().getDouble("time"), msg.getData().getInt("bpm"));
//					}
				//	series2.addLast(msg.getData().getDouble("time"), msg.getData().getInt("bpm"));
					Log.e(LOG_TAG, "time" +msg.getData().getDouble("time")+"bpm" +msg.getData().getInt("bpm") );
					break;	
				}
				default:
					break;
				}
			}
			
		};
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
	            
	            
	            
	            Log.e(LOG_TAG, "positon:" + position);
	            
	            
	 
	             p[position]= (XYPlot) v.findViewById(R.id.listplotitem);
	           
	            p[position].setTitle("plot" + position);

	            


	            PlotConfigure.plotConfiguration(p[position], position*NUM_TIME_LENTH_PER_PLOT,(position+1)*NUM_TIME_LENTH_PER_PLOT);
	          
//	            p[position].addSeries(series2, new LineAndPointFormatter(Color.BLACK,null, null, null));


	            return v;
		}	

	}	
	private void addNewPlot(XYPlot[] fhrPlot, SimpleXYSeries[] series)
	{
		try {
			for(int i = 0 ; i < fhrPlot.length;i++)
			{
				fhrPlot[i].clear();
	            PlotConfigure.plotConfiguration(p[i], i*NUM_TIME_LENTH_PER_PLOT,(i+1)*NUM_TIME_LENTH_PER_PLOT);
				series[i] = new SimpleXYSeries("FHR");	
				LineAndPointFormatter series2Format = new LineAndPointFormatter();
				series2Format = new LineAndPointFormatter(Color.BLACK, null, null,null);
			     
				fhrPlot[i].addSeries(series[i],series2Format);  
			    redrawer = new Redrawer(
		                Arrays.asList(new Plot[]{fhrPlot[i]}),
		                100, false);			
			}
		} catch (Exception e) {
			e.printStackTrace();
		}



	}
}
