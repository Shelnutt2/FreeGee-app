/** @author Seth Shelnutt
 * @License GPLv3 or later
 * All source code is released free and openly by Seth Shelnutt under the terms of GPLv3 or later, 2013 
 * */

package edu.shell.freegee;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

public class dropbox {
	final static private String APP_KEY = "ywebobijtcfo2yc";
	final static private String APP_SECRET = "ud1duwmbtlml0zz";
	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	private static String key,secret;
	// In the class declaration section:
	static DropboxAPI<AndroidAuthSession> mDBApi;
	
	public static void FirstAuthenticaton(Context MyContext){



		// And later in some initialization function:
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
		mDBApi = new DropboxAPI<AndroidAuthSession>(session);
		
		mDBApi.getSession().startAuthentication(MyContext);
	    if (mDBApi.getSession().authenticationSuccessful()) {
	        try {
	            // MANDATORY call to complete auth.
	            // Sets the access token on the session
	            mDBApi.getSession().finishAuthentication();

	            AccessTokenPair tokens = mDBApi.getSession().getAccessTokenPair();

	            // Provide your own storeKeys to persist the access token pair
	            // A typical way to store tokens is using SharedPreferences
	            storeKeys(tokens.key, tokens.secret, MyContext);
	            key = tokens.key;
	            secret = tokens.secret;
	            Log.i("FreeGee","key is: " + key);
	        } catch (IllegalStateException e) {
	            Log.i("DbAuthLog", "Error authenticating", e);
	        }
	    }
	    
	}
	
	public static String getSecret(){
		return secret;
	}
	
	public static String getkey(){
		return key;
	}
	
	public static DropboxAPI<AndroidAuthSession> newSession(Context MyContext){
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
		mDBApi = new DropboxAPI<AndroidAuthSession>(session);
		AccessTokenPair access = getkeys(MyContext);
		mDBApi.getSession().setAccessTokenPair(access);
		return mDBApi;
	}
	public static int uploadFile(File fname, Context MyContext){
		mDBApi = newSession(MyContext);
		// Uploading content.
		FileInputStream inputStream = null;
		try {
			
		    inputStream = new FileInputStream(fname);
		    Entry newEntry = mDBApi.putFile(fname.getName(), inputStream,
		            fname.length(), null, null);
		    Log.i("FreeGee", "The uploaded file's rev is: " + newEntry.rev);
		} catch (DropboxUnlinkedException e) {
		    // User has unlinked, ask them to link again here.
		    Log.e("FreeGee", "User has unlinked.");
		    return 1;
		} catch (DropboxException e) {
		    Log.e("FreeGee", "Something went wrong while uploading.");
		    return 2;
		} catch (FileNotFoundException e) {
		    Log.e("FreeGee", "File not found.");
		    return 3;
		} finally {
		    if (inputStream != null) {
		        try {
		            inputStream.close();
		            return 0;
		        } catch (IOException e) {}
		    }
		}
		return 0;
	}
	
	public static int downloadFile(String fname){
		// Get file.
		FileOutputStream outputStream = null;
		try {
		    File file = new File(fname);
		    outputStream = new FileOutputStream(file);
		    DropboxFileInfo info = mDBApi.getFile(fname, null, outputStream, null);
		    Log.i("FreeGee", "The file's rev is: " + info.getMetadata().rev);
		    // /path/to/new/file.txt now has stuff in it.
		} catch (DropboxException e) {
		    Log.e("FreeGee", "Something went wrong while downloading.");
		    return 1;
		} catch (FileNotFoundException e) {
		    Log.e("FreeGee", "File not found.");
		    return 2;
		} finally {
		    if (outputStream != null) {
		        try {
		            outputStream.close();
		            return 0;
		        } catch (IOException e) {
		        	return 3;
		        }
		        
		    }
		}
		return 0;

	}
	 private static void storeKeys(String key, String secret, Context MyContext) {
	    // Save the access key for later
		SharedPreferences prefs = MyContext.getSharedPreferences("FreeGee",MyContext.MODE_PRIVATE);
	    Editor edit = prefs.edit();
	    edit.putString("dropbox_key", key);
	    edit.putString("dropbox_secret", secret);
	    edit.commit();
	    Log.i("freegee", "Keys stored");
	}
	
	public static AccessTokenPair getkeys(Context MyContext){
		SharedPreferences prefs = MyContext.getSharedPreferences("FreeGee",MyContext.MODE_PRIVATE);
		String key = prefs.getString("dropbox_key","");
		String secret = prefs.getString("dropbox_secret","");
		return new AccessTokenPair(key, secret);
	}
	
}
