/** @author Seth Shelnutt
 * @License GPLv3 or later
 * All source code is released free and openly by Seth Shelnutt under the terms of GPLv3 or later, 2013 
 * */

package edu.shell.freegee;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.stericson.RootTools.RootTools;
import edu.shell.freegee.R;
import edu.shell.freegee.device.Action;
import edu.shell.freegee.device.Device;

import edu.shell.freegee.utils.FileDialog;
import edu.shell.freegee.utils.constants;
import edu.shell.freegee.utils.tools;
import edu.shell.freegee.utils.utils;
import edu.shell.freegee.view.FreegeeFragment;
import edu.shell.freegee.view.FreegeePager;
import edu.shell.freegee.view.Notices;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;

import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * @author Seth Shelnutt
 */
public class FreeGee extends Activity implements ActionBar.TabListener {

    
    private static ProgressDialog mProgressDialog;
    private static FileDialog fileDialog;
    public static boolean isSpecial;
    private ArrayList<Object> mButtons = new ArrayList<Object>();
        
    private static boolean mMainActivityActive;

	private HashMap<String,Integer> downloadTries = new HashMap<String,Integer>();
    
    private Properties buildProp = new Properties();
    
    private int actionsleft = 0;
    private List<Action> actionOrder = new ArrayList<Action>();	
    private HashMap<String, Boolean> actionDownloads = new HashMap<String,Boolean>();
    private Action mainAction;
    private boolean ActionSuccess = true;
    
    private File updateZipFile;
    
    public static String PACKAGE_NAME;
    private AdView adView;
    
    private Device myDevice;
    
    private String appVersion;
    
	File logFile = new File(constants.LOG_FILE);
	
	private ArrayList<FreegeeFragment> fragList = new ArrayList<FreegeeFragment>();
	private int currentPosition;
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	FreegeePager mSectionsPagerAdapter;
	
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	/**
	 * Used to show the Change Log as needed on opening of new version
	 */
    private void showChangeLog(boolean show){
        ChangeLog cl = new ChangeLog(this);
        appVersion = cl.getThisVersion();
        if (cl.firstRun() && show)
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
     * Checks for if the Internet is accessible or not
     * @return
     */
    public boolean isOnline() {
        ConnectivityManager cm =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && 
           cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
    
    /**
     * 
     * @return yyyy-MM-dd HH:mm:ss formate date as string
     */
    public static String getCurrentTimeStamp(){
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);
            String currentTimeStamp = dateFormat.format(new Date()); // Find todays date

            return currentTimeStamp;
        } catch (Exception e) {
            utils.customlog(Log.ERROR, "Exception occured trying to get curent timestamp");
            return null;
        }
    }
    
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainActivityActive = false;
        //setContentView(R.layout.activity_freegee);
        setContentView(R.layout.activity_main);
        
     // Set up the action bar.
     		final ActionBar actionBar = getActionBar();
     		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

     		fragList.add(FreegeeFragment.newInstance(fragList.size()+1,"Device Details"));
     		fragList.add(FreegeeFragment.newInstance(fragList.size()+1,"Recoveries"));
     		// Create the adapter that will return a fragment for each of the three
     		// primary sections of the activity.
     		mSectionsPagerAdapter = new FreegeePager(getFragmentManager(),fragList);

     		// Set up the ViewPager with the sections adapter.
     		mViewPager = (ViewPager) findViewById(R.id.pager);
     		mViewPager.setAdapter(mSectionsPagerAdapter);

