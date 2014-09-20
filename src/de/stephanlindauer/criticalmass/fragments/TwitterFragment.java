package de.stephanlindauer.criticalmass.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import de.stephanlindauer.criticalmass.R;
import de.stephanlindauer.criticalmass.adapter.TweetAdapter;
import de.stephanlindauer.criticalmass.twitter.ITweetListener;
import de.stephanlindauer.criticalmass.twitter.Tweet;
import de.stephanlindauer.criticalmass.twitter.TwitterApi;
import de.stephanlindauer.criticalmass.utils.AsyncCallback;
import de.stephanlindauer.criticalmass.utils.ImageCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import twitter4j.Status;

import java.util.Date;
import java.util.List;

public class TwitterFragment extends SuperFragment implements ITweetListener, AsyncCallback {

    public static final String TAG = TwitterFragment.class.getSimpleName();

    private TwitterApi twitter;
    public static final String TWITTER_CRITICAL_MASS_HASHTAG = "#CMBerlin";
    public static final int TWITTER_MAX_FEED = 10;
    public static final String TWITTER_SINCE = "2014-01-01";

    private boolean showRetweets = false;
    private TweetAdapter tweetAdapter;
    private RelativeLayout rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (rootView != null)
            return rootView;

        rootView = (RelativeLayout) inflater.inflate(R.layout.twitter, container, false);
        final ListView listView = (ListView) rootView.findViewById(R.id.tweet_list);
        listView.setAdapter(tweetAdapter = new TweetAdapter(getActivity(), 0));

//        addTestTweet();

        if (twitter == null) {
            twitter = new TwitterApi(getActivity());

            // new feeds
            twitter.searchTweets(new String[]{TWITTER_CRITICAL_MASS_HASHTAG}, this);

            // get past feeds
            twitter.searchTweetsAsync(TWITTER_CRITICAL_MASS_HASHTAG, this);
        }

        return rootView;
    }

    private void addTestTweet() {
        final Tweet t = new Tweet();
        t.userName = "name";
        t.content = "adaoihoiwhaiouwhdoahwpdahpf9ahwgawhgaiwhgüajwgawhg0ahwgü0a9hwgü0awhgü0a9hwgü0a9hwgü09ahwgü09ahwüg09ahwü0g9haüw0g9hawüg9ahwg90ahwgwgagawgEND"; // 144 signs
        t.creationDate = new Date(System.currentTimeMillis());
        t.screenName = "screename";
        t.userImageUrl = "http://pbs.twimg.com/profile_images/510808138212061185/wQSPWVZj_bigger.png";
        addTweet(t);
    }

    @Override
    public void onNewTweet(@NotNull final Tweet t) {
        Log.v(TAG, "searchTweets");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (t.retweets == 0 || showRetweets) addTweet(t);
            }
        });
    }

    @Override
    public void onException(@NotNull Exception e) {
        Log.e(TAG, "ITweetListener onException " + e.getMessage());
    }

    @Override
    public void onComplete(@Nullable final Object result) {
        Log.v(TAG, "searchTweetsAsync");
        if (!(result instanceof List<?>))
            return;

        final List<Status> stati = (List<Status>) result;

        for (final Status status : stati) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final Tweet t = new Tweet(status);
                    if (t.retweets == 0 || showRetweets) addTweet(t);
                }
            });
        }
    }

    private void addTweet(@NotNull final Tweet tweet) {
        ImageCache.shared.loadImage(getActivity(), tweet.userImageUrl, new AsyncCallback() {

            @Override
            public void onComplete(@Nullable final Object result) {
                Log.v(TAG, "addTweet onComplete");
                if (!(result instanceof Bitmap))
                    return;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tweet.image = (Bitmap) result;
                        tweetAdapter.add(tweet);
                        tweetAdapter.sortTweets();
                        tweetAdapter.notifyDataSetInvalidated();
                    }
                });
            }

            @Override
            public void onException(final Exception e) {
                Log.v(TAG, "addTweet onException");
                // use anonymous image
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tweetAdapter.add(tweet);
                        tweetAdapter.sortTweets();
                        tweetAdapter.notifyDataSetInvalidated();
                    }
                });
            }
        });
    }
}
