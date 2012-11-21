package edu.shell.freegee;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeoutException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
    ProgressDialog myDialog;
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
                // TODO Auto-generated method stub
              try {
				unlock();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RootToolsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_free_gee, menu);
        return true;
    }
    
    public void unlock() throws  RootToolsException, InterruptedException, IOException, TimeoutException{
    	
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
        myDialog = new ProgressDialog(FreeGee.this);
        myDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        myDialog.setTitle("Unlocking");
        myDialog.setMessage("Downloading images:");
        myDialog.show();
        download_images();
        myDialog.incrementProgressBy(20);

        if (RootTools.isAccessGiven()) {
        	backuppartitons();
        	erasepartitions();
         	installpartitions();
        	test();
        	myDialog.dismiss();
        	alertbuilder("Success!","Success. Your "+varient+" OptimusG has been liberated!","Yay!",0);
        }
        else{
        	alertbuilder("Not Rooted","Please root you're phone!", "Ok",1);
        	}        	
        }
    

	private void test() {
	    RootTools.Result result = new RootTools.Result() {
	        @Override
	        public void process(String line) throws Exception {
	            // Do something with current line;
	            // Maybe store it using setData()
	        }

	        @Override
	        public void onFailure(Exception ex) {
	            // Do something if we failed while trying to run a command or read its output
	            setError(1);
	        }

	        @Override
	        public void onComplete(int diag) {
	            // Invoked when we are done reading one or more command's output.
	            // Convenient because we are still within the context of our result object.
	        }

			@Override
			public void processError(String arg0) throws Exception {
				// TODO Auto-generated method stub
				
			}

	    };
	    try {
			RootTools.sendShell(
			    "ls /sdflksmdfs",
			    result,
			    -1
			);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RootToolsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    if(0 != result.getError()){
	    	Toast.makeText(this,"Test error code is: "+ result.getError(), Toast.LENGTH_LONG).show();
	    }
	
	
		
	}

	private void installpartitions() throws IOException, TimeoutException {
    	String command =  ("busybox dd if=/data/local/tmp/aboot-"+varient+"-freegee.img of=/dev/block/platform/msm_sdcc.1/by-name/aboot");
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
    		myDialog.setMessage("Copying bootloader");
    		myDialog.incrementProgressBy(5);
			RootTools.sendShell(command, result, -1);
		} catch (RootToolsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	Integer errcode = result.getError(); 
    	//Toast.makeText(this,"Chmod error code is: "+ errcode, Toast.LENGTH_LONG).show();
    	if(errcode != null && errcode != 0){
    		alertbuilder("Error", "Can't install new bootloader. DO NOT TURN OFF YOUR PHONE. Contact Shelnutt2 or thecubed on XDA or IRC (freenode #lg-optimus-g).","Ok",1);
    		
    	}
    	
    	command =  ("busybox dd if=/data/local/tmp/recovery-"+varient+"-freegee.img of=/dev/block/platform/msm_sdcc.1/by-name/recovery");
    	try {
    		myDialog.setMessage("Copying cwm recovery");
    		myDialog.incrementProgressBy(5);
			RootTools.sendShell(command, result, -1);
		} catch (RootToolsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	errcode = result.getError(); 
    	//Toast.makeText(this,"Chmod error code is: "+ errcode, Toast.LENGTH_LONG).show();
    	if(errcode != null && errcode != 0){
    		alertbuilder("Error", "Can't install new recovery image. DO NOT TURN OFF YOUR PHONE. Contact Shelnutt2 or thecubed on XDA or IRC (freenode #lg-optimus-g).","Ok",1);
    		
    	}
    	command =  ("busybox dd if=/data/local/tmp/boot-"+varient+"-freegee.img of=/dev/block/platform/msm_sdcc.1/by-name/boot");
    	try {
    		myDialog.setMessage("Copying unlocked boot image");
    		myDialog.incrementProgressBy(5);
			RootTools.sendShell(command, result, -1);
		} catch (RootToolsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	errcode = result.getError(); 
    	//Toast.makeText(this,"Chmod error code is: "+ errcode, Toast.LENGTH_LONG).show();
    	if(errcode != null && errcode != 0){
    		alertbuilder("Error", "Can't install new boot image. DO NOT TURN OFF YOUR PHONE. Contact Shelnutt2 or thecubed on XDA or IRC (freenode #lg-optimus-g).","Ok",1);
    		
    	}
		
	}

	private void erasepartitions() throws IOException, TimeoutException {
		

	    /*File file = getBaseContext().getFileStreamPath("/data/local/tmp/recovery-"+varient+"-freegee.img");
	    if(!file.exists()){
	    	alertbuilder("Error","can't find new images, something went wrong in download or untar, aborting","ok",1);
	    }*/
	    	  
    	String command =  ("busybox dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/recovery");
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
    		myDialog.setMessage("Erasing recovery");
    		myDialog.incrementProgressBy(5);
			RootTools.sendShell(command, result, -1);
		} catch (RootToolsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	Integer errcode = result.getError(); 
    	/*Toast.makeText(this,"Chmod error code is: "+ errcode, Toast.LENGTH_LONG).show();
    	if(errcode != null && errcode != 0){
    		alertbuilder("Error", "Can't create backup directory","Ok",1);
    		
    	}*/
    	
    	command =  ("busybox dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/boot");
    	try {
    		myDialog.setMessage("Erasing boot image");
    		myDialog.incrementProgressBy(5);
			RootTools.sendShell(command, result, -1);
		} catch (RootToolsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	errcode = result.getError(); 
    	/*Toast.makeText(this,"Chmod error code is: "+ errcode, Toast.LENGTH_LONG).show();
    	if(errcode != null && errcode != 0){
    		alertbuilder("Error", "Can not make backup of boot image, aborting","Ok",1);
    		
    	}*/
    	command =  ("busybox dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/aboot");
    	try {
    		myDialog.setMessage("Ereasing bootloader");
    		myDialog.incrementProgressBy(5);
			RootTools.sendShell(command, result, -1);
		} catch (RootToolsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	errcode = result.getError(); 
    	/*Toast.makeText(this,"Chmod error code is: "+ errcode, Toast.LENGTH_LONG).show();
    	if(errcode != null && errcode != 0){
    		alertbuilder("Error", "Can not make backup of boot image, aborting","Ok",1);
    		
    	}*/
	}

	public void untar() throws InterruptedException, IOException, TimeoutException{
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
    		myDialog.setMessage("Extracting images from download");
    		myDialog.incrementProgressBy(5);
			RootTools.sendShell(command, result, -1);
		} catch (RootToolsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	Integer errcode = result.getError(); 
    	//Toast.makeText(this,"Error code is: "+ errcode, Toast.LENGTH_LONG).show();
    	if(errcode != null && errcode != 0){
    		alertbuilder("Error", "Can't untar images!","Ok",1);
    		
    	}
    	
    }	
    public void backuppartitons() throws InterruptedException, IOException, TimeoutException{
    	String command =  ("mkdir Environment.getExternalStorageDirectory().getPath()/freegee");
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
    		myDialog.setMessage("Making backup directory");
    		myDialog.incrementProgressBy(5);
			RootTools.sendShell(command, result, -1);
		} catch (RootToolsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	Integer errcode = result.getError(); 
    	//Toast.makeText(this,"Chmod error code is: "+ errcode, Toast.LENGTH_LONG).show();
    	if(errcode != null && errcode != 0){
    		alertbuilder("Error", "Can't create backup directory","Ok",1);
    		
    	}
    	command =  ("busybox dd if=/dev/block/platform/msm_sdcc.1/by-name/aboot of=/sdcard/freegee/aboot-"+varient+"-backup.img");
    	try {
    		myDialog.setMessage("Backing up bootloader");
    		myDialog.incrementProgressBy(5);
			RootTools.sendShell(command, result, -1);
		} catch (RootToolsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	errcode = result.getError(); 
    	//Toast.makeText(this,"Chmod error code is: "+ errcode, Toast.LENGTH_LONG).show();
    	if(errcode != null && errcode != 0){
    		alertbuilder("Error", "Can not make backup of bootloader, aborting","Ok",1);
    		
    	}
    	command =  ("busybox dd if=/dev/block/platform/msm_sdcc.1/by-name/boot of=/sdcard/freegee/boot-"+varient+"-backup.img");
    	try {
    		myDialog.setMessage("Backing up boot image");
    		myDialog.incrementProgressBy(5);
			RootTools.sendShell(command, result, -1);
		} catch (RootToolsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	errcode = result.getError(); 
    	//Toast.makeText(this,"Chmod error code is: "+ errcode, Toast.LENGTH_LONG).show();
    	if(errcode != null && errcode != 0){
    		alertbuilder("Error", "Can not make backup of boot image, aborting","Ok",1);
    		
    	}
    	command =  ("busybox dd if=/dev/block/platform/msm_sdcc.1/by-name/recovery of=/sdcard/freegee/recovery-"+varient+"-backup.img");
    	try {
    		myDialog.setMessage("Backing up recovery");
    		myDialog.incrementProgressBy(5);
			RootTools.sendShell(command, result, -1);
		} catch (RootToolsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	errcode = result.getError(); 
    	//Toast.makeText(this,"Chmod error code is: "+ errcode, Toast.LENGTH_LONG).show();
    	if(errcode != null && errcode != 0){
    		alertbuilder("Error", "Can not make backup of bootloader, aborting","Ok",1);
    		
    	}

    }
    
    public void download_images() throws InterruptedException, IOException, TimeoutException, RootToolsException{
    	String device = android.os.Build.DEVICE;
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
    		if(device.equalsIgnoreCase("geehrc4g")){
    			varient = "sprint";
    			downloadFile.execute("http://jellybean.dccontests.com/optimusg/sprint/freegee-sprint.tar");	
    	    }
    		else if(device.equalsIgnoreCase("geeb")){
    			varient = "att";
    			downloadFile.execute("http://jellybean.dccontests.com/optimusg/att/freegee-att.tar");
    		}
    		else{
    			alertbuilder("Not supporter", "Your device currently isn't support","ok",1);
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

