package edu.shell.freegee;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

public class FreegeeJsonRequest extends SpringAndroidSpiceRequest< Device > {

    public FreegeeJsonRequest() {
        super( Device.class );
    }

    @Override
    public Device loadDataFromNetwork() throws Exception {
        return getRestTemplate().getForObject( "http://search.twitter.com/search.json?q=android&rpp=20", Device.class );
    }
}