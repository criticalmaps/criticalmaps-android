package de.stephanlindauer.criticalmaps.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import com.squareup.picasso.Callback;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.model.twitter.Tweet;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.TweetViewHolder> {

    private final Context context;
    private List<Tweet> tweets;

    public static class TweetViewHolder extends RecyclerView.ViewHolder {
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

        private final Context context;

        public TweetViewHolder(Context context, @NonNull View itemView) {
            super(itemView);
            this.context = context;
            ButterKnife.bind(this, itemView);
        }

        public void bind(Tweet tweet) {
            userImageProgress.setVisibility(View.VISIBLE);
            App.components().picasso().load(tweet.getProfileImageUrl())
                    .fit()
                    .centerInside()
                    //TODO replace with generic avatar
                    .error(R.drawable.ic_chat_bubble_24dp)
                    .into(userImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            hideProgressBar();
                        }

                        @Override
                        public void onError() {
                            hideProgressBar();
                        }

                        private void hideProgressBar() {
                            final ProgressBar progressBar = userImageProgress;
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });

            nameTextView.setText(tweet.getUserName());
            textTextView.setText(Html.fromHtml(tweet.getText()).toString());
            timeTextView.setText(
                    new SimpleDateFormat("HH:mm", Locale.US).format(tweet.getTimestamp()));
            dateTextView.setText(
                    new SimpleDateFormat("dd.MM.yyyy", Locale.US).format(tweet.getTimestamp()));
            handleTextView.setText(String.format(
                    context.getString(R.string.twitter_handle),tweet.getUserScreenName()));

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
