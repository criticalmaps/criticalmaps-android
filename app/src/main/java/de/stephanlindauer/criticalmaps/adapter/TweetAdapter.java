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

import butterknife.Bind;
import butterknife.ButterKnife;
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
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.view_tweet, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Tweet currentTweet = tweets.get(position);
        return buildTweetView(currentTweet, convertView, viewHolder);
    }

    private View buildTweetView(final Tweet tweet, View rowView, ViewHolder viewHolder) {
        Picasso.with(context)
                .load(tweet.getProfileImageUrl())
                .fit()
                .centerInside()
                .into(viewHolder.userImageView);

        viewHolder.nameTextView.setText(tweet.getUserName());
        viewHolder.textTextView.setText(Html.fromHtml(tweet.getText()).toString());
        viewHolder.dateTextView.setText(new SimpleDateFormat("HH:mm").format(tweet.getTimestamp()));
        viewHolder.timeTextView.setText(new SimpleDateFormat("dd.MM.yyyy").format(tweet.getTimestamp()));
        viewHolder.handleTextView.setText("@" + tweet.getUserScreenName());

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/aasif/status/" + tweet.getTweetId())));
            }
        });
        return rowView;
    }

    static class ViewHolder {
        @Bind(R.id.tweet_user_name)
        TextView nameTextView;
        @Bind(R.id.tweet_text)
        TextView textTextView;
        @Bind(R.id.tweet_creation_date)
        TextView dateTextView;
        @Bind(R.id.tweet_creation_time)
        TextView timeTextView;
        @Bind(R.id.tweet_user_handle)
        TextView handleTextView;
        @Bind(R.id.tweet_user_image)
        ImageView userImageView;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
