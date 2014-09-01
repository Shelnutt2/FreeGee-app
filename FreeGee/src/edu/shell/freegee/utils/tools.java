package edu.shell.freegee.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.provider.Settings.Secure;
import android.util.Log;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;

import edu.shell.freegee.view.Notices;

public class tools {
	ProgressDialog mProgressDialog;
	Activity activity;
	public tools(Activity activity, ProgressDialog mProgressDialog){
		this.mProgressDialog = mProgressDialog;
		this.activity = activity;
	}
	
	public class setupTools extends AsyncTask<Activity, Integer, Boolean> {
		
		protected void onPreExecute(Activity... activity){      
	        mProgressDialog.setIndeterminate(true);
	        mProgressDialog.setCancelable(false);
	        mProgressDialog.setMessage("Setting up utilities");
	        mProgressDialog.show();
		}
	    /** The system calls this to perform work in a worker thread and
	      * delivers it the parameters given to AsyncTask.execute() */
	    protected Boolean doInBackground(Activity... activity) {
	    	//Set android_id
	    	constants.android_id = Secure.getString(activity[0].getContentResolver(),
	                Secure.ANDROID_ID);
	        if(!findCP(activity[0]) || checkForBusyBox(activity[0]))
	        	return false;
	        publishProgress(25);
	        if(!setupUtilities(0,activity[0]))
	        	return false;
	        publishProgress(95);
	        return true;
	    }
	    
	    protected void onProgressUpdate(Integer... value){
	    	mProgressDialog.setProgress(value[0]);
	    }	    
	    /** The system calls this to perform work in the UI thread and delivers
	      * the result from doInBackground() */
	    protected void onPostExecute(Boolean result) {
	    	publishProgress(100);
	    	mProgressDialog.dismiss();
	    }
	}
	
