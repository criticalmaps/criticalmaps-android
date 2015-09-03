package de.stephanlindauer.criticalmaps.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
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

    private View buildTweetView(final Tweet tweet, LayoutInflater inflater, ViewGroup parent) {

        View rowView = inflater.inflate(R.layout.view_tweet, parent, false);

        TextView nameTextView = (TextView) rowView.findViewById(R.id.tweet_user_name);
        TextView textTextView = (TextView) rowView.findViewById(R.id.tweet_text);
        TextView dateTextView = (TextView) rowView.findViewById(R.id.tweet_creation_date);
        TextView timeTextView = (TextView) rowView.findViewById(R.id.tweet_creation_time);
        TextView handleTextView = (TextView) rowView.findViewById(R.id.tweet_user_handle);
        ImageView userImageView = (ImageView) rowView.findViewById(R.id.tweet_user_image);

        Picasso.with(context).load(tweet.getProfileImageUrl()).fit().centerInside().into(userImageView);

        nameTextView.setText(tweet.getUserName());
        textTextView.setText(Html.fromHtml(tweet.getText()).toString());
        dateTextView.setText(new SimpleDateFormat("HH:mm").format(tweet.getTimestamp()));
        timeTextView.setText(new SimpleDateFormat("dd.MM.yyyy").format(tweet.getTimestamp()));

        handleTextView.setText("@" + tweet.getUserScreenName());

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/aasif/status/" + tweet.getTweetId())));
            }
        });

        return rowView;
    }
}