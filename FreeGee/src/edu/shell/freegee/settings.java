package edu.shell.freegee;


import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

import edu.shell.freegee.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.Toast;

public class settings extends Activity {
private CheckBox dbcheck; 
final static private String APP_KEY = "ywebobijtcfo2yc";
final static private String APP_SECRET = "ud1duwmbtlml0zz";
final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
// In the class declaration section:
static DropboxAPI<AndroidAuthSession> mDBApi;
	
protected void onResume() {
    super.onResume();

    // ...

    if (mDBApi != null && mDBApi.getSession().authenticationSuccessful()) {
        try {
            // MANDATORY call to complete auth.
            // Sets the access token on the session
            mDBApi.getSession().finishAuthentication();

            AccessTokenPair tokens = mDBApi.getSession().getAccessTokenPair();

            // Provide your own storeKeys to persist the access token pair
            // A typical way to store tokens is using SharedPreferences
            //storeKeys(tokens.key, tokens.secret);
            SharedPreferences prefs = getApplicationContext().getSharedPreferences("FreeGee",MODE_PRIVATE);
    	    Editor edit = prefs.edit();
    	    edit.putString("dropbox_key", tokens.key);
    	    edit.putString("dropbox_secret", tokens.secret);
    	    edit.commit();
        } catch (IllegalStateException e) {
            Log.i("DbAuthLog", "Error authenticating", e);
        }
    }

    // ...
}

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        dbcheck = (CheckBox)findViewById(R.id.DropBoxCheckBox);
    	final SharedPreferences prefs = getApplicationContext().getSharedPreferences("FreeGee",MODE_PRIVATE);
        if(prefs.contains("dropbox_key")){
        	dbcheck.setChecked(true);
        }
        dbcheck.setOnClickListener(new OnClickListener(){
            public void onClick(View v) {
            	if(dbcheck.isChecked()){


            		// And later in some initialization function:
            		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
            		AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
            		mDBApi = new DropboxAPI<AndroidAuthSession>(session);
            		
            		mDBApi.getSession().startAuthentication(settings.this);
            		/*Toast.makeText(settings.this, "AuthSuc"+mDBApi.getSession().authenticationSuccessful(), Toast.LENGTH_LONG).show();
            		if (mDBApi.getSession().authenticationSuccessful()) {
            	        try {
            	            // MANDATORY call to complete auth.
            	            // Sets the access token on the session
            	            mDBApi.getSession().finishAuthentication();

            	            AccessTokenPair tokens = mDBApi.getSession().getAccessTokenPair();

            	            // Provide your own storeKeys to persist the access token pair
            	            // A typical way to store tokens is using SharedPreferences

            	            Log.i("FreeGee","key is: " + key);
            	        } catch (IllegalStateException e) {
            	            Log.i("DbAuthLog", "Error authenticating", e);
            	        }
            	    }

            	    Log.i("freegee", "Keys stored");*/
            	}
            	else{
            		try{
            			Editor edit = prefs.edit();
            		    edit.remove("dropbox_key");
            		    edit.remove("dropbox_secret");
            		    edit.commit();
            		    Toast.makeText(getBaseContext(), "Dropbox authentication cleared", Toast.LENGTH_SHORT).show();
            		}
            		finally{
            			
            		}
            	}
            }
        });
    }
}
