package de.stephanlindauer.criticalmaps.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

import de.stephanlindauer.criticalmaps.utils.TwitterUtils;
import de.stephanlindauer.criticalmaps.vo.twitter.Tweet;

public class TwitterModel {

    private ArrayList<Tweet> tweets = new ArrayList<Tweet>();

    //singleton
    private static TwitterModel instance;

    public static TwitterModel getInstance() {
        if (TwitterModel.instance == null) {
            TwitterModel.instance = new TwitterModel();
        }
        return TwitterModel.instance;
    }

    public void setTweetsFromJsonString(String tweetsString) throws JSONException, ParseException {
            JSONObject jsonObject = new JSONObject(tweetsString);
            JSONArray statusesArray = jsonObject.getJSONArray("statuses");

            for (int i = 0; i < statusesArray.length(); i++) {
                JSONObject currentStatus = statusesArray.getJSONObject(i);
                Tweet tweet = new Tweet()
                        .setUserName(currentStatus.getJSONObject("user").getString("name"))
                        .setUserScreenName(currentStatus.getJSONObject("user").getString("screen_name"))
                        .setTweetId(currentStatus.getString("id_str"))
                        .setText(currentStatus.getString("text"))
                        .setTimestamp(TwitterUtils.getTwitterDate(currentStatus.getString("created_at")))
                        .setProfileImageUrl(currentStatus.getJSONObject("user").getString("profile_image_url_https"));

                tweets.add(tweet);
            }
    }

    public ArrayList<Tweet> getTweets() {
        return tweets;
    }
}
