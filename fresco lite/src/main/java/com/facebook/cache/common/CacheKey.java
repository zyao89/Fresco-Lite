/*
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.cache.common;

import android.net.Uri;

/**
 * Strongly typed cache key to be used instead of {@link Object}.
 * <p>
 * <p> {@link #toString}, {@link #equals} and {@link #hashCode} methods must be implemented.
 */
public interface CacheKey
{

    /** This method must be implemented, otherwise the cache keys will be be compared by reference. */
    boolean equals (Object o);

    /** This method must be implemented with accordance to the {@link #equals} method. */
    int hashCode ();

    /** This is useful for instrumentation and debugging purposes. */
    String toString ();

    /**
     * Returns true if this key was constructed from this {@link Uri}.
     * <p>
     * Used for cases like deleting all keys for a given uri.
     */
    boolean containsUri (Uri uri);
}
