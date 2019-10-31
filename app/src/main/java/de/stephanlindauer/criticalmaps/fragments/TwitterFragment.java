package de.stephanlindauer.criticalmaps.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.stephanlindauer.criticalmaps.App;
import de.stephanlindauer.criticalmaps.R;
import de.stephanlindauer.criticalmaps.adapter.TweetAdapter;
import de.stephanlindauer.criticalmaps.handler.PostTweetHandler;
import de.stephanlindauer.criticalmaps.handler.TwitterGetHandler;
import de.stephanlindauer.criticalmaps.model.TwitterModel;

public class TwitterFragment extends Fragment {

    //dependencies
    private final TwitterModel twitterModel = App.components().twitterModel();

    //view
    @BindView(R.id.twitter_error)
    LinearLayout errorMessage;

    @BindView(R.id.twitter_error_button)
    Button errorButton;

    @BindView(R.id.tweet_list)
    RecyclerView tweetsRecyclerView;

    @BindView(R.id.swipe_container)
    public SwipeRefreshLayout swipeLayout;

    @BindView(R.id.twitter_new_tweet_button)
    public FloatingActionButton newTweetButton;

    //adapter
    private TweetAdapter tweetAdapter;
    private Unbinder unbinder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_twitter, container, false);
        unbinder = ButterKnife.bind(this, view);

        tweetsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedState) {
        super.onActivityCreated(savedState);

        tweetAdapter = new TweetAdapter(getActivity(), new ArrayList<>());
        tweetsRecyclerView.setAdapter(tweetAdapter);
        tweetsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && newTweetButton.getVisibility() == View.VISIBLE) {
                    newTweetButton.hide();
                } else if (dy < 0 && newTweetButton.getVisibility() != View.VISIBLE) {
                    newTweetButton.show();
                }
            }
        });
        swipeLayout.setOnRefreshListener(() ->
                new TwitterGetHandler(TwitterFragment.this).execute());

        swipeLayout.setColorSchemeResources(
                R.color.twitter_indicator_color_first,
                R.color.twitter_indicator_color_second);

        swipeLayout.setProgressBackgroundColorSchemeResource(R.color.tweet_progress_bar_background);

        errorButton.setOnClickListener(v -> {
            new TwitterGetHandler(TwitterFragment.this).execute();
            errorMessage.setVisibility(View.GONE);
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

        swipeLayout.setVisibility(View.VISIBLE);
        tweetAdapter.updateData(twitterModel.getTweets());
    }

    public void showErrorMessage() {
        swipeLayout.setVisibility(View.GONE);
        errorMessage.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.twitter_new_tweet_button)
    void handleNewTweetClicked(View view) {
        new PostTweetHandler(getActivity()).execute();
    }
}