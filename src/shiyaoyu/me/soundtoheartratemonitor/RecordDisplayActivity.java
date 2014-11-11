package shiyaoyu.me.soundtoheartratemonitor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.androidplot.Plot;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;

public class RecordDisplayActivity extends Activity{

	private int bufferSizeInByte = 8; //buffersize
	
	double time=0;
	double totalTimeInSec = 0;
	private static final String LOG_TAG = "ysy_AudioDisplayActivity";
	private static final int SAMPLE_RATE_IN_HZ = 11025;
	
	private static final int HISTORY_SIZE= 50;
	
	Button recordButton = null;
	private boolean recordFlag = true;
	private MediaRecorder mRecorder = null;
	
	private AudioRecord audioRecord;
	private boolean isRecord = true;
	private RecordThread recordThread = null;
	int beats = 0;
	int threshold = 3000;
	boolean thresholdflag = false;
    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FHR.pcm");
    
    private XYPlot fhrPlot = null; 
    private SimpleXYSeries fhrSeries = null;
    private SimpleXYSeries fullFHRSeries = null;
    Redrawer redrawer;

	
	TextView beatsTextView = null;
	long endTime = System.currentTimeMillis();
	long startTime = System.currentTimeMillis();
	

	class RecordButton extends Button{
		boolean mStartRecording = true;
		
		OnClickListener clicker = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
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

				while(isRecord == true)
				{
					double time=0;
					double timerecord = 0;
					readsize = audioRecord.read(audiodata, 0, bufferSizeInByte);
					for(int i =0; i<readsize; i++)
					{
		//				Log.e(LOG_TAG, "audiodata " +audiodata[i] );
						if(audiodata[i]>threshold && thresholdflag == false)
						{ 
							thresholdflag = true;
							endTime = System.currentTimeMillis();
							time = endTime - startTime;
							if(time > timerecord + 150)
							{
								timerecord = time;
								beats++;							
								Message msgMessage = new Message();
								msgMessage.what = 1;
								beatsHandler.sendMessage(msgMessage);				
							}
				            totalTimeInSec =  (time/1000d);
				            double bps = 0;
				            if(totalTimeInSec<0.5)
				            	bps = 0;
				            else
				            	bps= (beats / totalTimeInSec);
				            int bpm = (int) (bps * 60d);
				        	Log.e(LOG_TAG, "bpm:" +bpm);
				        	Log.e(LOG_TAG, "time:" + time);
				        	Log.e(LOG_TAG, "time:" + time + "beats:" + beats);
				            if(fhrSeries.size()>HISTORY_SIZE){
				            	fhrSeries.removeFirst();				           
				            }
				            fhrSeries.addLast(time, bpm);	
							//	beatsTextView.setText("" + beats);
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
		isRecord = true;
		int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
		startTime = System.currentTimeMillis();
	    //forbid the system lock 
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); setContentView(R.layout.main);   
	      // Delete any previous recording.
	    if (file.exists())
	       file.delete();
	    try {
	       file.createNewFile();}
	    catch (IOException e) {
	       throw new IllegalStateException("Failed to create " + file.toString());
	        }
//	    mythread = new Thread(data);
//	    mythread.start();
//	    df
		recordThread = new RecordThread();
		recordThread.start();
		redrawer.start();
	}
	
	private void stopRecording(){
		isRecord = false;	
		redrawer.finish();
//		data.stopThread();
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
				beatsTextView.setText("" + beats);
				break;
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
		setContentView(R.layout.activity_record);	
		creatAudioRecord();
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
					recordButton.setText("Stop");
				}				
				else {
					recordButton.setText("Start");
				}
				onRecord(recordFlag);
				recordFlag = !recordFlag;
				
				
			}
		});
		Button replayButton = (Button)findViewById(R.id.btnreplay);
		replayButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			//	play();
			}
		});
		
		
		/*plot part*/
		//TODO plot
		fhrPlot = (XYPlot)findViewById(R.id.xyplot);		
		fhrPlot.setDomainBoundaries(-1, 1, BoundaryMode.FIXED);
		fhrPlot.getGraphWidget().getDomainLabelPaint().setColor(Color.TRANSPARENT);
		
		fhrSeries = new SimpleXYSeries("FHR");
		
		fhrSeries.useImplicitXVals();
		fhrPlot.setRangeBoundaries(0,500,BoundaryMode.FIXED);
		fhrPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);
		
		fhrPlot.addSeries(fhrSeries,
	                new LineAndPointFormatter(
	                        Color.rgb(100, 100, 200), null, null, null));
		fhrPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
		fhrPlot.setDomainStepValue(HISTORY_SIZE/10);
		fhrPlot.setTicksPerRangeLabel(3);
		fhrPlot.setDomainLabel("time");
		fhrPlot.getDomainLabelWidget().pack();
		fhrPlot.setRangeLabel("bpm");
	    fhrPlot.getRangeLabelWidget().pack();	
	    
	    fhrPlot.setRangeValueFormat(new DecimalFormat("#"));
	    fhrPlot.setDomainValueFormat(new DecimalFormat("#"));
	    
	    redrawer = new Redrawer(
                Arrays.asList(new Plot[]{fhrPlot}),
                100, false);

		
	}

	
}
