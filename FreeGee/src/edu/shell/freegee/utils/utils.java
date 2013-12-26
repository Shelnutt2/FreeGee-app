package edu.shell.freegee.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import edu.shell.freegee.Action;
import edu.shell.freegee.Device;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.BatteryManager;
import android.text.TextUtils;
import android.util.Log;

public class utils {
	static File logFile = new File(constants.LOG_FILE);
	
    public static String calculateMD5(File updateFile) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            customlog(Log.ERROR, "Exception while getting digest");
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            customlog(Log.ERROR, "Exception while getting FileInputStream");
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5");
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                customlog(Log.ERROR, "Exception on closing MD5 input stream");
            }
        }
    }
    
    public static boolean checkMD5(String md5, File updateFile) {
        if (TextUtils.isEmpty(md5) || updateFile == null) {
            customlog(Log.ERROR, "MD5 string empty or updateFile null");
            return false;
        }

        String calculatedDigest = calculateMD5(updateFile);
        if (calculatedDigest == null) {
            customlog(Log.ERROR, "calculatedDigest null");
            return false;
        }

        customlog(Log.WARN, "Calculated digest: " + calculatedDigest);
        customlog(Log.WARN, "Provided digest: " + md5);
        

        return calculatedDigest.equalsIgnoreCase(md5);
    }
    
    public static void sendEmail(Activity activity, Action action, String message, String subject, Device myDevice) {
        Uri path = Uri.fromFile(logFile);
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("application/octet-stream");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        String to[] = { "shelnutt2@gmail.com" };
        intent.putExtra(Intent.EXTRA_EMAIL, to);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.putExtra(Intent.EXTRA_STREAM, path);
        activity.startActivityForResult(Intent.createChooser(intent, "Send mail..."), 1222);
    }
    
    public static boolean rebootRecovery(){
    	CommandCapture command = new CommandCapture(0,"reboot recovery");
		try {
			RootTools.getShell(true).add(command);
		} catch (IOException e) {
			customlog(Log.ERROR,"IOException trying to reboot to recovery");
			return false;
		} catch (TimeoutException e) {
			customlog(Log.ERROR,"TimeoutException trying to reboot to recovery");
			return false;
		} catch (RootDeniedException e) {
			customlog(Log.ERROR,"Root Denined trying to reboot to recovery");
			return false;
		}
		return true;
    }
    
    public static boolean rebootBootloader(){
    	CommandCapture command = new CommandCapture(0,"reboot bootloader");
		try {
			RootTools.getShell(true).add(command);
		} catch (IOException e) {
			customlog(Log.ERROR,"IOException trying to reboot to bootloader");
			return false;
		} catch (TimeoutException e) {
			customlog(Log.ERROR,"TimeoutException trying to reboot to bootloader");
			return false;
		} catch (RootDeniedException e) {
			customlog(Log.ERROR,"Root Denined trying to reboot to bootloader");
			return false;
		}
		return true;  	
    }
    
    public static boolean Shutdown(){
    	CommandCapture command = new CommandCapture(0,"/data/local/tmp/busybox halt");
		try {
			RootTools.getShell(true).add(command);
		} catch (IOException e) {
			customlog(Log.ERROR,"IOException trying to shutdown");
			return false;
		} catch (TimeoutException e) {
			customlog(Log.ERROR,"TimeoutException trying to shutdown");
			return false;
		} catch (RootDeniedException e) {
			customlog(Log.ERROR,"Root Denined trying to shutdown");
			return false;
		}
		return true;  	
    }
    
    public static void customlog(int logLevel, String lineToLog){
    	switch(logLevel){
    	    case Log.DEBUG:{
    	    	Log.d(constants.LOG_TAG, lineToLog);
    	    	writeToLog(lineToLog);
    	    	break;
    	    	}
    	    case Log.ERROR:{
    	    	Log.e(constants.LOG_TAG, lineToLog);
    	    	writeToLog(lineToLog);
    	    	break;
    	    	}
    	    case Log.INFO:{
    	    	Log.i(constants.LOG_TAG, lineToLog);
    	    	writeToLog(lineToLog);
    	    	break;
    	    	}
    	    case Log.VERBOSE:{
    	    	Log.v(constants.LOG_TAG, lineToLog);
    	    	writeToLog(lineToLog);
    	    	break;
    	    	}
    	    case Log.WARN:{
    	    	Log.w(constants.LOG_TAG, lineToLog);
    	    	writeToLog(lineToLog);
    	    	break;
    	    	}
    	    default:{
    	    	Log.d(constants.LOG_TAG, lineToLog);
    	    	writeToLog(lineToLog);
    	    	break;
    	    	}
    	
    	}
    }
    
    public static float getBatteryLevel(Context context) {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float)level / (float)scale) * 100.0f; 
    }
    
    public static void writeToLog(String lineToLog){
        FileOutputStream fop = null;
        try {
		    fop = new FileOutputStream(logFile,true);
 
			// if file does not exists, then create it; should not be needed here
			if (!logFile.exists()) {
			    logFile.createNewFile();
			}
			fop.write((lineToLog+"\n").getBytes());
			fop.flush();
			fop.close(); 
		}
        catch (IOException e) {
		      Log.e(constants.LOG_TAG,"IOException trying to write to log file");
		}
        finally {
			try {
				if (fop != null) {
					fop.close();
				}
			}
			catch (IOException e) {
				Log.e(constants.LOG_TAG,"IOException trying to close log file");
			}
		}
    }
}
