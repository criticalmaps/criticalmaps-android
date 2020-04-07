package de.stephanlindauer.criticalmaps.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.databinding.ViewTweetBinding;
import de.stephanlindauer.criticalmaps.model.twitter.Tweet;
import de.stephanlindauer.criticalmaps.utils.TimeToWordStringConverter;
import de.stephanlindauer.criticalmaps.views.CircleTransformation;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.TweetViewHolder> {
    private List<Tweet> tweets;

    public TweetAdapter(List<Tweet> tweets) {
        this.tweets = tweets;
    }

    @NonNull
    @Override
    public TweetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final ViewTweetBinding binding = ViewTweetBinding.inflate(inflater, parent, false);
        return new TweetViewHolder(binding);
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

    static class TweetViewHolder extends RecyclerView.ViewHolder {
        private static final Pattern HASH_TAG_PATTERN =
                Pattern.compile("(?<![a-zA-Z0-9_])#(?=[0-9_]*[a-zA-Z])[a-zA-Z0-9_]+");

        private final int hashTagColor;
        private final ViewTweetBinding binding;

        TweetViewHolder(@NonNull ViewTweetBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            hashTagColor = ContextCompat.getColor(itemView.getContext(), R.color.twitter_hashtag);
        }

        void bind(Tweet tweet) {
            Context context = itemView.getContext();

            App.components().picasso().load(tweet.getProfileImageUrl())
                    .fit()
                    .centerInside()
                    .transform(new CircleTransformation())
                    .error(R.drawable.ic_about_twitter)
                    .placeholder(R.drawable.twitter_avatar_placeholder)
                    .into(binding.tweetUserAvatarImage);

            binding.tweetUserNameText.setText(tweet.getUserName());

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
            binding.tweetContentText.setText(spannableBuilder);

            binding.tweetCreationDateTimeText.setText(
                    TimeToWordStringConverter.getTimeAgoShort(tweet.getTimestamp(), context));

            binding.tweetUserHandleText.setText(String.format(
                    context.getString(R.string.twitter_handle), tweet.getUserScreenName()));

            itemView.setOnClickListener(v ->
                    context.startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://twitter.com/" + tweet.getUserScreenName()
                                    + "/status/" + tweet.getTweetId()))));
        }
    }
}
