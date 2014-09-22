package de.stephanlindauer.criticalmass.twitter;

import android.graphics.Bitmap;
import android.util.Log;
import org.jetbrains.annotations.NotNull;
import twitter4j.Status;

import java.util.Date;

public class Tweet implements Comparable<Tweet> {

    public String userName;
    public String content;
    public String screenName;
    public String userImageUrl;
    public volatile Date creationDate;
    public int retweets;
    public Bitmap image;

    public Tweet(@NotNull final String userName, @NotNull final String content, @NotNull final String screenName, @NotNull final String userImage, @NotNull final Date creationDate, final int retweets) {
        this.userName = userName;
        this.content = content;
        this.screenName = screenName;
        this.userImageUrl = userImage;
        this.creationDate = creationDate;
        this.retweets = retweets;
    }

    public Tweet(@NotNull final Status status) {
        this(status.getUser().getName(),
                status.getText(),
                status.getUser().getScreenName(),
                status.getUser().getBiggerProfileImageURL(),
                status.getCreatedAt(),
                status.getRetweetCount());
    }

    public Tweet() {
    }

    @Override
    public String toString() {
        return "Tweet{" +
                "userName='" + userName + '\'' +
                ", content='" + content + '\'' +
                ", screenName='" + screenName + '\'' +
                ", userImageUrl='" + userImageUrl + '\'' +
                ", creationDate=" + creationDate +
                ", retweets=" + retweets +
                ", image=" + image +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tweet tweet = (Tweet) o;

        if (creationDate != null ? !creationDate.equals(tweet.creationDate) : tweet.creationDate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return creationDate != null ? creationDate.hashCode() : 0;
    }

    @Override
    public synchronized int compareTo(@NotNull final Tweet another) {
        return creationDate.compareTo(another.creationDate);
    }
}
