/** @author Seth Shelnutt
 * @License GPLv3 or later
 * All source code is released free and openly by Seth Shelnutt under the terms of GPLv3 or later, 2013 
 * */

package edu.shell.freegee;

import java.io.BufferedInputStream;
import java.io.File;
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
import java.util.Date;
import java.util.concurrent.TimeoutException;

import com.octo.android.robospice.JacksonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import edu.shell.freegee.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;
import android.graphics.PorterDuff;

@SuppressLint("SdCardPath")
public class FreeGee extends Activity implements OnClickListener {
   
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    public static final int DIALOG_INSTALL_PROGRESS = 1;
    public static final int DIALOG_RESTORE_PROGRESS = 2;
    public static final int DIALOG_BACKUP_PROGRESS = 3;
    private Button startBtn;
    private Button restoreBtn;
    private Button utilBtn;
    private boolean sblopen = false;
    private Device myDevice;
    private String saveloc;
    
    private static ProgressDialog mProgressDialog;
    public static boolean isSpecial;
    private ArrayList<Object> mButtons = new ArrayList<Object>();
    private static final String JSON_CACHE_KEY = "freegee_json";

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
        setContentView(R.layout.activity_freegee);
    	File freegeef=new File("/sdcard/freegee");
		  if(!freegeef.exists()){
			  freegeef.mkdirs();
		  }
		if (!RootTools.isAccessGiven()) {
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}
		if(getBatteryLevel() < 15.0)
			alertbuilder("Error!","Your batter is too low to do anything, please charge it or connect an ac adapter","OK",1);
		
		if(!new File("/data/data/edu.shell.freegee/edifier").exists()){
		  InputStream in = null;
		  OutputStream out = null;
		  try {
			// read this file into InputStream
			in = getAssets().open("edifier");
	 
			// write the inputStream to a FileOutputStream
			out = new FileOutputStream(new File("/data/data/edu.shell.freegee/edifier"));
			int read = 0;
			byte[] bytes = new byte[50468];
	 
			while ((read = in.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}	 
		  } catch (IOException e) {
			Log.e("Freegee","Edifier not found in assets");
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
		
		CommandCapture command = new CommandCapture(0,"chmod 744 /data/data/edu.shell.freegee/edifier");
		try {
			RootTools.getShell(true).add(command).isFinished();
		} catch (IOException e) {
			Log.e("Freegee","Edifier not found in assets");
			e.printStackTrace();
		} catch (TimeoutException e) {
			Log.e("Freegee","Chmod timed out");
			e.printStackTrace();
		} catch (RootDeniedException e) {
			Log.e("Freegee","No root access!");
			e.printStackTrace();
		}
		if(!spiceManager.isStarted())
           spiceManager.start( this );
        
		myDevice = new Device();
		myDevice.setName("LG Optimus G");
		ArrayList<Action> actions = new ArrayList<Action>();
		Action unlock = new Action();
		unlock.setName("unlock");
		actions.add(unlock);
		
		Action recovery = new Action();
		recovery.setName("recovery");
		actions.add(recovery);
		
		myDevice.setActions(actions);
		updateGridView(myDevice);
		GridView gridView = (GridView) findViewById(R.id.main_gridview);
		gridView.setAdapter(new ButtonAdapter(mButtons));
    }
    
    public void updateGridView(Device device){
        for(int i = 0; i < device.getActions().size();i++){
    		Button cb = new Button(this);
  		    cb.setText(device.getActions().get(i).getName());
  		    if(i % 2 == 1){
  		      cb.getBackground().setColorFilter(Color.parseColor("#f47321"), PorterDuff.Mode.DARKEN);
  		      cb.setTextColor(Color.parseColor("#005030"));
            }
  		    else{
  		      cb.getBackground().setColorFilter(Color.parseColor("#005030"), PorterDuff.Mode.DARKEN);
  		      cb.setTextColor(Color.parseColor("#f47321"));
            }
  		    cb.setTypeface(null, Typeface.BOLD);
  		    cb.setPadding(100, 100, 100, 100);
  		    cb.setOnClickListener(this);
  		    cb.setId(i);
  		    mButtons.add(cb);
        }
    }
    
    @Override
    public void onClick(View v) {
     Button selection = (Button)v;
     Toast.makeText(getBaseContext(), selection.getText()+ " was pressed!", Toast.LENGTH_SHORT).show();
     Action b;
     for(Action a:myDevice.getActions()){
    	 if(a.getName().equals(selection.getText())){
    		 b = a;
    	     new helper().process(b);
    		 break;
    	 }
    	 else{
    		 Log.e("Freegee","Confused by action pressed");
    	     alertbuilder("Error","Can't find the action to perform you requested","uh oh",0);
    	 }
     }
     //Toast.makeText(getBaseContext(), "Result is: "+result, Toast.LENGTH_SHORT).show();
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
    
    protected SpiceManager spiceManager = new SpiceManager( JacksonSpringAndroidSpiceService.class );
    
    private class FreegeeRequestListener implements RequestListener< Device >{

        @Override
        public void onRequestFailure( SpiceException spiceException ) {
          //update your UI
        }

        @Override
        public void onRequestSuccess( Device device ) {
        	updateGridView(device);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
		if(!spiceManager.isStarted())
	        spiceManager.start( this );
    }

    @Override
    protected void onStop() {
    	if(spiceManager.isStarted())
           spiceManager.shouldStop();
        super.onStop();
    }

    public void refreshSupported() {
        spiceManager.execute( new FreegeeJsonRequest(), JSON_CACHE_KEY, DurationInMillis.ALWAYS_EXPIRED, new FreegeeRequestListener() );
    }
    
    public float getBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if(level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float)level / (float)scale) * 100.0f; 
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
                setmProgressDialog(new ProgressDialog(this));
                getmProgressDialog().setMessage("Downloading file..");
                getmProgressDialog().setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                getmProgressDialog().setCancelable(false);
                getmProgressDialog().show();
                return getmProgressDialog();
		case DIALOG_INSTALL_PROGRESS:
                setmProgressDialog(new ProgressDialog(this));
                getmProgressDialog().setMessage("Installing..");
                getmProgressDialog().setProgressStyle(ProgressDialog.STYLE_SPINNER);
                getmProgressDialog().setCancelable(false);
                getmProgressDialog().show();
                return getmProgressDialog();
		case DIALOG_BACKUP_PROGRESS:
            setmProgressDialog(new ProgressDialog(this));
            getmProgressDialog().setMessage("Backing Up..");
            getmProgressDialog().setProgressStyle(ProgressDialog.STYLE_SPINNER);
            getmProgressDialog().setCancelable(false);
            getmProgressDialog().show();
            return getmProgressDialog();
		case DIALOG_RESTORE_PROGRESS:
			setmProgressDialog(new ProgressDialog(this));
            getmProgressDialog().setMessage("Restoring..");
            getmProgressDialog().setProgressStyle(ProgressDialog.STYLE_SPINNER);
            getmProgressDialog().setCancelable(false);
            getmProgressDialog().show();
            return getmProgressDialog();
		default:
                return null;
        }
    }
    
    class DownloadFileAsync extends AsyncTask<String, String, String> {
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
                OutputStream output = new FileOutputStream(saveloc);

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
             getmProgressDialog().setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
        	removeDialog(DIALOG_DOWNLOAD_PROGRESS);
        	

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
	public static ProgressDialog getmProgressDialog() {
		return mProgressDialog;
	}
	public void setmProgressDialog(ProgressDialog mProgressDialog) {
		this.mProgressDialog = mProgressDialog;
	}
}