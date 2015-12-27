package de.stephanlindauer.criticalmaps.fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.adapter.TweetAdapter;
import de.stephanlindauer.criticalmaps.handler.PostTweetHandler;
import de.stephanlindauer.criticalmaps.handler.TwitterGetHandler;
import de.stephanlindauer.criticalmaps.model.TwitterModel;
import de.stephanlindauer.criticalmaps.vo.twitter.Tweet;

public class TwitterFragment extends Fragment {

    //dependencies
    private TwitterModel twitterModel = App.components().twitterModel();

    //view
    @Bind(R.id.twitter_error)
    LinearLayout errorMessage;

    @Bind(R.id.twitter_error_button)
    Button errorButton;

    @Bind(R.id.progressSpinner)
    ProgressBar loadingSpinner;

    @Bind(R.id.tweet_list)
    ListView tweetListView;

    @Bind(R.id.swipe_container)
    SwipeRefreshLayout swipeLayout;

    @Bind(R.id.twitter_new_tweet_button)
    FloatingActionButton newTweetButton;

    //adapter
    private TweetAdapter tweetAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_twitter, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);

        tweetAdapter = new TweetAdapter(getActivity(), R.layout.view_tweet, new ArrayList<Tweet>());
        tweetListView.setAdapter(tweetAdapter);

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new TwitterGetHandler(TwitterFragment.this).execute();
            }
        });

        newTweetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new PostTweetHandler(getActivity()).execute();
            }
        });

        swipeLayout.setColorSchemeResources(
                R.color.twitter_indicator_color_first,
                R.color.twitter_indicator_color_second);

        errorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TwitterGetHandler(TwitterFragment.this).execute();
                loadingSpinner.setVisibility(View.VISIBLE);
                errorMessage.setVisibility(View.GONE);
            }
        });

        new TwitterGetHandler(this).execute();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public void displayNewData() {
        swipeLayout.setRefreshing(false);
        errorMessage.setVisibility(View.GONE);
        loadingSpinner.setVisibility(View.GONE);

        swipeLayout.setVisibility(View.VISIBLE);
        tweetAdapter.clear();
        tweetAdapter.addAll(twitterModel.getTweets());
        tweetAdapter.notifyDataSetChanged();
    }

    public void showErrorMessage() {
        swipeLayout.setVisibility(View.GONE);
        loadingSpinner.setVisibility(View.GONE);
        errorMessage.setVisibility(View.VISIBLE);
    }
}