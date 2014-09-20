package de.stephanlindauer.criticalmass.twitter;

import org.jetbrains.annotations.NotNull;

/**
 * Interface that proxies Twitter Stream API calls
 */
public interface ITweetListener {

    public void onNewTweet(@NotNull final Tweet tweet);

    public void onException(@NotNull final Exception e);
}
