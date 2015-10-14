package de.stephanlindauer.criticalmaps.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

import de.stephanlindauer.criticalmaps.utils.TwitterUtils;
import de.stephanlindauer.criticalmaps.vo.twitter.Tweet;

public class TwitterModel {

    private ArrayList<Tweet> tweets = new ArrayList<>();

    //singleton
    private static TwitterModel instance;

    private TwitterModel() {}

    public static TwitterModel getInstance() {
        if (TwitterModel.instance == null) {
            TwitterModel.instance = new TwitterModel();
        }
        return TwitterModel.instance;
    }

    public void setTweetsFromJsonString(String tweetsString) throws JSONException, ParseException {
        tweets.clear();
        JSONObject jsonObject = new JSONObject(tweetsString);
        JSONArray statusesArray = jsonObject.getJSONArray("statuses");

        for (int i = 0, statusesArrayLength = statusesArray.length(); i < statusesArrayLength; i++) {
            JSONObject currentStatus = statusesArray.getJSONObject(i);
            final JSONObject user = currentStatus.getJSONObject("user");
            Tweet tweet = new Tweet()
                    .setUserName(user.getString("name"))
                    .setUserScreenName(user.getString("screen_name"))
                    .setTweetId(currentStatus.getString("id_str"))
                    .setText(currentStatus.getString("text"))
                    .setTimestamp(TwitterUtils.getTwitterDate(currentStatus.getString("created_at")))
                    .setProfileImageUrl(user.getString("profile_image_url_https"));

            tweets.add(tweet);
        }
    }

    public ArrayList<Tweet> getTweets() {
        return tweets;
    }
}
