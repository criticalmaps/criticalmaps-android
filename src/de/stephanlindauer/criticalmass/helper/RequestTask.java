package de.stephanlindauer.criticalmass.helper;


import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.osmdroid.util.GeoPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RequestTask extends AsyncTask<String, String, String> {

    private final ICommand callback;
    private final String uniqueDeviceId;
    private final GeoPoint currentLocation;

    public RequestTask(String uniqueDeviceId, GeoPoint currentLocation, ICommand callback) {
        super();
        this.callback = callback;
        this.uniqueDeviceId = uniqueDeviceId;
        this.currentLocation = currentLocation;
    }

    @Override
    protected String doInBackground(String... bla) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        try {
            String requestUrl = "http://criticalmass.stephanlindauer.de/get.php";
            requestUrl += "?device=" + uniqueDeviceId;

            if (currentLocation != null) {
                requestUrl += "&longitude=" + currentLocation.getLongitudeE6();
                requestUrl += "&latitude=" + currentLocation.getLatitudeE6();
            }

            response = httpclient.execute(new HttpGet(requestUrl));

            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            } else {
                response.getEntity().getContent().close();
            }
        } catch (Exception e) {
        }
        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        callback.execute(result);
    }
}