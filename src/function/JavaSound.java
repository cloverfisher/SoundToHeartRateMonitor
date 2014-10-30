package function;

import java.io.File;

import android.os.Environment;

public class JavaSound {
public JavaSound() {
	// TODO Auto-generated constructor stub
	
	String mFileName;
    mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
    mFileName = 
        mFileName += "/Download/Baby_HR-1.wav";

	File file = new File(mFileName);
	
	
}
}
