package de.stephanlindauer.criticalmass.helper;


import android.os.AsyncTask;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.osmdroid.util.GeoPoint;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;

public class RequestTask extends AsyncTask<String, String, String> {

    public static String ERROR_STRING = "failed!!!";

    public static final int TIME_OUT = 15 * 1000; //30 sec

    private final ICommand callback;
    private final String uniqueDeviceId;
    private final GeoPoint currentLocation;
    private final String message;

    public RequestTask(String uniqueDeviceId, GeoPoint currentLocation, String message, ICommand callback) {
        super();
        this.callback = callback;
        this.uniqueDeviceId = uniqueDeviceId;
        this.message = message;
        this.currentLocation = currentLocation;
    }

    @Override
    protected String doInBackground(String... bla) {

        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, TIME_OUT);


        HttpClient httpclient = new DefaultHttpClient(httpParams);

        HttpResponse response;
        String responseString = null;
        try {
            String requestUrl = "http://api.criticalmaps.net/get.php";
            requestUrl += "?device=" + uniqueDeviceId;

            if (currentLocation != null) {
                requestUrl += "&longitude=" + currentLocation.getLongitudeE6();
                requestUrl += "&latitude=" + currentLocation.getLatitudeE6();
            }

            if (message != null) {
                String urlEncodedMessage = URLEncoder.encode(message, "UTF-8");
                requestUrl += "&message=" + urlEncodedMessage;
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
            return ERROR_STRING;
        }
        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        callback.execute(result);
    }
}