     		// When swiping between different sections, select the corresponding
     		// tab. We can also use ActionBar.Tab#select() to do this if we have
     		// a reference to the Tab.
     		mViewPager
     				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
     					@Override
     					public void onPageSelected(int position) {
     						actionBar.setSelectedNavigationItem(position);
     						currentPosition = position;
     					}
     				});
     		
     		// For each of the sections in the app, add a tab to the action bar.
    		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
    			// Create a tab with text corresponding to the page title defined by
    			// the adapter. Also specify this Activity object, which implements
    			// the TabListener interface, as the callback (listener) for when
    			// this tab is selected.
    			actionBar.addTab(actionBar.newTab()
    					.setText(mSectionsPagerAdapter.getPageTitle(i))
    					.setTabListener(this));
    		}
        PACKAGE_NAME = getApplicationContext().getPackageName();
        Log.v(constants.LOG_TAG,"FreeGee dir is: "+constants.FreeGeeFolder);
    	File freegeef=new File(constants.FreeGeeFolder);
		  if(!freegeef.exists()){
			  freegeef.mkdirs();
		  }
	    File freegeeft=new File(constants.FreeGeeFolder+"/tools");
		  if(!freegeeft.exists()){
			  freegeeft.mkdirs();
		  }
			
			//Move log if it exists, keep a backup copy just incase one needs to report old error but reruns freegee
		if(!logFile.exists()){
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				utils.customlog(Log.ERROR,"IOException trying to open log file");
				alertbuilder("Error","There was an error tring to open the log file. The app will continue but debugging will not be avaliable","ok",0);
			}
		}
		utils.customlog(Log.INFO,getCurrentTimeStamp());
		showChangeLog(false);
		utils.customlog(Log.INFO,"FreeGee version: " + appVersion);
		utils.customlog(Log.INFO,"FreeGee paid: " + freeVersion());
		utils.customlog(Log.VERBOSE,"FreeGee dir is: "+constants.FreeGeeFolder);
		
		if (!RootTools.isAccessGiven()) {
			utils.customlog(Log.ERROR, "Root Denined! Can't get root access.");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}
		
		new tools().new setupTools().execute(this);
			
		if(utils.getBatteryLevel(this) < 15.0 && !(utils.getBatteryCharging(this) && utils.getBatteryLevel(this) >= 10.0) )
			alertbuilder("Error!","Your batter is too low to do anything, please charge it or connect an ac adapter","OK",1);
		

		
		showChangeLog(true);
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
    	     //processAction(a);
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
        
    }
    
    private boolean actionDownloadsContains(Action i) {
		for(String actionName:actionDownloads.keySet()){
			if(actionName.equalsIgnoreCase(i.getName()))
				return true;
		}
		return false;
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
		for(String a:actionMap.keySet()){
			string += a + ", ";
		}
		return string;
	}
    
	private boolean allActionsDownloads() {
		if(actionDownloads != null){
			for(boolean action:actionDownloads.values()){
				if(!action){
					//utils.customlog(Log.VERBOSE,actionName+ " is not marked as downloaded.");
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
        dAction.setName(constants.DEVICE_XML_NAME);
        dAction.setZipFile(constants.DEVICE_XML_NAME);
        dAction.setZipFileLocation(constants.DEVICE_XML_NAME);
        dAction.setMd5sum("nono");
        File devicesXML = new File(constants.DEVICE_XML);
        if(devicesXML.exists()){
        	devicesXML.delete();
        }
        startDownload(dAction);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_free_gee, menu);
        if(myDevice != null && !myDevice.getName().equalsIgnoreCase("LG Optimus G"))
        	menu.removeItem(R.id.menu_reboot_bootloader);
/*        if(myDevice != null && !(myDevice.getBootloaderExploit() == 1))
        	menu.removeItem(R.id.menu_update_zip);*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
/*            case R.id.menu_settings:{
        		Intent newActivity = new Intent(this, settings.class);
                startActivity(newActivity);
                return true;
                }*/
            case R.id.menu_update_zip:{
            	File mPath = new File(constants.getSDCARD());
                fileDialog = new FileDialog(this, mPath, ".zip");
                fileDialog.setFileEndsWith(".zip");
                fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                    public void fileSelected(File file) {
                    	updateZipFile = file;
                        utils.customlog(Log.DEBUG, "zip file to update: " + updateZipFile.toString());
                        fileDialog.dismiss();
                        //new updateLokiZip().execute(updateZipFile);
                    }
                });
                fileDialog.showDialog();
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
    
/*	private class updateLokiZip extends AsyncTask<File, Void, Boolean> {
		
		protected void onPreExecute(){
		    mProgressDialog = new ProgressDialog(FreeGee.this);      
	        mProgressDialog.setIndeterminate(true);
	        mProgressDialog.setCancelable(false);
	        mProgressDialog.setMessage("Copying selected zip file for updating...");
	        mProgressDialog.show();
		}
	    *//** The system calls this to perform work in a worker thread and
	      * delivers it the parameters given to AsyncTask.execute() *//*
	    protected Boolean doInBackground(File... updateZip) {
	        return utils.updateZip(updateZip[0]);
	    }
	    
	    *//** The system calls this to perform work in the UI thread and delivers
	      * the result from doInBackground() *//*
	    protected void onPostExecute(Boolean result) {
	        if(result){
            	mProgressDialog.setMessage("Updating selected zip with newer loki support...");
	            mainAction = utils.findAction(myDevice.getActions(), "loki_update_zip");
	            if(mainAction != null){
				    actionOrder = new ArrayList<Action>();
				    actionDownloads = new HashMap<String,Boolean>();
	                processAction(mainAction);
	            }
	            else{
	            	mProgressDialog.dismiss();
	            	utils.customlog(Log.ERROR,"Could not find the loki_update_zip action for your device.");
	            	alertbuilder("Error","Could not find the loki_update_zip action for your device.","ok",0);
	            }
	        }
	        else{
	        	mProgressDialog.dismiss();
	        	utils.customlog(Log.ERROR,"There was an error copying the zip file to be updated");
	        	alertbuilder("Error","There was an error copying the zip file you requested to be updated","ok",0);
	        }
			
	    }
	}*/

	/**
     * Offer to email bug report
     * @param action
     *//*
    public void offerEmail(final Action action){
    	final String subject = "FreeGee (" + appVersion + ") " + " error on " + myDevice.getModel() + " performing " + action.getName();
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
    }*/

/*    public void offerAbootEmail(){
    	final String subject = "FreeGee (" + appVersion + ") " + " aboot not supported on " + myDevice.getModel() + ", swversion: " + swprop;
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
    }*/
    
    /**
     * Generic class to open an alertDialog with given title, message and button.
     * @param title Title for alertDialog
     * @param text Message
     * @param Button Text for button
     * @param exits 0 does not exit application, 1 forces exit
     */
    public void alertbuilder(String title, String text, String Button, final int exits){
    	Notices.alertbuilder(title, text, Button, exits, this);
    }

    /**
     * Is FreeGee the main activity?
     * @return True or False
     */
	public static boolean isMainActivityActive() {
	    return mMainActivityActive;
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}
}