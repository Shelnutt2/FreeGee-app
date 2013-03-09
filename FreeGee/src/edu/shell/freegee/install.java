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
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;


@SuppressLint("SdCardPath")
public class install extends Activity {

    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    public static final int DIALOG_INSTALL_PROGRESS = 1;
    public static final int DIALOG_RESTORE_PROGRESS = 2;
    public static final int DIALOG_BACKUP_PROGRESS = 3;
    private Button recoveryBtn;
    private String saveloc;
    public static String device;
    public static String version;
    public static String id = "cf573ca8ea7d2";
    public static String incremental = "61249c215428d3309c";
    public static String factoryversion = "46d0622d0";
    private String recovery;
    private String boot;
    private String aboot_md5sum = "8a742f5776e73df2e6753b1694cda7e2";
    private String sbl1_md5sum = "25d877b9fc5852846478b8e583be020a";
    private String sbl2_md5sum = "20732aa3ad2eb2049c32ce55c00b3edb";
    private String sbl3_md5sum = "afdf190f364cec079050ce7750251b20";
    private String recovery_md5sum; 
    private String boot_md5sum; 
	private HashMap<String,String[]> RSmap = new HashMap<String,String[]>();
	private HashMap<String,String[]> BSmap = new HashMap<String,String[]>();
    private String dfname;
    private int step = 0;
    private int sstep = 0;
	private ProgressDialog mProgressDialog;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install);
        
        
        
        String command;
        
        
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
            	.setMessage("By Pressing Okay you are acknowledging that you are voiding you are voiding you warrenty and no one from team codefire can be held responsible.")
            	.setCancelable(false)
            	.setPositiveButton("I agree",new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog,int id) {
            	   	File freegeefw=new File("/sdcard/freegee/working");
          		  if(!freegeefw.exists()){
          			  freegeefw.mkdirs();
          		  }
          		  step=1;
          		  dfname="Bootloader";
          		  saveloc="/sdcard/freegee/working/aboot-freegee.img";
          		  new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/freegee/aboot-freegee.img");
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
            	alertbuilder("Error!","There was a problem downloading a file. Please try agian.","Ok",0);
            }
            return null;

        }
        protected void onProgressUpdate(String... progress) {
             mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
        	removeDialog(DIALOG_DOWNLOAD_PROGRESS);
            if(step==0){
        	readxml();
        	}
            else if(step==1){
            	step=2;
        		saveloc="/sdcard/freegee/working/boot-freegee.img";
        		dfname="Boot image";
        		Toast.makeText(getApplicationContext(),boot, Toast.LENGTH_LONG).show();
        		new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/freegee/"+boot);
            }
            else if(step==2){
            	step=3;
        		saveloc="/sdcard/freegee/working/recovery-freegee.img";
        		dfname="Recovery Image";
        		Spinner spinner = (Spinner) findViewById(R.id.recoveryspinner);
        		recovery = RSmap.get(spinner.getSelectedItem().toString())[0];
        		recovery_md5sum = RSmap.get(spinner.getSelectedItem().toString())[1];
        		Toast.makeText(getApplicationContext(),recovery, Toast.LENGTH_LONG).show();
        		new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/freegee/"+recovery);            	
            }
            else if(step==3){
            	if(isSpecial && sstep < 3){
            		if(sstep==0){
            		sstep=1;
            		saveloc="/sdcard/freegee/working/sbl1-freegee.img";
            		dfname="SBL1 image";
            		new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/freegee/sbl1.img");
            		}
            		if(sstep==1){
            		sstep=2;
            		saveloc="/sdcard/freegee/working/sbl2-freegee.img";
            		dfname="SBL2 image";
            		new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/freegee/sbl2.img");
            		}
            		if(sstep==2){
            		sstep=3;
            		saveloc="/sdcard/freegee/working/sbl3-freegee.img";
            		dfname="SBL3 image";
            		new DownloadFileAsync().execute("http://downloads.codefi.re/direct.php?file=shelnutt2/optimusg/freegee/sbl3.img");
            		}
            	}
            	else{
            	   checkmd5sums();
            	}
            }

        }
    }
    private boolean isSpecial = isSpecial();
    private boolean isSpecial(){
    	SharedPreferences prefs = getPreferences(MODE_PRIVATE); 
    	if(prefs.getInt("Special", 2) ==3){
    	   return true;
    	}
    	else{
    	   return false;
    	}
    			   
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
    		    	alertbuilder("Error!",f+"md5sum mismatch. Aborting.","Ok",1);
    		    }
    		    else{
    		    	Toast.makeText(getApplicationContext(),f+ " md5sum okay", Toast.LENGTH_SHORT).show();
    		    }
    	}
    	if (err == 0){
    		new backup().execute();
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
 		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs1 of=/sdcard/freegee/m9kefs1-"+df+".img";
 		        	try {
 						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					} catch (IOException e) {
 						
 						e.printStackTrace();
 					}
 		        	
 			  if(err==0){
		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs2 of=/sdcard/freegee/m9kefs2-"+df+".img";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
 			  }
 			  
 			 if(err==0){
		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs3 of=/sdcard/freegee/m9kefs3-"+df+".img";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
     			 }
 			 
		      	if(err==0){
		         	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs1 of=/sdcard/freegee/m9kefs1.img";
 		        	try {
 						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
 					} catch (InterruptedException e) {
 						
 						e.printStackTrace();
 					} catch (IOException e) {
 						
 						e.printStackTrace();
 					}
	 			 }
		      	
		      	if(err==0){
		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs2 of=/sdcard/freegee/m9kefs2.img";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
	 			 }
		      	
		      	if(err==0){
		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs3 of=/sdcard/freegee/m9kefs3.img";
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
        	}
        	else{
        		alertbuilder("Error!","There was a problem creating backups. Aborting.","Ok",0);
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
    	//int err = 0;
		//Toast.makeText(getApplicationContext(),"Moving files", Toast.LENGTH_SHORT).show();
    	String command = "busybox mv -f /sdcard/freegee/working/* /data/local/tmp/";
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
    	//	Toast.makeText(getApplicationContext(),"Zeroing bootloader", Toast.LENGTH_SHORT).show();
   		    command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/aboot";
        	try {
				Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
        //	Toast.makeText(getApplicationContext(),"Flashing bootloader", Toast.LENGTH_SHORT).show();
   		    command = "dd if=/data/local/tmp/aboot-freegee.img of=/dev/block/platform/msm_sdcc.1/by-name/aboot";
	        	try {
					err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					
					e.printStackTrace();
				}

      	if(err==0){
      		publishProgress(1);
      	//	Toast.makeText(getApplicationContext(),"Zeroing boot", Toast.LENGTH_SHORT).show();
   		    command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/boot";
        	try {
				Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
        //	Toast.makeText(getApplicationContext(),"Flashing boot", Toast.LENGTH_SHORT).show();
        	command = "dd if=/data/local/tmp/boot-freegee.img of=/dev/block/platform/msm_sdcc.1/by-name/boot";
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
      	//	Toast.makeText(getApplicationContext(),"Zeroing Recovery", Toast.LENGTH_SHORT).show();
   		    command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/recovery";
        	try {
				Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
        //	Toast.makeText(getApplicationContext(),"Flashing recovery", Toast.LENGTH_SHORT).show();
        	command = "dd if=/data/local/tmp/recovery-freegee.img of=/dev/block/platform/msm_sdcc.1/by-name/recovery";
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
            //	Toast.makeText(getApplicationContext(),"Flashing bootloader", Toast.LENGTH_SHORT).show();
       		    command = "dd if=/data/local/tmp/sbl1-freegee.img of=/dev/block/platform/msm_sdcc.1/by-name/sbl1";
    	        	try {
    					err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
    				} catch (InterruptedException e) {
    					
    					e.printStackTrace();
    				} catch (IOException e) {
    					
    					e.printStackTrace();
    				}

          	if(err==0){
          		publishProgress(1);
          	//	Toast.makeText(getApplicationContext(),"Zeroing boot", Toast.LENGTH_SHORT).show();
       		    command = "dd if=/dev/zero of=/dev/block/platform/msm_sdcc.1/by-name/sbl2";
            	try {
    				Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
    			} catch (InterruptedException e) {
    				
    				e.printStackTrace();
    			} catch (IOException e) {
    				
    				e.printStackTrace();
    			}
            	command = "dd if=/data/local/tmp/sbl2-freegee.img of=/dev/block/platform/msm_sdcc.1/by-name/sbl2";
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
            	command = "dd if=/data/local/tmp/sbl3-freegee.img of=/dev/block/platform/msm_sdcc.1/by-name/sbl3";
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
           }
           else{
           alertbuilder("Success!","Success. Your "+device+" Optimus G has been liberated!","Yay!",0);
           new File("/sdcard/freegee/working").delete();
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
	    	Toast.makeText(getApplicationContext(),device, Toast.LENGTH_LONG).show();
	    	NodeList nList = doc.getElementsByTagName(device);
	    	if(nList != null && nList.getLength() > 0){
	    	for (int temp = 0; temp < nList.getLength(); temp++) {
	     
	    		Node nNode = nList.item(temp);

	    		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	     
	    			Element eElement = (Element) nNode;
	    			NodeList rList = eElement.getElementsByTagName("recovery").item(0).getChildNodes();
	    			//Toast.makeText(getApplicationContext(),rList.getLength(), Toast.LENGTH_LONG).show();
	    			if(rList.getLength() >0){
	    	        for (int temp2 = 0; temp2 < rList.getLength(); temp2++){
	    	            Node aNode = rList.item(temp2);
	    	        	if (aNode.getNodeType() == Node.ELEMENT_NODE) {
	    	        	Element rNode = (Element) aNode;
	    	        	RSArrayList.add(rNode.getAttribute("id"));
	    	        	String[] sa = {rNode.getTextContent(),rNode.getAttribute("md5sum")};
	    	        	RSmap.put(rNode.getAttribute("id"),sa);
	    	            //System.out.println("recovery "+ rNode.getAttribute("id") +"- "+" " + rNode.getTextContent());
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
	    			
	    			//System.out.println("recovery: " + eElement.getElementsByTagName("recovery").item(0).getTextContent());
	    			//System.out.println("boot: " + eElement.getElementsByTagName("boot").item(0).getTextContent());
	    		   	
	    		     }
	    	       }
	        	}
    			else{
    				alertbuilderu("Sorry!","Sorry your varient is not currently supported, will attempt to upload boot image for support.","Ok",0);
    			}
	    	
	    	
	        //Toast.makeText(getApplicationContext(),RSArrayList.get(0), Toast.LENGTH_LONG).show();
	    	Spinner spinner = (Spinner) findViewById(R.id.recoveryspinner);
	        // Create an ArrayAdapter using the string array and a default spinner layout
	    	//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, RSArrayList, android.R.layout.simple_spinner_item);
	    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, RSArrayList);
	    	// Specify the layout to use when the list of choices appears
	    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    	// Apply the adapter to the spinner
	    	spinner.setAdapter(adapter);
	    	if(BSmap.containsKey(version)){
	    	   boot = BSmap.get(version)[0];
	    	   boot_md5sum = BSmap.get(version)[1];
	    	}
	    	else
    		 alertbuilderu("Sorry!","Sorry your software version is not currently supported, will attempt to upload boot image for support.","Ok",0);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
		
	}
    
}
