/*
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.imagepipeline.request;

import android.net.Uri;

import com.facebook.common.internal.Objects;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imageutils.BitmapUtil;

import java.io.File;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Immutable object encapsulating everything pipeline has to know about requested image to proceed.
 */
@Immutable
public class ImageRequest
{

    /** Cache choice */
    private final CacheChoice mCacheChoice;

    /** Source Uri */
    private final Uri mSourceUri;
    /** If set - the client will receive intermediate results */
    private final boolean mProgressiveRenderingEnabled;
    /** If set the client will receive thumbnail previews for local images, before the whole image */
    private final boolean mLocalThumbnailPreviewsEnabled;
    private final ImageDecodeOptions mImageDecodeOptions;
    /** Is auto-rotate enabled? */
    private final boolean mAutoRotateEnabled;
    /** Priority levels of this request. */
    private final Priority mRequestPriority;
    /** Lowest level that is permitted to fetch an image from */
    private final RequestLevel mLowestPermittedRequestLevel;
    /** Whether the disk cache should be used for this request */
    private final boolean mIsDiskCacheEnabled;
    /** Postprocessor to run on the output bitmap. */
    private final Postprocessor mPostprocessor;
    /** resize options */
    @Nullable
    ResizeOptions mResizeOptions = null;
    /** Source File - for local fetches only, lazily initialized */
    private File mSourceFile;

    protected ImageRequest (ImageRequestBuilder builder)
    {
        mCacheChoice = builder.getCacheChoice();
        mSourceUri = builder.getSourceUri();

        mProgressiveRenderingEnabled = builder.isProgressiveRenderingEnabled();
        mLocalThumbnailPreviewsEnabled = builder.isLocalThumbnailPreviewsEnabled();

        mImageDecodeOptions = builder.getImageDecodeOptions();

        mResizeOptions = builder.getResizeOptions();
        mAutoRotateEnabled = builder.isAutoRotateEnabled();

        mRequestPriority = builder.getRequestPriority();
        mLowestPermittedRequestLevel = builder.getLowestPermittedRequestLevel();
        mIsDiskCacheEnabled = builder.isDiskCacheEnabled();

        mPostprocessor = builder.getPostprocessor();
    }

    public static ImageRequest fromUri (@Nullable Uri uri)
    {
        return (uri == null) ? null : ImageRequestBuilder.newBuilderWithSource(uri).build();
    }

    public static ImageRequest fromUri (@Nullable String uriString)
    {
        return (uriString == null || uriString.length() == 0) ? null : fromUri(Uri.parse(uriString));
    }

    public CacheChoice getCacheChoice ()
    {
        return mCacheChoice;
    }

    public Uri getSourceUri ()
    {
        return mSourceUri;
    }

    public int getPreferredWidth ()
    {
        return (mResizeOptions != null) ? mResizeOptions.width : (int) BitmapUtil.MAX_BITMAP_SIZE;
    }

    public int getPreferredHeight ()
    {
        return (mResizeOptions != null) ? mResizeOptions.height : (int) BitmapUtil.MAX_BITMAP_SIZE;
    }

    public
    @Nullable
    ResizeOptions getResizeOptions ()
    {
        return mResizeOptions;
    }

    public ImageDecodeOptions getImageDecodeOptions ()
    {
        return mImageDecodeOptions;
    }

    public boolean getAutoRotateEnabled ()
    {
        return mAutoRotateEnabled;
    }

    public boolean getProgressiveRenderingEnabled ()
    {
        return mProgressiveRenderingEnabled;
    }

    public boolean getLocalThumbnailPreviewsEnabled ()
    {
        return mLocalThumbnailPreviewsEnabled;
    }

    public Priority getPriority ()
    {
        return mRequestPriority;
    }

    public RequestLevel getLowestPermittedRequestLevel ()
    {
        return mLowestPermittedRequestLevel;
    }

    public boolean isDiskCacheEnabled ()
    {
        return mIsDiskCacheEnabled;
    }

    public synchronized File getSourceFile ()
    {
        if (mSourceFile == null)
        {
            mSourceFile = new File(mSourceUri.getPath());
        }
        return mSourceFile;
    }

    public
    @Nullable
    Postprocessor getPostprocessor ()
    {
        return mPostprocessor;
    }

    @Override
    public boolean equals (Object o)
    {
        if (!(o instanceof ImageRequest))
        {
            return false;
        }
        ImageRequest request = (ImageRequest) o;
        return Objects.equal(mSourceUri, request.mSourceUri) &&
                Objects.equal(mCacheChoice, request.mCacheChoice) &&
                Objects.equal(mSourceFile, request.mSourceFile);
    }

    @Override
    public int hashCode ()
    {
        return Objects.hashCode(mCacheChoice, mSourceUri, mSourceFile);
    }

    /**
     * An enum describing the cache choice.
     */
    public enum CacheChoice
    {
        /* Indicates that this image should go in the small disk cache, if one is being used */
        SMALL,

        /* Default */
        DEFAULT,
    }

    /**
     * Level down to we are willing to go in order to find an image. E.g., we might only want to go
     * down to bitmap memory cache, and not check the disk cache or do a full fetch.
     */
    public enum RequestLevel
    {
        /* Fetch (from the network or local storage) */
        FULL_FETCH(1),

        /* Disk caching */
        DISK_CACHE(2),

        /* Encoded memory caching */
        ENCODED_MEMORY_CACHE(3),

        /* Bitmap caching */
        BITMAP_MEMORY_CACHE(4);

        private int mValue;

        RequestLevel (int value)
        {
            mValue = value;
        }

        public static RequestLevel getMax (RequestLevel requestLevel1, RequestLevel requestLevel2)
        {
            return requestLevel1.getValue() > requestLevel2.getValue() ? requestLevel1 : requestLevel2;
        }

        public int getValue ()
        {
            return mValue;
        }
    }
}
