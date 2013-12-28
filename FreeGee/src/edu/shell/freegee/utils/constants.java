package edu.shell.freegee.utils;

import java.io.File;

import android.os.Environment;

public class constants {

	public static final String FreeGeeFolder = setFreeGeeFolder();
	public static String DEVICE_XML = FreeGeeFolder + "/devices2.xml";
    public static String LOG_FILE = FreeGeeFolder + "/log.txt";
    public static String LOG_FILE_OLD = FreeGeeFolder + "/log_old.txt";
    public static final String EXTRA_FINISHED_DOWNLOAD_ID = "download_id";
    public static final String EXTRA_FINISHED_DOWNLOAD_PATH = "download_path";
    public static final String DOWNLOAD_ERROR = "Error";
    public static String LOG_TAG = "Freegee";
    
    private static String setFreeGeeFolder(){
    	/* Disabled due to getExternalStorageDirectory() not always accessible from the cmd (RootTools) even though java applications can access it fine
    	if(Environment.getExternalStorageDirectory().exists() && Environment.getExternalStorageDirectory().canWrite() && Environment.getExternalStorageDirectory().canRead())
    		return Environment.getExternalStorageDirectory().toString()+"/freegee/";
    	else*/
    		return "/sdcard/freegee/";
    }
}
