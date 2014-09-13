package de.stephanlindauer.criticalmass.twitter;


import android.app.Activity;
import android.os.AsyncTask;
import de.stephanlindauer.criticalmass.Main;
import de.stephanlindauer.criticalmass.utils.AsyncCallback;
import de.stephanlindauer.criticalmass.utils.JSONUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Proxy implementation of twitter4j library
 */
public class TwitterApi implements ProxyApi {

    private final TwitterStream twitterStream;
    private final JSONObject appConfig;

    /**
     * Proxy constructor for Twitter4j library.
     *
     * @param context - Activity.
     */
    public TwitterApi(@NotNull final Activity context) {
        super();

        // load secrets
        appConfig = JSONUtils.loadJsonFromAssets(context, "app.json");

        final ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(appConfig.optString("CONSUMER_KEY"));
        cb.setOAuthConsumerSecret(appConfig.optString("CONSUMER_SECRET"));
        cb.setOAuthAccessToken(appConfig.optString("ACCESS_TOKEN"));
        cb.setOAuthAccessTokenSecret(appConfig.optString("ACCESS_TOKEN_SECRET"));
        twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
    }

    /**
     * Connects to Twitter stream API and starts queering for tweets with provided hashtags array
     *
     * @param hashTag  arrays of hashtags for searching
     * @param listener listener that returns new found tweets
     */
    public void searchTweets(@NotNull final String[] hashTag, @NotNull final ITweetListener listener) {
        final StatusListener statusListener = new StatusListener() {

            @Override
            public void onStatus(final Status status) {
                //mapping twitter4j tweet to application tweet object
                Tweet tweet = new Tweet();
                tweet.content = status.getText();
                tweet.userName = status.getUser().getName();
                listener.onNewTweet(tweet);
            }

            @Override
            public void onDeletionNotice(final StatusDeletionNotice statusDeletionNotice) {
                //ignored
            }

            @Override
            public void onTrackLimitationNotice(final int i) {
                //ignored
            }

            @Override
            public void onScrubGeo(final long l, final long l2) {
                //ignored
            }

            @Override
            public void onStallWarning(final StallWarning stallWarning) {
                //ignored
            }

            @Override
            public void onException(final Exception e) {
                listener.onException(e);
            }
        };

        twitterStream.addListener(statusListener);

        final FilterQuery fq = new FilterQuery();
        fq.track(hashTag);
        twitterStream.filter(fq);
    }

    public ResponseList<Status> getTweets() throws TwitterException {
        return TwitterFactory.getSingleton().getHomeTimeline();
    }

    public void searchTweetsAsync(@NotNull final String searchString, @NotNull final AsyncCallback cb) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(final @Nullable Void... params) {
                // The factory instance is re-useable and thread safe.
                final Twitter twitter = TwitterFactory.getSingleton();
                twitter.setOAuthConsumer(appConfig.optString("CONSUMER_KEY"), appConfig.optString("CONSUMER_SECRET"));
                twitter.setOAuthAccessToken(new AccessToken(appConfig.optString("ACCESS_TOKEN"), appConfig.optString("ACCESS_TOKEN_SECRET")));
                final Query query = new Query("wolkenschauer");
                try {
                    cb.onComplete(twitter.search(query).getTweets());
                } catch (TwitterException e) {
                    cb.onError(e);
                }
                return null;
            }
        }.execute();
    }
}
