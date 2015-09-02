package de.stephanlindauer.criticalmaps.handler;

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
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;

import de.stephanlindauer.criticalmaps.fragments.TwitterFragment;
import de.stephanlindauer.criticalmaps.model.TwitterModel;
import de.stephanlindauer.criticalmaps.vo.Endpoints;
import de.stephanlindauer.criticalmaps.vo.ResultType;

public class TwitterGetHandler extends AsyncTask<Void, Void, ResultType> {

    //const
    public static final int TIME_OUT = 15 * 1000; //15 sec

    //dependencies
    TwitterModel twitterModel = TwitterModel.getInstance();

    private String responseString = "";
    private final TwitterFragment twitterFragment;

    public TwitterGetHandler(TwitterFragment twitterFragment) {
        this.twitterFragment = twitterFragment;
    }

    @Override
    protected ResultType doInBackground(Void... params) {
        final HttpGet request = new HttpGet(Endpoints.GET_TWITTER);

        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, TIME_OUT);

        final HttpClient httpClient = new DefaultHttpClient(httpParams);

        try {
            HttpResponse response = httpClient.execute(request);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            } else {
                response.getEntity().getContent().close();
                return ResultType.FAILED;
            }
        } catch (Exception e) {
            return ResultType.FAILED;
        }
        return ResultType.SUCCEEDED;
    }

    @Override
    protected void onPostExecute(ResultType resultType) {
        if (resultType == ResultType.FAILED) {
            twitterFragment.showErrorMessage();
        } else {
            try {
                twitterModel.setTweetsFromJsonString(responseString);
                twitterFragment.displayNewData();
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
