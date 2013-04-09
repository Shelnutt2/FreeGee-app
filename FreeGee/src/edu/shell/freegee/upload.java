/** @author Seth Shelnutt
 * @License GPLv3 or later
 * All source code is released free and openly by Seth Shelnutt under the terms of GPLv3 or later, 2013 
 * */

package edu.shell.freegee;

import edu.shell.freegee.CustomMultiPartEntity.*;
import edu.shell.freegee.R;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class upload extends Activity{
    private Button UploadBtn;
	private ProgressDialog mProgressDialog;
    public static final int DIALOG_UPLOAD_PROGRESS = 4;
    long totalsize=0;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload);
        ListView lv = (ListView) findViewById(R.id.up_list_view);
        String[] lStr = new String[]{"Device Name: "+install.device,"Software Version: "+install.version};
        lv.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, lStr));

        
        UploadBtn = (Button)findViewById(R.id.UploadBtn);
        UploadBtn.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
        try {
        	if(install.version != null && install.device != null){
			   new upload_async().execute();
        	}
        	else{
        		alertbuilder("Not stock","Please return to stock to upload the images","ok",0);        		
        	}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       }
      });
    }
	
	
 
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_UPLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Uploading...");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
		default:
                return null;
        }
    }

class upload_async extends AsyncTask<String, String, String> {
	int err = 0;
	String msg;
	String result;
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        showDialog(DIALOG_UPLOAD_PROGRESS);
    }

    @Override
    protected String doInBackground(String... aurl) {
    	try {
        	File freegeef=new File("/sdcard/freegee/boot-original.img");
			  if(!freegeef.exists()){
				  String command = "dd if=/dev/block/platform/msm_sdcc.1/by-name/boot of=/sdcard/freegee/boot-original.img";
		        	try {
						err = Runtime.getRuntime().exec(new String[] { "su", "-c", command }).waitFor();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					} catch (IOException e) {
						
						e.printStackTrace();
					}
			  }
			  if(err==0){
			   err = upload_file();
			  }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
		return null;
    
    }
    protected void onProgressUpdate(String... progress) {
       //  mProgressDialog.setProgress(Integer.parseInt(progress[0]));
    }

    @Override
    protected void onPostExecute(String unused) {
    	removeDialog(DIALOG_UPLOAD_PROGRESS);
    	if(err == 0){
    		File freegeef=new File("/sdcard/freegee/boot-original.img");
    		freegeef.delete();
           if(result != null && result.equals("failed")){
        	   alertbuilder("Error!","There was an error uploading: \n" +msg,"Boo!",0);
           }
           else{
        	   alertbuilder("Success","Your images has been uploaded. Please allow time for it to be proccessed!","Cool!",0);
           }
    	}
    	else{
    		alertbuilder("Error!","There was a problem getting the boot image","Boo!",0);
    	}
    }
    
    public int upload_file() throws Exception {
        //Url of the server
        String url = "http://shelnutt2.codefi.re/80c17244abf/5de6a6161e/747882ba0ec16dffb22/upload.php";
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        //MultipartEntity mpEntity = new MultipartEntity();
        CustomMultiPartEntity mpEntity = new CustomMultiPartEntity(new ProgressListener()
		{
			@Override
			public void transferred(long num)
			{
				mProgressDialog.setProgress((int) ((num / (float) totalsize) * 100));
			}
		});
        //Path of the file to be uploaded
        String filepath = "/sdcard/freegee/boot-original.img";
        File file = new File(filepath);
        ContentBody cbFile = new FileBody(file);         

        //Add the data to the multipart entity
        mpEntity.addPart("image", cbFile);
        mpEntity.addPart("id",new StringBody(install.id, Charset.forName("UTF-8")));
        mpEntity.addPart("incremental",new StringBody(install.incremental, Charset.forName("UTF-8")));
        mpEntity.addPart("factoryversion",new StringBody(install.factoryversion, Charset.forName("UTF-8")));
        mpEntity.addPart("name", new StringBody(install.device, Charset.forName("UTF-8")));
        mpEntity.addPart("version", new StringBody(install.version, Charset.forName("UTF-8")));
        totalsize = mpEntity.getContentLength();
        post.setEntity(mpEntity);
        //Execute the post request
        HttpResponse response1 = client.execute(post);
        //Get the response from the server
        HttpEntity resEntity = response1.getEntity();
        String Response=EntityUtils.toString(resEntity);
        Log.d("Response:", Response);
        //Generate the array from the response
        JSONArray jsonarray = new JSONArray("["+Response+"]");
        JSONObject jsonobject = jsonarray.getJSONObject(0);
        //Get the result variables from response 
        result = (jsonobject.getString("result"));
        msg = (jsonobject.getString("msg"));
        //Close the connection
        client.getConnectionManager().shutdown();
        return 0;
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
			upload.this.finish();
			}
	}
	});

	// create alert dialog
	AlertDialog alertDialog = alertDialogBuilder.create();

	// show it
	alertDialog.show();

	}
}