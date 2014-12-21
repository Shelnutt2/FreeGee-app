package edu.shell.freegee.utils;

import java.util.Map.Entry;
import java.util.Properties;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import edu.shell.freegee.FreeGee;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.provider.Settings.Secure;
import android.util.Log;

public class JsonHelper {
	public static String JSON_COMPLETE = "du.shell.freegee.JsonHelper.COMPLETED";
	//Calling activity
	private Activity activity;
	private ProgressDialog mProgressDialog;
	private String response;
	JSON_class j = new JSON_class();
	/**
	 * 
	 * @param activity
	 */
	public JsonHelper(Activity activity, ProgressDialog mProgressDialog){
		this.activity = activity;
		this.mProgressDialog = mProgressDialog;
		j.setCookie(new PersistentCookieStore(activity));
	}

	/**
	 * 
	 * @return Json String
	 */
	public String Handshake(){
		RequestParams params = new RequestParams();
		if(constants.android_id.isEmpty())
			constants.android_id = Secure.getString(activity.getContentResolver(),
	                Secure.ANDROID_ID);
    	params.put("id", constants.android_id);
    	params.put("version", constants.appVersion);
    	j.get("/freegee/api/handshake", params, new JHHandler());
    	return getJsonResponse();
	}
	
	private class JHHandler extends JsonHttpResponseHandler {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        	//fragList.get(1).setContent(response.toString());
            Log.d("JSON","Good Results: " + response);
            try {
				setJsonResponse(response.toString(4));
				process(response);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
            // Pull out the first event on the public timeline
        	for (int i = 0; i < response.length(); ++i) {
        		try {
					Log.d("JSON","JSONArray returned: " + response.getJSONObject(i));
					setJsonResponse(response.getJSONObject(i).toString(4));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}

        }
        
        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response){
        	Log.d("JSON","Failed1 Results: " + response);
        	//T.append("Failed to get json");
        }
        
        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable e, JSONArray response){
        	Log.d("JSON","Failed2 Results: " + response);
        	//T.append("Failed to get json");
        }
        
        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable e){
        	Log.d("JSON","Failed3 Results: " + responseString);
        //	T.append("Failed to get json");
        }
    }
	
	private void setJsonResponse(String response){
		this.response = response;
	}
	
	public String getJsonResponse(){
		return response;
	}
	
	public void sendBuildProp(String URL){
		Properties buildProp = new tools(activity,mProgressDialog).getBuildProp(activity);
		RequestParams params = new RequestParams();
		for(Entry<Object, Object> p:buildProp.entrySet()){
			params.put((String)p.getKey(),(String)p.getValue());
		}
		j.post(URL, params, new JHHandler());
	}
	
	public void process(JSONObject jobject) throws JSONException{
		if(jobject.has("request_file") && jobject.has("request_location")){
			if(jobject.getString("request_file").equals("build.prop")){
				sendBuildProp(jobject.getString("request_location"));
			}
		}
		
		if(jobject.has("data_type") && jobject.getString("data_type").equals("device")){
			Intent updateIntent = new Intent(activity, FreeGee.class);
			updateIntent.putExtra("jobject", true);
			updateIntent.putExtra("device",jobject.getString("device"));
			updateIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        activity.startActivity(updateIntent);
		}
	}
	
	private void sendIntent(JSONObject jobject){
        Intent updateIntent = new Intent(activity, FreeGee.class);
        updateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        updateIntent.putExtra("jobject", jobject.toString());
        activity.startActivity(updateIntent);
	}
}
