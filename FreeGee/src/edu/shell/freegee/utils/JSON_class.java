package edu.shell.freegee.utils;

import java.util.List;

import org.apache.http.cookie.Cookie;

import android.util.Log;

import com.loopj.android.http.*;

public class JSON_class {
  private static final String BASE_URL = "http://freegee.ufteach.no-ip.org/public/index.php";

  private static AsyncHttpClient client = new AsyncHttpClient();

  private PersistentCookieStore myCookieStore;
  
  
  public void setCookie(PersistentCookieStore myCookieStore){
	this.myCookieStore = myCookieStore;
	client.setCookieStore(myCookieStore);
  }
  
  public List<Cookie> getCookie(){
	  return myCookieStore.getCookies();
  }

  public void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
      client.get(getAbsoluteUrl(url), params, responseHandler);
  }

  public void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
	  //params.add("ci_csrf_token", getCookie());
      client.post(getAbsoluteUrl(url), params, responseHandler);
  }

  private static String getAbsoluteUrl(String relativeUrl) {
      Log.d("JSON","URL is: " + BASE_URL + relativeUrl);
	return BASE_URL + relativeUrl;
  }
}
