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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;

import edu.shell.freegee.R;
import edu.shell.freegee.device.Action;
import edu.shell.freegee.device.Device;
import edu.shell.freegee.device.DeviceDetails;
import edu.shell.freegee.device.Devices;
import edu.shell.freegee.device.Partition;
import edu.shell.freegee.utils.constants;
import edu.shell.freegee.utils.utils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.graphics.PorterDuff;

@SuppressLint("SdCardPath")
/**
 * @author Seth Shelnutt
 */
public class FreeGee extends Activity implements OnClickListener {

    private Device myDevice;
    private DeviceDetails myDeviceDetails;
    
    private static ProgressDialog mProgressDialog;
    public static boolean isSpecial;
    private ArrayList<Object> mButtons = new ArrayList<Object>();
    private ArrayList<Device> DeviceList;
        
    private static boolean mMainActivityActive;


	private static String CP_COMMAND;
	private HashMap<String,Integer> downloadTries = new HashMap<String,Integer>();
    
    private Properties buildProp = new Properties();
    
    private int actionsleft = 0;
    private List<Action> actionOrder = new ArrayList<Action>();	
    private HashMap<String,Boolean> actionDownloads = new HashMap<String,Boolean>();
    private Action mainAction;
    private boolean ActionSuccess = true;
    private String swprop;
    
    private boolean makoUnlock = true;
    private Action ogunlock;
    private Action ogMakounlock;

    private Action loki_check;
    
    public static String PACKAGE_NAME;
    private AdView adView;
    
	File logFile = new File(constants.LOG_FILE);

	/**
	 * Used to show the Change Log as needed on opening of new version
	 */
    private void showChangeLog(){
        ChangeLog cl = new ChangeLog(this);
        if (cl.firstRun())
            cl.getLogDialog().show();
    }
    
    /**
     * Returns true if freegee is the free version or false if donate version
     * @return 
     */
    public boolean freeVersion(){
    	if(PACKAGE_NAME.equalsIgnoreCase("edu.shell.freegee_free"))
    		return true;
    	else
    		return false;
    }
    
    /**
     * Makes the ads to display
     * @param layout LinearLayout in which to show adds
     * @param index The index of the position to show the add
     */
    private void makeAds(LinearLayout layout, int index){
        adView = new AdView(this);
        adView.setAdUnitId(privateData.AdUnitID);
        adView.setAdSize(AdSize.SMART_BANNER);
        layout.addView(adView, index);
        
        // Initiate a generic request.
        AdRequest adRequest = new AdRequest.Builder()
        .addTestDevice("003b344396ca856a")
        .build();

        // Load the adView with the ad request.
        adView.loadAd(adRequest);


    }
    
