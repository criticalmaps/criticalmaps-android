package de.stephanlindauer.criticalmaps.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.adapter.TweetAdapter;
import de.stephanlindauer.criticalmaps.databinding.FragmentTwitterBinding;
import de.stephanlindauer.criticalmaps.handler.PostTweetHandler;
import de.stephanlindauer.criticalmaps.handler.TwitterGetHandler;
import de.stephanlindauer.criticalmaps.model.TwitterModel;

public class TwitterFragment extends Fragment {

    private final TwitterModel twitterModel = App.components().twitterModel();

    private TweetAdapter tweetAdapter;
    private FragmentTwitterBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = FragmentTwitterBinding.inflate(inflater, container, false);
        binding.twitterTweetsRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);

        tweetAdapter = new TweetAdapter(new ArrayList<>());
        binding.twitterTweetsRecyclerview.setAdapter(tweetAdapter);
        binding.twitterTweetsRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && binding.twitterNewTweetButton.getVisibility() == View.VISIBLE) {
                    binding.twitterNewTweetButton.hide();
                } else if (dy < 0 && binding.twitterNewTweetButton.getVisibility() != View.VISIBLE) {
                    binding.twitterNewTweetButton.show();
                }
            }
        });

        binding.twitterContainerLayout.setOnRefreshListener(() ->
                new TwitterGetHandler(TwitterFragment.this).execute());

        binding.twitterContainerLayout.setColorSchemeResources(
                R.color.twitter_indicator_color_first,
                R.color.twitter_indicator_color_second);

        binding.twitterContainerLayout.setProgressBackgroundColorSchemeResource(
                R.color.tweet_progress_bar_background);

        binding.twitterErrorButton.setOnClickListener(v -> {
            new TwitterGetHandler(TwitterFragment.this).execute();
            binding.twitterErrorLayout.setVisibility(View.GONE);
        });

        binding.twitterNewTweetButton.setOnClickListener(v ->
                new PostTweetHandler(getActivity()).execute());

        new TwitterGetHandler(this).execute();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void displayNewData() {
        binding.twitterContainerLayout.setRefreshing(false);
        binding.twitterErrorLayout.setVisibility(View.GONE);

        binding.twitterContainerLayout.setVisibility(View.VISIBLE);
        tweetAdapter.updateData(twitterModel.getTweets());
    }

    public void showErrorMessage() {
        binding.twitterContainerLayout.setVisibility(View.GONE);
        binding.twitterErrorLayout.setVisibility(View.VISIBLE);
    }

    public void setRefreshing(boolean refreshing) {
        binding.twitterContainerLayout.setRefreshing(refreshing);
    }
}
