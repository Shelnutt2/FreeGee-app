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
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
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
    private Button efsBtn;
    private ProgressDialog mProgressDialog;
    private String varient;
    private boolean restoring;
    private boolean override;
    public static boolean isSpecial;
    private Button DbBtn;
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    Date date = new Date();
    private String now = dateFormat.format(date);
    
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
            	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FreeGee.this);
        	    
            	// set title
            	alertDialogBuilder.setTitle("DropBox Sync");

            	// set dialog message
            	alertDialogBuilder
            	.setMessage("Do you want to force an upload or download of your backups?")
            	.setCancelable(true)
            	.setPositiveButton("Upload",new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog,int id) {
            		File[] toBeUploaded = {new File("/sdcard/freegee/boot-backup.img"),new File("/sdcard/freegee/aboot-backup.img"),new File("/sdcard/freegee/recovery-backup.img"),new File("/sdcard/freegee/m9kefs1-backup.img"),new File("/sdcard/freegee/m9kefs2-backup.img"),new File("/sdcard/freegee/m9kefs3-backup.img")};
            		DropboxAPI<AndroidAuthSession> mDBApi = dropbox.newSession(FreeGee.this);
            		DBUpload dbupload = new DBUpload(FreeGee.this, mDBApi, toBeUploaded);
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
        
        DbBtn = (Button)findViewById(R.id.DbBtn);
        SharedPreferences prefs = getSharedPreferences("FreeGee",MODE_PRIVATE);
        if(prefs.contains("dropbox_key")){
           DbBtn.setVisibility(View.VISIBLE); //View.GONE, View.INVISIBLE are available too.
        }
        DbBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
            	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FreeGee.this);
        	    
            	// set title
            	alertDialogBuilder.setTitle("DropBox Sync");

            	// set dialog message
            	alertDialogBuilder
            	.setMessage("Do you want to force an upload or download of your backups?")
            	.setCancelable(true)
            	.setPositiveButton("Upload",new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog,int id) {
            		File[] toBeUploaded = {new File("/sdcard/freegee/boot-backup.img"),new File("/sdcard/freegee/aboot-backup.img"),new File("/sdcard/freegee/recovery-backup.img"),new File("/sdcard/freegee/m9kefs1-backup.img"),new File("/sdcard/freegee/m9kefs2-backup.img"),new File("/sdcard/freegee/m9kefs3-backup.img")};
            		DropboxAPI<AndroidAuthSession> mDBApi = dropbox.newSession(FreeGee.this);
            		DBUpload dbupload = new DBUpload(FreeGee.this, mDBApi, toBeUploaded);
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
                	// if this button is clicked, close
                	// current activity
                		File backup_script=new File("/sdcard/freegee/freegee-restore.sh");
           			  if(!backup_script.exists()){
           				restoring = true; 
           				File file = new File("/system/build.prop");
           				FileInputStream fis = null;
           				try {
           					fis = new FileInputStream(file);
           				} catch (FileNotFoundException e) {

           					e.printStackTrace();
           				}

           				Properties prop = new Properties();
           				// feed the property with the file
           				try {
           					prop.load(fis);
           				} catch (IOException e) {

           					e.printStackTrace();
           				}
           				try {
           					fis.close();
           				} catch (IOException e) {

           					e.printStackTrace();
           				}
           				String recovery = "twrp";
           				String device = prop.getProperty("ro.product.name");
           				if(device.equalsIgnoreCase("geehrc4g_spr_us")){
           					varient = "sprint";
           		        String url = "http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/sprint/private/freegee/freegee-apk-sprint-"+recovery+".tar";
           		        new DownloadFileAsync().execute(url);
           		        }
           				else if(device.equalsIgnoreCase("geeb_att_us")){
           					varient = "att";
           					new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/att/private/freegee/freegee-apk-att-"+recovery+".tar");
           				}
           				else if(device.equalsIgnoreCase("geeb_bell_ca")){
           					varient = "bell";
           					new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/bell/private/freegee/freegee-apk-bell-"+recovery+".tar");
           				}
           				else if(device.equalsIgnoreCase("geeb_rgs_ca")){
           					varient = "rogers";
           					new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/rogers/private/freegee/freegee-apk-rogers-"+recovery+".tar");
           				}
           				else if(device.equalsIgnoreCase("geeb_tls_ca")){
           					varient = "telus";
           					new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/telus/private/freegee/freegee-apk-telus-"+recovery+".tar");
           				}
     			          }
           			  else{
                   		 new restore().execute();
           			  }
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
        efsBtn = (Button)findViewById(R.id.efsBtn);
        efsBtn.setOnClickListener(new OnClickListener(){
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
                	.setMessage("It is highly important to backup your efs partitions. In the event of radio issues these backups might save your device. Do you want to backup or restore efs?")
                	.setCancelable(true)
                	.setPositiveButton("Backup",new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog,int id) {
                		File efs1=new File("/sdcard/freegee/m9kefs1.img");
                		File efs2=new File("/sdcard/freegee/m9kefs2.img");
                		File efs3=new File("/sdcard/freegee/m9kefs3.img");
                		if(efs1.exists() || efs2.exists() || efs3.exists()){
                			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FreeGee.this);
                    	    
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
    class DownloadFileAsync extends AsyncTask<String, String, String> {
       
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
                OutputStream output = new FileOutputStream("/sdcard/freegee.tar");

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
            } catch (Exception e) {}
            return null;

        }
        protected void onProgressUpdate(String... progress) {
             mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
        	removeDialog(DIALOG_DOWNLOAD_PROGRESS);
        	if(restoring){
        		new restore().execute();
        	}
        	else{
        		new unlock().execute();
        	}
        }
    }
    
    private class unlock extends AsyncTask<String, Integer, String>{

    	@Override
        protected void onPreExecute() {
            super.onPreExecute();
            removeDialog(DIALOG_DOWNLOAD_PROGRESS);
            showDialog(DIALOG_INSTALL_PROGRESS);
        }
    	int err = 0;
		protected String doInBackground(String...Params) {
    	//int err = 0;
    	String command = "busybox mv /sdcard/freegee.tar /data/local/tmp/ && cd /data/local/tmp/ && busybox tar xvf freegee.tar";
    	try {
			 err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
			//RootTools.getShell(true).add(command).waitForFinish();
 catch (InterruptedException e) {
			
			e.printStackTrace();
		}
    	if (err == 0){
    		publishProgress(0);
    		command = "cd /data/local/tmp/ && chmod 777 freegee-backup.sh && sh freegee-backup.sh";
    		try {
   			 err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
   		} catch (IOException e) {
   			
   			e.printStackTrace();
   		}
   			//RootTools.getShell(true).add(command).waitForFinish();
    catch (InterruptedException e) {
   			
   			e.printStackTrace();
   		}
    		if (err == 0){
    			publishProgress(1);
    			command = "cd /data/local/tmp/ && chmod 777 freegee-install.sh && sh freegee-install.sh";
    			try {
    				 err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
    			} catch (IOException e) {
    				
    				e.printStackTrace();
    			}
    				//RootTools.getShell(true).add(command).waitForFinish();
    	 catch (InterruptedException e) {
    				
    				e.printStackTrace();
    			}
            	
    			if (err !=0){
    				
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FreeGee.this);
            	    
                	// set title
                	alertDialogBuilder.setTitle("Error!");

                	// set dialog message
                	alertDialogBuilder
                	.setMessage("There was a problem installing. Attempting to reinstall backups.")
                	.setCancelable(false)
                	.setPositiveButton("OK",new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog,int id) {
                	// if this button is clicked, close
                	// current activity
                		 new restore().execute();
                	}
                	});
                	// create alert dialog
                	AlertDialog alertDialog = alertDialogBuilder.create();
                	// show it
                	alertDialog.show();
                	
    			}
    		   }
    		
    		else{
    			err=-1;
    			
    		    }
    		}
    	else {
    		err = -2;
    		
    		
    	}
    	
		return null;
		}
		
		protected void onProgressUpdate(String... progress) {

            if(progress[0] == "0"){
             mProgressDialog.setMessage("Creating Backups...");
             }
            if(progress[0] == "1"){
                mProgressDialog.setMessage("Installing...");
                }
       }

       @Override
       protected void onPostExecute(String unused) {
           dismissDialog(DIALOG_INSTALL_PROGRESS);
           if(err==-1){
        	   alertbuilder("Error!","There was a problem creating backups. Aborting.","Ok",0);
           }
           else if(err==-2){
        	   alertbuilder("Error!","There was a problem untaring the file. Please try again","Ok!",0);
           }
           else{
           alertbuilder("Success!","Success. Your "+varient+" Optimus G has been liberated!","Yay!",0);
           }
           
       }
    	
    }	
    
    class restore extends AsyncTask<String, String, String> {
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
        	File boot=new File("/sdcard/freegee/boot-backup.img");
        	File recovery=new File("/sdcard/freegee/recovery-backup.img");
        	File aboot=new File("/sdcard/freegee/aboot-backup.img");
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
 			  else {
 				  err = -3;
 			  }
 			 if(isSpecial()){
	        		File sbl1=new File("/sdcard/freegee/sbl1-backup.img");
	            	File sbl2=new File("/sdcard/freegee/sbl2-backup.img");
	            	File sbl3=new File("/sdcard/freegee/sbl3-backup.img");	
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
        	if(err==0){
        	    alertbuilder("Success!","Success. Your Optimus G been restored up!","Yay!",0);
        	}
        	else if(err<=-1){
        		alertbuilder("Error!","Could not restore, backups not found! Do not reboot!","Boo!",0);
        	}
        	else{
        		alertbuilder("Error!","There was an error restoring your backups Do not reboot!","Boo!",0);
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
    String secret = "letshopeitdoesn'tbrick-";
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
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    	    
    	// set title
    	alertDialogBuilder.setTitle("Special Unlock");
    	final EditText input = new EditText(this); 

    	// set dialog message
    	alertDialogBuilder
    	.setMessage("This is the special SBL unlock need for a select few devices. Due to the hard brick risk, please enter the code given to you by Shelnutt2")
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
				}
			} catch (NoSuchAlgorithmException e) {
				alertbuilder("Error!","Failed to sha1!","Boo!",0);
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
    	}
    	})
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                
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