package de.stephanlindauer.criticalmaps.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.model.twitter.Tweet;
import de.stephanlindauer.criticalmaps.views.CircleTransformation;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.TweetViewHolder> {

    private final Context context;
    private List<Tweet> tweets;

    public static class TweetViewHolder extends RecyclerView.ViewHolder {

        private final static long DAY_MS = 86_400_400;
        private final static long HOUR_MS = 3_600_600;
        private final static long MINUTE_MS = 60_000;

        private final Context context;

        @BindView(R.id.tweet_user_name)
        TextView nameTextView;
        @BindView(R.id.tweet_text)
        TextView textTextView;
        @BindView(R.id.tweet_creation_date_time)
        TextView dateTimeTextView;
        @BindView(R.id.tweet_user_handle)
        TextView handleTextView;
        @BindView(R.id.tweet_user_image)
        ImageView userImageView;

        public TweetViewHolder(Context context, @NonNull View itemView) {
            super(itemView);
            this.context = context;
            ButterKnife.bind(this, itemView);
        }

        public void bind(Tweet tweet) {
            App.components().picasso().load(tweet.getProfileImageUrl())
                    .fit()
                    .centerInside()
                    .transform(new CircleTransformation())
                    .error(R.drawable.ic_about_twitter)
                    .placeholder(R.drawable.twitter_avatar_placeholder)
                    .into(userImageView);

            nameTextView.setText(tweet.getUserName());
            textTextView.setText(Html.fromHtml(tweet.getText()).toString());

            final long currentDateMs = new Date().getTime();
            final long tweetDateMs = tweet.getTimestamp().getTime();
            final long delta = currentDateMs - tweetDateMs;
            final int days = (int) (delta / DAY_MS);
            final int hours = (int) (delta / HOUR_MS);
            final int minutes = (int) ((delta / MINUTE_MS) % 60);
            if (days > 10) {
                dateTimeTextView.setText(new SimpleDateFormat("dd.MMM", Locale.US).format(tweetDateMs));
            }
            if (days > 0 && days < 10) {
                dateTimeTextView.setText(String.format("%d d", days));
            } else if (hours > 0 && hours < 24) {
                dateTimeTextView.setText(String.format("%d h", hours));
            } else {
                dateTimeTextView.setText(String.format("%d min", minutes));
            }

            handleTextView.setText(String.format(
                    context.getString(R.string.twitter_handle), tweet.getUserScreenName()));

            itemView.setOnClickListener(v ->
                    context.startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://twitter.com/aasif/status/" + tweet.getTweetId()))));
        }
    }

    public TweetAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    @NonNull
    @Override
    public TweetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(R.layout.view_tweet, parent, false);
        return new TweetViewHolder(context, view);
    }

    @Override
    public void onBindViewHolder(@NonNull TweetViewHolder holder, int position) {
        holder.bind(tweets.get(position));
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    public void updateData(List<Tweet> tweets) {
        this.tweets = tweets;
        notifyDataSetChanged();
    }
}
