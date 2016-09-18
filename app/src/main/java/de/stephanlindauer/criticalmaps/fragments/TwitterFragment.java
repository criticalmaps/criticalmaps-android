package de.stephanlindauer.criticalmaps.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.Unbinder;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;
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
    @BindView(R.id.twitter_error)
    LinearLayout errorMessage;

    @BindView(R.id.twitter_error_button)
    Button errorButton;

    @BindView(R.id.progressSpinner)
    ProgressBar loadingSpinner;

    @BindView(R.id.tweet_list)
    ListView tweetListView;

    @BindView(R.id.swipe_container)
    public SwipeRefreshLayout swipeLayout;

    //adapter
    private TweetAdapter tweetAdapter;
    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_twitter, container, false);
        unbinder = ButterKnife.bind(this, view);
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
        unbinder.unbind();
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

    @OnClick(R.id.twitter_new_tweet_button)
    void handleNewTweetClicked(View view) {
        new PostTweetHandler(getActivity()).execute();
    }
}