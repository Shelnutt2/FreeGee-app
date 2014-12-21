package edu.shell.freegee.utils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import android.util.Log;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;
import com.stericson.RootTools.execution.Shell;

import edu.shell.freegee.device.Action;

public class actionTools {

    /**
     * Run an action
     * @param i Action
     * @param fullPathName Path to zip file
     * @return True if action is success or false if action fails
     */
    /*public boolean doAction(Action i, String fullPathName){
    	utils.customlog(Log.VERBOSE,"Performing action: " + i.getName());
		CommandCapture command = new CommandCapture(0,60000,"/data/local/tmp/edifier "+ constants.FreeGeeFolder + "/"+i.getZipFile()){
	        @Override
	        public void output(int id, String line)
	        {
	        	utils.customlog(Log.VERBOSE,line);
	            //RootTools.log(constants.LOG_TAG, line);
	            
	        }
		 };
			try {
				RootTools.debugMode = true; //ON
				Shell shell = RootTools.getShell(true,60000);
				shell.add(command);
				new tools().commandWait(command);
				int err = command.getExitCode();
				utils.customlog(Log.VERBOSE,"Exit code is: " + err);
				if(err == 0){
					actionsleft--;
					utils.customlog(Log.VERBOSE,"actionsleft is: " + actionsleft);
					if(actionsleft == 0){
						if(mainAction.getName().equalsIgnoreCase("loki_update_zip")){
							if(utils.copyUpdatedZip(updateZipFile)){
								mProgressDialog.dismiss();
							    alertbuilder("Done","The zip " + updateZipFile +" has been updated with the latest loki_patch and loki_flash","Ok",0);
							}
							else{
								mProgressDialog.dismiss();
							    alertbuilder("Error","The zip " + updateZipFile +" could not be copied back after updating","Ok",0);
								offerEmail(i);
							}	
						}
					    else{
						    mProgressDialog.dismiss();
						    alertbuilder("Done","The requested action of " + mainAction.getName() +" is complete","Ok",0);
					    }
					}
					return true;
				}
				else{
					Toast.makeText(this, "Error code is: " + err, Toast.LENGTH_LONG).show();
					actionsleft--;
					mProgressDialog.dismiss();
					Notices.alertbuilder("Error","There was an error running action " + i.getName(),"ok",0);
					//offerEmail(i);
					return false;
				}
				
			} catch (IOException e) {
				utils.customlog(Log.ERROR,"Edifier not found");
				Notices.alertbuilder("Error","Edifier not found.","ok",0);
			} catch (TimeoutException e) {
				utils.customlog(Log.ERROR,"command timed out");
				Notices.alertbuilder("Error","Edifier "+i.getName() + " command timed out","ok",0);
			} catch (RootDeniedException e) {
				utils.customlog(Log.ERROR,"No root access!");
				Notices.alertbuilder("Error","Please check root access","ok",0);
			} catch (Exception e) {
				utils.customlog(Log.ERROR,"Exception thrown waiting for command to finish");
				Notices.alertbuilder("Error","Exception thrown waiting for command to finish","ok",0,activity);
			}
			return false;
    }*/
	
    /**
     * Process an action, iterate through all dependencies and start downloads
     * @param action
     */
    /*public void processAction(Action action){
    	if(action.getName().equalsIgnoreCase("Re-lock (restore backups)")){
    		if(myDeviceDetails != null)
    			if(!checkForBackups()){
    				mProgressDialog.dismiss();
    			    return;
    			}
    	}
    	if((action.getStockOnly() && onStock()) || !action.getStockOnly()){
    	    actionsleft++;
    	    if(action.getDependencies() != null && !action.getDependencies().isEmpty()){
    		    for(Action i:action.getDependencies()){
    			    processAction(i);
    		    }
    	    }
    	    
    	    if(myDevice.getName().equalsIgnoreCase("LG Optimus G")){
    	    	if(makoUnlock && action.getName().equalsIgnoreCase("Optimus G Unlock")){
    	    		action = ogMakounlock;
    	    	}
    	    	else if(!makoUnlock && action.getName().equalsIgnoreCase("Mako Unlock")){
    	    		action = ogunlock;
    	    	}
    	    }
    	    actionOrder.add(action);
    	    Collections.sort(actionOrder);
    	    utils.customlog(Log.VERBOSE,"Current actions are: " + printList(actionOrder));
    	    String azipS = constants.FreeGeeFolder + "/"+action.getZipFile();
    	    File azipF = new File(azipS);
    	    if(azipF.exists()){
    	    	if(utils.checkMD5(action.getMd5sum(), azipF)){
    	    	    utils.customlog(Log.VERBOSE,"Using predownloaded "+action.getName());
    	    	    actionDownloads.put(action.getName(),true);
    	    	    if(actionDownloads.size() == actionsleft && allActionsDownloads()){
    	    		    if(action.getName().equalsIgnoreCase("loki_check"))
    	    		    	checkLoki(action,azipS);
    	    		    else
    	    		    	doAllActions();
    	    		    }
    	    	}
    	    	else{
    	    		utils.customlog(Log.VERBOSE,"Downloading "+action.getName());
    	    	    actionDownloads.put(action.getName(),false);
    	    		startDownload(action);
    	    	}
    	    }
	    	else{
	    		utils.customlog(Log.VERBOSE,"Downloading "+action.getName());
	    	    actionDownloads.put(action.getName(),false);
	    		startDownload(action);
	    	}
    	}
    }*/
	
	/*	public boolean doAllActions(){
	utils.customlog(Log.VERBOSE,"actionDownloads size: " + actionDownloads.size());
	utils.customlog(Log.VERBOSE,"actionOrder size: " + actionOrder.size());
	utils.customlog(Log.VERBOSE,"actionsleft: " + actionsleft);
	utils.customlog(Log.VERBOSE,"allActionsDownloads is: " + allActionsDownloads());
	if(actionDownloads.size() == actionsleft && allActionsDownloads()){
		for(Action action:actionOrder){
			if(ActionSuccess){
			    ActionSuccess = doAction(action, constants.FreeGeeFolder+action.getZipFile());
			    //actionOrder.remove(action);
			    //actionDownloads.remove(action);
			}
			else
				return false;
		}
		if(ActionSuccess)
			return true;
	}
	return false;
}*/

}
