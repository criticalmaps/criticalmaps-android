package de.stephanlindauer.criticalmaps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.vo.twitter.Tweet;

public class TweetAdapter extends ArrayAdapter<Tweet> {

    private ArrayList<Tweet> tweets;
    private Context context;


    public TweetAdapter(Context context, int layoutResourceId, ArrayList<Tweet> tweets) {
        super(context, layoutResourceId, tweets);
        this.tweets = tweets;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Tweet currentTweet = tweets.get(position);

        return buildTweetView(currentTweet, inflater, parent);
    }

    private View buildTweetView(Tweet tweet, LayoutInflater inflater, ViewGroup parent) {

        View rowView = inflater.inflate(R.layout.view_tweet, parent, false);

        TextView nameTextView = (TextView) rowView.findViewById(R.id.tweet_user_name);
        TextView textTextView = (TextView) rowView.findViewById(R.id.tweet_user_content);

        nameTextView.setText(tweet.getUserName());
        textTextView.setText(tweet.getText());

        return rowView;
    }
}