    /**
     * Find's the "cp" binary if it exists on the system already
     * @return True if found, false if not found
     */
    private boolean findCP(){
    	CommandCapture command = new CommandCapture(0,"ls /system/bin/cp"){
	        @Override
	        public void output(int id, String line)
	        {
	        	utils.customlog(Log.VERBOSE,line);
	            //RootTools.log(constants.LOG_TAG, line);
	            
	        }
		 };
		Shell shell = null;
		try {
			shell = RootTools.getShell(true);
			shell.add(command);
			commandWait(command);
		} catch (IOException e) {
			utils.customlog(Log.ERROR, "Root Denined!");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR, "Timed out ls /system/bin/cp!");
			alertbuilder("Error!","Timed out looking for cp","Ok",1);
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR, "Root Denined!");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}		
		int err = command.getExitCode();
		if(err == 0){
			CP_COMMAND="/system/bin/cp";
			return true;
		}
		else{
			command = new CommandCapture(0,"ls /system/xbin/cp"){
		        @Override
		        public void output(int id, String line)
		        {
		        	utils.customlog(Log.VERBOSE,line);
		            //RootTools.log(constants.LOG_TAG, line);
		            
		        }
			 };
			try {
				shell = RootTools.getShell(true);
				shell.add(command);
				commandWait(command);
			} catch (IOException e) {
				utils.customlog(Log.ERROR, "Timed out ls /system/xbin/cp!");
				alertbuilder("Error!","Timed out looking for cp","Ok",1);
			} catch (TimeoutException e) {
				utils.customlog(Log.ERROR, "Timed out ls /system/xbin/cp!");
				alertbuilder("Error!","Timed out looking for cp","Ok",1);
			} catch (RootDeniedException e) {
				utils.customlog(Log.ERROR, "Root Denined!");
				alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
			}
			err = command.getExitCode();
			if(err == 0){
				CP_COMMAND="/system/xbin/cp";
				return true;
			}
		}
		
    	return false;
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        mMainActivityActive = true;
        checkForDownloadCompleted(getIntent());
    }

    @Override
    protected void onStop() {
        mMainActivityActive = false;
        super.onStop();
    }
    
    @Override
    public void onPause() {
        if(adView != null)
            adView.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(adView != null)
            adView.resume();
        mMainActivityActive = true;
  	    checkForDownloadCompleted(getIntent());
    }

    @Override
    public void onDestroy() {
    	if(adView != null)
    		adView.destroy();
      super.onDestroy();
    }

    /**
     * Function to check for if busybox is available and if not offer to install it for users
     * @return True is it exists
     */
    public boolean checkForBusyBox(){
		if(!RootTools.isBusyboxAvailable()){
			utils.customlog(Log.ERROR, "Buysbox no found!");
			alertbuilder("Error!","BusyBox not installed. Please install it now","Ok",0);
			RootTools.offerBusyBox(this);
			return false;
		}
		else
			return true;
    }
    
    /**
     * Checks for if the Internet is accessible or not
     * @return
     */
    public boolean isOnline() {
        ConnectivityManager cm =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && 
           cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainActivityActive = false;
        setContentView(R.layout.activity_freegee);
        utils.customlog(Log.VERBOSE,"FreeGee dir is: "+constants.FreeGeeFolder);
    	File freegeef=new File(constants.FreeGeeFolder);
		  if(!freegeef.exists()){
			  freegeef.mkdirs();
		  }
	    File freegeeft=new File(constants.FreeGeeFolder+"/tools");
		  if(!freegeeft.exists()){
			  freegeeft.mkdirs();
		  }
			
			//Move log if it exists, keep a backup copy just incase one needs to report old error but reruns freegee
		if(logFile.exists()){
			logFile.renameTo(new File(constants.LOG_FILE_OLD));
			logFile = new File(constants.LOG_FILE);
		} else{
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				utils.customlog(Log.ERROR,"IOException trying to open log file");
				alertbuilder("Error","There was an error tring to open the log file. The app will continue but debugging will not be avaliable","ok",0);
			}
		}
		
		if (!RootTools.isAccessGiven()) {
			utils.customlog(Log.ERROR, "Root Denined!");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}
		
		if(!findCP()){
			int count = 0;
			while(!checkForBusyBox()){
				count++;
				if(count>3){
					alertbuilder("Error!","No CP or busybox command found, attempted to find it and offer install 3 times. Please install busybox and relaunch FreeGee","Exit",1);
					break;
				}
			}
			CP_COMMAND="busybox cp";
		}
		utils.customlog(Log.VERBOSE,"CP_COMMAND is " + CP_COMMAND);
		
		if(utils.getBatteryLevel(this) < 15.0 && !(utils.getBatteryCharging(this) && utils.getBatteryLevel(this) >= 10.0) )
			alertbuilder("Error!","Your batter is too low to do anything, please charge it or connect an ac adapter","OK",1);
		
		PACKAGE_NAME = getApplicationContext().getPackageName();
		
	    // read the property text  file
		File file = new File("/system/build.prop");
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException f) {
			CommandCapture command = new CommandCapture(0,"mount -o remount,rw /system");
			try {
				RootTools.getShell(true).add(command).isFinished();
			} catch (IOException e) {
				utils.customlog(Log.ERROR,"Can't remount /system");
				alertbuilder("Error!","Can't remount /system","Ok",1);
			} catch (TimeoutException e) {
				utils.customlog(Log.ERROR,"Chmod timed out");
				alertbuilder("Error!","remount timed out","Ok",1);
			} catch (RootDeniedException e) {
				utils.customlog(Log.ERROR,"No root access!");
				alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
			}
			command = new CommandCapture(0,"chmod 644 /system/build.prop");
			try {
				RootTools.getShell(true).add(command).isFinished();
			} catch (IOException e) {
				utils.customlog(Log.ERROR,"");
				alertbuilder("Error!","Can't chmod build.prop","Ok",1);
			} catch (TimeoutException e) {
				utils.customlog(Log.ERROR,"Chmod timed out");
				alertbuilder("Error!","Chmod timed out","Ok",1);
			} catch (RootDeniedException e) {
				utils.customlog(Log.ERROR,"No root access!");
				alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
			}
			command = new CommandCapture(0,"mount -o remount,ro /system");
			try {
				RootTools.getShell(true).add(command).isFinished();
			} catch (IOException e) {
				utils.customlog(Log.ERROR,"Can't remount /system");
				alertbuilder("Error!","Can't remount /system","Ok",1);
			} catch (TimeoutException e) {
				utils.customlog(Log.ERROR,"remount timed out");
				alertbuilder("Error!","remount timed out","Ok",1);
			} catch (RootDeniedException e) {
				utils.customlog(Log.ERROR,"No root access!");
				alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
			}
		}

		// feed the property with the file
		try {
			buildProp.load(fis);
		} catch (IOException e) {
			alertbuilder("Error!","Can't load build.prop make sure you have root and perms are set correctly","Ok",0);
			e.printStackTrace();
		}
		try {
			fis.close();
		} catch (IOException e) {
			alertbuilder("Error!","Can't close build.prop, something went wrong.","Ok",0);
			e.printStackTrace();
		}
		
		setupUtilities(0);
		showChangeLog();
		if(isOnline())
            getDevices();
		else
			alertbuilder("No Network","You are not connected to the internet, please connect and launch FreeGee again","Ok",1);
        LinearLayout layout =  (LinearLayout)findViewById(R.id.main_linear_layout);
        //Toast.makeText(this, "number of children: "+layout.getChildCount(), Toast.LENGTH_LONG).show();
        //utils.customlog(Log.VERBOSE,"number of children: "+layout.getChildCount());
        if(freeVersion()){
            makeAds(layout,0);
            makeAds(layout,2);
        }
        //makeAds(layout,layout.getChildCount()/2);
        //makeAds(layout,layout.getChildCount());
        checkForDownloadCompleted(getIntent());
		GridView gridView = (GridView) findViewById(R.id.main_gridview);
		gridView.setAdapter(new ButtonAdapter(mButtons));
    }
    
    /**
     * Update the grid view based on actions supported by the device
     * @param device
     */
    public void updateGridView(Device device){
    	//Toast.makeText(this, "Updating Grid View", Toast.LENGTH_LONG).show();
    	int j = 0;
        for(int i = 0; i < device.getActions().size();i++){
        	if(((device.getActions().get(i).getStockOnly() && onStock() ) || !device.getActions().get(i).getStockOnly()) && !device.getActions().get(i).getHidden()){
    		Button cb = new Button(this);
  		    cb.setText(device.getActions().get(i).getName());
  		    
  		    if(j % 4 == 0){
  		      cb.getBackground().setColorFilter(Color.parseColor("#005030"), PorterDuff.Mode.DARKEN);
  		      cb.setTextColor(Color.parseColor("#f47321"));
            }
  		    else if(j % 4 == 1){
    		      cb.getBackground().setColorFilter(Color.parseColor("#f47321"), PorterDuff.Mode.DARKEN);
    		      cb.setTextColor(Color.parseColor("#005030"));
              }
  		    else if(j % 4 == 2){
    		      cb.getBackground().setColorFilter(Color.parseColor("#f47321"), PorterDuff.Mode.DARKEN);
    		      cb.setTextColor(Color.parseColor("#005030"));
              }
  		    else if(j % 4 == 3){
    		      cb.getBackground().setColorFilter(Color.parseColor("#005030"), PorterDuff.Mode.DARKEN);
    		      cb.setTextColor(Color.parseColor("#f47321"));
              }
  		    cb.setTypeface(null, Typeface.BOLD);
  		    cb.setMinHeight(333);
  		    cb.setMinWidth(100);
  		    //cb.setPadding(100, 100, 100, 100);
  		    cb.setOnClickListener(this);
  		    cb.setId(i);
  		    mButtons.add(cb);
  		    j++;
        }
        }
		GridView gridView = (GridView) findViewById(R.id.main_gridview);
		gridView.setAdapter(new ButtonAdapter(mButtons));
    }
    
    /**
     * Check if on stock or now by checking if the swversion exists or not
     * @return
     */
    public boolean onStock(){
    	if(swprop != null)
    		return true;
    	else
    		return false;
    	
    }

    /**
     * Parse the xml into devices object
     * @return True if completed or false if error
     */
    public boolean unSerializeDevices(){
    	//Toast.makeText(this, "Unserializing Devices", Toast.LENGTH_LONG).show();
    	Serializer serializer = new Persister();
    	File source = new File(constants.DEVICE_XML);
    	try {
			DeviceList = serializer.read(Devices.class, source).getDevices();
			return true;
		} catch (Exception e) {
			utils.customlog(Log.ERROR,"Could not unserialize");
			mProgressDialog.dismiss();
			alertbuilder("Error!","Could not unserialize devices from xml","Ok",1);
			return false;
		}
    }
    
    /**
     * Parse the xml into devices object
     * @return True if completed or false if error
     */
    public boolean unSerializeDeviceDetails(String DeviceDetailsXML){
    	//Toast.makeText(this, "Unserializing Devices", Toast.LENGTH_LONG).show();
    	mProgressDialog.setMessage("UnSerializing Device Detials");
    	Serializer serializer = new Persister();
    	File source = new File(DeviceDetailsXML);
    	try {
			myDeviceDetails = serializer.read(DeviceDetails.class, source);
			mProgressDialog.dismiss();
			return true;
		} catch (Exception e) {
			utils.customlog(Log.ERROR,"Could not unserialize deviceDetails");
			mProgressDialog.dismiss();
			alertbuilder("Error!","Could not unserialize device details from xml","Ok",1);
			return false;
		}
    }
    
    @Override
    public void onClick(View v) {
     Button selection = (Button)v;
     //Toast.makeText(getBaseContext(), selection.getText()+ " was pressed!", Toast.LENGTH_SHORT).show();
     for(final Action a:myDevice.getActions()){
    	 if(a.getName().equals(selection.getText())){
    		 if(onStock()){
    			 if(checkForUnlock(a,false)){
    				 promptUnlockAlertDialog(a);
    			 }
    			 else{
    				 showActionAlertDialog(a);
    			 }
    		 }
    		 else{
    			 showActionAlertDialog(a);
    		 }
    	    	break;
    	 }
     }
     //Toast.makeText(getBaseContext(), "Result is: "+result, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Check to see if action or it's dependencies require the unlock action for optimus g 
     * @param a Action
     * @param unlock
     * @return True if unlocked required, false if unlock not required by action
     */
    private boolean checkForUnlock(Action a, boolean unlock) {
    	if(!myDevice.getName().equalsIgnoreCase("LG Optimus G"))
    		return false;
    	if (unlock)
    		return true;
    	if(a.getDependencies() != null && !a.getDependencies().isEmpty()){
    		for(Action b:a.getDependencies()){
    			unlock = checkForUnlock(b, unlock);
    		}
    	}
    	if (unlock)
    		return true;
    	if(a.getName().equalsIgnoreCase("Optimus G Unlock") || a.getName().equalsIgnoreCase("Mako Unlock")){
    		return true;
    	}
    	else
    		return false;
		
	}
    
    /**
     * Prompt for unlock Alert Dialog for LG Optimus G, so users can choose mako sbl stack or ics sbl stack
     * @param a Action
     */
	private void promptUnlockAlertDialog(final Action a) {
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
	    
    	// set title
    	alertDialogBuilder.setTitle(a.getName());

    	// set dialog message
    	alertDialogBuilder
    	.setMessage("The action of "+ a.getName() + " requires your device to be unlocked. There are two unlock options avaliable. The mako (formerly sbl) unlock will give you the boot menu screen. It has no increased risk over the standard unlock. The standard is the classic unlock procedure used on old freegee and has a soft brick risk.")
    	.setCancelable(false)
    	.setPositiveButton("Mako Unlock",new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog,int id) {
    	     makoUnlock = true;
    	     showActionAlertDialog(a);
    	}
    	})
    	.setNegativeButton("Classic Unlock",new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog,int id) {
	    		makoUnlock = false;
	    		showActionAlertDialog(a);
	    	}
    	});

    	// create alert dialog
    	AlertDialog alertDialog = alertDialogBuilder.create();

    	// show it
    	alertDialog.show();
		
	}

	/**
	 * Show an alertDialog to prompt for action selected
	 * @param a Action
	 */
	private void showActionAlertDialog(final Action a) {
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
	    
    	// set title
    	alertDialogBuilder.setTitle(a.getName());

    	// set dialog message
    	alertDialogBuilder
    	.setMessage(a.getDescription())
    	.setCancelable(false)
    	.setPositiveButton("Proceed",new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog,int id) {
    	     mProgressDialog = new ProgressDialog(FreeGee.this);      
    	     mProgressDialog.setIndeterminate(true);
    	     mProgressDialog.setCancelable(false);
    	     mProgressDialog.setMessage("Performing action " + a.getName() + " ...");
    	     mProgressDialog.show();
    		 mainAction = a;
			 actionOrder = new ArrayList<Action>();
			 actionDownloads = new HashMap<String,Boolean>();
    	     processAction(a);
    	}
    	})
    	.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog,int id) {
	    		Toast.makeText(getBaseContext(), "Action " + a.getName() + " was cancelled", Toast.LENGTH_SHORT).show();
	    	}
    	});

    	// create alert dialog
    	AlertDialog alertDialog = alertDialogBuilder.create();

    	// show it
    	alertDialog.show();
		
	}

	public class ButtonAdapter extends BaseAdapter {  
    	private ArrayList<Object> mButtons = null;

    	public ButtonAdapter(ArrayList<Object> b) {
    	 mButtons = b;
    	} 
         
        // Total number of things contained within the adapter  
        public int getCount() {  
         return mButtons.size();  
        }  
         
         // Require for structure, not really used in my code.  
        public Object getItem(int position) {  
         return (Object) mButtons.get(position);
        }  
         
        // Require for structure, not really used in my code. Can  
        // be used to get the id of an item in the adapter for   
        // manual control.   
        public long getItemId(int position) {  
         return position;  
        }  

       @Override
       public View getView(int position, View convertView, ViewGroup parent) {
        Button button;
        if (convertView == null) {
         button = (Button) mButtons.get(position);
        } else {
         button = (Button) convertView;
        }
         return button;
        }
       }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(intent.hasExtra(constants.DOWNLOAD_ERROR)){
        	mProgressDialog.dismiss();
        	alertbuilder("Error","There was an error downloading the necessary files","ok",0);
        }
        else
            checkForDownloadCompleted(intent);
    }
    
    /**
     * Check for if the completed download was our download or not.
     * @param intent
     */
    private void checkForDownloadCompleted(Intent intent) { //Based on CMUpdater
    	//Toast.makeText(this, "Checking for Completed Downloads", Toast.LENGTH_SHORT).show();
        if (intent == null) {
            return;
        }

        long downloadId = intent.getLongExtra(constants.EXTRA_FINISHED_DOWNLOAD_ID, -1);
        if (downloadId < 0) {
        //	Toast.makeText(this, "Not download", Toast.LENGTH_LONG).show();
            return;
        }

        String fullPathName = intent.getStringExtra(constants.EXTRA_FINISHED_DOWNLOAD_PATH);
        if (fullPathName == null) {
       // 	Toast.makeText(this, "No path given", Toast.LENGTH_LONG).show();
            return;
        }
        
        utils.customlog(Log.VERBOSE,"Downloaded file path is: " + fullPathName);
        String fileName = new File(fullPathName).getName();
        if(fileName.equalsIgnoreCase("devices2.xml")){
        	utils.customlog(Log.VERBOSE,"DeviceXML is: " + fullPathName);
        	if(unSerializeDevices())
        	    matchDevice();
        }
        else if(myDevice != null && myDevice.getDeviceDetailsLocation() != null && fileName.equalsIgnoreCase(new File(myDevice.getDeviceDetailsLocation()).getName().toString())){
        	utils.customlog(Log.VERBOSE,"DeviceDetailsXML is: " + fullPathName);
        	unSerializeDeviceDetails(fullPathName);
        }
        else if(myDevice != null && myDevice.getActions() != null){
        	if(loki_check.getZipFile().equalsIgnoreCase(fileName)){
        		if(utils.checkMD5(loki_check.getMd5sum(), new File(fullPathName))){
        			checkLoki(loki_check,fullPathName);
        		}
            	else{
            		if(downloadTries.containsKey(loki_check.getName()) && downloadTries.get(loki_check.getName())<=3){
            		    int count;
            		    if(downloadTries.containsKey(loki_check.getName()))
            			    count =+ downloadTries.get(loki_check.getName());        	            		
            		    else
            		    	count = 1;
            		    downloadTries.put(loki_check.getName(), count);
            		    Toast.makeText(this, "md5sum mismatch for "+loki_check.getName()+". Redownloading", Toast.LENGTH_LONG).show();
            		    startDownload(loki_check);
            		}
            		else{
            			Toast.makeText(this, "md5sum mismatch for "+loki_check.getName()+". Failed 3 times, aborting", Toast.LENGTH_LONG).show();
            		}
            	}
        	}
            else{
        	    if(myDevice != null && myDevice.getActions() != null){
        	        if(ActionSuccess){
        	            utils.customlog(Log.VERBOSE,"Matching action");
                        ArrayList<Action> actions = myDevice.getActions();
                        for(Action i:actions){
        	                if (i.getZipFile().equalsIgnoreCase(fileName)){
        	                	utils.customlog(Log.VERBOSE,"Action matches as: " + i.getName());
        	            	    if(utils.checkMD5(i.getMd5sum(), new File(fullPathName))){
        	            	    	utils.customlog(Log.VERBOSE,"Current list of actionDownloads are: "+ printList(actionDownloads));
        	            		    if(actionDownloadsContains(i))
        	            			    actionDownloads.put(i.getName(),true);
        	            		    else{
        	            			    utils.customlog(Log.ERROR,"Downloaded action of " + i.getName() + " wasn't part of actions to be downloaded");
//        	            			    utils.customlog(Log.VERBOSE,"Current list of actions are: "+ printList(actionDownloads));
        	            			    mProgressDialog.dismiss();
        	            			    alertbuilder("Error","The wrong action was received, aborting processing actions","ok",0);
        	            			    ActionSuccess = false;
        	            		    }
        	            		    utils.customlog(Log.VERBOSE,"Current size actionDownloads is: "+ actionDownloads.size());
        	            		    utils.customlog(Log.VERBOSE,"Current number of actionsleft is: "+ actionsleft);
        	            		    utils.customlog(Log.VERBOSE,"Current stauts of allActionsDownloads is: "+ allActionsDownloads());
        	            		    if(actionDownloads.size() == actionsleft && allActionsDownloads())
                	                    doAllActions();
            	            	}
            	            	else{
        	                		if(downloadTries.containsKey(i.getName()) && downloadTries.get(i.getName())<=3){
        	                		    int count;
        	                		    if(downloadTries.containsKey(i.getName()))
        	                			    count =+ downloadTries.get(i.getName());        	            		
        	            	    	    else
        	            		        	count = 1;
        	            		        downloadTries.put(i.getName(), count);
        	            		        Toast.makeText(this, "md5sum mismatch for "+i.getName()+". Redownloading", Toast.LENGTH_LONG).show();
        	            		        startDownload(i);
        	            		    }
        	            		    else{
            	            			Toast.makeText(this, "md5sum mismatch for "+i.getName()+". Failed 3 times, aborting", Toast.LENGTH_LONG).show();
            	            		}
            	            	}
        	                }
                        }
        	        }
        	        else{
        		        actionsleft--;
        		        if(actionsleft == 0)
            			    ActionSuccess = false;
            	    }
            	}
        	    else{
        		    alertbuilder("Error","There was an error detecting what was downloaded. This usually can be fixed by closing and reopening the applications.","Close now",1);
        	    }
            }
        }
    }
    
    private boolean actionDownloadsContains(Action i) {
		for(String actionName:actionDownloads.keySet()){
			if(actionName.equalsIgnoreCase(i.getName()))
				return true;
		}
		return false;
	}

	public boolean doAllActions(){
    	utils.customlog(Log.VERBOSE,"actionDownloads size: " + actionDownloads.size());
    	utils.customlog(Log.VERBOSE,"actionOrder size: " + actionOrder.size());
    	if(actionDownloads.size() == actionsleft && allActionsDownloads()){
    		for(Action action:actionOrder){
    			if(ActionSuccess){
    			    ActionSuccess = doAction(action, constants.FreeGeeFolder+action.getZipFile());
    			    //actionOrder.remove(action);
    			    //actionDownloads.remove(action);
    			}
    			else
    				return false;
    		}
    		if(ActionSuccess)
    			return true;
    	}
    	return false;
    }
    
    /**
     * Match device to supported devices from devices object
     */
    public void matchDevice(){
    	mProgressDialog.setMessage("Matching device");
    	if(DeviceList == null){
    		mProgressDialog.dismiss();
    		alertbuilder("Error!","Could not unserialize devices from xml","Ok",1);
    		return;
    	}
    	for(Device device:DeviceList){
    		String prop = buildProp.getProperty(device.getProp_id());
    		String prop2 = buildProp.getProperty(device.getProp_id().toLowerCase(Locale.US));
    		swprop = buildProp.getProperty(device.getSW_Prop_id());
    		ArrayList<String> models = device.getModel();
    		for(String model:models){
    		    if(prop != null){
    			     if(prop.equalsIgnoreCase(model)){
    				    if(onStock()){
    					     if(device.getFirmware().contains(swprop) || device.getFirmware().contains("any")){
    						    myDevice = device;
    						    if(myDevice.getDeviceDetailsLocation() != null){
    							    mProgressDialog.dismiss();
    							    getDeviceDetails(myDevice.getDeviceDetailsLocation());
    						    }
    					    }
    					    else{
    						    mProgressDialog.dismiss();
    						    utils.customlog(Log.ERROR,"Software version: " + swprop +" on device" + prop != null ? prop : prop2  +" not supported yet");
    						    alertbuilder("Unspported", "Your devices specific software version of " + swprop + " is not currently supported","Ok",0);
    					    }
    				    }
    				    else{
    					    myDevice = device;
						    if(myDevice.getDeviceDetailsLocation() != null){
							    mProgressDialog.dismiss();
							    getDeviceDetails(myDevice.getDeviceDetailsLocation());
						    }
    				    }
    				    if(myDevice.getName().equalsIgnoreCase("LG Optimus G"))
    					    setUnlocks();
    				    break;
    			    }
    		    }
    		    else if(prop2 != null){
    			    if(prop2.equalsIgnoreCase(model)){
    				    if(onStock()){
    					    if(device.getFirmware().contains(swprop) || device.getFirmware().contains("any")){
    						    myDevice = device;
    						    if(myDevice.getDeviceDetailsLocation() != null){
    						    	mProgressDialog.dismiss();
    							    getDeviceDetails(myDevice.getDeviceDetailsLocation());
    						    }
    					    }
    					    else{
    						    mProgressDialog.dismiss();
    						    utils.customlog(Log.ERROR,"Software version: " + swprop +" on device" + prop != null ? prop : prop2  +" not supported yet");
    						    alertbuilder("Unspported", "Your devices specific software version of " + swprop + " is not currently supported","Ok",0);
    					    }
    				    }
    				    else{
    				        myDevice = device;
						    if(myDevice.getDeviceDetailsLocation() != null){
							    mProgressDialog.dismiss();
							    getDeviceDetails(myDevice.getDeviceDetailsLocation());
						    }
    				    }
    				    if(myDevice.getName().equalsIgnoreCase("LG Optimus G"))
    					    setUnlocks();
    				     break;
    			    }
    		    }
    		}
    	}
    	if(myDevice != null){
    		 updateGridView(myDevice);
    	     ListView lv = (ListView) findViewById(R.id.deviceInfo);
    	     String[] lStr;
    	     if(swprop == null){
    	         lStr = new String[]{"Device Name: "+myDevice.getName(),"Device Model: "+myDevice.getModel()};
    	         utils.customlog(Log.VERBOSE,"Device Name: "+myDevice.getName() + "\n" +"Device Model: "+myDevice.getModel());
    	     }
    	     else{
    	    	 lStr = new String[]{"Device Name: "+myDevice.getName(),"Device Model: "+myDevice.getModel(),"Software Version: "+swprop};
    	    	 utils.customlog(Log.VERBOSE,"Device Name: "+myDevice.getName() + "\n" +"Device Model: "+myDevice.getModel() + "\n" + "Software Version: "+swprop);
    	     }
    	     lv.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, lStr));
    	     if(mProgressDialog.isShowing())
    	    	 mProgressDialog.dismiss();
    	     if(myDevice.getBootloaderExploit() == 1)
    	    	 checkLoki();
    	}
    	else{
    		mProgressDialog.dismiss();
    		String Genericprop = buildProp.getProperty("ro.product.Model");
    		String Genericprop2 = buildProp.getProperty("ro.product.model");
        	for(Device device:DeviceList){
        		swprop = buildProp.getProperty(device.getSW_Prop_id());
        		ArrayList<String> models = device.getModel();
        		for(String model:models){
        		    if(Genericprop != null){
        			    if(Genericprop.equalsIgnoreCase(model)){
        				    if(onStock()){
        					    if(device.getFirmware().contains(swprop) || device.getFirmware().contains("any")){
        						    myDevice = device;
        						    if(myDevice.getDeviceDetailsLocation() != null){
        							    mProgressDialog.dismiss();
        							    getDeviceDetails(myDevice.getDeviceDetailsLocation());
        						    }
        					    }
        					    else{
        						    mProgressDialog.dismiss();
        						    utils.customlog(Log.ERROR,"Software version: " + swprop +" on device" + Genericprop != null ? Genericprop : Genericprop2  +" not supported yet");
        						    alertbuilder("Unspported", "Your devices specific software version of " + swprop + " is not currently supported","Ok",0);
        					    }
        				    }
        				    else{
        					    myDevice = device;
    						    if(myDevice.getDeviceDetailsLocation() != null){
    							    mProgressDialog.dismiss();
    							    getDeviceDetails(myDevice.getDeviceDetailsLocation());
    						    }
        				    }
        				    if(myDevice.getName().equalsIgnoreCase("LG Optimus G"))
        					    setUnlocks();
        				    break;
        			    }
        			    else if(Genericprop.contains(model)){
        				    if(onStock()){
        					    if(device.getFirmware().contains(swprop) || device.getFirmware().contains("any")){
        						    myDevice = device;
        						    if(myDevice.getDeviceDetailsLocation() != null){
        							    mProgressDialog.dismiss();
        							    getDeviceDetails(myDevice.getDeviceDetailsLocation());
        						    }
        					    }
        					    else{
        						    mProgressDialog.dismiss();
        						    utils.customlog(Log.ERROR,"Software version: " + swprop +" on device" + Genericprop != null ? Genericprop : Genericprop2  +" not supported yet");
        						    alertbuilder("Unspported", "Your devices specific software version of " + swprop + " is not currently supported","Ok",0);
        					    }
        				    }
        				    else{
        					    myDevice = device;
    						    if(myDevice.getDeviceDetailsLocation() != null){
    							    mProgressDialog.dismiss();
    							    getDeviceDetails(myDevice.getDeviceDetailsLocation());
    						    }
        				    }
        				    if(myDevice.getName().equalsIgnoreCase("LG Optimus G"))
            					setUnlocks();
            				break;
            			}
        	    		else if(model.contains(Genericprop)){
        		     		if(onStock()){
        			    		if(device.getFirmware().contains(swprop) || device.getFirmware().contains("any")){
        				    		myDevice = device;
        					    	if(myDevice.getDeviceDetailsLocation() != null){
        						    	mProgressDialog.dismiss();
        							    getDeviceDetails(myDevice.getDeviceDetailsLocation());
        						    }
        					    }
        					    else{
        						    mProgressDialog.dismiss();
        						    utils.customlog(Log.ERROR,"Software version: " + swprop +" on device" + Genericprop != null ? Genericprop : Genericprop2  +" not supported yet");
        						    alertbuilder("Unspported", "Your devices specific software version of " + swprop + " is not currently supported","Ok",0);
        					    }
        				    }
        				    else{
            					myDevice = device;
         						if(myDevice.getDeviceDetailsLocation() != null){
    	     						mProgressDialog.dismiss();
    		     					getDeviceDetails(myDevice.getDeviceDetailsLocation());
    			    			}
        			    	}
        				    if(myDevice.getName().equalsIgnoreCase("LG Optimus G"))
        					    setUnlocks();
        				    break;
        			    }
        		    }
        		    else if(Genericprop2 != null){
        			    if(Genericprop2.equalsIgnoreCase(model)){
        				    if(onStock()){
        					     if(device.getFirmware().contains(swprop) || device.getFirmware().contains("any")){
        						    myDevice = device;
        						    if(myDevice.getDeviceDetailsLocation() != null){
            							mProgressDialog.dismiss();
             							getDeviceDetails(myDevice.getDeviceDetailsLocation());
            						}
        	    				}
        		    			else{
        			    			mProgressDialog.dismiss();
        				    		utils.customlog(Log.ERROR,"Software version: " + swprop +" on device" + Genericprop != null ? Genericprop : Genericprop2  +" not supported yet");
        					    	alertbuilder("Unspported", "Your devices specific software version of " + swprop + " is not currently supported","Ok",0);
        					    }
        				     }
        				    else{
        				        myDevice = device;
    						    if(myDevice.getDeviceDetailsLocation() != null){
        							mProgressDialog.dismiss();
         							getDeviceDetails(myDevice.getDeviceDetailsLocation());
    	    					}
        	    			}
        		    		if(myDevice.getName().equalsIgnoreCase("LG Optimus G"))
        			    		setUnlocks();
        				    break;
        			    }
        		    }
        	    }
        	}
        	if(myDevice != null){
        		updateGridView(myDevice);
        	     ListView lv = (ListView) findViewById(R.id.deviceInfo);
        	     String[] lStr;
        	     if(swprop == null){
        	         lStr = new String[]{"Device Name: "+myDevice.getName(),"Device Model: "+myDevice.getModel()};
        	         utils.customlog(Log.VERBOSE,"Device Name: "+myDevice.getName() + "\n" +"Device Model: "+myDevice.getModel());
        	     }
        	     else{
        	    	 lStr = new String[]{"Device Name: "+myDevice.getName(),"Device Model: "+myDevice.getModel(),"Software Version: "+swprop};
        	    	 utils.customlog(Log.VERBOSE,"Device Name: "+myDevice.getName() + "\n" +"Device Model: "+myDevice.getModel() + "\n" + "Software Version: "+swprop);
        	     }
        	     lv.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, lStr));
        	     if(mProgressDialog.isShowing())
        	    	 mProgressDialog.dismiss();
        	     if(myDevice.getBootloaderExploit() == 1)
        	    	 checkLoki();
        	}
        	else{
        		mProgressDialog.dismiss();
    		    if(Genericprop != null)
    			    alertbuilder("Unsupported", "Your device, detected as a " + Genericprop + " is not currently supported", "ok", 1);
    		    else if(Genericprop2 != null)
    			    alertbuilder("Unsupported", "Your device, detected as a " + Genericprop2 + " is not currently supported", "ok", 1);
    		    else
    		    	alertbuilder("Unsupported", "Your device, could not be dected generically or specifically and thus is not currently supported", "ok", 1);
        	}
    		
    	}
    }
    
    /**
     * Set the unlock objects to our local objects
     */
    public void setUnlocks(){
    	for(Action a:myDevice.getActions()){
    		if(a.getName().equalsIgnoreCase("Optimus G Unlock"))
    			ogunlock = a;
    		else if(a.getName().equalsIgnoreCase("Mako Unlock"))
    			ogMakounlock = a;
    	}    	
    }
    
    public void checkLoki(){
    	utils.customlog(Log.VERBOSE, "Checking for loki support");
    	if(myDevice != null && myDevice.getBootloaderExploit() == 1){
   	     if(mProgressDialog.isShowing())
	    	 mProgressDialog.dismiss();
        	mProgressDialog = new ProgressDialog(FreeGee.this);
    	    mProgressDialog.setIndeterminate(true);
    	    mProgressDialog.setCancelable(false);
    	    mProgressDialog.setMessage("Checking for loki support...");
    	    mProgressDialog.show();
			actionOrder = new ArrayList<Action>();
			actionDownloads = new HashMap<String,Boolean>();
    		for(Action action:myDevice.getActions()){
    			if(action.getName().equalsIgnoreCase("loki_check")){
    				loki_check = action;
    				processAction(action);
    				break;
    			}
    		}
    		if(loki_check == null){
    			utils.customlog(Log.ERROR, "Error loki_check action not found");
    		}
    	}
    }
    
    public boolean checkLoki(Action action, String fullPathName){
    	utils.customlog(Log.VERBOSE, "Running check for loki support through zip");
		 CommandCapture command = new CommandCapture(0,"/data/local/tmp/edifier "+ constants.FreeGeeFolder + "/"+action.getZipFile()){
	        @Override
	        public void output(int id, String line)
	        {
	        	utils.customlog(Log.VERBOSE,line);
	            //RootTools.log(constants.LOG_TAG, line);
	            
	        }
		 };
			try {
				RootTools.debugMode = true; //ON
				Shell shell = RootTools.getShell(true,60000);
				shell.add(command);
				commandWait(command);
				int err = command.getExitCode();
				utils.customlog(Log.VERBOSE,"Exit code is: " + err);
				mProgressDialog.dismiss();
				if(err == 0){
					actionsleft--;
					Toast.makeText(this, "Loki support verified succesfully", Toast.LENGTH_LONG).show();
					utils.customlog(Log.VERBOSE,"This device is supported by loki");
					return true;
				}
				else{
					actionsleft--;
					//Toast.makeText(this, "Error code is: " + err, Toast.LENGTH_LONG).show();
					String swvm = "";
					if(swprop != null)
						swvm = " on software version: " + swprop;
					alertbuilder("Error","Your aboot is not supported by loki" + swvm,"ok",0);
					offerAbootEmail();
					return false;
				}
				
			} catch (IOException e) {
				utils.customlog(Log.ERROR,"Edifier not found");
				alertbuilder("Error","Edifier not found.","ok",0);
			} catch (TimeoutException e) {
				utils.customlog(Log.ERROR,"command timed out");
				alertbuilder("Error","Edifier "+action.getName() + " command timed out","ok",0);
			} catch (RootDeniedException e) {
				utils.customlog(Log.ERROR,"No root access!");
				alertbuilder("Error","Please check root access","ok",0);
			} catch (Exception e) {
				utils.customlog(Log.ERROR,"Exception thrown waiting for command to finish");
				alertbuilder("Error","Exception thrown waiting for command to finish","ok",0);
			}
			return false;
    }
    
    /**
     * Run an action
     * @param i Action
     * @param fullPathName Path to zip file
     * @return True if action is success or false if action fails
     */
    public boolean doAction(Action i, String fullPathName){

		 CommandCapture command = new CommandCapture(0,"/data/local/tmp/edifier "+ constants.FreeGeeFolder + "/"+i.getZipFile()){
	        @Override
	        public void output(int id, String line)
	        {
	        	utils.customlog(Log.VERBOSE,line);
	            //RootTools.log(constants.LOG_TAG, line);
	            
	        }
		 };
			try {
				RootTools.debugMode = true; //ON
				Shell shell = RootTools.getShell(true,60000);
				shell.add(command);
				commandWait(command);
				int err = command.getExitCode();
				utils.customlog(Log.VERBOSE,"Exit code is: " + err);
				if(err == 0){
					actionsleft--;
					utils.customlog(Log.VERBOSE,"actionsleft is: " + actionsleft);
					if(actionsleft == 0){
						mProgressDialog.dismiss();
						alertbuilder("Done","The requested action of " + mainAction.getName() +" is complete","Ok",0);
					}
					return true;
				}
				else{
					Toast.makeText(this, "Error code is: " + err, Toast.LENGTH_LONG).show();
					actionsleft--;
					mProgressDialog.dismiss();
					alertbuilder("Error","There was an error running action " + i.getName(),"ok",0);
					offerEmail(i);
					return false;
				}
				
			} catch (IOException e) {
				utils.customlog(Log.ERROR,"Edifier not found");
				alertbuilder("Error","Edifier not found.","ok",0);
			} catch (TimeoutException e) {
				utils.customlog(Log.ERROR,"command timed out");
				alertbuilder("Error","Edifier "+i.getName() + " command timed out","ok",0);
			} catch (RootDeniedException e) {
				utils.customlog(Log.ERROR,"No root access!");
				alertbuilder("Error","Please check root access","ok",0);
			} catch (Exception e) {
				utils.customlog(Log.ERROR,"Exception thrown waiting for command to finish");
				alertbuilder("Error","Exception thrown waiting for command to finish","ok",0);
			}
			return false;
    }
    
    /**
     * Wait for RootTools command to finish
     * @param cmd Command
     */
    private void commandWait(Command cmd) {
        int waitTill = 50;
        int waitTillMultiplier = 2;
        int waitTillLimit = 60000; //7 tries, 6350 msec

        while (!cmd.isFinished() && waitTill<=waitTillLimit) {
            synchronized (cmd) {
                try {
                    if (!cmd.isFinished()) {
                        cmd.wait(waitTill);
                        waitTill *= waitTillMultiplier;
                    }
                } catch (InterruptedException e) {
                    utils.customlog(Log.ERROR,"Error with waiting for command: "+ cmd.toString());
                    alertbuilder("Error","Error with waiting for command: "+ cmd.toString(),"ok",1);
                }
            }
        }
        if (!cmd.isFinished()){
            utils.customlog(Log.ERROR, "Could not finish root command in " + (waitTill/waitTillMultiplier));
        }
    }
    
    /**
     * Setup utilities needed, such as edifier
     * This extract the utilities from assets to /sdcard/freegee and then copies them to /data/local/tmp
     */
    public void setupUtilities(int tries){
		if(!new File(constants.FreeGeeFolder + "/tools/edifier").exists()){
		  InputStream in = null;
		  OutputStream out = null;
		  try {
			// read this file into InputStream
			in = getAssets().open("edifier");
	 
			// write the inputStream to a FileOutputStream
			out = new FileOutputStream(new File(constants.FreeGeeFolder + "/tools/edifier"));
			int read = 0;
			byte[] bytes = new byte[50468];
	 
			while ((read = in.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}	 
		  } catch (IOException e) {
			utils.customlog(Log.ERROR,"Edifier not found in assets");
			alertbuilder("Error!","Can't copy Edifier from assets","Ok",1);
			e.printStackTrace();
		  } finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					// outputStream.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	 
			  }
		  }
		}
		
		if(!new File(constants.FreeGeeFolder + "/tools/keys").exists()){
			  InputStream in = null;
			  OutputStream out = null;
			  try {
				// read this file into InputStream
				in = getAssets().open("keys");
		 
				// write the inputStream to a FileOutputStream
				out = new FileOutputStream(new File(constants.FreeGeeFolder + "/tools/keys"));
				int read = 0;
				byte[] bytes = new byte[50468];
		 
				while ((read = in.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}	 
			  } catch (IOException e) {
				utils.customlog(Log.ERROR,"Keys not found in assets");
				alertbuilder("Error!","Can't copy keys from assets","Ok",1);
				e.printStackTrace();
			  } finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (out != null) {
					try {
						// outputStream.flush();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
		 
				  }
			  }
			}
		
		if(!new File(constants.FreeGeeFolder + "/tools/mkbootimg").exists()){
			  InputStream in = null;
			  OutputStream out = null;
			  try {
				// read this file into InputStream
				in = getAssets().open("mkbootimg");
		 
				// write the inputStream to a FileOutputStream
				out = new FileOutputStream(new File(constants.FreeGeeFolder + "/tools/mkbootimg"));
				int read = 0;
				byte[] bytes = new byte[50468];
		 
				while ((read = in.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}	 
			  } catch (IOException e) {
				utils.customlog(Log.ERROR,"mkbootimg not found in assets");
				alertbuilder("Error!","Can't copy mkbootimg from assets","Ok",1);
				e.printStackTrace();
			  } finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (out != null) {
					try {
						// outputStream.flush();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
		 
				  }
			  }
			}
		
		if(!new File(constants.FreeGeeFolder + "/tools/unpackbootimg").exists()){
			  InputStream in = null;
			  OutputStream out = null;
			  try {
				// read this file into InputStream
				in = getAssets().open("unpackbootimg");
		 
				// write the inputStream to a FileOutputStream
				out = new FileOutputStream(new File(constants.FreeGeeFolder + "/tools/unpackbootimg"));
				int read = 0;
				byte[] bytes = new byte[50468];
		 
				while ((read = in.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}	 
			  } catch (IOException e) {
				utils.customlog(Log.ERROR,"unpackbootimg not found in assets");
				alertbuilder("Error!","Can't copy unpackbootimg from assets","Ok",1);
				e.printStackTrace();
			  } finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (out != null) {
					try {
						// outputStream.flush();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
		 
				  }
			  }
			}
		
		if(!new File(constants.FreeGeeFolder + "/tools/busybox").exists()){
			  InputStream in = null;
			  OutputStream out = null;
			  try {
				// read this file into InputStream
				in = getAssets().open("busybox");
		 
				// write the inputStream to a FileOutputStream
				out = new FileOutputStream(new File(constants.FreeGeeFolder + "/tools/busybox"));
				int read = 0;
				byte[] bytes = new byte[50468];
		 
				while ((read = in.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}	 
			  } catch (IOException e) {
				utils.customlog(Log.ERROR,"busybox not found in assets");
				alertbuilder("Error!","Can't copy busybox from assets","Ok",1);
				e.printStackTrace();
			  } finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (out != null) {
					try {
						// outputStream.flush();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
		 
				  }
			  }
			}
		
		CommandCapture command = new CommandCapture(0, "mkdir -p /data/local/tmp/"){
	        @Override
	        public void output(int id, String line)
	        {
	        	utils.customlog(Log.VERBOSE,line);
	            //RootTools.log(constants.LOG_TAG, line);
	            
	        }
		 };
		try {
			RootTools.getShell(true).add(command).isFinished();
		} catch (IOException e) {
			utils.customlog(Log.ERROR,"IOException when making /data/local/tmp dir");
			alertbuilder("Error!","Can't make /data/local/tmp","Ok",1);
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR,"mkdir timeout when making /data/local/tmp dir");
			alertbuilder("Error!","Mkdir timed out","Ok",1);
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR,"No root access!");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}
		
		command = new CommandCapture(0,CP_COMMAND + " /sdcard/freegee/tools/edifier /data/local/tmp/ && chmod 755 /data/local/tmp/edifier"){
	        @Override
	        public void output(int id, String line)
	        {
	        	utils.customlog(Log.VERBOSE,line);
	            //RootTools.log(constants.LOG_TAG, line);
	            
	        }
		 };
		try {
			RootTools.getShell(true).add(command).isFinished();
		} catch (IOException e) {
			utils.customlog(Log.ERROR,"Edifier not found in assets");
			alertbuilder("Error!","Can't open edifier","Ok",1);
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR,"Chmod timed out");
			alertbuilder("Error!","Chmod timed out","Ok",1);
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR,"No root access!");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}
		
		command = new CommandCapture(0,CP_COMMAND + " /sdcard/freegee/tools/keys /data/local/tmp/ && chmod 644 /data/local/tmp/keys"){
	        @Override
	        public void output(int id, String line)
	        {
	        	utils.customlog(Log.VERBOSE,line);
	            //RootTools.log(constants.LOG_TAG, line);
	            
	        }
		 };
		try {
			RootTools.getShell(true).add(command).isFinished();
		} catch (IOException e) {
			utils.customlog(Log.ERROR,"keys not found in assets");
			alertbuilder("Error!","Can't open keys","Ok",1);
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR,"Chmod timed out");
			alertbuilder("Error!","Chmod timed out","Ok",1);
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR,"No root access!");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}
		
		command = new CommandCapture(0,CP_COMMAND + " /sdcard/freegee/tools/mkbootimg /data/local/tmp/ && chmod 755 /data/local/tmp/mkbootimg"){
	        @Override
	        public void output(int id, String line)
	        {
	        	utils.customlog(Log.VERBOSE,line);
	            //RootTools.log(constants.LOG_TAG, line);
	            
	        }
		 };
		try {
			RootTools.getShell(true).add(command).isFinished();
		} catch (IOException e) {
			utils.customlog(Log.ERROR,"mkbootimg not found in assets");
			alertbuilder("Error!","Can't open mkbootimg","Ok",1);
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR,"Chmod timed out");
			alertbuilder("Error!","Chmod timed out","Ok",1);
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR,"No root access!");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}
		
		command = new CommandCapture(0,CP_COMMAND + " /sdcard/freegee/tools/unpackbootimg /data/local/tmp/ && chmod 755 /data/local/tmp/unpackbootimg"){
	        @Override
	        public void output(int id, String line)
	        {
	        	utils.customlog(Log.VERBOSE,line);
	            //RootTools.log(constants.LOG_TAG, line);
	            
	        }
		 };
		try {
			RootTools.getShell(true).add(command).isFinished();
		} catch (IOException e) {
			utils.customlog(Log.ERROR,"unpackbootimg not found in assets");
			alertbuilder("Error!","Can't open unpackbootimg","Ok",1);
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR,"Chmod timed out");
			alertbuilder("Error!","Chmod timed out","Ok",1);
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR,"No root access!");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}
		
		command = new CommandCapture(0,CP_COMMAND + " /sdcard/freegee/tools/busybox /data/local/tmp/ && chmod 755 /data/local/tmp/busybox"){
	        @Override
	        public void output(int id, String line)
	        {
	        	utils.customlog(Log.VERBOSE,line);
	            //RootTools.log(constants.LOG_TAG, line);
	            
	        }
		 };
		try {
			RootTools.getShell(true).add(command).isFinished();
		} catch (IOException e) {
			utils.customlog(Log.ERROR,"busybox not found in assets");
			alertbuilder("Error!","Can't open busybox","Ok",1);
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR,"Chmod timed out");
			alertbuilder("Error!","Chmod timed out","Ok",1);
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR,"No root access!");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}
		
    	command = new CommandCapture(0,"ls /data/local/tmp/edifier"){
	        @Override
	        public void output(int id, String line)
	        {
	        	utils.customlog(Log.VERBOSE,line);
	            //RootTools.log(constants.LOG_TAG, line);
	            
	        }
		 };
		Shell shell = null;
		try {
			shell = RootTools.getShell(true);
			shell.add(command);
			commandWait(command);
		} catch (IOException e) {
			utils.customlog(Log.ERROR, "IOException on ls /data/local/tmp/edifier!");
			alertbuilder("Error!","IOException checking if edifier copied fine","Ok",1);
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR, "Timed out ls /data/local/tmp/edifier!");
			alertbuilder("Error!","Timed out looking for /data/local/tmp/edifier","Ok",1);
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR, "Root Denined!");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}		
		int err = command.getExitCode();
		if(err != 0){
			if(tries<=2)
			    setupUtilities(tries+1);
			else{
				utils.customlog(Log.ERROR,"Tried twice to copy utlities and it failed");
				alertbuilder("Error","Tried twice to copy utlities and it failed","ok",1);
			}
		}
		command = new CommandCapture(0,"ls /data/local/tmp/busybox"){
		    @Override
		    public void output(int id, String line)
		    {
		    	utils.customlog(Log.VERBOSE,line);
		        //RootTools.log(constants.LOG_TAG, line);
		        }
	    };
		try {
		    shell = RootTools.getShell(true);
			shell.add(command);
			commandWait(command);
		} catch (IOException e) {
			utils.customlog(Log.ERROR, "IOException on ls /data/local/tmp/busybox!");
			alertbuilder("Error!","IOException checking if busybox copied fine","Ok",1);
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR, "Timed out ls /data/local/tmp/busybox!");
			alertbuilder("Error!","Timed out looking for /data/local/tmp/busybox","Ok",1);
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR, "Root Denined!");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}
		err = command.getExitCode();
		if(err != 0){
			if(tries<=2)
			    setupUtilities(tries+1);
			else{
				utils.customlog(Log.ERROR,"Tried twice to copy utlities and it failed");
				alertbuilder("Error","Tried twice to copy utlities and it failed","ok",1);
			}
		}
		
		command = new CommandCapture(0,"ls /data/local/tmp/busybox"){
		    @Override
		    public void output(int id, String line)
		    {
		    	utils.customlog(Log.VERBOSE,line);
		        //RootTools.log(constants.LOG_TAG, line);
		        }
	    };
		try {
		    shell = RootTools.getShell(true);
			shell.add(command);
			commandWait(command);
		} catch (IOException e) {
			utils.customlog(Log.ERROR, "IOException on ls /data/local/tmp/keys!");
			alertbuilder("Error!","IOException checking if keys copied fine","Ok",1);
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR, "Timed out ls /data/local/tmp/keys!");
			alertbuilder("Error!","Timed out looking for /data/local/tmp/keys","Ok",1);
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR, "Root Denined!");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}
		err = command.getExitCode();
		if(err != 0){
			if(tries<=2)
			    setupUtilities(tries+1);
			else{
				utils.customlog(Log.ERROR,"Tried twice to copy utlities and it failed");
				alertbuilder("Error","Tried twice to copy utlities and it failed","ok",1);
			}
		}
		
		command = new CommandCapture(0,"ls /data/local/tmp/mkbootimg"){
		    @Override
		    public void output(int id, String line)
		    {
		    	utils.customlog(Log.VERBOSE,line);
		        //RootTools.log(constants.LOG_TAG, line);
		        }
	    };
		try {
		    shell = RootTools.getShell(true);
			shell.add(command);
			commandWait(command);
		} catch (IOException e) {
			utils.customlog(Log.ERROR, "IOException on ls /data/local/tmp/mkbootimg!");
			alertbuilder("Error!","IOException checking if mkbootimg copied fine","Ok",1);
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR, "Timed out ls /data/local/tmp/mkbootimg!");
			alertbuilder("Error!","Timed out looking for /data/local/tmp/mkbootimg","Ok",1);
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR, "Root Denined!");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}
		err = command.getExitCode();
		if(err != 0){
			if(tries<=2)
			    setupUtilities(tries+1);
			else{
				utils.customlog(Log.ERROR,"Tried twice to copy utlities and it failed");
				alertbuilder("Error","Tried twice to copy utlities and it failed","ok",1);
			}
		}
		
		command = new CommandCapture(0,"ls /data/local/tmp/unpackbootimg"){
		    @Override
		    public void output(int id, String line)
		    {
		    	utils.customlog(Log.VERBOSE,line);
		        //RootTools.log(constants.LOG_TAG, line);
		        }
	    };
		try {
		    shell = RootTools.getShell(true);
			shell.add(command);
			commandWait(command);
		} catch (IOException e) {
			utils.customlog(Log.ERROR, "IOException on ls /data/local/tmp/unpackbootimg!");
			alertbuilder("Error!","IOException checking if unpackbootimg copied fine","Ok",1);
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR, "Timed out ls /data/local/tmp/unpackbootimg!");
			alertbuilder("Error!","Timed out looking for /data/local/tmp/unpackbootimg","Ok",1);
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR, "Root Denined!");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}
		err = command.getExitCode();
		if(err != 0){
			if(tries<=2)
			    setupUtilities(tries+1);
			else{
				utils.customlog(Log.ERROR,"Tried twice to copy utlities and it failed");
				alertbuilder("Error","Tried twice to copy utlities and it failed","ok",1);
			}
		}
	}
    
    public boolean checkForBackups(){
    	if(myDeviceDetails == null){
    	    alertbuilder("Error","Can not check if backups exists, restoring might fail if partition backups are not in /sdcard/freegee. Will continue with attempt anyway.","Ok",0);
    	    return true;
    	}
    	else{
    	    for(Partition partition: myDeviceDetails.getPartitions()){
    	    	if(partition.getRequiredBackup()){
    	    		if(! new File(constants.FreeGeeFolder+"/"+partition.getName()+"-backup.img").exists()){
    	    			utils.customlog(Log.ERROR,"Backup of "+partition.getName() + " is not found in "+constants.FreeGeeFolder+"/"+partition.getName()+"-backup.img. Aborting restoring backups");
    	    			alertbuilder("Backup not found","Backup of "+partition.getName() + " is not found. Aborting restoring backups","Ok",0);
    	    			return false;
    	    		}
    	    	}
    	    }
    	}
    	return true;
    }

    
    /**
     * Process an action, iterate through all dependencies and start downloads
     * @param action
     */
    public void processAction(Action action){
    	if(action.getName().equalsIgnoreCase("Re-lock (restore backups)")){
    		if(myDeviceDetails != null)
    			if(!checkForBackups()){
    				mProgressDialog.dismiss();
    			    return;
    			}
    	}
    	if((action.getStockOnly() && onStock()) || !action.getStockOnly()){
    	    actionsleft++;
    	    if(action.getDependencies() != null && !action.getDependencies().isEmpty()){
    		    for(Action i:action.getDependencies()){
    			    processAction(i);
    		    }
    	    }
    	    
    	    if(myDevice.getName().equalsIgnoreCase("LG Optimus G")){
    	    	if(makoUnlock && action.getName().equalsIgnoreCase("Optimus G Unlock")){
    	    		action = ogMakounlock;
    	    	}
    	    	else if(!makoUnlock && action.getName().equalsIgnoreCase("Mako Unlock")){
    	    		action = ogunlock;
    	    	}
    	    }
    	    actionOrder.add(action);
    	    Collections.sort(actionOrder);
    	    utils.customlog(Log.VERBOSE,"Current actions are: " + printList(actionOrder));
    	    String azipS = constants.FreeGeeFolder + "/"+action.getZipFile();
    	    File azipF = new File(azipS);
    	    if(azipF.exists()){
    	    	if(utils.checkMD5(action.getMd5sum(), azipF)){
    	    	    utils.customlog(Log.VERBOSE,"Using predownloaded "+action.getName());
    	    	    actionDownloads.put(action.getName(),true);
    	    	    if(actionDownloads.size() == actionsleft && allActionsDownloads()){
    	    		    if(action.getName().equalsIgnoreCase("loki_check"))
    	    		    	checkLoki(action,azipS);
    	    		    else
    	    		    	doAllActions();
    	    		    }
    	    	}
    	    	else{
    	    		utils.customlog(Log.VERBOSE,"Downloading "+action.getName());
    	    	    actionDownloads.put(action.getName(),false);
    	    		startDownload(action);
    	    	}
    	    }
	    	else{
	    		utils.customlog(Log.VERBOSE,"Downloading "+action.getName());
	    	    actionDownloads.put(action.getName(),false);
	    		startDownload(action);
	    	}
    	}
    }
    
    private String printList(List<Action> actionList) {
		String string ="";
		for(Action i:actionList){
			string += i.getName() + ", ";
		}
		return string;
	}

    private String printList(HashMap<String,Boolean> actionMap) {
		String string ="";
		for(String i:actionMap.keySet()){
			string += i + ", ";
		}
		return string;
	}
    
	private boolean allActionsDownloads() {
		if(actionDownloads != null){
			for(String actionName:actionDownloads.keySet()){
				if(!actionDownloads.get(actionName)){
					utils.customlog(Log.VERBOSE,actionName+ " is not marked as downloaded.");
					return false;
				}
			}
			return true;
		}
		else
		    return false;
	}

	/**
     * Start download by sending action to our DownloadReceiver
     * @param action
     */
    public void startDownload(Action action){
        Intent intent = new Intent(this, DownloadReceiver.class);
        intent.setAction(DownloadReceiver.ACTION_START_DOWNLOAD);
        intent.putExtra(DownloadReceiver.DEVICE_ACTION, (Serializable) action);
        sendBroadcast(intent);    	
    }
    
    public File getAbootImage(){
    	String ending = ".img";
    	if(myDevice !=null && swprop != null)
    		ending = "-"+myDevice.getModel()+"_"+swprop+".img";
    	else if(myDevice !=null)
    		ending = "-"+myDevice.getModel()+".img";
    	String abootOut = constants.FreeGeeFolder+"aboot"+ending;
		Command command = new CommandCapture(0,"/data/local/tmp/busybox dd if="+"/dev/block/platform/msm_sdcc.1/by-name/"+"aboot" + " of="+abootOut){
	        @Override
	        public void output(int id, String line)
	        {
	        	utils.customlog(Log.VERBOSE,line);
	            //RootTools.log(constants.LOG_TAG, line);
	            
	        }
		 };
		try {
			RootTools.getShell(true).add(command).isFinished();
		} catch (IOException e) {
			utils.customlog(Log.ERROR,"busybox not found in assets");
			alertbuilder("Error!","Can't open busybox","Ok",1);
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR,"Chmod timed out");
			alertbuilder("Error!","Chmod timed out","Ok",1);
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR,"No root access!");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}
		return new File(abootOut);
    }
    
    /**
     * Create an action for devices xml and download it
     */
    public void getDevices(){
    	mProgressDialog = new ProgressDialog(FreeGee.this);
	    mProgressDialog.setIndeterminate(true);
	    mProgressDialog.setCancelable(false);
	    mProgressDialog.setMessage("Downloading supported device list...");
	    mProgressDialog.show();
	    
        Action dAction = new Action();
        dAction.setName("devices2.xml");
        dAction.setZipFile("devices2.xml");
        dAction.setZipFileLocation("devices2.xml");
        dAction.setMd5sum("nono");
        File devicesXML = new File(constants.DEVICE_XML);
        if(devicesXML.exists()){
        	devicesXML.delete();
        }
        startDownload(dAction);
    }
    
    public void getDeviceDetails(String DeviceDetailsLocation){
    	mProgressDialog = new ProgressDialog(FreeGee.this);
	    mProgressDialog.setIndeterminate(true);
	    mProgressDialog.setCancelable(false);
	    mProgressDialog.setMessage("Downloading additional device details...");
	    mProgressDialog.show();
	    
        Action ddAction = new Action();
        String fileName = new File(DeviceDetailsLocation).getName().toString();
        ddAction.setName(fileName);
        ddAction.setZipFile(fileName);
        ddAction.setZipFileLocation(DeviceDetailsLocation);
        ddAction.setMd5sum("nono");
        File deviceDetailsXML = new File(constants.FreeGeeFolder+"/"+fileName);
        if(deviceDetailsXML.exists()){
        	deviceDetailsXML.delete();
        }
        startDownload(ddAction);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_free_gee, menu);
        if(myDevice != null && !myDevice.getName().equalsIgnoreCase("LG Optimus G"))
        	menu.removeItem(R.id.menu_reboot_bootloader);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:{
        		Intent newActivity = new Intent(this, settings.class);
                startActivity(newActivity);
                return true;
                }
            case R.id.menu_reboot_recovery:{
        		if(!utils.rebootRecovery())
        			Toast.makeText(this, "Reboot to Recovery Failed", Toast.LENGTH_LONG).show();
                return true;
                }
            case R.id.menu_reboot_bootloader:{
        		if(!utils.rebootBootloader())
        			Toast.makeText(this, "Reboot to Bootloader Failed", Toast.LENGTH_LONG).show();
                return true;
                }
            case R.id.menu_shutdown:{
        		if(!utils.Shutdown())
        			Toast.makeText(this, "Shutdown Failed", Toast.LENGTH_LONG).show();
                return true;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * Offer to email bug report
     * @param action
     */
    public void offerEmail(final Action action){
    	final String subject = "FreeGee error on " + myDevice.getModel() + " performing " + action.getName();
    	final String message = "There was an error performing " + action.getName()+ ". " + "Please see the attached logcat.";
    	final Activity activity = this;
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
	    
    	// set title
    	alertDialogBuilder.setTitle("Email log of Error?");

    	// set dialog message
    	alertDialogBuilder
    	.setMessage("Would you like to email a log of your error, so it can be fixed?")
    	.setCancelable(false)
    	.setPositiveButton("Send Email",new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog,int id) {
        	utils.sendEmail(activity, message ,subject,myDevice);
    	}
    	})
    	.setNegativeButton("No Thank You", new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog,int id) {
    	}
    	});

    	// create alert dialog
    	AlertDialog alertDialog = alertDialogBuilder.create();

    	// show it
    	alertDialog.show();
    }

    public void offerAbootEmail(){
    	final String subject = "FreeGee aboot not supported on " + myDevice.getModel() + ", swversion: " + swprop;
    	final String message = "Aboot is not supported by loki for  " + myDevice.getModel() + ", swversion: " + swprop + ". Please see the attached aboot image.";
    	final Activity activity = this;
    	final File AbootImage = getAbootImage();
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
	    
    	// set title
    	alertDialogBuilder.setTitle("Email Aboot image for support?");

    	// set dialog message
    	alertDialogBuilder
    	.setMessage("Would you like to email a copy of your aboot image for loki support?")
    	.setCancelable(false)
    	.setPositiveButton("Send Email",new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog,int id) {
        	utils.sendAbootEmail(activity, AbootImage ,message ,subject,myDevice);
    	}
    	})
    	.setNegativeButton("No Thank You", new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog,int id) {
    	}
    	});

    	// create alert dialog
    	AlertDialog alertDialog = alertDialogBuilder.create();

    	// show it
    	alertDialog.show();
    }
    
    /**
     * Generic class to open an alertDialog with given title, message and button.
     * @param title Title for alertDialog
     * @param text Message
     * @param Button Text for button
     * @param exits 0 does not exit application, 1 forces exit
     */
    public void alertbuilder(String title, String text, String Button, final int exits){
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    	    
    	// set title
    	alertDialogBuilder.setTitle(title);

    	// set dialog message
    	alertDialogBuilder
    	.setMessage(text)
    	.setCancelable(false)
    	.setPositiveButton(Button,new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog,int id) {
    	// if this button is clicked, close
    	// current activity
    		if(exits==1){
    			FreeGee.this.finish();
    			}
    	}
    	});

    	// create alert dialog
    	AlertDialog alertDialog = alertDialogBuilder.create();

    	// show it
    	alertDialog.show();

    	}

    /**
     * Is FreeGee the main activity?
     * @return True or False
     */
	public static boolean isMainActivityActive() {
	    return mMainActivityActive;
	}
}