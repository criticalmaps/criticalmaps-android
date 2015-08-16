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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import de.stephanlindauer.criticalmaps.utils.TwitterUtils;
import de.stephanlindauer.criticalmaps.vo.Endpoints;
import de.stephanlindauer.criticalmaps.vo.twitter.Tweet;

public class TwitterGetHandler extends AsyncTask<Void, Void, String> {

    //const
    public static final int TIME_OUT = 15 * 1000; //15 sec

    @Override
    protected String doInBackground(Void... params) {
        final HttpGet request = new HttpGet(Endpoints.GET_TWITTER);

        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, TIME_OUT);

        final HttpClient httpClient = new DefaultHttpClient(httpParams);

        String responseString = "";
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
            }
        } catch (IOException e) {
        }
        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        JSONObject jsonObject = null;
        try {
            ArrayList<Tweet> tweets = new ArrayList<Tweet>();

            jsonObject = new JSONObject(result);
            JSONArray statusesArray = jsonObject.getJSONArray("statuses");
            for (int i = 0; i < statusesArray.length(); i++) {
                JSONObject currentStatus = statusesArray.getJSONObject(i);
                Tweet tweet = new Tweet()
                        .setUserName(currentStatus.getJSONObject("user").getString("name"))
                        .setUserScreenName(currentStatus.getJSONObject("user").getString("screen_name"))
                        .setTweetId(currentStatus.getString("id_str"))
                        .setText(currentStatus.getString("text"))
                        .setTimestamp(TwitterUtils.getTwitterDate(currentStatus.getString("created_at")))
                        .setProfileImageUrl(currentStatus.getString("profile_image_url_https"));

                tweets.add(tweet);
            }
        } catch (Exception e) {
        }

//        System.out.println(jsonObject.toString());
        System.out.println();
    }
}
