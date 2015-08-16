package de.stephanlindauer.criticalmaps.vo.twitter;

import java.util.Date;

public class Tweet {
    private String profileImageUrl;
    private String userName;
    private String userScreenName;
    private String tweetId;
    private String text;
    private Date timestamp;

    public Tweet setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public Tweet setUserScreenName(String userScreenName) {
        this.userScreenName = userScreenName;
        return this;
    }

    public Tweet setTweetId(String tweetId) {
        this.tweetId = tweetId;
        return this;
    }

    public Tweet setText(String text) {
        this.text = text;
        return this;
    }

    public Tweet setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Tweet setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public String getText() {
        return text;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getUserScreenName() {
        return userScreenName;
    }

    public String getTweetId() {
        return tweetId;
    }
}
