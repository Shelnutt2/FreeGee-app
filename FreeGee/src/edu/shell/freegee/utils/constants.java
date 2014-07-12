package edu.shell.freegee.utils;

import java.io.File;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

public class constants {

	public static final String FreeGeeFolder = setFreeGeeFolder();
	public static boolean beta = true;
	public static String DEVICE_XML_NAME = setDeviceXML();
	public static String DEVICE_XML = FreeGeeFolder + "/" + DEVICE_XML_NAME;
    public static String LOG_FILE = FreeGeeFolder + "/log.txt";
    public static final String EXTRA_FINISHED_DOWNLOAD_ID = "download_id";
    public static final String EXTRA_FINISHED_DOWNLOAD_PATH = "download_path";
    public static final String DOWNLOAD_ERROR = "Error";
    public static String LOG_TAG = "Freegee";
	public static String CP_COMMAND;
	


	public static String setDeviceXML(){
		if(beta)
			return "devices2_beta.xml";
		else
			return "devices2.xml";
	}
		
	private static String setFreeGeeFolder(){
    	/* Disabled due to getExternalStorageDirectory() not always accessible from the cmd (RootTools) even though java applications can access it fine
    	if(Environment.getExternalStorageDirectory().exists() && Environment.getExternalStorageDirectory().canWrite() && Environment.getExternalStorageDirectory().canRead())
    		return Environment.getExternalStorageDirectory().toString()+"/freegee/";
    	else*/
    	String sdcard = getSDCARD();
		if(sdcard != null)
			return sdcard+"/freegee/";
		else
    	    return "/sdcard/freegee/";
    }
    
	public static String getSDCARD(){
    	File dir = new File(System.getenv("EXTERNAL_STORAGE"));
    	if (!dir.exists())
            dir = Environment.getExternalStorageDirectory();
    	String sdcard;
		try {
            sdcard = dir.getCanonicalPath();
    	     } catch (IOException e) {
    	       utils.customlog(Log.VERBOSE,"IOException trying to get CanonicalPath for external Storage dir, falling back to absolute");
    	       sdcard = dir.getAbsolutePath();
    	     }
		Log.v(constants.LOG_TAG,"sdcard path is: " + sdcard);
		return sdcard;
	}
}
