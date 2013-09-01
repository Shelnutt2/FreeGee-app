package edu.shell.freegee;

import android.util.Log;

public class NativeTask {
    
	public static final String MSG_TAG = "FREEGEE -> NativeTask";

	static {
        try {
            Log.i(MSG_TAG, "Trying to load libfreegeenativetask.so");
            System.loadLibrary("freegeenativetask");
        }
        catch (UnsatisfiedLinkError ule) {
            Log.e(MSG_TAG, "Could not load libfreegeenativetask.so");
        }
    }
    public static native int action(String name);
}
