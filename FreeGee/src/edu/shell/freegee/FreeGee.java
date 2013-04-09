/** @author Seth Shelnutt
 * @License GPLv3 or later
 * All source code is released free and openly by Seth Shelnutt under the terms of GPLv3 or later, 2013 
 * */

package edu.shell.freegee;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

import edu.shell.freegee.R;
import edu.shell.freegee.utilities.DBDownload;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

@SuppressLint("SdCardPath")
public class FreeGee extends Activity {
   
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    public static final int DIALOG_INSTALL_PROGRESS = 1;
    public static final int DIALOG_RESTORE_PROGRESS = 2;
    public static final int DIALOG_BACKUP_PROGRESS = 3;
    private Button startBtn;
    private Button restoreBtn;
    private Button utilBtn;
    private boolean sblopen = false;

    private ProgressDialog mProgressDialog;
    public static boolean isSpecial;

    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    Date date = new Date();
    private String now = dateFormat.format(date);
    
    @Override
    public void onResume(){
    	super.onResume();
    	if(sblopen){
    		sblalert();
    	}
    }
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_gee);
    	File freegeef=new File("/sdcard/freegee");
		  if(!freegeef.exists()){
			  freegeef.mkdirs();
		  }
        startBtn = (Button)findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
            	IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            	Intent batteryStatus = FreeGee.this.registerReceiver(null, ifilter);
            	int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            	int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            	float batteryPct = level / (float)scale;
            	if(batteryPct < 0.10){
            		alertbuilder("Battery Too Low","Your battery is too low. For safety please charge it before attempting unlock","ok",1);
            	}
            	else{
            		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FreeGee.this);
            	    
                	// set title
                	alertDialogBuilder.setTitle("Warning");

                	// set dialog message
                	alertDialogBuilder
                	.setMessage("By Pressing Okay you are acknowledging that you are voiding you are voiding you warrenty and no one from team codefire can be held responsible.")
                	.setCancelable(false)
                	.setPositiveButton("I agree",new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog,int id) {
                	
                		Intent newActivity = new Intent(getBaseContext(), install.class);
                        startActivity(newActivity);
                	}
                	})
                	.setNegativeButton("I disagree",new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog,int id) {
                	// if this button is clicked, close
                	// current activity
                		
                			FreeGee.this.finish();
                			
                	}
                	});

                	// create alert dialog
                	AlertDialog alertDialog = alertDialogBuilder.create();

                	// show it
                	alertDialog.show();
            		
                   
                }
            }
        });
        
        restoreBtn = (Button)findViewById(R.id.restoreBtn);
        restoreBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
            	IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            	Intent batteryStatus = FreeGee.this.registerReceiver(null, ifilter);
            	int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            	int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            	float batteryPct = level / (float)scale;
            	if(batteryPct < 0.10){
            		alertbuilder("Battery Too Low","Your battery is too low. For safety please charge it before attempting unlock","ok",1);
            	}
            	else{
            		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FreeGee.this);
            	    
                	// set title
                	alertDialogBuilder.setTitle("Warning");

                	// set dialog message
                	alertDialogBuilder
                	.setMessage("By Pressing Okay you are acknowledging that you are returning to a locked state by using the backups made while unlocking.")
                	.setCancelable(true)
                	.setPositiveButton("I agree",new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog,int id) {
                   		 new restore().execute();
           			  
                	}
                	})
                	.setNegativeButton("I disagree",new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog,int id) {
 
                	}
                	});

                	// create alert dialog
                	AlertDialog alertDialog = alertDialogBuilder.create();

                	// show it
                	alertDialog.show();
            		
                   
                }
            }
        });
        
        utilBtn = (Button)findViewById(R.id.utilBtn);
        utilBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
            	Intent newActivity = new Intent(getBaseContext(), utilities.class);
                startActivity(newActivity);
   
                }
            
        });
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
            case R.id.sbl_ul:
            	SharedPreferences prefs = getSharedPreferences("FreeGee",MODE_PRIVATE);
            	if(prefs.getInt("Special-always", 2) ==3){
            		sbltoggle();
            	}
            	else
            	  sblalert();
                return true;
            case R.id.menu_settings:
        		Intent newActivity = new Intent(this, settings.class);
                startActivity(newActivity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Downloading file..");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
		case DIALOG_INSTALL_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Installing..");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
		case DIALOG_BACKUP_PROGRESS:
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Backing Up..");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            return mProgressDialog;
		case DIALOG_RESTORE_PROGRESS:
			mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Restoring..");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            return mProgressDialog;
		default:
                return null;
        }
    }

    
    public boolean isSpecial(){
    	SharedPreferences prefs = getSharedPreferences("FreeGee",MODE_PRIVATE); 
    	if(prefs.getInt("Special", 2) ==3){
    		isSpecial = true;
    	   return true;
    	}
    	else{
    		isSpecial = false;
    	   return false;
    	}
    }    
    
