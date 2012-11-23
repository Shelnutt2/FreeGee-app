package edu.shell.freegee;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.stericson.RootTools.Command;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.RootTools.Result;
import com.stericson.RootTools.RootToolsException;


public class FreeGee extends Activity {
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private ProgressDialog mProgressDialog;
    private ProgressDialog myDialog;
    boolean abort;
    //private static final Activity FreeGee = null;
    private String varient;
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_gee);
        
        Button button = (Button) findViewById(R.id.unlock);
        button.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
              myDialog = new ProgressDialog(FreeGee.this);
			  myDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			  myDialog.setTitle("Unlocking");
			  myDialog.setMessage("Downloading images:");
			  myDialog.show();

			Thread backgroundThread = new Thread(new Runnable() {            
			    @Override
			    public void run() {
			        // keep sure that this operations
			        // are thread-safe!
			    	try {
						unlock();
					} catch (RootToolsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				        catch (Exception e){
				        e.printStackTrace();
					}

			        runOnUiThread(new Runnable() {                    
			            @Override
			            public void run() {
			            //	myDialog.dismiss();                        
			            }
			        });
			    }
			});
			backgroundThread.start();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_free_gee, menu);
        return true;
    }
    
    
    public void unlock() throws  RootToolsException, InterruptedException, IOException, TimeoutException, ExecutionException{
    	
    	try
        {
        	RootTools.getShell(true);
        }
        catch (Exception e)
        {
        	
        }
        
        if (!RootTools.isBusyboxAvailable()) {
        	RootTools.offerBusyBox(this);
        }

        download_images();
        this.runOnUiThread(new Runnable() {
        	  public void run() {
        	    myDialog.incrementProgressBy(25);
        	  }
        	});
        

        if (RootTools.isAccessGiven()) {
            if(!abort){

            	untar unt = new untar();
        	   unt.execute().get(30, TimeUnit.SECONDS);
        	if(!abort){

        		backuppartitons bckpart = new backuppartitons();
        		bckpart.execute().get(60, TimeUnit.SECONDS);;
        	if(!abort){
         	    installpartitions inpart = new installpartitions();
         	   inpart.execute().get(60, TimeUnit.SECONDS);;
         	if(!abort){
        	    //myDialog.dismiss();
        	//alertbuilder("Success!","Success. Your "+varient+" Optimus G has been liberated!","Yay!",0);
        }
         	}
        	}
            }
         
        }
        else{
        	alertbuilder("Not Rooted","Please root you're phone!", "Ok",1);
        	}        	
        }
    
	private class untar extends AsyncTask<Void, Integer, String>{
		
		protected void onPreExecute() { 
			runOnUiThread(new Runnable() {
				  public void run() {
						myDialog.setMessage("Extracting images from download");
						myDialog.incrementProgressBy(25);
				  }
				});

		}
		protected String doInBackground(Void...Params) {
		boolean hastarinbusybox = false;
		try {
			for (String curVal : RootTools.getBusyBoxApplets()){
				  if (curVal.matches("tar")){
					  hastarinbusybox = true;
				  }
				}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	if(hastarinbusybox == false){
    		//alertbuilder("No tar support","Your busybox doesn't support tar, please download the new one from the market","ok",0);
    		//not working yet
    		//Activity activity = FreeGee;
			
			//Intent intent = RootTools.offerBusyBox(activity, 0);    		
    	}
    	
    	String command =  "cd /data/local/tmp/ && busybox tar xvf freegee.tar";
    	RootTools.Result  result = new RootTools.Result() {
			
			@Override
			public void processError(String arg0) throws Exception {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void process(String arg0) throws Exception {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFailure(Exception arg0) {
				setError(1);

				
			}
			
			@Override
			public void onComplete(int arg0) {
				// TODO Auto-generated method stub
				
			}
		};
    	try {
			RootTools.sendShell(command, result, -1);
		} catch (RootToolsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	Integer errcode = result.getError(); 
    	//Toast.makeText(this,"Error code is: "+ errcode, Toast.LENGTH_LONG).show();
    	if(errcode != null && errcode != 0){
    		alertbuilder("Error", "Can't untar images!","Ok",1);
    		
    	}
    	File f = new File("/data/local/tmp/freegee-working");
    	if(!f.exists()) {
    		alertbuilder("Error", "Can't untar images! Aborting!","Ok",1);
    	}
		return null;
		}
    	
    }	
	private class backuppartitons extends AsyncTask<Void, Integer, String>{
		
		protected void onPreExecute() { 
			runOnUiThread(new Runnable() {
				  public void run() {
			    	    myDialog.setMessage("Making backup directory");
			    		myDialog.incrementProgressBy(25);
				  }
				});

		}
		protected String doInBackground(Void...Params) {
    	String command =  ("cd /data/local/tmp/ && chmod 777 freegee-backup.sh && sh freegee-backup.sh");
    	RootTools.Result  result = new RootTools.Result() {
			
			@Override
			public void processError(String arg0) throws Exception {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void process(String arg0) throws Exception {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFailure(Exception arg0) {
				setError(1);

				
			}
			
			@Override
			public void onComplete(int arg0) {
				// TODO Auto-generated method stub
				
			}
		};
    	try {
			RootTools.sendShell(command, result, -1);
		} catch (RootToolsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	File f = new File("/sdcard/freegee/error");
    	if(f.exists()) {
    		alertbuilder("Error", "Can't make backups! Aborting!","Ok",1);
    	}
		return null;
		}
    }
    
	private class installpartitions extends AsyncTask<Void, Integer, String>{
		protected void onPreExecute() { 
			runOnUiThread(new Runnable() {
				  public void run() {
			    		myDialog.setMessage("Copying bootloader");
			    		myDialog.incrementProgressBy(25);
				  }
				});
		}
		protected String doInBackground(Void...Params) {
    	String command =  ("cd /data/local/tmp/ && chmod 777 freegee-install.sh && sh freegee-backup.sh");
    	RootTools.Result  result = new RootTools.Result() {
			
			@Override
			public void processError(String arg0) throws Exception {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void process(String arg0) throws Exception {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFailure(Exception arg0) {
				setError(1);

				
			}
			
			@Override
			public void onComplete(int arg0) {
				// TODO Auto-generated method stub
				
			}
		};
    	try {

			RootTools.sendShell(command, result, -1);
		} catch (RootToolsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	File f = new File("/sdcard/freegee/error");
    	if(f.exists()) {
    		alertbuilder("Error", "Can't flash images. Do not reboot, contact Shelnutt2 or thecubed on xda or irc","Ok",1);
    	}
		return null;
		}
	}
    
    public void download_images() throws InterruptedException, IOException, TimeoutException, RootToolsException, ExecutionException{
    	String device;
    	
    	
    		// read the property text  file
    		File file = new File("/system/build.prop");
    		FileInputStream fis = new FileInputStream(file);

    		Properties prop = new Properties();
    		// feed the property with the file
    		prop.load(fis);

    		// Get the application to print out all the key and the value
    		// of your property file
    		device = prop.getProperty("ro.product.name");
    	    	
    	//Toast.makeText(this,"Device is: "+ device, Toast.LENGTH_LONG).show();
    	
    		// declare the dialog as a member field of your activity
    		
    	//	ProgressDialog mProgressDialog;

    		// instantiate it within the onCreate method
    		mProgressDialog = new ProgressDialog(FreeGee.this);
    		mProgressDialog.setMessage("A message");
    		mProgressDialog.setIndeterminate(false);
    		mProgressDialog.setMax(100);
    		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    		
    		// execute this when the downloader must be fired
    		DownloadFile downloadFile = new DownloadFile();
    		if(device.equalsIgnoreCase("geehrc4g_spr_us")){
    			varient = "sprint";
    			//http://downloads.codefi.re/direct.php?file=Shelnutt2/optimusg/sprint/private/freegee/freegee-apk-sprint.tar
    			downloadFile.execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/sprint/private/freegee/freegee-apk-sprint.tar").get(1000, TimeUnit.SECONDS);	
    	    }
    		else if(device.equalsIgnoreCase("geeb_att_us")){
    			varient = "att";
    			downloadFile.execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/att/private/freegee/freegee-apk-att.tar").get(1000, TimeUnit.SECONDS);
    		}
    		else if(device.equalsIgnoreCase("geeb_rgs_ca")){
    			varient = "rogers";
    			downloadFile.execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/rogers/private/freegee/freegee-apk-rogers.tar").get(1000, TimeUnit.SECONDS);
    		}
    		else if(device.equalsIgnoreCase("geeb_tls_ca")){
    			varient = "telus";
    			downloadFile.execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/telus/private/freegee/freegee-apk-telus.tar").get(1000, TimeUnit.SECONDS);
    		}
    		else{
    			alertbuilder("Not supported", "Your device currently isn't support","ok",1);
    		}
    		
    		File f = new File(Environment.getExternalStorageDirectory().getPath() +"/freegee.tar");
    		if(!f.exists()) { 
    			alertbuilder("Download Failed", "The download failed. Please make sure you have internet access","ok",1);
    		 }
    		
    		String command =  ( "busybox mv "+ Environment.getExternalStorageDirectory().getPath() +"/freegee.tar /data/local/tmp/");
        	
        	RootTools.Result  result = new RootTools.Result() {
				
				@Override
				public void processError(String arg0) throws Exception {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void process(String arg0) throws Exception {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onFailure(Exception arg0) {
					setError(1);

					
				}
				
				@Override
				public void onComplete(int arg0) {
					// TODO Auto-generated method stub
					
				}
			};
        	//RootTools.getShell(true).add(command).waitForFinish();
        	RootTools.sendShell(command, result, -1);
        	Integer errcode = result.getError(); 
        	//Toast.makeText(this,"Move error code is: "+ errcode, Toast.LENGTH_LONG).show();
        	if(errcode != null && errcode != 0){
        		alertbuilder("Error", "Can't move files to /data/local/tmp","Ok",1);
        		
        	}
    	
    }
    
 // usually, subclasses of AsyncTask are declared inside the activity class.
 // that way, you can easily modify the UI thread from here
 private class DownloadFile extends AsyncTask<String, Integer, String> {
     @Override
     protected String doInBackground(String... sUrl) {
         try {
             URL url = new URL(sUrl[0]);
             URLConnection connection = url.openConnection();
             connection.connect();
             // this will be useful so that you can show a typical 0-100% progress bar
             int fileLength = connection.getContentLength();

             // download the file
             InputStream input = new BufferedInputStream(url.openStream());
             OutputStream output = new FileOutputStream( Environment.getExternalStorageDirectory().getPath() +"/freegee.tar");

             byte data[] = new byte[1024];
             long total = 0;
             int count;
             while ((count = input.read(data)) != -1) {
                 total += count;
                 // publishing the progress....
                 publishProgress((int) (total * 100 / fileLength));
                 output.write(data, 0, count);
             }

             output.flush();
             output.close();
             input.close();
         } catch (Exception e) {
         }
         return null;
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
				abort = true;
				}
			}
		  });

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	
}


 
}

