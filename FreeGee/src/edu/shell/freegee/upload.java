package edu.shell.freegee;

import edu.shell.freegee.CustomMultiPartEntity.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
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
			new upload_async().execute();
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
                mProgressDialog.setMessage("Uploading  ..");
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
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        showDialog(DIALOG_UPLOAD_PROGRESS);
    }

    @Override
    protected String doInBackground(String... aurl) {
    	try {
			upload_file();
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
    	
    	 //alertbuilder("Success!","Success. Your Optimus G EFS been backed up!","Yay!",0);
    	
    	
    }
    
    public void upload_file() throws Exception {
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
        String filepath = "/sdcard/freegee/boot.img";
        File file = new File(filepath);
        ContentBody cbFile = new FileBody(file, filepath);         

        //Add the data to the multipart entity
        mpEntity.addPart("image", cbFile);
        mpEntity.addPart("id",new StringBody("cf573ca8ea7d2", Charset.forName("UTF-8")));
        mpEntity.addPart("incremental",new StringBody("61249c215428d3309c", Charset.forName("UTF-8")));
        mpEntity.addPart("factoryversion",new StringBody("46d0622d0", Charset.forName("UTF-8")));
        mpEntity.addPart("name", new StringBody("geehrc4g_spr_us", Charset.forName("UTF-8")));
        mpEntity.addPart("version", new StringBody("LS970ZV8", Charset.forName("UTF-8")));
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
        String result = (jsonobject.getString("result"));
        String msg = (jsonobject.getString("msg"));
        //Close the connection
        client.getConnectionManager().shutdown();
    }
    
  }
}