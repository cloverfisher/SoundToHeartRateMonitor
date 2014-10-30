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

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class RecordDisplayActivity extends Activity{

	private int bufferSizeInByte = 8; //buffersize
	private  String AudioNamme = "";
	private String NewAudioName = "";
	
	
	private static final String LOG_TAG = "ysy_AudioDisplayActivity";
	private static final int SAMPLE_RATE_IN_HZ = 11025;
	private static String mFileName = null;
	
	//private RecordButton  mRecordButton = null;
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
	int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
	int BytesPerElement = 2; // 2 bytes in 16bit format
	
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
		//audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes)
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
	
	//Thread recordThread = new Thread();
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
					long time=0;
					readsize = audioRecord.read(audiodata, 0, bufferSizeInByte);
				//	Log.e(LOG_TAG, "readsize " +readsize );
					for(int i =0; i<readsize; i++)
					{
						Log.e(LOG_TAG, "audiodata " +audiodata[i] );
						if(audiodata[i]>threshold && thresholdflag == false)
						{ 
							thresholdflag = true;
							endTime = System.currentTimeMillis();
						//	Log.e(LOG_TAG, "audiodata " +audiodata[i] );
							if(endTime - startTime < time + 40)
							{
								time = endTime - startTime;
								continue;
							}
							time = endTime - startTime;	
							Log.e(LOG_TAG, "t:" +time);
							beats++;							
							Message msgMessage = new Message();
							msgMessage.what = 1;
							beatsHandler.sendMessage(msgMessage);
							//	beatsTextView.setText("" + beats);
						}
						else if (audiodata[i]<threshold) {
							thresholdflag = false;
						}
						
						try {
							dos.writeShort(audiodata[i]);
						
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				try {
					dos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				audioRecord.stop();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		

		}
		
	}
	private void startRecording(){
		isRecord = true;
		int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
		startTime = System.currentTimeMillis();
	      
	      // Delete any previous recording.
	    if (file.exists())
	       file.delete();
	    try {
	       file.createNewFile();}
	    catch (IOException e) {
	       throw new IllegalStateException("Failed to create " + file.toString());
	        }
		recordThread = new RecordThread();
		recordThread.start();
		/*
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mRecorder.setOutputFile(mFileName);
		
		try {
			mRecorder.prepare();
		} catch (Exception e) {
			Log.e(LOG_TAG, "prepare fail");
			// TODO: handle exception
		}
		*/
		
//		int readsize = 0;
//		
//		byte[] audiodata = new byte[bufferSizeInByte];
//		while(isRecord == true)
//		{
//			readsize = audioRecord.read(audiodata, 0, bufferSizeInByte);
//			Log.e(LOG_TAG, "readsize " +audiodata );
//		}
	}
	
	private void stopRecording(){
		isRecord = false;
		
//		mRecorder.stop();
//		mRecorder.release();
//		mRecorder = null;
	}
	
	

	
    public void AudioRecordTest() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";
    }
	
    private Handler beatsHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);	
		creatAudioRecord();
	//	RadioButton radioButton = (RadioButton)findViewById(R.id.btnRecord);
	//	LinearLayout ll = new LinearLayout(this);
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
				// TODO Auto-generated method stub
				
			}
		});
		Button replayButton = (Button)findViewById(R.id.btnreplay);
		replayButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				play();
			}
		});
		
	//	mRecordButton = (Button)findViewById(R.id.btnRecord);
	//	int bufferSize;		
		
	}

	
}
