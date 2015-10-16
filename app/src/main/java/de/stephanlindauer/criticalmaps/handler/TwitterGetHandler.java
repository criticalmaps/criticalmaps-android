package de.stephanlindauer.criticalmaps.handler;

import android.os.AsyncTask;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import de.stephanlindauer.criticalmaps.fragments.TwitterFragment;
import de.stephanlindauer.criticalmaps.model.TwitterModel;
import de.stephanlindauer.criticalmaps.provider.HttpClientProvider;
import de.stephanlindauer.criticalmaps.vo.Endpoints;
import de.stephanlindauer.criticalmaps.vo.ResultType;
import java.net.HttpURLConnection;
import java.text.ParseException;
import org.json.JSONException;

public class TwitterGetHandler extends AsyncTask<Void, Void, ResultType> {

    //dependencies
    private final TwitterModel twitterModel = TwitterModel.getInstance();
    private final TwitterFragment twitterFragment;

    private String responseString = "";

    public TwitterGetHandler(TwitterFragment twitterFragment) {
        this.twitterFragment = twitterFragment;
    }

    @Override
    protected ResultType doInBackground(Void... params) {
        final Request request = new Request.Builder().url(Endpoints.GET_TWITTER).get().build();

        final OkHttpClient httpClient = HttpClientProvider.get();
        try {
            final Response response = httpClient.newCall(request).execute();

            if (response.code() == HttpURLConnection.HTTP_OK) {
                responseString = response.body().string();
                return ResultType.SUCCEEDED;
            }
        } catch (Exception ignored) {
        }

        return ResultType.FAILED;
    }

    @Override
    protected void onPostExecute(ResultType resultType) {
        if (!twitterFragment.isAdded()) {
            return;
        }

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
