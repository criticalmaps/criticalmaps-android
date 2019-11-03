package de.stephanlindauer.criticalmaps.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.model.twitter.Tweet;
import de.stephanlindauer.criticalmaps.utils.TimeToWordStringConverter;
import de.stephanlindauer.criticalmaps.views.CircleTransformation;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.TweetViewHolder> {

    private final Context context;
    private List<Tweet> tweets;

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

    public static class TweetViewHolder extends RecyclerView.ViewHolder {

        private static final Pattern HASH_TAG_PATTERN =
                Pattern.compile("(?<![a-zA-Z0-9_])#(?=[0-9_]*[a-zA-Z])[a-zA-Z0-9_]+");

        private final Context context;
        private final int hashTagColor;

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
            hashTagColor = ContextCompat.getColor(context, R.color.twitter_hashtag);
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

            String text = Html.fromHtml(tweet.getText()).toString();
            Matcher matcher = HASH_TAG_PATTERN.matcher(text);
            SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(text);
            while (matcher.find()) {
                spannableBuilder.setSpan(
                        new ForegroundColorSpan(hashTagColor),
                        matcher.start(),
                        matcher.end(),
                        SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
            textTextView.setText(spannableBuilder);

            dateTimeTextView.setText(
                    TimeToWordStringConverter.getTimeAgoShort(tweet.getTimestamp(), context));

            handleTextView.setText(String.format(
                    context.getString(R.string.twitter_handle), tweet.getUserScreenName()));

            itemView.setOnClickListener(v ->
                    context.startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://twitter.com/aasif/status/" + tweet.getTweetId()))));
        }
    }
}
