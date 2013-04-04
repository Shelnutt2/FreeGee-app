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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import edu.shell.freegee.R;
import edu.shell.freegee.utilities.DBUpload;


@SuppressLint("SdCardPath")
public class install extends Activity {

    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    public static final int DIALOG_INSTALL_PROGRESS = 1;
    public static final int DIALOG_RESTORE_PROGRESS = 2;
    public static final int DIALOG_BACKUP_PROGRESS = 3;
    private Button recoveryBtn;
    private String saveloc;
    public static String device, version, aversion;
    public static String id = "cf573ca8ea7d2";
    public static String incremental = "61249c215428d3309c";
    public static String factoryversion = "46d0622d0";
    private String recovery;
    private String boot;
    private String aboot_md5sum = "8a742f5776e73df2e6753b1694cda7e2";
    private String sbl1_md5sum = "25d877b9fc5852846478b8e583be020a";
    private String sbl2_md5sum = "20732aa3ad2eb2049c32ce55c00b3edb";
    private String sbl3_md5sum = "afdf190f364cec079050ce7750251b20";
    private String sbl1_after_md5sum = "25d877b9fc5852846478b8e583be020a";
    private String sbl2_after_md5sum = "3ab81262ccbd8df348aae1d4ab296401";
    private String sbl3_after_md5sum = "77a4d6622b8f169ce74b39b5403a6c78";
    private String recovery_md5sum; 
    private String boot_md5sum; 
	private HashMap<String,String[]> RSmap = new HashMap<String,String[]>();
	private HashMap<String,String[]> BSmap = new HashMap<String,String[]>();
    private String dfname;
    private int step = 0;
    private int sstep = 0;
	private ProgressDialog mProgressDialog;
	SharedPreferences prefs;
	private boolean isSpecial;
	private String sbl1 = "sbl1-freegee";
	private String sbl2 = "sbl2-freegee";
	private String sbl3 = "sbl3-freegee";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install);
        
        prefs = getSharedPreferences("FreeGee",MODE_PRIVATE);
        
        String command;
        
    isSpecial = isSpecial();//FreeGee.isSpecial;
	// read the property text  file
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
	version = prop.getProperty("ro.lge.swversion");
	aversion = prop.getProperty("ro.build.version.release");
	ListView lv = (ListView) findViewById(R.id.install_list_view);
    String[] lStr = new String[]{"Device Name: "+device,"Software Version: "+version};
    lv.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, lStr));

    String url = "http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/freegee/supported.xml";
    saveloc="/sdcard/freegee/supported.xml";
    dfname = "supported device list";
    new DownloadFileAsync().execute(url);
    
    recoveryBtn = (Button)findViewById(R.id.recoveryBtn);
    recoveryBtn.setOnClickListener(new OnClickListener(){
        public void onClick(View v) {

        		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(install.this);
        	    
            	// set title
            	alertDialogBuilder.setTitle("Warning");

            	// set dialog message
            	alertDialogBuilder
            	.setMessage("By Pressing I agree you are acknowledging that you are voiding you are voiding you warrenty and no one from team codefire can be held responsible.")
            	.setCancelable(false)
            	.setPositiveButton("I agree",new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog,int id) {
            	   	File freegeefw=new File("/sdcard/freegee/working");
          		  if(!freegeefw.exists()){
          			  freegeefw.mkdirs();
          		  }
          		if(!isSpecial && aversion.equals("4.1.2") && !device.equals("geehrc4g_spr_us")){
          			//alertbuilderu("Sorry!","Sorry you are running a varient with jellybean which requires the sbl unlock. You must read and accept the warning first disabled on this activity.","Ok",0);
          			AlertDialog alertDialog;
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(install.this);
	        	    
	    	    	// set title
	    	    	alertDialogBuilder.setTitle("Please Read Carefully");

	    	    	// set dialog message
	    	    	alertDialogBuilder
	    	    	.setMessage("Your device is running jellybean and requires the new special unlock. This unlock flashes your sbl stack. If anything goes wrong you might hard brick. Download mode is stored in the sbl stack, thus if it fails to flash you can't recover. If you get any errors or any problems do NOT reboot. Find Shelnutt2 on xda or IRC. After you enable this click 'Unlock my Optimus G' to proceed.")
	    	    	.setCancelable(false)
	    	    	.setPositiveButton("I Understand",new DialogInterface.OnClickListener() {
	    	    	public void onClick(DialogInterface dialog,int id) {
	    	    	// if this button is clicked, close
	    	    	// current activity
	    	    		//Toast.makeText(install.this, "Jellybean unlock NOT enabled, please read the instructions!", Toast.LENGTH_LONG).show();
	    	    		isSpecial = true;
                        sbl1 = "sbl1-E971-ICS";
                        sbl2 = "sbl2-E971-ICS";
                        sbl3 = "sbl3-E971-ICS";
                        sbl1_md5sum = "ff193e1835c633d94c61240085428c9d"; 
                        sbl2_md5sum = "13d7941c2aada2b67e4e6f3f0ce5d31e";
                        sbl3_md5sum = "4ac3be33e8a5e8b83b1212160a769e7c";
                        sbl1_after_md5sum = "ff193e1835c633d94c61240085428c9d";
                        sbl2_after_md5sum = "13d7941c2aada2b67e4e6f3f0ce5d31e";
                        sbl3_after_md5sum = "4ac3be33e8a5e8b83b1212160a769e7c";
                        Toast.makeText(install.this, "Jellybean unlock enabled", Toast.LENGTH_SHORT).show();
                        step=1;
              		    dfname="Bootloader";
              		    saveloc="/sdcard/freegee/working/aboot-freegee.img";
              		    new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/freegee/aboot-freegee.img");
	    	    	}
	    	    	})
	    	    	.setNegativeButton("I do not want to risk it", new DialogInterface.OnClickListener() {
	    				
	    				@Override
	    				public void onClick(DialogInterface dialog, int which) {
	    					Toast.makeText(install.this, "Jellybean unlock NOT enabled", Toast.LENGTH_LONG).show();
	    					install.this.finish();
	    					
	    				}
	    			});

	    	    	// create alert dialog
	    	    	alertDialog = alertDialogBuilder.create();
	    	    	

	    	    	// show it
	    	    	alertDialog.show();
          		}
          		  else{
          		    step=1;
          		    dfname="Bootloader";
          		    saveloc="/sdcard/freegee/working/aboot-freegee.img";
          		    new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/freegee/aboot-freegee.img");
          		  }
            	}
            	})
            	.setNegativeButton("I disagree",new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog,int id) {
            	// if this button is clicked, close
            	// current activity
            		
            			install.this.finish();
            			
            	}
            	});

            	// create alert dialog
            	AlertDialog alertDialog = alertDialogBuilder.create();

            	// show it
            	alertDialog.show();
           }	
               
       });
        
    

    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Downloading "+dfname+"..");
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
        int err;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_DOWNLOAD_PROGRESS);
        }

        @Override
        protected String doInBackground(String... aurl) {
            int count;
            boolean fgood = false;
            if(new File(saveloc).exists()){
              if(saveloc.contains("aboot")){
            	fgood=checkmd5sum("/sdcard/freegee/working/aboot-freegee.img",aboot_md5sum);
              }
              else if(saveloc.contains("boot")){
            	fgood=checkmd5sum("/sdcard/freegee/working/boot-freegee.img",boot_md5sum);
              }
              else if(saveloc.contains("recovery")){
            	fgood=checkmd5sum("/sdcard/freegee/working/recovery-freegee.img",recovery_md5sum);
              }
            }
            if(!fgood){
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
        	alertbuilder("Error!","There was a problem downloading a file. Please try agian.","Ok",0);
        	}
        	else{
            if(step==0){
        	readxml();
        	}
            else if(step==1){
            	step=2;
        		saveloc="/sdcard/freegee/working/boot-freegee.img";
        		dfname="Boot image";
        		new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/freegee/"+boot);
        		return;
            }
            else if(step==2){
            	step=3;
        		saveloc="/sdcard/freegee/working/recovery-freegee.img";
        		dfname="Recovery Image";
        		Spinner spinner = (Spinner) findViewById(R.id.recoveryspinner);
        		recovery = RSmap.get(spinner.getSelectedItem().toString())[0];
        		recovery_md5sum = RSmap.get(spinner.getSelectedItem().toString())[1];
        		new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/freegee/"+recovery);
        		return;
            }
            else if(step==3){
            	if(isSpecial){
            		sstep=1;
            		saveloc="/sdcard/freegee/working/sbl1-freegee.img";
            		dfname="SBL1 image";
            		new DownloadSBLFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/freegee/"+sbl1+".img");
            		return;
            	}
            	else{
            	  checkmd5sums();
            	  return;
            	}
            }
            
          }

        }
    }
    
    class DownloadSBLFileAsync extends AsyncTask<String, String, String> {
        int err;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_DOWNLOAD_PROGRESS);
        }

        @Override
        protected String doInBackground(String... aurl) {
            int count;
            boolean fgood = false;
            if(new File(saveloc).exists()){
              if(saveloc.contains("sbl1")){
            	fgood=checkmd5sum("/sdcard/freegee/working/sbl1-freegee.img",sbl1_md5sum);
              }
              else if(saveloc.contains("sbl2")){
            	fgood=checkmd5sum("/sdcard/freegee/working/sbl2-freegee.img",sbl2_md5sum);
              }
              else if(saveloc.contains("sbl3")){
            	fgood=checkmd5sum("/sdcard/freegee/working/sbl3-freegee.img",sbl3_md5sum);
              }
            }      
            if(!fgood){
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
        	alertbuilder("Error!","There was a problem downloading a file. Please try agian.","Ok",0);
        	}
        	else{
        		if(sstep==1){
            		sstep=2;
            		saveloc="/sdcard/freegee/working/sbl2-freegee.img";
            		dfname="SBL2 image";            		
            		new DownloadSBLFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/freegee/"+sbl2+".img");
            		return;
            		}
            	if(sstep==2){
            		sstep=3;
            		saveloc="/sdcard/freegee/working/sbl3-freegee.img";
            		dfname="SBL3 image";
            		new DownloadSBLFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/freegee/"+sbl3+".img");
            		return;
            		}
            	if(sstep==3){
            		checkmd5sums();
            		return;
            	}
            }
            
          }

        
    }
    
    private boolean isSpecial(){
        if(prefs.contains("Special")){
           if(prefs.getInt("Special", 2) == 3){
    	      return true;
    	   }
        }    	
    	   return false;    			   
    }
    
    private void checkmd5sums() {
    	int err = 0;
    	HashMap<String,String> files = new HashMap<String, String>();
    	files.put("recovery",recovery_md5sum);
    	files.put("aboot",aboot_md5sum);
    	files.put("boot",boot_md5sum);
    	if(isSpecial){
    		files.put("sbl1",sbl1_md5sum);
    		files.put("sbl2",sbl2_md5sum);
    		files.put("sbl3",sbl3_md5sum);
    	}
    	for(String f:files.keySet()){
    		 MessageDigest md = null;
			try {
				md = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e1) {
				
				e1.printStackTrace();
			}
    		    FileInputStream fis = null;
				try {
					fis = new FileInputStream("/sdcard/freegee/working/"+f+"-freegee.img");
				} catch (FileNotFoundException e1) {
					
					e1.printStackTrace();
				}
    		    byte[] dataBytes = new byte[1024];
    		    int nread = 0; 
    		    try {
    				while ((nread = fis.read(dataBytes)) != -1) {
    				  md.update(dataBytes, 0, nread);
    				}
    			} catch (IOException e) {
    				
    				e.printStackTrace();
    			};
    		    byte[] mdbytes = md.digest();

    		    //convert the byte to hex format method 1
    		    StringBuffer sb = new StringBuffer();
    		    for (int i = 0; i < mdbytes.length; i++) {
    		      sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
    		    }

    		    if(! sb.toString().equalsIgnoreCase(files.get(f))){
    		    	err = -1;
    		    	alertbuilder("Error!",f+" md5sum mismatch. Aborting.","Ok",1);
    		    }
    		    
    	}
    	if (err == 0){
    		new backup().execute();
    	}
    }
    
    private boolean checkmd5sum(String fname,String fmd5) {
    	int err = 0;
    		 MessageDigest md = null;
			try {
				md = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e1) {
				
				e1.printStackTrace();
			}
    		    FileInputStream fis = null;
				try {
					fis = new FileInputStream(fname);
				} catch (FileNotFoundException e1) {
					
					e1.printStackTrace();
				}
    		    byte[] dataBytes = new byte[1024];
    		    int nread = 0; 
    		    try {
    				while ((nread = fis.read(dataBytes)) != -1) {
    				  md.update(dataBytes, 0, nread);
    				}
    			} catch (IOException e) {
    				
    				e.printStackTrace();
    			};
    		    byte[] mdbytes = md.digest();

    		    //convert the byte to hex format method 1
    		    StringBuffer sb = new StringBuffer();
    		    for (int i = 0; i < mdbytes.length; i++) {
    		      sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
    		    }

    		    if(! sb.toString().equalsIgnoreCase(fmd5)){
    		    	err = -1;
    		    	
    		    	
    		    }
    		    
    	
    	if (err == 0){
    		return true;
    	}
    	else{
    		return false;
    	}
    }
    
    class backup extends AsyncTask<Void, Void, Void> {
    	int err = 0;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            removeDialog(DIALOG_DOWNLOAD_PROGRESS);
            showDialog(DIALOG_BACKUP_PROGRESS);
        }

        @SuppressLint("SimpleDateFormat")
		@Override
        protected Void doInBackground(Void... aurl) {
        	String command;
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            Date date = new Date();
            String df = dateFormat.format(date);
        	File freegeef=new File("/sdcard/freegee");
			  if(!freegeef.exists()){
				  freegeef.mkdirs();
			  }
 		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs1 of=/sdcard/freegee/m9kefs1-"+df+"-backup.img";
 		        	try {
 						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					} catch (IOException e) {
 						
 						e.printStackTrace();
 					}
 		        	
 			  if(err==0){
		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs2 of=/sdcard/freegee/m9kefs2-"+df+"-backup.img";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
 			  }
 			  
 			 if(err==0){
		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs3 of=/sdcard/freegee/m9kefs3-"+df+"-backup.img";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
     			 }
 			 
		      	if(err==0){
		         	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs1 of=/sdcard/freegee/m9kefs1-backup.img";
 		        	try {
 						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					} catch (IOException e) {
 						
 						e.printStackTrace();
 					}
	 			 }
		      	
		      	if(err==0){
		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs2 of=/sdcard/freegee/m9kefs2-backup.img";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
	 			 }
		      	
		      	if(err==0){
		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs3 of=/sdcard/freegee/m9kefs3-backup.img";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
	 			 }
		      	
		      	if(err==0){
		         	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/aboot of=/sdcard/freegee/aboot-backup.img";
 		        	try {
 						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					} catch (IOException e) {
 						
 						e.printStackTrace();
 					}
	 			 }
		      	
		      	if(err==0){
		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/boot of=/sdcard/freegee/boot-backup.img";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
	 			 }
		      	
		      	if(err==0){
		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/recovery of=/sdcard/freegee/recovery-backup.img";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
		      	}
		      	if(err==0){
		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/misc of=/sdcard/freegee/misc-backup.img";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
		      	}
		      	if(isSpecial){
			      	if(err==0){
			         	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/sbl1 of=/sdcard/freegee/sbl1-backup.img";
	 		        	try {
	 						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
	 					} catch (InterruptedException e) {
	 						
	 						e.printStackTrace();
	 					} catch (IOException e) {
	 						
	 						e.printStackTrace();
	 					}
		 			 }
			      	
			      	if(err==0){
			        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/sbl2 of=/sdcard/freegee/sbl2-backup.img";
			        	try {
							err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
						} catch (InterruptedException e) {
							
							e.printStackTrace();
						} catch (IOException e) {
							
							e.printStackTrace();
						}
		 			 }
			      	
			      	if(err==0){
			        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/sbl3 of=/sdcard/freegee/sbl3-backup.img";
			        	try {
							err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
						} catch (InterruptedException e) {
							
							e.printStackTrace();
						} catch (IOException e) {
							
							e.printStackTrace();
						}
			      	}
		      	}
					return null;
			  
        }
        protected void onProgressUpdate(String... progress) {
           //  mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(Void unused) {
        	removeDialog(DIALOG_BACKUP_PROGRESS);
        	if(err==0){
        	   new unlock().execute();
        	   return;
        	}
        	else{
        		alertbuilder("Error!","There was a problem creating backups. Aborting.","Ok",0);
        		return;
        	}
        	
        }
    }
    
    private class unlock extends AsyncTask<String, Integer, String>{

    	@Override
        protected void onPreExecute() {
            super.onPreExecute();
            removeDialog(DIALOG_BACKUP_PROGRESS);
            showDialog(DIALOG_INSTALL_PROGRESS);
        }
    	int err = 0;
    	int err2 = 0;
    	int err3 = 0;
		protected String doInBackground(String...Params) {
    	String command;
    	if (err == 0){
    		publishProgress(0);
   		    command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/aboot";
        	try {
				Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
        	command = "dd if=/sdcard/freegee/working/aboot-freegee.img of=/dev/block/platform/msm_sdcc.1/by-name/aboot";
	        	try {
					err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					
					e.printStackTrace();
				}

      	if(err==0){
      		publishProgress(1);
   		    command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/boot";
        	try {
				Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
        	command = "dd if=/sdcard/freegee/working/boot-freegee.img of=/dev/block/platform/msm_sdcc.1/by-name/boot";
        	try {
				err2 = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			 }
      	
      	if(err2==0){
      		publishProgress(2);
   		    command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/recovery";
        	try {
				Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
        	command = "dd if=/sdcard/freegee/working/recovery-freegee.img of=/dev/block/platform/msm_sdcc.1/by-name/recovery";
        	try {
				err3 = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
      	 }
    	}  	
    	if (err != 0 || err2 != 0 || err3 != 0){
    			
                err=-10;
    			}
    	if(isSpecial && err != -10){
    		
    	
        	if (err == 0){
        		publishProgress(0);
        	    command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/sbl1";
            	try {
    				Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
    			} catch (InterruptedException e) {
    				
    				e.printStackTrace();
    			} catch (IOException e) {
    				
    				e.printStackTrace();
    			}
       		    command = "dd if=/sdcard/freegee/working/sbl1-freegee.img of=/dev/block/platform/msm_sdcc.1/by-name/sbl1";
    	        	try {
    					err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
    				} catch (InterruptedException e) {
    					
    					e.printStackTrace();
    				} catch (IOException e) {
    					
    					e.printStackTrace();
    				}

          	if(err==0){
          		publishProgress(1);
       		    command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/sbl2";
            	try {
    				Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
    			} catch (InterruptedException e) {
    				
    				e.printStackTrace();
    			} catch (IOException e) {
    				
    				e.printStackTrace();
    			}
            	command = "dd if=/sdcard/freegee/working/sbl2-freegee.img of=/dev/block/platform/msm_sdcc.1/by-name/sbl2";
            	try {
    				err2 = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
    			} catch (InterruptedException e) {
    				
    				e.printStackTrace();
    			} catch (IOException e) {
    				
    				e.printStackTrace();
    			}
    			 }
          	
          	if(err2==0){
          		publishProgress(2);
       		    command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/sbl3";
            	try {
    				Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
    			} catch (InterruptedException e) {
    				
    				e.printStackTrace();
    			} catch (IOException e) {
    				
    				e.printStackTrace();
    			}
            	command = "dd if=/sdcard/freegee/working/sbl3-freegee.img of=/dev/block/platform/msm_sdcc.1/by-name/sbl3";
            	try {
    				err3 = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
    			} catch (InterruptedException e) {
    				
    				e.printStackTrace();
    			} catch (IOException e) {
    				
    				e.printStackTrace();
    			}
          	 }
    		
    	}
    		   
    	}
    	if (err != 0 || err2 != 0 || err3 != 0){
			
            err=-10;
			}
		return null;
		}
		
		protected void onProgressUpdate(String... progress) {

            if(progress[0] == "0"){
             mProgressDialog.setMessage("Installing Bootloader...");
             }
            if(progress[0] == "1"){
                mProgressDialog.setMessage("Installing Boot...");
                }
       }

       @Override
       protected void onPostExecute(String unused) {
           dismissDialog(DIALOG_INSTALL_PROGRESS);
           if(err==-10){
        	   alertbuilder("Error!","Error encountared attempting to restore backups","Ok",0);
        	   new restore().execute();
        	   return;
           }
           else{
        	   if(isSpecial){
        		   checksblflash();
        		   return;
        	   }
        	   else{
        		    SharedPreferences prefs = getSharedPreferences("FreeGee",MODE_PRIVATE);
        	        if(prefs.contains("dropbox_key")){
               		File[] toBeUploaded = {new File("/sdcard/freegee/boot-backup.img"),new File("/sdcard/freegee/aboot-backup.img"),new File("/sdcard/freegee/recovery-backup.img"),new File("/sdcard/freegee/m9kefs1-backup.img"),new File("/sdcard/freegee/m9kefs2-backup.img"),new File("/sdcard/freegee/m9kefs3-backup.img"), new File("/sdcard/freegee/misc-backup.img")};
               		DropboxAPI<AndroidAuthSession> mDBApi = dropbox.newSession(install.this);
               		DBUpload dbupload = new DBUpload(install.this, mDBApi, toBeUploaded);
               			dbupload.execute();
        			   return;
        		   }
        		   else {
                       alertbuilder("Success!","Success. Your "+device+" Optimus G has been liberated!","Yay!",0);
                       new File("/sdcard/freegee/working").delete();
                       return;
        		   }
           }
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
 			  if(isSpecial){
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
        	    alertbuilder("Success!","Success. Your Optimus G been restored!","Yay!",0);
        	    return;
        	}
        	else if(err<=-1){
        		alertbuilder("Error!","Could not restore, backups not found! Do not reboot!","Boo!",0);
        		return;
        	}
        	else{
        		alertbuilder("Error!","There was an error restoring your backups Do not reboot!","Boo!",0);
        		return;
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
    			install.this.finish();
    			}
    	}
    	});

    	// create alert dialog
    	AlertDialog alertDialog = alertDialogBuilder.create();

    	// show it
    	alertDialog.show();

    	}
    
    public void checksblflash() {
    	String command;
    	int err = 0;
    	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/sbl1 of=/sdcard/freegee/working/sbl1_after.img";
     	try {
				err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
    	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/sbl2 of=/sdcard/freegee/working/sbl2_after.img";
     	try {
				err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
    	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/sbl3 of=/sdcard/freegee/working/sbl3_after.img";
     	try {
				err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			}

		if(checkmd5sum("/sdcard/freegee/working/sbl1_after.img",sbl1_after_md5sum)){		
		  if(checkmd5sum("/sdcard/freegee/working/sbl2_after.img",sbl2_after_md5sum)){
		    if(checkmd5sum("/sdcard/freegee/working/sbl3_after.img",sbl3_after_md5sum)){
    		    SharedPreferences prefs = getSharedPreferences("FreeGee",MODE_PRIVATE);
    	        if(prefs.contains("dropbox_key")){
           		File[] toBeUploaded = {new File("/sdcard/freegee/boot-backup.img"),new File("/sdcard/freegee/aboot-backup.img"),new File("/sdcard/freegee/recovery-backup.img"),new File("/sdcard/freegee/m9kefs1-backup.img"),new File("/sdcard/freegee/m9kefs2-backup.img"),new File("/sdcard/freegee/m9kefs3-backup.img"), new File("/sdcard/freegee/misc-backup.img"), new File("/sdcard/freegee/sbl1-backup.img"),new File("/sdcard/freegee/sbl2-backup.img"),new File("/sdcard/freegee/sbl3-backup.img")};
           		DropboxAPI<AndroidAuthSession> mDBApi = dropbox.newSession(install.this);
           		DBUpload dbupload = new DBUpload(install.this, mDBApi, toBeUploaded);
           			dbupload.execute();
    			   return;
    		   }
    		   else {
		    	alertbuilder("Success!","Success. Your "+device+" Optimus G has been liberated!","Yay!",0);
                new File("/sdcard/freegee/working").delete();
    		   }
		    }
		    else{
	        	   alertbuilder("Error!","sbl3 md5sum did not match after flashing. Attempting to restore original sbls. Do not reboot until finished","Ok",0);
	        	   new restore().execute();
		    }
		  }
		    else{
	        	   alertbuilder("Error!","sbl2 md5sum did not match after flashing. Attempting to restore original sbls. Do not reboot until finished","Ok",0);
	        	   new restore().execute();
		    }
		}
	    else{
     	   alertbuilder("Error!","sbl1 md5sum did not match after flashing. Attempting to restore original sbls. Do not reboot until finished","Ok",0);
     	   new restore().execute();
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

    	private String mErrorMsg,path;

    	//new class variables:
    	private int mFilesUploaded;
    	private File[] mFilesToUpload;
    	private int mCurrentFileIndex;
    	int totalBytes = 0, indBytes = 0;
    	
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
    	    
    	    for (int i = 0; i < mFilesToUpload.length; i++) {
    	        Long bytes = mFilesToUpload[i].length();
    	        totalBytes += bytes;
    	    }

    	    mDialog = new ProgressDialog(context);
    	    mDialog.setMax(100);
    	    mDialog.setMessage("Uploading file 1 / " + filesToUpload.length + " to Dropbox");
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

    	            int bytes = (int) mFilesToUpload[i].length();
    	            indBytes = bytes;
    	            
    	            // By creating a request, we get a handle to the putFile operation,
    	            // so we can cancel it later if we want to
    	            FileInputStream fis = new FileInputStream(file);
    	            path = file.getName();
    	            mRequest = mApi.putFileOverwriteRequest(path, fis, file.length(),
    	                    new ProgressListener() {
    	                @Override
    	                public long progressInterval() {
    	                     // Update the progress bar every half-second or so
    	                     return 100;
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

    	    mDialog.setMessage("Uploading file " + (mCurrentFileIndex + 1) + " / "
    	            + mFilesToUpload.length+"\n"+path  + " to DropBox");
    	    int percent = (int) (100.0 * (double) progress[0] / indBytes + 0.5);
    	    Log.i("pro", percent + "    " + progress[0] + "/" + indBytes);
    	    mDialog.setProgress(percent);
    	}

    	@Override
    	protected void onPostExecute(Boolean result) {
    	    mDialog.dismiss();
    	    if (result) {
    	        showToast("Backups successfully uploaded");
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


	public void alertbuilderu(String title, String text, String Button, final int exits){
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
    		Intent newActivity = new Intent(getBaseContext(), upload.class);
            startActivity(newActivity);
    	}
    	})
    	.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				install.this.finish();
				
			}
		});

    	// create alert dialog
    	AlertDialog alertDialog = alertDialogBuilder.create();

    	// show it
    	alertDialog.show();

    	}


	public void readxml() {
		ArrayList<String> RSArrayList = new ArrayList<String>();
	    try{
	    	File fXmlFile = new File("/sdcard/freegee/supported.xml");
	    	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    	Document doc = dBuilder.parse(fXmlFile);
	    	doc.getDocumentElement().normalize();
	    	NodeList nList = doc.getElementsByTagName(device);
	    	if(nList != null && nList.getLength() > 0){
	    	for (int temp = 0; temp < nList.getLength(); temp++) {
	     
	    		Node nNode = nList.item(temp);

	    		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	     
	    			Element eElement = (Element) nNode;
	    			NodeList rList = eElement.getElementsByTagName("recovery").item(0).getChildNodes();
	    			if(rList.getLength() >0){
	    	        for (int temp2 = 0; temp2 < rList.getLength(); temp2++){
	    	            Node aNode = rList.item(temp2);
	    	        	if (aNode.getNodeType() == Node.ELEMENT_NODE) {
	    	        	Element rNode = (Element) aNode;
	    	        	RSArrayList.add(rNode.getAttribute("id"));
	    	        	String[] sa = {rNode.getTextContent(),rNode.getAttribute("md5sum")};
	    	        	RSmap.put(rNode.getAttribute("id"),sa);
	    	        }
	    	        }
	    	        
	    	        NodeList bList = eElement.getElementsByTagName("boot").item(0).getChildNodes();
	    			if(bList.getLength() >0){
	    	        for (int temp2 = 0; temp2 < bList.getLength(); temp2++){
	    	            Node aNode = bList.item(temp2);
	    	        	if (aNode.getNodeType() == Node.ELEMENT_NODE) {
	    	        	Element bNode = (Element) aNode;
	    	        	String[] sa ={bNode.getTextContent(),bNode.getAttribute("md5sum")};
	    	        	BSmap.put(bNode.getTagName(),sa);
	    	        	   
	    	            }
	    	          }
	    			 }
	    			else{
	    				alertbuilderu("Sorry!","Sorry your varient is not currently supported, will attempt to upload boot image for support.","Ok",0);
	    			 }
	    			}
	    			else{
	    				alertbuilderu("Sorry!","Sorry your varient is not currently supported, will attempt to upload boot image for support.","Ok",0);
	    			  } 		   	
	    		    }
	    	       }
	        	}
    			else{
    				alertbuilderu("Sorry!","Sorry your varient is not currently supported, will attempt to upload boot image for support.","Ok",0);
    			}
	    	
	    	
	    	Spinner spinner = (Spinner) findViewById(R.id.recoveryspinner);
	        // Create an ArrayAdapter using the string array and a default spinner layout
	    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, RSArrayList);
	    	// Specify the layout to use when the list of choices appears
	    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    	// Apply the adapter to the spinner
	    	spinner.setAdapter(adapter);
	    	if(BSmap.containsKey(version.toUpperCase(Locale.US))){
	    	   boot = BSmap.get(version.toUpperCase(Locale.US))[0];
	    	   boot_md5sum = BSmap.get(version.toUpperCase(Locale.US))[1];

	    	}
	    	else
    		 alertbuilderu("Sorry!","Sorry your software version is not currently supported, will attempt to upload boot image for support.","Ok",0);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
		
	}
    
}
