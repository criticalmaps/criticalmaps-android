package de.stephanlindauer.criticalmass.twitter;

public class Tweet {

    public String userName;
    public String content;

    @Override
    public String toString() {
        return "Tweet{" +
                "userName='" + userName + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tweet tweet = (Tweet) o;

        return !(content != null ? !content.equals(tweet.content) : tweet.content != null) && !(userName != null ? !userName.equals(tweet.userName) : tweet.userName != null);
    }

    @Override
    public int hashCode() {
        int result = userName != null ? userName.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }
}
