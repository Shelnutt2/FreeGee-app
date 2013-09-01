package edu.shell.freegee;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.octo.android.robospice.JacksonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.exception.RequestCancelledException;
import com.octo.android.robospice.notification.SpiceNotificationService;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;
import com.octo.android.robospice.request.simple.BigBinaryRequest;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

public class helper {
	
    private SpiceManager spiceManager = new SpiceManager(JacksonSpringAndroidSpiceService.class);
    private BigBinaryRequest BinaryRequest;
    private String SAVE_LOCATION = "/sdcard/freegee/working";

    public int process(Action b){
    	downloadAll(b);
    	if(!b.getDependencies().isEmpty()){
		  for(Action c:b.getDependencies()){
			  int result = install(c);
			if (result!=0){
				return result;
			}
		  }
    	}
    	return install(b);
    }
    
	public void downloadAll(Action b) {
		if(!b.getDependencies().isEmpty()){
			for(Action c:b.getDependencies()){
				downloadAll(c);
			}
		}
		download(b.getZipFileLocation(),b.getZipFile());
		checkmd5sum(SAVE_LOCATION + b.getZipFile(),b.getMd5sum());
	}
	
	public int install(Action d){
		int err = -1;
		 CommandCapture command = new CommandCapture(0,"/data/data/edu.shell.freegee/edifier", SAVE_LOCATION+d.getZipFile());
			try {
				err = RootTools.getShell(true).add(command).getExitCode();
			} catch (IOException e) {
				Log.e("Freegee","Edifier not found in assets");
				e.printStackTrace();
			} catch (TimeoutException e) {
				Log.e("Freegee","command timed out");
				e.printStackTrace();
			} catch (RootDeniedException e) {
				Log.e("Freegee","No root access!");
				e.printStackTrace();
			}
		return err;	
	}
	
	public void download(String location, String FileName){		
        File cacheFile = new File(SAVE_LOCATION, FileName);
        BinaryRequest = new BigBinaryRequest(location, cacheFile);
        spiceManager.execute(BinaryRequest, "zip", DurationInMillis.ALWAYS_EXPIRED, new BinaryRequestListener());
        
	}
	
	private boolean checkmd5sum(String fname,String fmd5) {
    	int err = 0;
    		 MessageDigest md = null;
			try {
				md = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e1) {
				
				e1.printStackTrace();
			}
    		    FileInputStream fis = null;
				try {
					fis = new FileInputStream(fname);
				} catch (FileNotFoundException e1) {
					
					e1.printStackTrace();
				}
    		    byte[] dataBytes = new byte[1024];
    		    int nread = 0; 
    		    try {
    				while ((nread = fis.read(dataBytes)) != -1) {
    				  md.update(dataBytes, 0, nread);
    				}
    			} catch (IOException e) {
    				
    				e.printStackTrace();
    			};
    		    byte[] mdbytes = md.digest();

    		    //convert the byte to hex format method 1
    		    StringBuffer sb = new StringBuffer();
    		    for (int i = 0; i < mdbytes.length; i++) {
    		      sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
    		    }

    		    if(! sb.toString().equalsIgnoreCase(fmd5)){
    		    	err = -1;
    		    	
    		    	
    		    }
    		    
    	
    	if (err == 0){
    		return true;
    	}
    	else{
    		return false;
    	}
    }
	
    private class BinaryRequestListener implements RequestListener<InputStream>, RequestProgressListener {

        @Override
        public void onRequestFailure(SpiceException arg0) {
            if (!(arg0 instanceof RequestCancelledException)) {
                //Toast.makeText(FreeGee.this, "Failed to load zipfile.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onRequestSuccess(InputStream inputStream) {

            if (inputStream == null) {
                return;
            }
        }

        @Override
        public void onRequestProgressUpdate(RequestProgress progress) {
            FreeGee.getmProgressDialog().setProgress(Math.round(progress.getProgress() * 100));
        }
    }

	
	
}