	  /**
     * Find's the "cp" binary if it exists on the system already
     * @return True if found, false if not found
     */
	public boolean findCP(Activity activity){
		CommandCapture command = new CommandCapture(0,"ls /system/bin/cp"){
		@Override
			public void output(int id, String line)
			{
				utils.customlog(Log.VERBOSE,line);
			}
		};
		Shell shell = null;
		try {
			shell = RootTools.getShell(true);
			shell.add(command);
			commandWait(command);
		} catch (IOException e) {
			utils.customlog(Log.ERROR, "Root Denined!");
			Notices.alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1,activity);
			return false;
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR, "Timed out ls /system/bin/cp!");
			Notices.alertbuilder("Error!","Timed out looking for cp","Ok",1,activity);
			return false;
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR, "Root Denined!");
			Notices.alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1,activity);
			return false;
		}	
		int err = command.getExitCode();
		if(err == 0){
			constants.CP_COMMAND="/system/bin/cp";
			utils.customlog(Log.VERBOSE,"constants.CP_COMMAND is " + constants.CP_COMMAND);
			return true;
		}
		else{
			command = new CommandCapture(0,"ls /system/xbin/cp"){
				@Override
				public void output(int id, String line)
				{
					utils.customlog(Log.VERBOSE,line);
				}
			};
			try {
				shell = RootTools.getShell(true);
				shell.add(command);
				commandWait(command);
			} catch (IOException e) {
				utils.customlog(Log.ERROR, "Timed out ls /system/xbin/cp!");
				Notices.alertbuilder("Error!","Timed out looking for cp","Ok",1,activity);
				return false;
			} catch (TimeoutException e) {
				utils.customlog(Log.ERROR, "Timed out ls /system/xbin/cp!");
				Notices.alertbuilder("Error!","Timed out looking for cp","Ok",1,activity);
				return false;
			} catch (RootDeniedException e) {
				utils.customlog(Log.ERROR, "Root Denined!");
				Notices.alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1,activity);
				return false;
			}
			err = command.getExitCode();
			if(err == 0){
				constants.CP_COMMAND="/system/xbin/cp";
				utils.customlog(Log.VERBOSE,"constants.CP_COMMAND is " + constants.CP_COMMAND);
				return true;
			}
		}
  	return false;
	}
	
    /**
     * Function to check for if busybox is available and if not offer to install it for users
     * @return True is it exists
     */
    public boolean checkForBusyBox(Activity activity){
		if(!RootTools.isBusyboxAvailable()){
			utils.customlog(Log.ERROR, "Buysbox no found!");
			Notices.alertbuilder("Error!","BusyBox not installed. Please install it now","Ok",0,activity);
			RootTools.offerBusyBox(activity);
			return false;
		}
		else{
			constants.CP_COMMAND="busybox cp";
			utils.customlog(Log.VERBOSE,"constants.CP_COMMAND is " + constants.CP_COMMAND);
			return true;
		}
    }

    /**
     * Wait for RootTools command to finish
     * @param cmd Command
     */
    public boolean commandWait(Command cmd) {
        int waitTill = 50;
        int waitTillMultiplier = 2;
        int waitTillLimit = 60000; //7 tries, 6350 msec

        while (!cmd.isFinished() && waitTill<=waitTillLimit) {
            synchronized (cmd) {
                try {
                    if (!cmd.isFinished()) {
                        cmd.wait(waitTill);
                        waitTill *= waitTillMultiplier;
                    }
                } catch (InterruptedException e) {
                    utils.customlog(Log.ERROR,"Error with waiting for command: "+ cmd.toString());
                    return false;
                }
            }
        }
        if (!cmd.isFinished()){
            utils.customlog(Log.ERROR, "Could not finish root command in " + (waitTill/waitTillMultiplier));
            return false;
        }
        return true;
    }

	/**
	 * Setup utilities needed, such as edifier
	 * This extract the utilities from assets to /sdcard/freegee and then copies them to /data/local/tmp
	 */
	public boolean setupUtilities(int tries, Activity activity){
		if(!new File(constants.FreeGeeFolder + "/tools/edifier").exists()){
		  InputStream in = null;
		  OutputStream out = null;
		  try {
			// read this file into InputStream
			in = activity.getAssets().open("edifier");
	 
			// write the inputStream to a FileOutputStream
			out = new FileOutputStream(new File(constants.FreeGeeFolder + "/tools/edifier"));
			int read = 0;
			byte[] bytes = new byte[50468];
	 
			while ((read = in.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}	 
		  } catch (IOException e) {
			utils.customlog(Log.ERROR,"Edifier not found in assets");
			Notices.alertbuilder("Error!","Can't copy Edifier from assets","Ok",1,activity);
			return false;
		  } finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					return false;
				}
			}
			if (out != null) {
				try {
					// outputStream.flush();
					out.close();
				} catch (IOException e) {
					return false;
				}
	 
			  }
		  }
		}
		
		if(!new File(constants.FreeGeeFolder + "/tools/keys").exists()){
			  InputStream in = null;
			  OutputStream out = null;
			  try {
				// read this file into InputStream
				in = activity.getAssets().open("keys");
		 
				// write the inputStream to a FileOutputStream
				out = new FileOutputStream(new File(constants.FreeGeeFolder + "/tools/keys"));
				int read = 0;
				byte[] bytes = new byte[50468];
		 
				while ((read = in.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}	 
			  } catch (IOException e) {
				utils.customlog(Log.ERROR,"Keys not found in assets");
				Notices.alertbuilder("Error!","Can't copy keys from assets","Ok",1,activity);
				return false;
			  } finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						return false;
					}
				}
				if (out != null) {
					try {
						// outputStream.flush();
						out.close();
					} catch (IOException e) {
						return false;
					}
		 
				  }
			  }
			}
		
		if(!new File(constants.FreeGeeFolder + "/tools/mkbootimg").exists()){
			  InputStream in = null;
			  OutputStream out = null;
			  try {
				// read this file into InputStream
				in = activity.getAssets().open("mkbootimg");
		 
				// write the inputStream to a FileOutputStream
				out = new FileOutputStream(new File(constants.FreeGeeFolder + "/tools/mkbootimg"));
				int read = 0;
				byte[] bytes = new byte[50468];
		 
				while ((read = in.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}	 
			  } catch (IOException e) {
				utils.customlog(Log.ERROR,"mkbootimg not found in assets");
				Notices.alertbuilder("Error!","Can't copy mkbootimg from assets","Ok",1,activity);
				return false;
			  } finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						return false;
					}
				}
				if (out != null) {
					try {
						// outputStream.flush();
						out.close();
					} catch (IOException e) {
						return false;
					}
		 
				  }
			  }
			}
		
		if(!new File(constants.FreeGeeFolder + "/tools/unpackbootimg").exists()){
			  InputStream in = null;
			  OutputStream out = null;
			  try {
				// read this file into InputStream
				in = activity.getAssets().open("unpackbootimg");
		 
				// write the inputStream to a FileOutputStream
				out = new FileOutputStream(new File(constants.FreeGeeFolder + "/tools/unpackbootimg"));
				int read = 0;
				byte[] bytes = new byte[50468];
		 
				while ((read = in.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}	 
			  } catch (IOException e) {
				utils.customlog(Log.ERROR,"unpackbootimg not found in assets");
				Notices.alertbuilder("Error!","Can't copy unpackbootimg from assets","Ok",1,activity);
				return false;
			  } finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						return false;
					}
				}
				if (out != null) {
					try {
						// outputStream.flush();
						out.close();
					} catch (IOException e) {
						return false;
					}
		 
				  }
			  }
			}
		
		if(!new File(constants.FreeGeeFolder + "/tools/busybox").exists()){
			  InputStream in = null;
			  OutputStream out = null;
			  try {
				// read this file into InputStream
				in = activity.getAssets().open("busybox");
		 
				// write the inputStream to a FileOutputStream
				out = new FileOutputStream(new File(constants.FreeGeeFolder + "/tools/busybox"));
				int read = 0;
				byte[] bytes = new byte[50468];
		 
				while ((read = in.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}	 
			  } catch (IOException e) {
				utils.customlog(Log.ERROR,"busybox not found in assets");
				Notices.alertbuilder("Error!","Can't copy busybox from assets","Ok",1,activity);
				return false;
			  } finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						return false;
					}
				}
				if (out != null) {
					try {
						// outputStream.flush();
						out.close();
					} catch (IOException e) {
						return false;
					}
		 
				  }
			  }
			}
		
		CommandCapture command = new CommandCapture(0, "mkdir -p /data/local/tmp/"){
	        @Override
	        public void output(int id, String line)
	        {
	        	utils.customlog(Log.VERBOSE,line);
	            //RootTools.log(constants.LOG_TAG, line);
	        }
		 };
		try {
			RootTools.getShell(true).add(command).isFinished();
		} catch (IOException e) {
			utils.customlog(Log.ERROR,"IOException when making /data/local/tmp dir");
			Notices.alertbuilder("Error!","Can't make /data/local/tmp","Ok",1,activity);
			return false;
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR,"mkdir timeout when making /data/local/tmp dir");
			Notices.alertbuilder("Error!","Mkdir timed out","Ok",1,activity);
			return false;
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR,"No root access!");
			Notices.alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1,activity);
			return false;
		}
		
		command = new CommandCapture(0,constants.CP_COMMAND + " /sdcard/freegee/tools/edifier /data/local/tmp/ && chmod 755 /data/local/tmp/edifier"){
	        @Override
	        public void output(int id, String line)
	        {
	        	utils.customlog(Log.VERBOSE,line);
	            //RootTools.log(constants.LOG_TAG, line);
	            
	        }
		 };
		try {
			RootTools.getShell(true).add(command).isFinished();
		} catch (IOException e) {
			utils.customlog(Log.ERROR,"Edifier not found in assets");
			Notices.alertbuilder("Error!","Can't open edifier","Ok",1,activity);
			return false;
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR,"Chmod timed out");
			Notices.alertbuilder("Error!","Chmod timed out","Ok",1,activity);
			return false;
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR,"No root access!");
			Notices.alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1,activity);
			return false;
		}
		
		command = new CommandCapture(0,constants.CP_COMMAND + " /sdcard/freegee/tools/keys /data/local/tmp/ && chmod 644 /data/local/tmp/keys"){
	        @Override
	        public void output(int id, String line)
	        {
	        	utils.customlog(Log.VERBOSE,line);
	            //RootTools.log(constants.LOG_TAG, line);
	            
	        }
		 };
		try {
			RootTools.getShell(true).add(command).isFinished();
		} catch (IOException e) {
			utils.customlog(Log.ERROR,"keys not found in assets");
			Notices.alertbuilder("Error!","Can't open keys","Ok",1,activity);
			return false;
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR,"Chmod timed out");
			Notices.alertbuilder("Error!","Chmod timed out","Ok",1,activity);
			return false;
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR,"No root access!");
			Notices.alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1,activity);
			return false;
		}
		
		command = new CommandCapture(0,constants.CP_COMMAND + " /sdcard/freegee/tools/mkbootimg /data/local/tmp/ && chmod 755 /data/local/tmp/mkbootimg"){
	        @Override
	        public void output(int id, String line)
	        {
	        	utils.customlog(Log.VERBOSE,line);
	            //RootTools.log(constants.LOG_TAG, line);
	            
	        }
		 };
		try {
			RootTools.getShell(true).add(command).isFinished();
		} catch (IOException e) {
			utils.customlog(Log.ERROR,"mkbootimg not found in assets");
			Notices.alertbuilder("Error!","Can't open mkbootimg","Ok",1,activity);
			return false;
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR,"Chmod timed out");
			Notices.alertbuilder("Error!","Chmod timed out","Ok",1,activity);
			return false;
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR,"No root access!");
			Notices.alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1,activity);
			return false;
		}
		
		command = new CommandCapture(0,constants.CP_COMMAND + " /sdcard/freegee/tools/unpackbootimg /data/local/tmp/ && chmod 755 /data/local/tmp/unpackbootimg"){
	        @Override
	        public void output(int id, String line)
	        {
	        	utils.customlog(Log.VERBOSE,line);
	            //RootTools.log(constants.LOG_TAG, line);
	            
	        }
		 };
		try {
			RootTools.getShell(true).add(command).isFinished();
		} catch (IOException e) {
			utils.customlog(Log.ERROR,"unpackbootimg not found in assets");
			Notices.alertbuilder("Error!","Can't open unpackbootimg","Ok",1,activity);
			return false;
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR,"Chmod timed out");
			Notices.alertbuilder("Error!","Chmod timed out","Ok",1,activity);
			return false;
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR,"No root access!");
			Notices.alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1,activity);
			return false;
		}
		
		command = new CommandCapture(0,constants.CP_COMMAND + " /sdcard/freegee/tools/busybox /data/local/tmp/ && chmod 755 /data/local/tmp/busybox"){
	        @Override
	        public void output(int id, String line)
	        {
	        	utils.customlog(Log.VERBOSE,line);
	            //RootTools.log(constants.LOG_TAG, line);
	            
	        }
		 };
		try {
			RootTools.getShell(true).add(command).isFinished();
		} catch (IOException e) {
			utils.customlog(Log.ERROR,"busybox not found in assets");
			Notices.alertbuilder("Error!","Can't open busybox","Ok",1,activity);
			return false;
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR,"Chmod timed out");
			Notices.alertbuilder("Error!","Chmod timed out","Ok",1,activity);
			return false;
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR,"No root access!");
			Notices.alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1,activity);
			return false;
		}
		
		command = new CommandCapture(0,"ls /data/local/tmp/edifier"){
	        @Override
	        public void output(int id, String line)
	        {
	        	utils.customlog(Log.VERBOSE,line);
	            //RootTools.log(constants.LOG_TAG, line);
	            
	        }
		 };
		Shell shell = null;
		try {
			shell = RootTools.getShell(true);
			shell.add(command);
			commandWait(command);
		} catch (IOException e) {
			utils.customlog(Log.ERROR, "IOException on ls /data/local/tmp/edifier!");
			Notices.alertbuilder("Error!","IOException checking if edifier copied fine","Ok",1,activity);
			return false;
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR, "Timed out ls /data/local/tmp/edifier!");
			Notices.alertbuilder("Error!","Timed out looking for /data/local/tmp/edifier","Ok",1,activity);
			return false;
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR, "Root Denined!");
			Notices.alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1,activity);
			return false;
		}		
		int err = command.getExitCode();
		if(err != 0){
			if(tries<=2)
			    setupUtilities(tries+1,activity);
			else{
				utils.customlog(Log.ERROR,"Tried twice to copy utlities and it failed");
				Notices.alertbuilder("Error","Tried twice to copy utlities and it failed","ok",1,activity);
				return false;
			}
		}
		command = new CommandCapture(0,"ls /data/local/tmp/busybox"){
		    @Override
		    public void output(int id, String line)
		    {
		    	utils.customlog(Log.VERBOSE,line);
		        //RootTools.log(constants.LOG_TAG, line);
		        }
	    };
		try {
		    shell = RootTools.getShell(true);
			shell.add(command);
			commandWait(command);
		} catch (IOException e) {
			utils.customlog(Log.ERROR, "IOException on ls /data/local/tmp/busybox!");
			Notices.alertbuilder("Error!","IOException checking if busybox copied fine","Ok",1,activity);
			return false;
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR, "Timed out ls /data/local/tmp/busybox!");
			Notices.alertbuilder("Error!","Timed out looking for /data/local/tmp/busybox","Ok",1,activity);
			return false;
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR, "Root Denined!");
			Notices.alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1,activity);
			return false;
		}
		err = command.getExitCode();
		if(err != 0){
			if(tries<=2)
			    setupUtilities(tries+1,activity);
			else{
				utils.customlog(Log.ERROR,"Tried twice to copy utlities and it failed");
				Notices.alertbuilder("Error","Tried twice to copy utlities and it failed","ok",1,activity);
				return false;
			}
		}
		
		command = new CommandCapture(0,"ls /data/local/tmp/busybox"){
		    @Override
		    public void output(int id, String line)
		    {
		    	utils.customlog(Log.VERBOSE,line);
		        //RootTools.log(constants.LOG_TAG, line);
		        }
	    };
		try {
		    shell = RootTools.getShell(true);
			shell.add(command);
			commandWait(command);
		} catch (IOException e) {
			utils.customlog(Log.ERROR, "IOException on ls /data/local/tmp/keys!");
			Notices.alertbuilder("Error!","IOException checking if keys copied fine","Ok",1,activity);
			return false;
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR, "Timed out ls /data/local/tmp/keys!");
			Notices.alertbuilder("Error!","Timed out looking for /data/local/tmp/keys","Ok",1,activity);
			return false;
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR, "Root Denined!");
			Notices.alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1,activity);
			return false;
		}
		err = command.getExitCode();
		if(err != 0){
			if(tries<=2)
			    setupUtilities(tries+1,activity);
			else{
				utils.customlog(Log.ERROR,"Tried twice to copy utlities and it failed");
				Notices.alertbuilder("Error","Tried twice to copy utlities and it failed","ok",1,activity);
				return false;
			}
		}
		
		command = new CommandCapture(0,"ls /data/local/tmp/mkbootimg"){
		    @Override
		    public void output(int id, String line)
		    {
		    	utils.customlog(Log.VERBOSE,line);
		        //RootTools.log(constants.LOG_TAG, line);
		        }
	    };
		try {
		    shell = RootTools.getShell(true);
			shell.add(command);
			commandWait(command);
		} catch (IOException e) {
			utils.customlog(Log.ERROR, "IOException on ls /data/local/tmp/mkbootimg!");
			Notices.alertbuilder("Error!","IOException checking if mkbootimg copied fine","Ok",1,activity);
			return false;
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR, "Timed out ls /data/local/tmp/mkbootimg!");
			Notices.alertbuilder("Error!","Timed out looking for /data/local/tmp/mkbootimg","Ok",1,activity);
			return false;
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR, "Root Denined!");
			Notices.alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1,activity);
			return false;
		}
		err = command.getExitCode();
		if(err != 0){
			if(tries<=2)
			    setupUtilities(tries+1,activity);
			else{
				utils.customlog(Log.ERROR,"Tried twice to copy utlities and it failed");
				Notices.alertbuilder("Error","Tried twice to copy utlities and it failed","ok",1,activity);
				return false;
			}
		}
		
		command = new CommandCapture(0,"ls /data/local/tmp/unpackbootimg"){
		    @Override
		    public void output(int id, String line)
		    {
		    	utils.customlog(Log.VERBOSE,line);
		        //RootTools.log(constants.LOG_TAG, line);
		        }
	    };
		try {
		    shell = RootTools.getShell(true);
			shell.add(command);
			commandWait(command);
		} catch (IOException e) {
			utils.customlog(Log.ERROR, "IOException on ls /data/local/tmp/unpackbootimg!");
			Notices.alertbuilder("Error!","IOException checking if unpackbootimg copied fine","Ok",1,activity);
			return false;
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR, "Timed out ls /data/local/tmp/unpackbootimg!");
			Notices.alertbuilder("Error!","Timed out looking for /data/local/tmp/unpackbootimg","Ok",1,activity);
			return false;
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR, "Root Denined!");
			Notices.alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1,activity);
			return false;
		}
		err = command.getExitCode();
		if(err != 0){
			if(tries<=2)
			    setupUtilities(tries+1,activity);
			else{
				utils.customlog(Log.ERROR,"Tried twice to copy utlities and it failed");
				Notices.alertbuilder("Error","Tried twice to copy utlities and it failed","ok",1,activity);
				return false;
			}
		}
		return true;
	}
	
	public Properties getBuildProp(Activity activity){

		Properties buildProp = new Properties();
	    
	    // read the property text  file
		File file = new File("/system/build.prop");
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException f) {
			CommandCapture command = new CommandCapture(0,"mount -o remount,rw /system");
			try {
				RootTools.getShell(true).add(command).isFinished();
			} catch (IOException e) {
				utils.customlog(Log.ERROR,"Can't remount /system");
				Notices.alertbuilder("Error!","Can't remount /system","Ok",1,activity);
			} catch (TimeoutException e) {
				utils.customlog(Log.ERROR,"Chmod timed out");
				Notices.alertbuilder("Error!","remount timed out","Ok",1,activity);
			} catch (RootDeniedException e) {
				utils.customlog(Log.ERROR,"No root access!");
				Notices.alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1,activity);
			}
			command = new CommandCapture(0,"chmod 644 /system/build.prop");
			try {
				RootTools.getShell(true).add(command).isFinished();
			} catch (IOException e) {
				utils.customlog(Log.ERROR,"");
				Notices.alertbuilder("Error!","Can't chmod build.prop","Ok",1,activity);
			} catch (TimeoutException e) {
				utils.customlog(Log.ERROR,"Chmod timed out");
				Notices.alertbuilder("Error!","Chmod timed out","Ok",1,activity);
			} catch (RootDeniedException e) {
				utils.customlog(Log.ERROR,"No root access!");
				Notices.alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1,activity);
			}
			command = new CommandCapture(0,"mount -o remount,ro /system");
			try {
				RootTools.getShell(true).add(command).isFinished();
			} catch (IOException e) {
				utils.customlog(Log.ERROR,"Can't remount /system");
				Notices.alertbuilder("Error!","Can't remount /system","Ok",1,activity);
			} catch (TimeoutException e) {
				utils.customlog(Log.ERROR,"remount timed out");
				Notices.alertbuilder("Error!","remount timed out","Ok",1,activity);
			} catch (RootDeniedException e) {
				utils.customlog(Log.ERROR,"No root access!");
				Notices.alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1,activity);
			}
		}

		// feed the property with the file
		try {
			buildProp.load(fis);
		} catch (IOException e) {
			Notices.alertbuilder("Error!","Can't load build.prop make sure you have root and perms are set correctly","Ok",0,activity);
		}
		try {
			fis.close();
		} catch (IOException e) {
			Notices.alertbuilder("Error!","Can't close build.prop, something went wrong.","Ok",0,activity);
		}
		return buildProp;
	}
	
 /*   public File getAbootImage(){
    	String ending = ".img";
    	if(myDevice !=null && swprop != null)
    		ending = "-"+myDevice.getModel()+"_"+swprop+".img";
    	else if(myDevice !=null)
    		ending = "-"+myDevice.getModel()+".img";
    	String abootOut = constants.FreeGeeFolder+"aboot"+ending;
		Command command = new CommandCapture(0,"/data/local/tmp/busybox dd if="+"/dev/block/platform/msm_sdcc.1/by-name/"+"aboot" + " of="+abootOut){
	        @Override
	        public void output(int id, String line)
	        {
	        	utils.customlog(Log.VERBOSE,line);
	            //RootTools.log(constants.LOG_TAG, line);
	            
	        }
		 };
		try {
			RootTools.getShell(true).add(command).isFinished();
		} catch (IOException e) {
			utils.customlog(Log.ERROR,"busybox not found in assets");
			alertbuilder("Error!","Can't open busybox","Ok",1);
		} catch (TimeoutException e) {
			utils.customlog(Log.ERROR,"Chmod timed out");
			alertbuilder("Error!","Chmod timed out","Ok",1);
		} catch (RootDeniedException e) {
			utils.customlog(Log.ERROR,"No root access!");
			alertbuilder("Error!","Can't get root access. Please verify root and try again","Ok",1);
		}
		return new File(abootOut);
    }*/
}