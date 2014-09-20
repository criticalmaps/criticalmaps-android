package de.stephanlindauer.criticalmass.utils;

import org.jetbrains.annotations.Nullable;

public interface AsyncCallback {
    void onComplete(@Nullable final Object result);

    void onException(final Exception e);
}
