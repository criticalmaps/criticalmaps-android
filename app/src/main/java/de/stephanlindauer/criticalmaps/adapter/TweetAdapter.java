package de.stephanlindauer.criticalmaps.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.model.twitter.Tweet;

public class TweetAdapter extends ArrayAdapter<Tweet> {

    private final List<Tweet> tweets;
    private final Context context;

    private final Picasso picasso = App.components().picasso();

    public TweetAdapter(Context context, int layoutResourceId, List<Tweet> tweets) {
        super(context, layoutResourceId, tweets);
        this.tweets = tweets;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.view_tweet, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Tweet currentTweet = tweets.get(position);
        return buildTweetView(currentTweet, convertView, viewHolder);
    }

    private View buildTweetView(final Tweet tweet, View rowView, final ViewHolder viewHolder) {
        viewHolder.userImageProgress.setVisibility(View.VISIBLE);
        picasso.load(tweet.getProfileImageUrl())
                .fit()
                .centerInside()
                .error(R.drawable.ic_chat_bubble_24dp)
                .into(viewHolder.userImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        hideProgressBar();
                    }

                    @Override
                    public void onError() {
                        hideProgressBar();
                    }

                    private void hideProgressBar() {
                        final ProgressBar progressBar = viewHolder.userImageProgress;
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });

        viewHolder.nameTextView.setText(tweet.getUserName());
        viewHolder.textTextView.setText(Html.fromHtml(tweet.getText()).toString());
        viewHolder.dateTextView.setText(new SimpleDateFormat("HH:mm", Locale.US).format(tweet.getTimestamp()));
        viewHolder.timeTextView.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.US).format(tweet.getTimestamp()));
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
        @BindView(R.id.tweet_user_name)
        TextView nameTextView;
        @BindView(R.id.tweet_text)
        TextView textTextView;
        @BindView(R.id.tweet_creation_date)
        TextView dateTextView;
        @BindView(R.id.tweet_creation_time)
        TextView timeTextView;
        @BindView(R.id.tweet_user_handle)
        TextView handleTextView;
        @BindView(R.id.tweet_user_image)
        ImageView userImageView;
        @BindView(R.id.tweet_user_image_progress)
        ProgressBar userImageProgress;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
