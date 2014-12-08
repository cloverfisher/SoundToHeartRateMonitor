package shiyaoyu.me.soundtoheartratemonitor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.androidplot.Plot;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

import function.PlotConfigure;

public class RecordDisplayActivity extends Activity{
	//size can be used to get the screen size.
	int width;
	int height;
	int offset = 0;
	int scroll_width;
	double rate;
	ScrollView sv;
	private int bufferSizeInByte = 8; //buffersize
	
	double time=0;
	double totalTimeInSec = 0;
	private static final String LOG_TAG = "ysy_AudioDisplayActivity";
	private static final int SAMPLE_RATE_IN_HZ = 11025;
	
	private static final int HISTORY_SIZE= 420;
	private static final int NUM_PLOT = 3;
	Button recordButton = null;
	Button exportButton = null;
	Button listFHRButton = null;
	private boolean recordFlag = true;
	private MediaRecorder mRecorder = null;
	
	private AudioRecord audioRecord;
	private boolean isRecord = true;
	boolean mStartRecording = true;
	private RecordThread recordThread = null;
	int beats = 0;
	int threshold = 1000;
	boolean thresholdflag = false;
    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FHR.pcm");
    
//    private XYPlot fhrPlot = null; 
//    private SimpleXYSeries fhrSeries = null;
    Redrawer redrawer;
    LinearLayout mylayout;
    LinearLayout scrollLayout;

	
	TextView beatsTextView = null;
	long endTime = System.currentTimeMillis();
	long startTime = System.currentTimeMillis();
	
	ArrayList timeList = new ArrayList<Double>();
	ArrayList bpmList = new ArrayList<Integer>();
	//double bpmarr[];
	
	private XYPlot myxyplot[];
	private SimpleXYSeries myseries[];
	private ScrollView myScrollView[];
	
	class RecordButton extends Button{

		
		OnClickListener clicker = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
			
		};
		
