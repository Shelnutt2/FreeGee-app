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
    public static final int DIALOG_BACKUP_PROGRESS = 3;
    private Button recoveryBtn;
    private String saveloc;
    private String device;
    private String version;
    private String recovery;
    private String boot;
    private String aboot_md5sum = "bc54a6a730658550713a0779b30bf6b7";
    private String recovery_md5sum; 
    private String boot_md5sum; 
	private HashMap<String,String[]> RSmap = new HashMap<String,String[]>();
	private HashMap<String,String[]> BSmap = new HashMap<String,String[]>();
    private String dfname;
    private int step = 0;
	private ProgressDialog mProgressDialog;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install);
        
        
        
        String command;
        int err;
        
        
	// read the property text  file
	File file = new File("/system/build.prop");
	FileInputStream fis = null;
	try {
		fis = new FileInputStream(file);
	} catch (FileNotFoundException f) {
		command = "mount -o remount,rw /system";
    	try {
			err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		command = "chmod 644 /system/build.prop";
    	try {
			err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		command = "mount -o remount,ro /system";
    	try {
			err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
    dfname = "supported.xml";
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
            } catch (Exception e) {}
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
            	checkmd5sums();
            }

        }
    }
    private void checkmd5sums() {
    	int err = 0;
    	HashMap<String,String> files = new HashMap<String, String>();
    	files.put("recovery",recovery_md5sum);
    	files.put("aboot",aboot_md5sum);
    	files.put("boot",boot_md5sum);
    	for(String f:files.keySet()){
    		 MessageDigest md = null;
			try {
				md = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    		    FileInputStream fis = null;
				try {
					fis = new FileInputStream("/sdcard/freegee/working/"+f+"-freegee.img");
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
    		    byte[] dataBytes = new byte[1024];
    		    int nread = 0; 
    		    try {
    				while ((nread = fis.read(dataBytes)) != -1) {
    				  md.update(dataBytes, 0, nread);
    				}
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
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
    		    	Toast.makeText(getApplicationContext(),f+ " md5sum okay", Toast.LENGTH_LONG).show();
    		    }
    	}
    	if (err ==0){
    		new backup().execute();
    	}
    }
    
    class backup extends AsyncTask<Void, Void, Void> {
    	int err = 0;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 		        	
 			  if(err==0){
		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs2 of=/sdcard/freegee/m9kefs2-"+df+".img";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
 			  }
 			  
 			 if(err==0){
		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs3 of=/sdcard/freegee/m9kefs3-"+df+".img";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
     			 }
 			 
		      	if(err==0){
		         	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs1 of=/sdcard/freegee/m9kefs1.img";
 		        	try {
 						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
	 			 }
		      	
		      	if(err==0){
		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs2 of=/sdcard/freegee/m9kefs2.img";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	 			 }
		      	
		      	if(err==0){
		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/m9kefs3 of=/sdcard/freegee/m9kefs3.img";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	 			 }
		      	
		      	if(err==0){
		         	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/aboot of=/sdcard/freegee/aboot.img";
 		        	try {
 						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
	 			 }
		      	
		      	if(err==0){
		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/boot of=/sdcard/freegee/boot.img";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	 			 }
		      	
		      	if(err==0){
		        	command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/recovery of=/sdcard/freegee/recovery.img";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
		protected String doInBackground(String...Params) {
    	//int err = 0;
    	String command = "busybox mv /sdcard/freegee/working /data/local/tmp/";
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
   		    command = "dd if=/data/local/tmp/working/aboot.img of=/dev/block/platform/msm_sdcc.1/by-name/aboot";
	        	try {
					err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

      	if(err==0){
      		publishProgress(1);
        	command = "dd if=/data/local/tmp/working/boot.img of=/dev/block/platform/msm_sdcc.1/by-name/boot";
        	try {
				err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 }
      	
      	if(err==0){
      		publishProgress(2);
        	command = "dd if=/data/local/tmp/working/recovery.img of=/dev/block/platform/msm_sdcc.1/by-name/recovery";
        	try {
				err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
      	 }
    	}  	
    	if (err !=0){
    				
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(install.this);
            	    
                	// set title
                	alertDialogBuilder.setTitle("Error!");

                	// set dialog message
                	alertDialogBuilder
                	.setMessage("There was a problem installing. Attempting to reinstall backups.")
                	.setCancelable(false)
                	.setPositiveButton("OK",new DialogInterface.OnClickListener() {
                	public void onClick(DialogInterface dialog,int id) {
                		String command;
                		
        		         	command = "dd if=/sdcard/freegee/aboot.img of=/dev/block/platform/msm_sdcc.1/by-name/aboot";
         		        	try {
         						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
         					} catch (InterruptedException e) {
         						// TODO Auto-generated catch block
         						e.printStackTrace();
         					} catch (IOException e) {
         						// TODO Auto-generated catch block
         						e.printStackTrace();
         					}

        		      	if(err==0){
        		        	command = "dd if=/sdcard/freegee/boot.img of=/dev/block/platform/msm_sdcc.1/by-name/boot";
        		        	try {
        						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
        					} catch (InterruptedException e) {
        						// TODO Auto-generated catch block
        						e.printStackTrace();
        					} catch (IOException e) {
        						// TODO Auto-generated catch block
        						e.printStackTrace();
        					}
        	 			 }
        		      	
        		      	if(err==0){
        		        	command = "dd if=/sdcard/freegee/recovery.img of=/dev/block/platform/msm_sdcc.1/by-name/recovery";
        		        	try {
        						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
        					} catch (InterruptedException e) {
        						// TODO Auto-generated catch block
        						e.printStackTrace();
        					} catch (IOException e) {
        						// TODO Auto-generated catch block
        						e.printStackTrace();
        					}
        		      	}
                		if(err==0){
                			err=-10;
                		}
                	}
                	});
                	// create alert dialog
                	AlertDialog alertDialog = alertDialogBuilder.create();
                	// show it
                	alertDialog.show();
                	
    			}
    		   
    		
    		else{
    			err=-1;
    			
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
           if(err==-10){
        	   alertbuilder("Error!","Error encountared but backups restored.","Ok",0);
           }
           else{
           alertbuilder("Success!","Success. Your "+device+" Optimus G has been liberated!","Yay!",0);
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
	    	
	    	for (int temp = 0; temp < nList.getLength(); temp++) {
	     
	    		Node nNode = nList.item(temp);

	    		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	     
	    			Element eElement = (Element) nNode;
	    			NodeList rList = eElement.getElementsByTagName("recovery").item(0).getChildNodes();
	    			//Toast.makeText(getApplicationContext(),rList.getLength(), Toast.LENGTH_LONG).show();
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
	    			
	    	        for (int temp2 = 0; temp2 < bList.getLength(); temp2++){
	    	            Node aNode = bList.item(temp2);
	    	        	if (aNode.getNodeType() == Node.ELEMENT_NODE) {
	    	        	Element bNode = (Element) aNode;
	    	        	String[] sa ={bNode.getTextContent(),bNode.getAttribute("md5sum")};
	    	        	BSmap.put(bNode.getTagName(),sa);
	    	        	   
	    	        }
	    	        }
	    			
	    			//System.out.println("recovery: " + eElement.getElementsByTagName("recovery").item(0).getTextContent());
	    			//System.out.println("boot: " + eElement.getElementsByTagName("boot").item(0).getTextContent());
	    			
	    		}
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
	    	
	    	boot = BSmap.get(version)[0];
	    	boot_md5sum = BSmap.get(version)[1];
	    	
	    } catch (Exception e) {
		e.printStackTrace();
	    }
		
	}
    
}
