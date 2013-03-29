package edu.shell.freegee;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.DropboxAPI.UploadRequest;
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

import edu.shell.freegee.install.DownloadFileAsync;
import edu.shell.freegee.install.DownloadSBLFileAsync;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class utilities extends Activity{
	
    private Button efsBtn;
    private Button DbBtn;
    private boolean override;
    
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    public static final int DIALOG_INSTALL_PROGRESS = 1;
    public static final int DIALOG_RESTORE_PROGRESS = 2;
    public static final int DIALOG_BACKUP_PROGRESS = 3;
    @SuppressWarnings("unused")
	private Button startBtn;
    private Button restoreBtn;
    private Button miscBtn;
    public static String device;

    private ProgressDialog mProgressDialog;
    public static boolean isSpecial;
	
    @Override
    public void onResume(){
    	super.onResume();
    	DbBtn = (Button)findViewById(R.id.DbBtn);
        SharedPreferences prefs = getSharedPreferences("FreeGee",MODE_PRIVATE);
        if(prefs.contains("dropbox_key")){
           DbBtn.setVisibility(View.VISIBLE); //View.GONE, View.INVISIBLE are available too.
        }
        else{
           DbBtn.setVisibility(View.INVISIBLE);
        }
        DbBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
            	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(utilities.this);
        	    
            	// set title
            	alertDialogBuilder.setTitle("DropBox Sync");

            	// set dialog message
            	alertDialogBuilder
            	.setMessage("Do you want to force an upload or download of your backups?")
            	.setCancelable(true)
            	.setPositiveButton("Upload",new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog,int id) {
            		File[] toBeUploaded = {new File("/sdcard/freegee/boot-backup.img"),new File("/sdcard/freegee/aboot-backup.img"),new File("/sdcard/freegee/recovery-backup.img"),new File("/sdcard/freegee/m9kefs1-backup.img"),new File("/sdcard/freegee/m9kefs2-backup.img"),new File("/sdcard/freegee/m9kefs3-backup.img")};
            		DropboxAPI<AndroidAuthSession> mDBApi = dropbox.newSession(utilities.this);
            		DBUpload dbupload = new DBUpload(utilities.this, mDBApi, toBeUploaded);
            			dbupload.execute();
            		}
            	})
            	.setNegativeButton("Download", new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog,int id) {
            		}
            	});
            	// create alert dialog
            	AlertDialog alertDialog = alertDialogBuilder.create();

            	// show it
            	alertDialog.show();
            	
            }
        
        });
        
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utilities);
        
        DbBtn = (Button)findViewById(R.id.DbBtn);
        SharedPreferences prefs = getSharedPreferences("FreeGee",MODE_PRIVATE);
        if(prefs.contains("dropbox_key")){
           DbBtn.setVisibility(View.VISIBLE); //View.GONE, View.INVISIBLE are available too.
        }
        String command;
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
    	device = prop.getProperty("ro.product.name");
        DbBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
            	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(utilities.this);
        	    
            	// set title
            	alertDialogBuilder.setTitle("DropBox Sync");

            	// set dialog message
            	alertDialogBuilder
            	.setMessage("Do you want to force an upload or download of your backups?")
            	.setCancelable(true)
            	.setPositiveButton("Upload",new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog,int id) {
            		File[] toBeUploaded = {new File("/sdcard/freegee/boot-backup.img"),new File("/sdcard/freegee/aboot-backup.img"),new File("/sdcard/freegee/recovery-backup.img"),new File("/sdcard/freegee/m9kefs1-backup.img"),new File("/sdcard/freegee/m9kefs2-backup.img"),new File("/sdcard/freegee/m9kefs3-backup.img")};
            		DropboxAPI<AndroidAuthSession> mDBApi = dropbox.newSession(utilities.this);
            		DBUpload dbupload = new DBUpload(utilities.this, mDBApi, toBeUploaded);
            			dbupload.execute();
            		}
            	})
            	.setNegativeButton("Download", new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog,int id) {
            		}
            	});
            	// create alert dialog
            	AlertDialog alertDialog = alertDialogBuilder.create();

            	// show it
            	alertDialog.show();
        		
            }
            
        
        
        });
        
        efsBtn = (Button)findViewById(R.id.efsBtn);
        efsBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
            	IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            	Intent batteryStatus = utilities.this.registerReceiver(null, ifilter);
            	int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            	int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            	float batteryPct = level / (float)scale;
            	if(batteryPct < 0.10){
            		alertbuilder("Battery Too Low","Your battery is too low. For safety please charge it before attempting unlock","ok",1);
            	}
            	else{
            		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(utilities.this);
            	    
                	// set title
                	alertDialogBuilder.setTitle("Warning");

                	// set dialog message
                	alertDialogBuilder
                	.setMessage("It is highly important to backup your efs partitions. In the event of radio issues these backups might save your device. Do you want to backup or restore efs?")
                	.setCancelable(true)
                	.setPositiveButton("Backup",new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog,int id) {
                		File efs1=new File("/sdcard/freegee/m9kefs1.img");
                		File efs2=new File("/sdcard/freegee/m9kefs2.img");
                		File efs3=new File("/sdcard/freegee/m9kefs3.img");
                		if(efs1.exists() || efs2.exists() || efs3.exists()){
                			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(utilities.this);
                    	    
                        	// set title
                        	alertDialogBuilder.setTitle("Warning");

                        	// set dialog message
                        	alertDialogBuilder
                        	.setMessage("Existing backups detected, do you want to override them?")
                        	.setCancelable(false)
                        	.setPositiveButton("Override",new DialogInterface.OnClickListener() {
                        		public void onClick(DialogInterface dialog,int id) {
                        			override = true;
                              		new efsbackup().execute();
                        		}
                        	})
                        	.setNegativeButton("No",new DialogInterface.OnClickListener() {
                            	public void onClick(DialogInterface dialog,int id) {
                            	   override = false;  
                            	}
                            	});

                            	// create alert dialog
                            	AlertDialog alertDialog = alertDialogBuilder.create();

                            	// show it
                            	alertDialog.show();
                		}
                		else{
                			new efsbackup().execute();
                		}
                	}
                	})
                	.setNegativeButton("Restore",new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog,int id) {

                  		 new efsrestore().execute();
                			
                	}
                	});

                	// create alert dialog
                	AlertDialog alertDialog = alertDialogBuilder.create();

                	// show it
                	alertDialog.show();
            		
                   
                }
            }
        });
        
        miscBtn = (Button)findViewById(R.id.miscBtn);
        miscBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
            	IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            	Intent batteryStatus = utilities.this.registerReceiver(null, ifilter);
            	int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            	int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            	float batteryPct = level / (float)scale;
            	if(batteryPct < 0.10){
            		alertbuilder("Battery Too Low","Your battery is too low. For safety please charge it before attempting unlock","ok",1);
            	}
            	else{
            		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(utilities.this);
            	    
                	// set title
                	alertDialogBuilder.setTitle("Warning");

                	// set dialog message
                	alertDialogBuilder
                	.setMessage("This will attempt to fix misc from backups if they exist or flash a blank one.")
                	.setCancelable(true)
                	.setPositiveButton("Fix",new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog,int id) {
                		File misc=new File("/sdcard/freegee/misc-backup.img");
                		if(misc.exists()){
                			Toast.makeText(utilities.this, "Restoring from backup found", Toast.LENGTH_SHORT).show();
                			fixmisc("/sdcard/freegee/misc-backup.img");
                		}
                		else{
                			Toast.makeText(utilities.this, "No backup found, downloading blank misc", Toast.LENGTH_SHORT).show();
                			String miscblank;
                			if(device.equalsIgnoreCase("geehrc4g_spr_us")){
                				miscblank="http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/freegee/geehrc4g_spr_us/misc-sprint-blank.img";
                			}
                			else if(device.equalsIgnoreCase("geeb_att_us")){
                				miscblank="http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/freegee/geeb_att_us/misc-att-blank.misc";
                			}
                			else{
                				miscblank="http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/freegee/geeb_bell_ca/misc-bell-blank.misc";
                			}
                			new DownloadMisc().execute(miscblank);
                		}
                	}

                	})
                	.setNegativeButton("Abort",new DialogInterface.OnClickListener() {
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
    
	private void fixmisc(String miscloc) {
		int err = 0;
		String command;
    	File misc=new File(miscloc);
			  if(misc.exists()){
	        	command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/misc";
	        	try {
					err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
		        	command = "dd if="+miscloc+" of=/dev/block/platform/msm_sdcc.1/by-name/m9kefs1";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
		        	
		        	if(err==0){
		        		alertbuilder("Success!","Success. Your Optimus G misc has been fixed! Reboot once before attempting unlock.","Yay!",0);
		        	}
		        	else{
		        		alertbuilder("Failed!","Failed to flash new misc partition. Please check root permissions","Ok",0);
		        	}
			  }
			  else{
				  alertbuilder("Failed!","Could not find backup misc. Misc not fixed","Ok",0);
			  }
		
	}
	
	class DownloadMisc extends AsyncTask<String, String, String> {
        int err;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_DOWNLOAD_PROGRESS);
        }

        @Override
        protected String doInBackground(String... aurl) {
            int count;
           
            
            
            try {
                URL url = new URL(aurl[0]);
                URLConnection conexion = url.openConnection();
                conexion.connect();
                int lenghtOfFile = conexion.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream("/sdcard/freegee/misc-blank.img");

                byte data[] = new byte[1024];
                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(""+(int)((total*100)/lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
            	err = -1;
            }
            
            return null;

        }
        protected void onProgressUpdate(String... progress) {
             mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
        	removeDialog(DIALOG_DOWNLOAD_PROGRESS);
        	if (err!=0) {
        	alertbuilder("Error!","There was a problem downloading misc. Please try agian.","Ok",0);
        	return;
        	}
        	else{
        		fixmisc("/sdcard/freegee/misc-blank.img");
        		return;
          }

        }
    }
	
    public class DBUpload extends AsyncTask<Void, Long, Boolean> {

    	
    	final static private String APP_KEY = "ywebobijtcfo2yc";
    	final static private String APP_SECRET = "ud1duwmbtlml0zz";
    	final  private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
    	// In the class declaration section:
    	DropboxAPI<AndroidAuthSession> mApi;


    	private UploadRequest mRequest;
    	private Context mContext;
    	private ProgressDialog mDialog;

    	private String mErrorMsg;

    	//new class variables:
    	private int mFilesUploaded;
    	private File[] mFilesToUpload;
    	private int mCurrentFileIndex;

    	public DBUpload(Context context, DropboxAPI<?> api, File[] filesToUpload) {
    	    // We set the context this way so we don't accidentally leak activities
    	    mContext = context.getApplicationContext();
    		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
    		AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
    		mApi = new DropboxAPI<AndroidAuthSession>(session);
    		AccessTokenPair access = dropbox.getkeys(mContext);
    		Toast.makeText(mContext, "Key is: "+dropbox.getkeys(mContext).toString(), Toast.LENGTH_LONG).show();
    		mApi.getSession().setAccessTokenPair(access);
    	    

    	    //set number of files uploaded to zero.
    	    mFilesUploaded = 0;
    	    mFilesToUpload = filesToUpload;
    	    mCurrentFileIndex = 0;

    	    mDialog = new ProgressDialog(context);
    	    mDialog.setMax(100);
    	    mDialog.setMessage("Uploading file 1 / " + filesToUpload.length);
    	    mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    	    mDialog.setProgress(0);
    	    mDialog.setButton(DialogInterface.BUTTON_NEUTRAL,"Cancel", new  DialogInterface.OnClickListener() {
    	        public void onClick(DialogInterface dialog, int which) {
    	            cancel(true);
    	        }
    	    });
    	    mDialog.show();
    	}

    	@Override
    	protected Boolean doInBackground(Void... params) {
    	    try {
    	        for (int i = 0; i < mFilesToUpload.length; i++) {
    	            mCurrentFileIndex = i;
    	            File file = mFilesToUpload[i];

    	            // By creating a request, we get a handle to the putFile operation,
    	            // so we can cancel it later if we want to
    	            FileInputStream fis = new FileInputStream(file);
    	            String path = file.getName();
    	            mRequest = mApi.putFileOverwriteRequest(path, fis, file.length(),
    	                    new ProgressListener() {
    	                @Override
    	                public long progressInterval() {
    	                     // Update the progress bar every half-second or so
    	                     return 500;
    	                }

    	                @Override
    	                public void onProgress(long bytes, long total) {
    	                    if(isCancelled()) {
    	                        // This will cancel the putFile operation
    	                        mRequest.abort();
    	                    }
    	                    else {
    	                        publishProgress(bytes);
    	                    }
    	                }
    	            });

    	            mRequest.upload();

    	            if(!isCancelled()) {
    	                mFilesUploaded++;
    	            }
    	            else {
    	                return false;
    	            }
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
    	    int totalBytes = 0;
    	    int bytesUploaded = 0;
    	    for(int i=0;i<mFilesToUpload.length;i++) {
    	        Long bytes = mFilesToUpload[i].length();
    	        totalBytes += bytes;

    	        if(i < mCurrentFileIndex) {
    	            bytesUploaded += bytes;
    	        }
    	    }
    	    bytesUploaded += progress[0];

    	    mDialog.setMessage("Uploading file " + (mCurrentFileIndex+1) + " / " + mFilesToUpload.length);
    	    mDialog.setProgress((int) ((bytesUploaded / totalBytes) * 100));
    	}

    	@Override
    	protected void onPostExecute(Boolean result) {
    	    mDialog.dismiss();
    	    if (result) {
    	        showToast("Image successfully uploaded");
    	    } else {
    	        showToast(mErrorMsg);
    	    }
    	}

    	private void showToast(String msg) {
    	    Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
    	    error.show();
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
    	
    
    class efsbackup extends AsyncTask<String, String, String> {
    	int err = 0;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_BACKUP_PROGRESS);
        }
    

        @Override
        protected String doInBackground(String... aurl) {
        	String command;
        	File freegeef=new File("/sdcard/freegee");
			  if(!freegeef.exists()){
				  freegeef.mkdirs();
			  }
              File efs1=new File("/sdcard/freegee/m9kefs1.img");
 			  if(!efs1.exists() || override == true){
 		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs1 of=/sdcard/freegee/m9kefs1.img";
 		        	try {
 						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					} catch (IOException e) {
 						
 						e.printStackTrace();
 					}
 			  }
 	          File efs2=new File("/sdcard/freegee/m9kefs2.img");
 			  if(!efs2.exists() || override == true){
		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs2 of=/sdcard/freegee/m9kefs2.img";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
			  }
 			  File efs3=new File("/sdcard/freegee/m9kefs3.img");
 			  if(!efs3.exists() || override == true){
		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs3 of=/sdcard/freegee/m9kefs3.img";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
			  }
			return null;
        }
        protected void onProgressUpdate(String... progress) {
           //  mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
        	removeDialog(DIALOG_BACKUP_PROGRESS);
        	
        	 alertbuilder("Success!","Success. Your Optimus G EFS been backed up!","Yay!",0);
        	
        	
        }
    }
    
    class efsrestore extends AsyncTask<String, String, String> {
    	int err = 0;
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
        	File efs1=new File("/sdcard/freegee/m9kefs1.img");
        	File efs2=new File("/sdcard/freegee/m9kefs3.img");
        	File efs3=new File("/sdcard/freegee/m9kefs2.img");
 			  if(efs1.exists()){
		        	command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/m9kefs1";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
 		        	command = "dd if=/sdcard/freegee/m9kefs1.img of=/dev/block/platform/msm_sdcc.1/by-name/m9kefs1";
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
 			  
 			  if(efs2.exists()){
		        	command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/m9kefs2";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
		        	command = "dd if=/sdcard/freegee/m9kefs2.img of=/dev/block/platform/msm_sdcc.1/by-name/m9kefs2";
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
 			  
 			  if(efs3.exists()){
		        	command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/m9kefs3";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
		        	command = "dd if=/sdcard/freegee/m9kefs3.img of=/dev/block/platform/msm_sdcc.1/by-name/m9kefs3";
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
			return null;
        }
        protected void onProgressUpdate(String... progress) {
           //  mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
        	removeDialog(DIALOG_RESTORE_PROGRESS);
        	if(err==0){
        	    alertbuilder("Success!","Success. Your Optimus G EFS been restored up!","Yay!",0);
        	}
        	else if(err<=-1){
        		alertbuilder("Error!","Could not restore, EFS backups not found!","Boo!",0);
        	}
        	else{
        		alertbuilder("Error!","There was an error restoring your EFS backups","Boo!",0);
        	}
        	
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
    			utilities.this.finish();
    			}
    	}
    	});

    	// create alert dialog
    	AlertDialog alertDialog = alertDialogBuilder.create();

    	// show it
    	alertDialog.show();

    	}

}
