/*
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.imagepipeline.bitmaps;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;

import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.memory.BitmapPool;
import com.facebook.imagepipeline.nativecode.Bitmaps;
import com.facebook.imageutils.BitmapUtil;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Bitmap factory for ART VM (Lollipop and up).
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@ThreadSafe
public class ArtBitmapFactory extends PlatformBitmapFactory
{

    private final BitmapPool mBitmapPool;

    public ArtBitmapFactory (BitmapPool bitmapPool)
    {
        mBitmapPool = bitmapPool;
    }

    /**
     * Creates a bitmap of the specified width and height.
     *
     * @param width        the width of the bitmap
     * @param height       the height of the bitmap
     * @param bitmapConfig the {@link Bitmap.Config}
     *                     used to create the decoded Bitmap
     *
     * @return a reference to the bitmap
     *
     * @throws OutOfMemoryError if the Bitmap cannot be allocated
     */
    @Override
    public CloseableReference<Bitmap> createBitmap (int width, int height, Bitmap.Config bitmapConfig)
    {
        int sizeInBytes = BitmapUtil.getSizeInByteForBitmap(width, height, bitmapConfig);
        Bitmap bitmap = mBitmapPool.get(sizeInBytes);
        Bitmaps.reconfigureBitmap(bitmap, width, height, bitmapConfig);
        return CloseableReference.of(bitmap, mBitmapPool);
    }
}