		public RecordButton(Context context) {
			super(context);
			setText("start recording");
			setOnClickListener(clicker);
		}
		
	}
	
	public void openPicture(String uri)
	{
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + uri), "image/*");
		startActivity(intent);
	}
	
	private void creatAudioRecord()
	{
		bufferSizeInByte = AudioRecord.getMinBufferSize( SAMPLE_RATE_IN_HZ,  
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);  
		Log.e(LOG_TAG, "buffersize " + bufferSizeInByte);
		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ,  
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInByte);  
	}
	
	private void onRecord(boolean start)
	{		
		Log.e("ysystart", "start?"+start);
		if(start)
			startRecording();
		else
			stopRecording();
	}
	
	class RecordThread extends Thread
	{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			Log.e(LOG_TAG, "recordThread start");
			short[] audiodata = new short[bufferSizeInByte];
			int readsize = 0;
		//	int i =0;
	        OutputStream os;
			try {
				beats = 0;
				audioRecord.startRecording();
				os = new FileOutputStream(file);
		        BufferedOutputStream bos = new BufferedOutputStream(os);
		        DataOutputStream dos = new DataOutputStream(bos);
				double time=0;
				double timerecord = 0;
				int bpm = 0;
				while(isRecord == true)
				{
					readsize = audioRecord.read(audiodata, 0, bufferSizeInByte);
					for(int i =0; i<readsize; i++)
					{
		//				Log.e(LOG_TAG, "audiodata " +audiodata[i] );
						if(audiodata[i]>threshold && thresholdflag == false)
						{ 
							thresholdflag = true;
							endTime = System.currentTimeMillis();
							time = endTime - startTime;
							if(time > timerecord + 250)
							{
					        	Log.e(LOG_TAG, "time:" + time + "timerecord:" + timerecord);
								bpm = (int) (60*1000/(time-timerecord));
								timerecord = time;
								beats++;							
								Message msgMessage = new Message();
								msgMessage.what = 1;
								beatsHandler.sendMessage(msgMessage);

					        	Log.e(LOG_TAG, "bpm:" +bpm);
					        	Log.e(LOG_TAG, "time:" + time/1000);
					            timeList.add(time/1000);
					            bpmList.add(bpm);
					      //      fhrSeries.addLast(time/1000, bpm);	
					            if(time/1000<=HISTORY_SIZE)
					            {
					            	myseries[0].addLast(time/1000, bpm);
					            }
					            else if (time/1000<=2*HISTORY_SIZE &&time/1000>=HISTORY_SIZE) {
					            	myseries[1].addLast(time/1000, bpm);
								}
					            else {
					            	myseries[2].addLast(time/1000, bpm);
								}

							}

							
							
						}
						else if (audiodata[i]<threshold) {
							thresholdflag = false;
						}
						dos.writeShort(audiodata[i]);
					}
				}
				dos.close();
				audioRecord.stop();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	private void startRecording(){
		creatAudioRecord();
		isRecord = true;
		int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
		startTime = System.currentTimeMillis();
	    if (file.exists())
	       file.delete();
	    
	    try {
	       file.createNewFile();}
	    catch (IOException e) {
	       throw new IllegalStateException("Failed to create " + file.toString());
	        }

		recordThread = new RecordThread();
		recordThread.start();
		bpmList.clear();;
		timeList.clear();
		addNewPlot();
		redrawer.start();
	}
	
	private void stopRecording(){
		isRecord = false;	
		redrawer.finish();
		//Log.e(LOG_TAG, "x:"+ fhrSeries.ge)
		}
	
    /*
     * Handler ,send the message from record thread to the main thread
     * */
    
    private Handler beatsHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
		
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
			{
				
				beatsTextView.setText("" + beats);
				break;
			}
			default:
				break;
			}
		}
    	
    };
    
    /*
     * replay the record
     * */
    
    public void play() {
        // Get the file we want to playback.
        // Get the length of the audio stored in the file (16 bit so 2 bytes per short)
        // and create a short array to store the recorded audio.
    	Log.e(LOG_TAG, "play start");
        int musicLength = (int)(file.length()/2);
        short[] music = new short[musicLength];
   
   
        try {
          // Create a DataInputStream to read the audio data back from the saved file.
          InputStream is = new FileInputStream(file);
          BufferedInputStream bis = new BufferedInputStream(is);
          DataInputStream dis = new DataInputStream(bis);  
           
          // Read the file into the music array.
          int i = 0;
          while (dis.available() > 0) {
            music[i] = dis.readShort();
            i++;
          }
   
   
          // Close the input streams.
          dis.close();    
   
   
          // Create a new AudioTrack object using the same parameters as the AudioRecord
          // object used to create the file.
          AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
        		  									SAMPLE_RATE_IN_HZ,
                                                 AudioFormat.CHANNEL_IN_MONO,
                                                 AudioFormat.ENCODING_PCM_16BIT,
                                                 musicLength*2,
                                                 AudioTrack.MODE_STREAM);
          // Start playback
          audioTrack.play();
       
          // Write the music buffer to the AudioTrack object
          audioTrack.write(music, 0, musicLength);
   
          audioTrack.stop() ;
   
        } catch (Throwable t) {
          Log.e("AudioTrack","Playback Failed");
        }
    	Log.e(LOG_TAG, "play end");
      }

	
    /*
     * oncreate 
     * */
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	 	width = getWindowManager().getDefaultDisplay().getWidth();
	 	height = getWindowManager().getDefaultDisplay().getHeight();
	 	offset = 0;
	 	myxyplot = new XYPlot[3];
	 	myseries = new SimpleXYSeries[3];
	 	myScrollView = new ScrollView[3]; 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		
		setContentView(R.layout.activity_record);	
		sv = (ScrollView)findViewById(R.id.scrollView1);
		mylayout = (LinearLayout)findViewById(R.id.plotLinearlayout);
		scrollLayout = (LinearLayout)findViewById(R.id.scrolllayout1);


		
		beatsTextView = (TextView)findViewById(R.id.beattext);
		beatsTextView.setText(""+ beats);
		recordButton = (Button)findViewById(R.id.btnRecord);
		recordButton.setText("Start");
		recordButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.e(LOG_TAG, "onclick");
				if(recordFlag)
				{
					exportButton.setEnabled(false);
					recordButton.setText("Stop");
				}				
				else {
					exportButton.setEnabled(true);
					recordButton.setText("Start");
				}
				onRecord(recordFlag);
				recordFlag = !recordFlag;			
			}
		});

		
		exportButton = (Button)findViewById(R.id.btnexport);
		exportButton.setEnabled(false);
		exportButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			//	play();
				if(timeList.size()>4)
				{					
					Bitmap bmpplot[]= new Bitmap[3];
					for(int i = 0; i < 3; i++)
					{
						Log.e(LOG_TAG, "asd"+i);
						myxyplot[i].setDrawingCacheEnabled(true);
				        int width = myxyplot[i].getWidth();
				        int height = myxyplot[i].getHeight();
				        myxyplot[i].measure(width, height);
				        bmpplot[i] = Bitmap.createBitmap(myxyplot[i].getDrawingCache());
				        myxyplot[i].setDrawingCacheEnabled(false);
	
					}
			        int width = myxyplot[0].getWidth();
			        int height =myxyplot[0].getHeight()*3;

			        Bitmap btm = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
			        Canvas canvas = new Canvas(btm);
			        canvas.drawBitmap(bmpplot[0], 0,0, null);
			        canvas.drawBitmap(bmpplot[1], 0, bmpplot[0].getHeight(), null);
			        canvas.drawBitmap(bmpplot[2], 0, bmpplot[0].getHeight()*2, null);
			        canvas.drawBitmap(btm, 0, 0, null);
			        FileOutputStream fos;
			    	File file2 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/fhrPhoto.png");
			    	if(file2.exists())
			    		file2.delete();
					try {
					//	File pbgfile = new File("fhrPhoto.png"); 
						fos = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/fhrPhoto.png", true);
						btm.compress(CompressFormat.PNG, 100, fos);
						openPicture(Environment.getExternalStorageDirectory().getAbsolutePath() + "/fhrPhoto.png");
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			}
		});
		
		/*plot part*/
	//	fhrPlot = (XYPlot)findViewById(R.id.xyplot1);
		scroll_width = scrollLayout.getWidth();
		rate = scroll_width/HISTORY_SIZE;
		Log.e(LOG_TAG, "plotwidth:"+scroll_width+" rate "+ rate);
	//	PlotConfigure.plotConfiguration(fhrPlot, 0,0+HISTORY_SIZE);
		
		myxyplot[0] = (XYPlot)findViewById(R.id.xyplot1);
		myxyplot[1] = (XYPlot)findViewById(R.id.xyplot2);
		myxyplot[2] = (XYPlot)findViewById(R.id.xyplot3);

		PlotConfigure.plotConfiguration(myxyplot[0], 0,0+HISTORY_SIZE);
		PlotConfigure.plotConfiguration(myxyplot[1], HISTORY_SIZE,2*HISTORY_SIZE);	
		PlotConfigure.plotConfiguration(myxyplot[2], 2*HISTORY_SIZE,3*HISTORY_SIZE);		
//		fhrPlot.scrollBy(-width+100, 0);
	    addNewPlot();		
	}
	


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
 	   width = getWindowManager().getDefaultDisplay().getWidth();
 	   height = getWindowManager().getDefaultDisplay().getHeight();
 	   offset = 0;
 	   Log.e(LOG_TAG, "width=" + width + " height=" + height);
	      if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
	           // Nothing need to be done here   	   
	        } else {
	           // Nothing need to be done here
	        }
	}

	private void addNewPlot()
	{
		LineAndPointFormatter series2Format = new LineAndPointFormatter();
		series2Format = new LineAndPointFormatter(Color.rgb(0, 0, 0), null, null,null);
		Paint paint = series2Format.getLinePaint();
		paint.setStrokeWidth(1);
		series2Format.setLinePaint(paint);
		for(int i = 0 ; i < NUM_PLOT; i++)
		{
			myxyplot[i].clear();
			myseries[i] =new SimpleXYSeries("FHR"+i);
			myxyplot[i].addSeries(myseries[i], series2Format);
		}
	    redrawer = new Redrawer(
                Arrays.asList(new Plot[]{myxyplot[0],myxyplot[1],myxyplot[2]}),
                100, false);
	}

	
}
