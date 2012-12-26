package edu.shell.freegee;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.apache.commons.codec.digest.DigestUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class FreeGee extends Activity {
   
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    public static final int DIALOG_INSTALL_PROGRESS = 1;
    public static final int DIALOG_RESTORE_PROGRESS = 2;
    private Button startBtn, rootBtn, restoreBtn;
    private ProgressDialog mProgressDialog;
    private String varient;
    private String SaveLocal;
    private int wait,downnum;
   
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_gee);
        rootBtn = (Button)findViewById(R.id.rootBtn);
        rootBtn.setOnClickListener(new OnClickListener(){
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
                	.setMessage("By Pressing Okay you are acknowledging that you are voiding your warranty and rooting the device.")
                	.setCancelable(false)
                	.setPositiveButton("I agree",new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog,int id) {
                	// if this button is clicked, close
                	// current activity
                		Toast.makeText(getApplicationContext(), "rooting", Toast.LENGTH_LONG).show();
                		 downnum = 0;
                		 try {
							root_device(downnum);
						} catch (NoSuchAlgorithmException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
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
                	// if this button is clicked, close
                	// current activity
                		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FreeGee.this);
                	    
                    	// set title
                    	alertDialogBuilder.setTitle("Warning");

                    	// set dialog message
                    	alertDialogBuilder
                    	.setMessage("Please choose the recovery you want. CWM is stable but has backup issues. TWRP works in all aspects exception compressed backups. TWRP is the recommended choice.")
                    	.setCancelable(false)
                    	.setPositiveButton("TWRP",new DialogInterface.OnClickListener() {
                    	public void onClick(DialogInterface dialog,int id) {
                    	// if this button is clicked, close
                    	// current activity
                    		 startDownload("twrp");
                    	}
                    	})
                    	.setNegativeButton("CWM",new DialogInterface.OnClickListener() {
                    	public void onClick(DialogInterface dialog,int id) {
                    	// if this button is clicked, close
                    	// current activity
                    		
                    		 startDownload("cwm");
                    			
                    	}
                    	});

                    	// create alert dialog
                    	AlertDialog alertDialog = alertDialogBuilder.create();

                    	// show it
                    	alertDialog.show();
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
                	.setCancelable(false)
                	.setPositiveButton("I agree",new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog,int id) {
                	// if this button is clicked, close
                	// current activity
                		 new restore().execute();
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
    }

    private void root_device(int down) throws NoSuchAlgorithmException, FileNotFoundException{
    	String downloads[] = {"su","Superuser.apk","busybox","fakebackup.ab","root.md5sum"};
    	 String url;
    	switch(down) {
        case 0:
        	SaveLocal="/sdcard/"+downloads[down];
     	    url = "http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/private/bin4ry-root/"+downloads[down];
            new DownloadFileAsync2().execute(url);
            break;
        case 1:
        	SaveLocal="/sdcard/"+downloads[down];
     	    url = "http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/private/bin4ry-root/"+downloads[down];
            new DownloadFileAsync2().execute(url);
            break;
        case 2:
        	SaveLocal="/sdcard/"+downloads[down];
     	    url = "http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/private/bin4ry-root/"+downloads[down];
            new DownloadFileAsync2().execute(url);
            break;
        case 3:
        	SaveLocal="/sdcard/"+downloads[down];
     	    url = "http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/private/bin4ry-root/"+downloads[down];
            new DownloadFileAsync2().execute(url);
            break;
        case 4:
        	SaveLocal="/sdcard/"+downloads[down];
     	    url = "http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/private/bin4ry-root/"+downloads[down];
            new DownloadFileAsync2().execute(url);
            break;
     default:    	    	
    	/*File file = new File("/data/local/tmp/root.md5sum");
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Properties prop = new Properties();
		// feed the property with the file
		try {
			prop.load(fis);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String gmd5 = prop.getProperty("Superuser.apk");
		Toast.makeText(getApplicationContext(), "given md5 txt is: "+gmd5, Toast.LENGTH_LONG).show();*/
    		MessageDigest digest = MessageDigest.getInstance("MD5");
    		File f = new File("/data/local/tmp/Superuser.apk");
    		InputStream is = new FileInputStream(f);				
    		byte[] buffer = new byte[8192];
    		int read = 0;
    		String output;
    		try {
    			while( (read = is.read(buffer)) > 0) {
    				digest.update(buffer, 0, read);
    			}		
    			byte[] md5sum = digest.digest();
    			BigInteger bigInt = new BigInteger(1, md5sum);
    			output = bigInt.toString(16);
    			System.out.println("MD5: " + output);
    		}
    		catch(IOException e) {
    			throw new RuntimeException("Unable to process file for MD5", e);
    		}
    		finally {
    			try {
    				is.close();
    			}
    			catch(IOException e) {
    				throw new RuntimeException("Unable to close input stream for MD5 calculation", e);
    			}
    		}
		Toast.makeText(getApplicationContext(), "calculated md5 txt is: "+output, Toast.LENGTH_LONG).show();
    
    	break;
        }
    }
    private void startDownload(String recovery) {
    	String device;
    	if(new Root().isDeviceRooted()){
    	int err = 0;
    	String command = "busybox";
    	try {
			 err = Runtime.getRuntime().exec(new String[] { command }).waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			//RootTools.getShell(true).add(command).waitForFinish();
        catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		if (err == 0){
		// read the property text  file
		File file = new File("/system/build.prop");
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Properties prop = new Properties();
		// feed the property with the file
		try {
			prop.load(fis);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		device = prop.getProperty("ro.product.name");
		if(device.equalsIgnoreCase("geehrc4g_spr_us")){
			SaveLocal="/sdcard/freegee.tar";
			varient = "sprint";
        String url = "http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/sprint/private/freegee/freegee-apk-sprint-"+recovery+".tar";
        new DownloadFileAsync().execute(url);
        }
		else if(device.equalsIgnoreCase("geeb_att_us")){
			SaveLocal="/sdcard/freegee.tar";
			varient = "att";
			new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/att/private/freegee/freegee-apk-att-"+recovery+".tar");
		}
		else if(device.equalsIgnoreCase("geeb_bell_ca")){
			SaveLocal="/sdcard/freegee.tar";
			varient = "bell";
			new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/bell/private/freegee/freegee-apk-bell-"+recovery+".tar");
		}
		else if(device.equalsIgnoreCase("geeb_rgs_ca")){
			SaveLocal="/sdcard/freegee.tar";
			varient = "rogers";
			new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/rogers/private/freegee/freegee-apk-rogers-"+recovery+".tar");
		}
		else if(device.equalsIgnoreCase("geeb_tls_ca")){
			SaveLocal="/sdcard/freegee.tar";
			varient = "telus";
			new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/telus/private/freegee/freegee-apk-telus-"+recovery+".tar");
		}
		else if(device.equalsIgnoreCase("geehrc_kt_kr")){
			SaveLocal="/sdcard/freegee.tar";
			varient = "korean_k";
			new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/korean_k/private/freegee/freegee-apk-korean_k-"+recovery+".tar");
		}
		else if(device.equalsIgnoreCase("geehrc4g_lgu_kr")){
			SaveLocal="/sdcard/freegee.tar";
			varient = "korean_l";
			new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/korean_l/private/freegee/freegee-apk-korean_l-"+recovery+".tar");
		}
		else if(device.equalsIgnoreCase("geehrc_skt_kr")){
			SaveLocal="/sdcard/freegee.tar";
			varient = "korean_s";
			new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/korean_k/private/freegee/freegee-apk-korean_k-"+recovery+".tar");
		}
		else{
			alertbuilder("Error!","Your device currently isn't supported.","Ok",1);
		}
		}
		else{
			alertbuilder("Error!","Please install busybox from the market.","Ok",0);
		}
        }
        else{
		alertbuilder("Error!","Please root your device first.","Ok",0);
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
                OutputStream output = new FileOutputStream(SaveLocal);

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
        	new unlock().execute();
        }
    }
    
    class DownloadFileAsync2 extends AsyncTask<String, String, String> {
        
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
                OutputStream output = new FileOutputStream(SaveLocal);

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
        	downnum++;
        	try {
				root_device(downnum);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			//RootTools.getShell(true).add(command).waitForFinish();
 catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if (err == 0){
    		publishProgress(0);
    		String backups[] = {"aboot-"+varient+"-backup.img",
    		"boot-"+varient+"-backup.img", "recovery-"+varient+"-backup.img"};
             boolean missing = false;
             for(int i=0;i<backups.length;i++){
    		 File file=new File("/sdcard/freegee/"+backups[i]);
    				  if(!file.exists());{
    					  missing = true;
    				  }
    		}
    		 File backup_script=new File("/sdcard/freegee/freegee-restore.sh");
			  if(!backup_script.exists());{
				  command = "cd /data/local/tmp/ && chmod 777 freegee-backup.sh && busybox cp freegee-backup.sh /sdcard/freegee/";
		    		try {
		   			 err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
		   		} catch (IOException e) {
		   			// TODO Auto-generated catch block
		   			e.printStackTrace();
		   		}
		   			//RootTools.getShell(true).add(command).waitForFinish();
		    catch (InterruptedException e) {
		   			// TODO Auto-generated catch block
		   			e.printStackTrace();
		   		}
			  }
    		if(missing){
    		command = "cd /data/local/tmp/ && chmod 777 freegee-backup.sh && sh freegee-backup.sh";
    		try {
   			 err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
   		} catch (IOException e) {
   			// TODO Auto-generated catch block
   			e.printStackTrace();
   		}
   			//RootTools.getShell(true).add(command).waitForFinish();
    catch (InterruptedException e) {
   			// TODO Auto-generated catch block
   			e.printStackTrace();
   		}
    		}
    		
    		if (err == 0){
    			publishProgress(1);
    			command = "cd /data/local/tmp/ && chmod 777 freegee-install.sh && sh freegee-install.sh";
    			try {
    				 err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    				//RootTools.getShell(true).add(command).waitForFinish();
    	 catch (InterruptedException e) {
    				// TODO Auto-generated catch block
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
        	
        	String command = "busybox cp /sdcard/freegee/freegee-restore.sh /data/local/tmp";
        	//int err = 0;
        	try {
			    err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	if ( err == 0 ){ 
        	command = "cd /data/local/tmp/ && chmod 777 freegee-restore.sh && sh freegee-restore.sh";
        	try {
				err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	if (err !=0){
        		 err=-1;
        	}
        	}
        	else{
        		 err=-2;
        	}
            return null;

        }
        protected void onProgressUpdate(String... progress) {
           //  mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
        	removeDialog(DIALOG_RESTORE_PROGRESS);
        	if(err==-1){
        		alertbuilder("Error","Restoring failed! Please make sure backups and freegee-restore.sh are in /sdcard/freegee","Ok!",0);
        	}
        	else if(err==-2){
        		alertbuilder("Error","Restore script not found! Please put backups and freegee-restore.sh in /sdcard/freegee","Ok!",0);
        	}
        	else{
        	 alertbuilder("Success!","Success. Your Optimus G has been restored!","Yay!",0);
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
    			FreeGee.this.finish();
    			}
    	}
    	});

    	// create alert dialog
    	AlertDialog alertDialog = alertDialogBuilder.create();

    	// show it
    	alertDialog.show();

    	}
}