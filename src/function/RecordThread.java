package function;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import shiyaoyu.me.soundtoheartratemonitor.ListViewActivity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;



public class RecordThread extends Thread{
	
	private static final String LOG_TAG = "ysy_record";
	int beats;
	File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FHR.pcm");
	private AudioRecord audioRecord;
	private static final int SAMPLE_RATE_IN_HZ = 11025;
	private int bufferSizeInByte = 8; //buffersize
	int threshold = 3000;
	boolean thresholdflag = false;
	public boolean isRecord = true;
    ArrayList<Double> timearr = new ArrayList<Double>();
    ArrayList<Integer> bpmarr = new ArrayList<Integer>();
	Bundle data = new Bundle();
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		long endTime = System.currentTimeMillis();
		long startTime = System.currentTimeMillis();
		Log.e(LOG_TAG, "recordThread start");
		creatAudioRecord();
		short[] audiodata = new short[bufferSizeInByte];
		
		timearr.clear();
		bpmarr.clear();
		int readsize = 0;
	//	int i =0;
        OutputStream os;
		try {
			beats = 0;

			audioRecord.startRecording();
			startTime = System.currentTimeMillis();
			int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
			
		    if (file.exists())
			       file.delete();
			    try {
			       file.createNewFile();
			       }
			    catch (IOException e) {
			       throw new IllegalStateException("Failed to create " + file.toString());
			        }
			    
			os = new FileOutputStream(file);
	        BufferedOutputStream bos = new BufferedOutputStream(os);
	        DataOutputStream dos = new DataOutputStream(bos);
			double time=0;
			double timerecord = 0;
			int bpm = 0;
			while(isRecord == true)
			{
				readsize = audioRecord.read(audiodata, 0, bufferSizeInByte);
		//		Log.e(LOG_TAG, "readsize:" + readsize);
				for(int i =0; i<readsize; i++)
				{
			//		Log.e(LOG_TAG, "audiodata " +audiodata[i] );
					if(audiodata[i]>threshold && thresholdflag == false)
					{ 
						thresholdflag = true;
						endTime = System.currentTimeMillis();
						time = endTime - startTime;
						if(time > timerecord + 250)
						{
				   //     	Log.e(LOG_TAG, "time:" + time + "timerecord:" + timerecord);
							bpm = (int) (60*1000/(time-timerecord));
							timerecord = time;
							beats++;			
				//			Log.e(LOG_TAG, "beats:" + beats);
							Message msgMessage = new Message();
							msgMessage.what = 1;
						
							data.putInt("bpm", bpm);
							data.putDouble("time", time/1000);
							msgMessage.setData(data);
							ListViewActivity.lva.getHandler().sendMessage(msgMessage);
//							timearr.add(timerecord);
//							bpmarr.add(bpm);
					//		beatsHandler.sendMessage(msgMessage);

						}
						
					}
					else if (audiodata[i]<threshold) {
						thresholdflag = false;
					}
					dos.writeShort(audiodata[i]);
				}
			}
			Log.e(LOG_TAG, "thread end");
			dos.close();
			audioRecord.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	

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
			//	ListViewActivity.this.beatTextView.setText(beats);
			//	beatsTextView.setText("" + beats);
				break;
			default:
				break;
			}
		}
    	
    };
    
	public void creatAudioRecord()
	{
		bufferSizeInByte = AudioRecord.getMinBufferSize( SAMPLE_RATE_IN_HZ,  
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);  
		Log.e(LOG_TAG, "buffersize " + bufferSizeInByte);
		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ,  
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInByte);  
	}
	
	
	public void setIsRecord(boolean isrecord)
	{
		this.isRecord = isrecord;
	}
	
	public int getbeats()
	{
		return beats;
	}



	
}
