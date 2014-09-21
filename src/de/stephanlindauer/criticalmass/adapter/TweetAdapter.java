package de.stephanlindauer.criticalmass.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import de.stephanlindauer.criticalmass.R;
import de.stephanlindauer.criticalmass.twitter.Tweet;
import twitter4j.util.TimeSpanConverter;

import java.util.Comparator;

public class TweetAdapter extends ArrayAdapter<Tweet> {

    public static final String TAG = TweetAdapter.class.getSimpleName();

    public TweetAdapter(final Context context, final int resource) {
        super(context, resource);
    }

    @Override
    public synchronized View getView(final int position, final View convertView, final ViewGroup parent) {

        if (convertView != null)
            return convertView;

        Log.v(TAG, "creating tweet for position: " + position);

        final Tweet tweet = getItem(position);
        RelativeLayout tweetRootView = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.tweet, null);
        final ImageView userImage = (ImageView) tweetRootView.findViewById(R.id.tweet_user_image);
        final TextView userName = (TextView) tweetRootView.findViewById(R.id.tweet_user_name);
        final TextView creationDate = (TextView) tweetRootView.findViewById(R.id.tweet_creation_date);
        final TextView userContent = (TextView) tweetRootView.findViewById(R.id.tweet_user_content);
//        final ImageView retweetImage = (ImageView) tweetRootView.findViewById(R.id.tweet_retweet_image);
//        final TextView retweetCounter = (TextView) tweetRootView.findViewById(R.id.tweet_retweet_counter);

        if (tweet.image != null)
            userImage.setImageBitmap(tweet.image);
        else
            userImage.setImageResource(R.drawable.anonymous);

        userName.setText(tweet.screenName + "\n@" + tweet.userName);
        creationDate.setText(new TimeSpanConverter().toTimeSpanString(tweet.creationDate));

        userContent.setText(tweet.content);
//        if(tweet.retweets > 0) {
//            retweetCounter.setText(""+tweet.retweets);
//            retweetImage.setVisibility(View.VISIBLE);
//        } else {
//            retweetCounter.setVisibility(View.GONE);
//            retweetImage.setVisibility(View.GONE);
//        }

        return tweetRootView;
    }

    public void sortTweets() {
        // TODO sort me, right now there is a weird bug when using this, showing only one tweet multiple times
//        sort(new Comparator<Tweet>() {
//            @Override
//            public int compare(Tweet _this, Tweet _other) {
//                return _this.compareTo(_other);
//            }
//        });
    }
}
