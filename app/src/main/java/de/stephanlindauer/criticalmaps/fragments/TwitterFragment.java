package de.stephanlindauer.criticalmaps.fragments;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.adapter.TweetAdapter;
import de.stephanlindauer.criticalmaps.handler.TwitterGetHandler;
import de.stephanlindauer.criticalmaps.model.TwitterModel;
import de.stephanlindauer.criticalmaps.vo.twitter.Tweet;

public class TwitterFragment extends SuperFragment {

    //dependencies
    private TwitterModel twitterModel = TwitterModel.getInstance();

    //view
    private View twitterView;
    private ProgressBar loadingSpinner;
    private SwipeRefreshLayout swipeLayout;
    //adapter
    private TweetAdapter tweetAdapter;

    //misc
    private final TwitterFragment thiz = this;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        twitterView = inflater.inflate(R.layout.fragment_twitter, container, false);
        return twitterView;
    }

    @Override
    public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);

        loadingSpinner = (ProgressBar) getActivity().findViewById(R.id.progressSpinner);
        tweetAdapter = new TweetAdapter(getActivity(), R.layout.view_tweet, new ArrayList<Tweet>());

        ListView tweetListView = (ListView) getActivity().findViewById(R.id.tweet_list);
        tweetListView.setAdapter(tweetAdapter);

        swipeLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new TwitterGetHandler(thiz).execute();
                Toast.makeText(getActivity(), R.string.twitter_refreshed, Toast.LENGTH_SHORT).show();
            }
        });
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.darker_gray,
                android.R.color.holo_blue_bright,
                android.R.color.darker_gray);

        new TwitterGetHandler(this).execute();
    }

    public void displayNewData() {
        swipeLayout.setRefreshing(false);
        loadingSpinner.setVisibility(View.GONE);

        tweetAdapter.clear();
        tweetAdapter.addAll(twitterModel.getTweets());
        tweetAdapter.notifyDataSetChanged();
    }
}