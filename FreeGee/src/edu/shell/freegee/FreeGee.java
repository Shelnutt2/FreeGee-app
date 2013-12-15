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
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

/*import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;*/

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;

import edu.shell.freegee.R;
import edu.shell.freegee.utils.constants;
import edu.shell.freegee.utils.utils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.ListView;
import android.widget.Toast;
import android.graphics.PorterDuff;

@SuppressLint("SdCardPath")
public class FreeGee extends Activity implements OnClickListener {
   
    public static final int DIALOG_ACTION_PROGRESS = 0;

    private Device myDevice;
    
    private static ProgressDialog mProgressDialog;
    public static boolean isSpecial;
    private ArrayList<Object> mButtons = new ArrayList<Object>();
    private ArrayList<Device> DeviceList;
        
    private static boolean mMainActivityActive;


	private static String CP_COMMAND;
	private HashMap<String,Integer> downloadTries = new HashMap<String,Integer>();
    
    private Properties buildProp = new Properties();
    
    private int actionsleft = 0;
    private Action mainAction;
    private boolean ActionSuccess = true;
    private String swprop;
    
    private boolean makoUnlock = true;
    private Action ogunlock;
    private Action ogMakounlock;
    
	File logFile = new File(constants.LOG_FILE);
    
    @Override
    public void onResume(){
    	super.onResume();
        mMainActivityActive = true;
    	checkForDownloadCompleted(getIntent());
    }

    private void showChangeLog(){
        ChangeLog cl = new ChangeLog(this);
        if (cl.firstRun())
            cl.getLogDialog().show();
    }
    
