package de.stephanlindauer.criticalmass.twitter;

import org.jetbrains.annotations.NotNull;

/**
 * Abstract class for proxy connection to external twitter libraries
 */
public interface ProxyApi {

    public void searchTweets(@NotNull final String[] hashTag, @NotNull final ITweetListener listener);
}