class restore extends AsyncTask<String, String, String> {
	int err = 0;
	boolean db = false;
	ArrayList<File> fal = new ArrayList<File>();
	ArrayList<File> fal2 = new ArrayList<File>();
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        showDialog(DIALOG_RESTORE_PROGRESS);
    }

    @Override
    protected String doInBackground(String... aurl) {
    	String command;
    	File freegeef=new File("/sdcard/freegee");
		  if(!freegeef.exists()){
			  freegeef.mkdirs();
		  }
		  String varient = "";
			File file = new File("/system/build.prop");
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
			} catch (FileNotFoundException f) {
		        int err;
				command = "mount -o remount,rw /system";
		    	try {
					err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				command = "chmod 644 /system/build.prop";
		    	try {
					err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				command = "mount -o remount,ro /system";
		    	try {
					err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}

			Properties prop = new Properties();
			// feed the property with the file
			try {
				prop.load(fis);
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
			String device = prop.getProperty("ro.product.name");
			if(device.equalsIgnoreCase("geehrc4g_spr_us")){
				varient = "sprint";
		        }
				else if(device.equalsIgnoreCase("geeb_att_us")){
				varient = "att";
				}
				else if(device.equalsIgnoreCase("geeb_bell_ca")){
				varient = "bell";
				}
				else if(device.equalsIgnoreCase("geeb_rgs_ca")){
				varient = "rogers";
				}
				else if(device.equalsIgnoreCase("geeb_tls_ca")){
				varient = "telus";
				}
				else if(device.equalsIgnoreCase("geehrc_kt_kr")){
				varient = "korean_k";
				}
				else if(device.equalsIgnoreCase("geehrc4g_lgu_kr")){
				varient = "korean_l";
				}
				else if(device.equalsIgnoreCase("geehrc_skt_kr")){
				varient = "korean_s";
				}
				else if(device.equalsIgnoreCase("geehrc_open_hk")){
				varient = "e975";
				}	
				else if(device.equalsIgnoreCase("geehrc_open_tw")){
				varient = "e975";
				}
				else if(device.equalsIgnoreCase("geehrc_open_eu")){
				varient = "e975";
				}
				else if(device.equalsIgnoreCase("geehrc_shb_sg")){
				varient = "e975";
				}
			
    	File boot=new File("/sdcard/freegee/boot-backup.img");
    	File recovery=new File("/sdcard/freegee/recovery-backup.img");
    	File aboot=new File("/sdcard/freegee/aboot-backup.img");
		File sbl1=new File("/sdcard/freegee/sbl1-backup.img");
    	File sbl2=new File("/sdcard/freegee/sbl2-backup.img");
    	File sbl3=new File("/sdcard/freegee/sbl3-backup.img");

    	if(isSpecial){
    		File[] fa = {boot,recovery,aboot,sbl1,sbl2,sbl3};
    		Collections.addAll(fal,fa);
    	}
    	else{
    		File[] fa = {boot,recovery,aboot};
    		Collections.addAll(fal,fa);
    		}
    	for(int i=0;i<fal.size();i++){
    		if(!fal.get(i).exists()){
    			fal2.add(fal.get(i));
    		}
    	}
		SharedPreferences prefs = getSharedPreferences("FreeGee",MODE_PRIVATE);
		if(prefs.contains("dropbox_key")){
		  if(fal2.size() > 0){
			db = true;
			return null;
		  }
		}
		
			  if(boot.exists()){
	        	command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/boot";
	        	try {
					err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
		        	command = "dd if=/sdcard/freegee/boot-backup.img of=/dev/block/platform/msm_sdcc.1/by-name/boot";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
			  }
			  else if(new File("/sdcard/freegee/boot-"+varient+"-backup.img").exists()){
		        	command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/boot";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
			        	command = "dd if=/sdcard/freegee/boot-"+varient+"-backup.img of=/dev/block/platform/msm_sdcc.1/by-name/boot";
			        	try {
							err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
						} catch (InterruptedException e) {
							
							e.printStackTrace();
						} catch (IOException e) {
							
							e.printStackTrace();
						}
				  
			  }
			  else{
				  err=-1;
			  }
			  
			  if(aboot.exists()){
	        	command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/aboot";
	        	try {
					err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
	        	command = "dd if=/sdcard/freegee/aboot-backup.img of=/dev/block/platform/msm_sdcc.1/by-name/aboot";
	        	try {
					err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
		  }
			  else if(new File("/sdcard/freegee/aboot-"+varient+"-backup.img").exists()){
		        	command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/aboot";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
			        	command = "dd if=/sdcard/freegee/aboot-"+varient+"-backup.img of=/dev/block/platform/msm_sdcc.1/by-name/aboot";
			        	try {
							err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
						} catch (InterruptedException e) {
							
							e.printStackTrace();
						} catch (IOException e) {
							
							e.printStackTrace();
						}
				  
			  }
			  else{
				  err=-2;
			  }
			  
			  if(recovery.exists()){
	        	command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/recovery";
	        	try {
					err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
	        	command = "dd if=/sdcard/freegee/recovery-backup.img of=/dev/block/platform/msm_sdcc.1/by-name/recovery";
	        	try {
					err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
		  }
			  else if(new File("/sdcard/freegee/recovery-"+varient+"-backup.img").exists()){
		        	command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/recovery";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
			        	command = "dd if=/sdcard/freegee/recovery-"+varient+"-backup.img of=/dev/block/platform/msm_sdcc.1/by-name/recovery";
			        	try {
							err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
						} catch (InterruptedException e) {
							
							e.printStackTrace();
						} catch (IOException e) {
							
							e.printStackTrace();
						}
				  
			  }
			  else {
				  err = -3;
			  }
			 if(isSpecial()){
			   if(sbl1.exists()){
	        	command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/sbl1";
	        	try {
					err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
	        	command = "dd if=/sdcard/freegee/sbl1-backup.img of=/dev/block/platform/msm_sdcc.1/by-name/sbl1";
	        	try {
					err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
		  }
		  else{
			  err=-1;
		  }
		  
		  if(sbl2.exists()){
	        	command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/sbl2";
	        	try {
					err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
	        	command = "dd if=/sdcard/freegee/sbl2-backup.img of=/dev/block/platform/msm_sdcc.1/by-name/sbl2";
	        	try {
					err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
		  }
		  else{
			  err=-2;
		  }
		  
		  if(sbl3.exists()){
	        	command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/sbl3";
	        	try {
					err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
	        	command = "dd if=/sdcard/freegee/sbl3-backup.img of=/dev/block/platform/msm_sdcc.1/by-name/sbl3";
	        	try {
					err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
		    }
		    else {
			  err = -3;
		    }
		  }
		return null;
    }
    protected void onProgressUpdate(String... progress) {
       //  mProgressDialog.setProgress(Integer.parseInt(progress[0]));
    }

    @Override
    protected void onPostExecute(String unused) {
    	removeDialog(DIALOG_RESTORE_PROGRESS);
    	if(db == true){
    		SharedPreferences prefs = getSharedPreferences("FreeGee",MODE_PRIVATE);
    		if(prefs.contains("dropbox_key")){
    		   DropboxAPI<AndroidAuthSession> mDBApi = dropbox.newSession(FreeGee.this);
			   DBDownload dbdownload = new DBDownload(FreeGee.this, mDBApi, fal2.toArray(new File[fal2.size()]));
			   dbdownload.execute();
			   return;
    		}
    		else{
    			alertbuilder("Error!","Could not restore, backups not found! Do not reboot!","Boo!",0);
    			return;
    		}
    	}
    	if(err==0){
    	    alertbuilder("Success!","Success. Your Optimus G been restored up!","Yay!",0);
    	    return;
    	}
    	else if(err<=-1){
    		alertbuilder("Error!","Could not restore, backups not found!","Boo!",0);
    		return;
    	}
    	else{
    		alertbuilder("Error!","There was an error restoring your backups Do not reboot!","Boo!",0);
    		return;
    	}
    	
       }
    }

public class DBDownload extends AsyncTask<Void, Long, Boolean> {

	
	final static private String APP_KEY = "ywebobijtcfo2yc";
	final static private String APP_SECRET = "ud1duwmbtlml0zz";
	final  private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	// In the class declaration section:
	DropboxAPI<AndroidAuthSession> mApi;


	private Context mContext;
	private ProgressDialog mDialog;

	private String mErrorMsg,path;

	//new class variables:
	private int mFilesDownloaded;
	private File[] mFilesToDownload;
	private int mCurrentFileIndex;
	int totalBytes = 0, indBytes = 0;
	
	public DBDownload(Context context, DropboxAPI<?> api, File[] filesToDownload) {
	    // We set the context this way so we don't accidentally leak activities
	    mContext = context.getApplicationContext();
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
		mApi = new DropboxAPI<AndroidAuthSession>(session);
		AccessTokenPair access = dropbox.getkeys(mContext);
		//Toast.makeText(mContext, "Key is: "+dropbox.getkeys(mContext).toString(), Toast.LENGTH_LONG).show();
		mApi.getSession().setAccessTokenPair(access);
	    

	    //set number of files uploaded to zero.
	    mFilesToDownload = filesToDownload;
	    mCurrentFileIndex = 0;
	    
/*	    for (int i = 0; i < mFilesToDownload.length; i++) {
	    	long bytes = 0;
			try {
				bytes = mApi.metadata(mFilesToDownload[i].getName(),1,"",false,"").bytes;
			} catch (DropboxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        totalBytes += bytes;
	    }*/
	    

	    mDialog = new ProgressDialog(context);
	    mDialog.setMax(100);
	    mDialog.setMessage("Downloading file 1 / " + mFilesToDownload.length + " from Dropbox");
	    mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	    mDialog.setProgress(0);
	    mDialog.setCancelable(false);
	    mDialog.show();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
	    try {
	        for (int i = 0; i < mFilesToDownload.length; i++) {
	            mCurrentFileIndex = i;
	            File file = mFilesToDownload[i];

	            int bytes = (int) mApi.metadata("/"+mFilesToDownload[i].getName(),1,"",false,"").bytes;
	            indBytes = bytes;
	            
	            // By creating a request, we get a handle to the putFile operation,
	            // so we can cancel it later if we want to
	            FileOutputStream fis = new FileOutputStream(file);
	            path = file.getName();
	            mApi.getFile(path, "", fis,
	                    new ProgressListener() {
	                @Override
	                public long progressInterval() {
	                     // Update the progress bar every half-second or so
	                     return 100;
	                }

	                @Override
	                public void onProgress(long bytes, long total) {
	                        publishProgress(bytes);
	                }
	            });
	        }
	        return true;
	    } catch (DropboxUnlinkedException e) {
	        // This session wasn't authenticated properly or user unlinked
	        mErrorMsg = "This app wasn't authenticated properly.";
	    } catch (DropboxFileSizeException e) {
	        // File size too big to upload via the API
	        mErrorMsg = "This file is too big to upload";
	    } catch (DropboxPartialFileException e) {
	        // We canceled the operation
	        mErrorMsg = "Upload canceled";
	    } catch (DropboxServerException e) {
	        // Server-side exception.  These are examples of what could happen,
	        // but we don't do anything special with them here.
	        if (e.error == DropboxServerException._401_UNAUTHORIZED) {
	            // Unauthorized, so we should unlink them.  You may want to
	            // automatically log the user out in this case.
	        } else if (e.error == DropboxServerException._403_FORBIDDEN) {
	            // Not allowed to access this
	        } else if (e.error == DropboxServerException._404_NOT_FOUND) {
	            // path not found (or if it was the thumbnail, can't be
	            // thumbnailed)
	        } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
	            // user is over quota
	        } else {
	            // Something else
	        }
	        // This gets the Dropbox error, translated into the user's language
	        mErrorMsg = e.body.userError;
	        if (mErrorMsg == null) {
	            mErrorMsg = e.body.error;
	        }
	    } catch (DropboxIOException e) {
	        // Happens all the time, probably want to retry automatically.
	        mErrorMsg = "Network error.  Try again.";
	    } catch (DropboxParseException e) {
	        // Probably due to Dropbox server restarting, should retry
	        mErrorMsg = "Dropbox error.  Try again.";
	    } catch (DropboxException e) {
	        // Unknown error
	        mErrorMsg = "Unknown error.  Try again.";
	    } catch (FileNotFoundException e) {
	    }
	    return false;
	  
	}

	@Override
	protected void onProgressUpdate(Long... progress) {
		mDialog.setMessage("Downloading file " + (mCurrentFileIndex + 1) + " / "
	            + mFilesToDownload.length+"\n"+path+" from Dropbox");
	    int percent = (int) (100.0 * (double) progress[0] / indBytes + 0.5);
	    Log.i("pro", percent + "    " + progress[0] + "/" + indBytes);
	    mDialog.setProgress(percent);
	}

	@Override
	protected void onPostExecute(Boolean result) {
	    mDialog.dismiss();
	    if (result) {
	        showToast("Download successfull");
	        new restore().execute();
	        return;
	    } else {
	        showToast(mErrorMsg);
	        return;
	    }
	}

	private void showToast(String msg) {
	    Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
	    error.show();
	}
}
    String secret = "letshopeitdoesn'tbrick-";
    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String computeSum(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
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
    
    public void sblalert(){
    	sblopen=true;
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    	    
    	// set title
    	alertDialogBuilder.setTitle("Special Unlock");
    	final EditText input = new EditText(this); 

    	// set dialog message
    	alertDialogBuilder
    	.setMessage("This is the special SBL unlock need for a select few devices. Due to the hard brick risk, please enter the code you were given.")
    	.setCancelable(true)
    	.setView(input)
    	.setPositiveButton("ok",new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog,int id) {
    		String value = input.getText().toString();
    		try {
				if(value.equals(computeSum(secret+now).substring(0, Math.min(value.length(), 5)))){
					SharedPreferences prefs = getSharedPreferences("FreeGee",MODE_PRIVATE);
					SharedPreferences.Editor editor = prefs.edit();
					editor.putInt("Special", 3);
					editor.putInt("Special-always", 3);
					editor.commit();
					alertbuilder("Success!","Sbl unlock now enabled!","Ok",0);
				}
				else{
					alertbuilder("Error!","Wrong code entered, sbl unlock not enabled.","Ok",0);
				}
				sblopen = false;
				return;
			} catch (NoSuchAlgorithmException e) {
				alertbuilder("Error!","Failed to sha1!","Boo!",0);
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
    	}
    	})
    	.setNeutralButton("Get Unlock Code", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog,int id) {
    		    Uri uriUrl = Uri.parse("http://shelnutt2.codefi.re/freegee/index.php");  
    		    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
    		    startActivity(launchBrowser); 
    		}
    	})
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                sblopen = false;
                return;
            }
        });

    	// create alert dialog
    	AlertDialog alertDialog = alertDialogBuilder.create();

    	// show it
    	alertDialog.show();

    	}
    
    public void sbltoggle(){
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    	    
    	// set title
    	alertDialogBuilder.setTitle("Special Unlock");

    	// set dialog message
    	alertDialogBuilder
    	.setMessage("You have already Enabled the special sbl unlock process. Please select if you want to enable or disable it currently.")
    	.setCancelable(true)
    	.setPositiveButton("Enable",new DialogInterface.OnClickListener() {
    	public void onClick(DialogInterface dialog,int id) {
    		SharedPreferences prefs = getSharedPreferences("FreeGee",MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt("Special", 3);
			editor.commit();
    	}
    	})
        .setNegativeButton("Disable", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
            	SharedPreferences prefs = getSharedPreferences("FreeGee",MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("Special", 2);
				editor.commit();
                return;   
            }
        });

    	// create alert dialog
    	AlertDialog alertDialog = alertDialogBuilder.create();

    	// show it
    	alertDialog.show();

    	}
}