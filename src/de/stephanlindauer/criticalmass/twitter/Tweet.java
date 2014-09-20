package de.stephanlindauer.criticalmass.twitter;

import android.graphics.Bitmap;
import org.jetbrains.annotations.NotNull;
import twitter4j.Status;

import java.util.Date;

public class Tweet implements Comparable<Tweet> {

    public String userName;
    public String content;
    public String screenName;
    public String userImageUrl;
    public Date creationDate;
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

        if (retweets != tweet.retweets) return false;
        if (content != null ? !content.equals(tweet.content) : tweet.content != null) return false;
        if (creationDate != null ? !creationDate.equals(tweet.creationDate) : tweet.creationDate != null) return false;
        if (image != null ? !image.equals(tweet.image) : tweet.image != null) return false;
        if (screenName != null ? !screenName.equals(tweet.screenName) : tweet.screenName != null) return false;
        if (userImageUrl != null ? !userImageUrl.equals(tweet.userImageUrl) : tweet.userImageUrl != null) return false;
        if (userName != null ? !userName.equals(tweet.userName) : tweet.userName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = userName != null ? userName.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (screenName != null ? screenName.hashCode() : 0);
        result = 31 * result + (userImageUrl != null ? userImageUrl.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + retweets;
        result = 31 * result + (image != null ? image.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(final Tweet another) {
        return (int) (creationDate.getTime() - another.creationDate.getTime());
    }
}
