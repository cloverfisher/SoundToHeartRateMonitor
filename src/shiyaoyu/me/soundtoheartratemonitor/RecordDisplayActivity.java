package shiyaoyu.me.soundtoheartratemonitor;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RecordDisplayActivity extends Activity{

	private int bufferSizeInByte = 8; //buffersize
	private  String AudioNamme = "";
	private String NewAudioName = "";
	
	
	private static final String LOG_TAG = "ysy_AudioDisplayActivity";
	private static String mFileName = null;
	
	//private RecordButton  mRecordButton = null;
	Button recordButton = null;
	private boolean recordFlag = true;
	private MediaRecorder mRecorder = null;
	
	private AudioRecord audioRecord;
	private boolean isRecord = true;
	private RecordThread recordThread = null;
	
	int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
	int BytesPerElement = 2; // 2 bytes in 16bit format
	
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
		bufferSizeInByte = AudioRecord.getMinBufferSize( 44100,  
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);  
		Log.e(LOG_TAG, "buffersize " + bufferSizeInByte);
		//audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes)
		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,  
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
			byte[] audiodata = new byte[bufferSizeInByte];
			int readsize = 0;
			int i =0;
			while(isRecord == true)
			{
				i++;
				readsize = audioRecord.read(audiodata, 0, bufferSizeInByte);
				if(i%100000==1)
					Log.e(LOG_TAG, "readsize " +audiodata );
			}
		}
		
	}
	private void startRecording(){
		isRecord = true;
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
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);	
		creatAudioRecord();
	//	RadioButton radioButton = (RadioButton)findViewById(R.id.btnRecord);
	//	LinearLayout ll = new LinearLayout(this);
		
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
		
	//	mRecordButton = (Button)findViewById(R.id.btnRecord);
	//	int bufferSize;
		
		
	
		
		
	}

	
}
