package de.stephanlindauer.criticalmass_berlin.helper;


import android.os.AsyncTask;
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

    public RequestTask(String uniqueDeviceId, GeoPoint currentLocation, ICommand callback) {
        super();
        this.callback = callback;
        this.uniqueDeviceId = uniqueDeviceId;
    }

    @Override
    protected String doInBackground(String... bla) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        try {
            response = httpclient.execute(new HttpGet("http://criticalmass.stephanlindauer.de/test.php"));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            } else {
                response.getEntity().getContent().close();
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }
        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        callback.execute(result);
    }
}