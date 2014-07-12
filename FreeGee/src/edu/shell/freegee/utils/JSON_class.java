package edu.shell.freegee.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.util.Log;

import com.loopj.android.http.*;

public class JSON_class {
  private static final String BASE_URL = "http://freegee.ufteach.no-ip.org/public/index.php/";

  private static AsyncHttpClient client = new AsyncHttpClient();

  public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
      client.get(getAbsoluteUrl(url), params, responseHandler);
  }

  public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
      client.post(getAbsoluteUrl(url), params, responseHandler);
  }

  private static String getAbsoluteUrl(String relativeUrl) {
      Log.d("JSON","URL is: " + BASE_URL + relativeUrl);
	return BASE_URL + relativeUrl;
  }
}