    private boolean findCP(){
    	CommandCapture command = new CommandCapture(0,"ls /system/bin/cp");
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
			utils.customlog(Log.VERBOSE,"CP_COMMAND is " + CP_COMMAND);
			return true;
		}
		else{
			command = new CommandCapture(0,"ls /system/xbin/cp");
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
				utils.customlog(Log.VERBOSE,"CP_COMMAND is " + CP_COMMAND);
				return true;
			}
		}
		
    	return false;
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainActivityActive = false;
        setContentView(R.layout.activity_freegee);
    	File freegeef=new File( "/sdcard"+"/freegee");
		  if(!freegeef.exists()){
			  freegeef.mkdirs();
		  }
	    File freegeeft=new File( "/sdcard"+"/freegee"+"/tools");
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
			if(!RootTools.isBusyboxAvailable()){
				utils.customlog(Log.ERROR, "Buysbox no found!");
				alertbuilder("Error!","BusyBox not installed. Please install it now","Ok",0);
				RootTools.offerBusyBox(this);
			}
			CP_COMMAND="busybox cp";
		}
		utils.customlog(Log.VERBOSE,"CP_COMMAND is " + CP_COMMAND);
		
		if(utils.getBatteryLevel(this) < 15.0)
			alertbuilder("Error!","Your batter is too low to do anything, please charge it or connect an ac adapter","OK",1);
		
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
		
		setupUtilities();
		showChangeLog();		
        getDevices();
       
        checkForDownloadCompleted(getIntent());
		GridView gridView = (GridView) findViewById(R.id.main_gridview);
		gridView.setAdapter(new ButtonAdapter(mButtons));
    }
    
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
    
    public boolean onStock(){
    	if(swprop != null)
    		return true;
    	else
    		return false;
    	
    }

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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(intent.hasExtra(constants.DOWNLOAD_ERROR)){
        	mProgressDialog.dismiss();
        	alertbuilder("Error","There was an error downloading the necessary files","ok",0);
        }
        else
            checkForDownloadCompleted(intent);
    }
    
    private void checkForDownloadCompleted(Intent intent) {
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

        String fileName = new File(fullPathName).getName();
        if(fileName.equalsIgnoreCase("devices.xml")){
        	if(unSerializeDevices())
        	    matchDevice();
        }
        else{
        	if(myDevice != null && myDevice.getActions() != null){
        	    if(ActionSuccess){
        	        utils.customlog(Log.VERBOSE,"Matching action");
                    ArrayList<Action> actions = myDevice.getActions();
                    for(Action i:actions){
        	            if (i.getZipFile().equalsIgnoreCase(fileName)){
        	            	if(utils.checkMD5(i.getMd5sum(), new File(fullPathName))){
            	                ActionSuccess = doAction(i,fullPathName);
        	            	}
        	            	else{
        	            		int count;
        	            		if(downloadTries.containsKey(i.getName()))
        	            			count = downloadTries.get(i.getName());        	            		
        	            		else
        	            			count = 1;
        	            		downloadTries.put(i.getName(), count);
        	            		Toast.makeText(this, "md5sum mismatch for "+i.getName()+". Redownloading", Toast.LENGTH_LONG).show();
        	            		startDownload(i);
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
    		String model = device.getModel();
    		if(prop != null){
    			if(prop.equalsIgnoreCase(model)){
    				myDevice = device;
    				if(myDevice.getName().equalsIgnoreCase("LG Optimus G"))
    					setUnlocks();
    				break;
    			}
    		}
    		if(prop2 != null){
    			if(prop2.equalsIgnoreCase(model)){
    				myDevice = device;
    				if(myDevice.getName().equalsIgnoreCase("LG Optimus G"))
    					setUnlocks();
    				break;
    			}
    		}
    	}
    	if(myDevice != null){
    		updateGridView(myDevice);
    	     ListView lv = (ListView) findViewById(R.id.deviceInfo);
    	     String[] lStr;
    	     if(swprop == null){
    	         lStr = new String[]{"Device Name: "+myDevice.getName(),"Device Model: "+myDevice.getModel()};
    	     }
    	     else{
    	    	 lStr = new String[]{"Device Name: "+myDevice.getName(),"Device Model: "+myDevice.getModel(),"Software Version: "+swprop};
    	     }
    	     lv.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, lStr));
    	}
    	else{
    		mProgressDialog.dismiss();
    		alertbuilder("Unsupported", "Your devices is not currently supported", "ok", 1);
    	}
    	mProgressDialog.dismiss();
    }
    
    public void setUnlocks(){
    	for(Action a:myDevice.getActions()){
    		if(a.getName().equalsIgnoreCase("Optimus G Unlock"))
    			ogunlock = a;
    		else if(a.getName().equalsIgnoreCase("Mako Unlock"))
    			ogMakounlock = a;
    	}    	
    }
    
    public boolean doAction(Action i, String fullPathName){

		 CommandCapture command = new CommandCapture(0,"/data/local/tmp/edifier "+ "/sdcard/freegee/"+i.getZipFile()){
	        @Override
	        public void output(int id, String line)
	        {
	            RootTools.log(constants.LOG_TAG, line);
	            
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
    
    public void setupUtilities(){
		if(!new File("/sdcard/freegee/tools/edifier").exists()){
		  InputStream in = null;
		  OutputStream out = null;
		  try {
			// read this file into InputStream
			in = getAssets().open("edifier");
	 
			// write the inputStream to a FileOutputStream
			out = new FileOutputStream(new File("/sdcard/freegee/tools/edifier"));
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
		
		if(!new File("/sdcard/freegee/tools/keys").exists()){
			  InputStream in = null;
			  OutputStream out = null;
			  try {
				// read this file into InputStream
				in = getAssets().open("keys");
		 
				// write the inputStream to a FileOutputStream
				out = new FileOutputStream(new File("/sdcard/freegee/tools/keys"));
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
		
		if(!new File("/sdcard/freegee/tools/mkbootimg").exists()){
			  InputStream in = null;
			  OutputStream out = null;
			  try {
				// read this file into InputStream
				in = getAssets().open("mkbootimg");
		 
				// write the inputStream to a FileOutputStream
				out = new FileOutputStream(new File("/sdcard/freegee/tools/mkbootimg"));
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
		
		if(!new File("/sdcard/freegee/tools/unpackbootimg").exists()){
			  InputStream in = null;
			  OutputStream out = null;
			  try {
				// read this file into InputStream
				in = getAssets().open("unpackbootimg");
		 
				// write the inputStream to a FileOutputStream
				out = new FileOutputStream(new File("/sdcard/freegee/tools/unpackbootimg"));
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
		
		if(!new File("/sdcard/freegee/tools/busybox").exists()){
			  InputStream in = null;
			  OutputStream out = null;
			  try {
				// read this file into InputStream
				in = getAssets().open("busybox");
		 
				// write the inputStream to a FileOutputStream
				out = new FileOutputStream(new File("/sdcard/freegee/tools/busybox"));
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
		
		CommandCapture command = new CommandCapture(0,CP_COMMAND + " /sdcard/freegee/tools/edifier /data/local/tmp/ && chmod 755 /data/local/tmp/edifier");
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
		
		command = new CommandCapture(0,CP_COMMAND + " /sdcard/freegee/tools/keys /data/local/tmp/ && chmod 644 /data/local/tmp/keys");
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
		
		command = new CommandCapture(0,CP_COMMAND + " /sdcard/freegee/tools/mkbootimg /data/local/tmp/ && chmod 755 /data/local/tmp/mkbootimg");
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
		
		command = new CommandCapture(0,CP_COMMAND + " /sdcard/freegee/tools/unpackbootimg /data/local/tmp/ && chmod 755 /data/local/tmp/unpackbootimg");
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
		
		command = new CommandCapture(0,CP_COMMAND + " /sdcard/freegee/tools/busybox /data/local/tmp/ && chmod 755 /data/local/tmp/busybox");
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
    }
    
    public void processAction(Action action){
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
    	    
    	    String azipS = "/sdcard/freegee/"+action.getZipFile();
    	    File azipF = new File(azipS);
    	    if(azipF.exists()){
    	    	if(utils.checkMD5(action.getMd5sum(), azipF)){
    	    	    utils.customlog(Log.VERBOSE,"Using predownloaded "+action.getName());
    	    		doAction(action,azipS);
    	    	}
    	    	else{
    	    		utils.customlog(Log.VERBOSE,"Downloading "+action.getName());
    	    		startDownload(action);
    	    	}
    	    }
	    	else{
	    		utils.customlog(Log.VERBOSE,"Downloading "+action.getName());
	    		startDownload(action);
	    	}
    	}
    }
    
    public void startDownload(Action action){
        Intent intent = new Intent(this, DownloadReceiver.class);
        intent.setAction(DownloadReceiver.ACTION_START_DOWNLOAD);
        intent.putExtra(DownloadReceiver.DEVICE_ACTION, (Serializable) action);
        sendBroadcast(intent);    	
    }
    
    public void getDevices(){
    	mProgressDialog = new ProgressDialog(FreeGee.this);
	    mProgressDialog.setIndeterminate(true);
	    mProgressDialog.setCancelable(false);
	    mProgressDialog.setMessage("Downloading supported device list...");
	    mProgressDialog.show();
	    
        Action dAction = new Action();
        dAction.setName("devices.xml");
        dAction.setZipFile("devices.xml");
        dAction.setZipFileLocation("devices.xml");
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:
        		Intent newActivity = new Intent(this, settings.class);
                startActivity(newActivity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

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

	public static boolean isMainActivityActive() {
	    return mMainActivityActive;
	    
	}
}