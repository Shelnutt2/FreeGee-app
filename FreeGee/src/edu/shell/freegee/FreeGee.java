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
import java.util.concurrent.TimeoutException;

import com.stericson.RootTools.CommandCapture;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.RootToolsException;



import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FreeGee extends Activity {
   
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    public static final int DIALOG_INSTALL_PROGRESS = 1;
    private Button startBtn;
    private ProgressDialog mProgressDialog;
   
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_gee);
        startBtn = (Button)findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
                startDownload();
            }
        });
    }

    private void startDownload() {
    	String device;
    	
    	
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
			//varient = "sprint";
        String url = "http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/sprint/private/freegee/freegee-apk-sprint.tar";
        new DownloadFileAsync().execute(url);
        }
		else if(device.equalsIgnoreCase("geeb_att_us")){
			//varient = "att";
			new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/att/private/freegee/freegee-apk-att.tar");
		}
		else if(device.equalsIgnoreCase("geeb_rgs_ca")){
			//varient = "rogers";
			new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/rogers/private/freegee/freegee-apk-rogers.tar");
		}
		else if(device.equalsIgnoreCase("geeb_tls_ca")){
			//varient = "telus";
			new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/telus/private/freegee/freegee-apk-telus.tar");
		}
        new unlock().execute();
		
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
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
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
                Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);

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
             Log.d("ANDRO_ASYNC",progress[0]);
             mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
        }
    }
    
    private class unlock extends AsyncTask<String, Integer, String>{

    	@Override
        protected void onPreExecute() {
            super.onPreExecute();
            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
            showDialog(DIALOG_INSTALL_PROGRESS);
        }

		protected String doInBackground(String...Params) {
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
    	int err = 0;
    	publishProgress((int)((25)));
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
    	publishProgress((int)((40)));
    	if (err == 0){
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
    		if (err == 0){
    			publishProgress((int)((60)));
    			command = "cd /data/local/tmp/ && chmod 777 freegee-install.sh && sh freegee-backup.sh";
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
    		}
    	
    	publishProgress((int)((100)));
		return null;
		}
		
		protected void onProgressUpdate(String... progress) {
            Log.d("ANDRO_ASYNC",progress[0]);
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
       }

       @Override
       protected void onPostExecute(String unused) {
           dismissDialog(DIALOG_INSTALL_PROGRESS);
       }
    	
    }	
}