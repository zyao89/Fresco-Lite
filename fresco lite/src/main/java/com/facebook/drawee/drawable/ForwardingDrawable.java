/*
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.drawee.drawable;

import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;

/**
 * A forwarding drawable class - the goal is to forward (delegate) drawable functionality to an
 * inner drawable instance. ForwardingDrawable intercepts the public (and protected) methods of
 * {@link Drawable}, maintains local state if needed.
 * <p>
 * Design note: It would have been very helpful to re-use Android library classes
 * like DrawableContainer, LevelListDrawable etc. DrawableContainer is not directly subclassable,
 * and the others don't allow changing the member drawables.
 */
public class ForwardingDrawable extends Drawable implements Drawable.Callback, TransformCallback, TransformAwareDrawable, DrawableParent
{

    /**
     * Matrix used to store temporary transform. Drawables should be accessed on UI thread only, and
     * this matrix is used only as a temporary variable so it's safe to be static.
     */
    private static final Matrix sTempTransform = new Matrix();
    private final DrawableProperties mDrawableProperties = new DrawableProperties();

    protected TransformCallback mTransformCallback;
    /** The current drawable to be drawn by this drawable when drawing is needed */
    private Drawable mCurrentDelegate;

    /**
     * Constructs a new forwarding drawable.
     *
     * @param drawable drawable that this forwarding drawable will forward to
     */
    public ForwardingDrawable (Drawable drawable)
    {
        mCurrentDelegate = drawable;
        DrawableUtils.setCallbacks(mCurrentDelegate, this, this);
    }

    /**
     * Sets a new drawable to be the delegate, and returns the old one (or null).
     * <p>
     * <p>This method will cause the drawable to be invalidated.
     *
     * @param newDelegate
     *
     * @return the previous delegate
     */
    public Drawable setCurrent (Drawable newDelegate)
    {
        Drawable previousDelegate = setCurrentWithoutInvalidate(newDelegate);
        invalidateSelf();
        return previousDelegate;
    }

    /**
     * As {@code setCurrent}, but without invalidating a drawable. Subclasses are responsible to call
     * {@code invalidateSelf} on their own.
     *
     * @param newDelegate
     *
     * @return the previous delegate
     */
    protected Drawable setCurrentWithoutInvalidate (Drawable newDelegate)
    {
        Drawable previousDelegate = mCurrentDelegate;
        DrawableUtils.setCallbacks(previousDelegate, null, null);
        DrawableUtils.setCallbacks(newDelegate, null, null);
        DrawableUtils.setDrawableProperties(newDelegate, mDrawableProperties);
        DrawableUtils.copyProperties(newDelegate, this);
        DrawableUtils.setCallbacks(newDelegate, this, this);
        mCurrentDelegate = newDelegate;
        return previousDelegate;
    }

    @Override
    public void draw (Canvas canvas)
    {
        mCurrentDelegate.draw(canvas);
    }

    @Override
    public void setDither (boolean dither)
    {
        mDrawableProperties.setDither(dither);
        mCurrentDelegate.setDither(dither);
    }

    @Override
    public void setFilterBitmap (boolean filterBitmap)
    {
        mDrawableProperties.setFilterBitmap(filterBitmap);
        mCurrentDelegate.setFilterBitmap(filterBitmap);
    }

    @Override
    public void setAlpha (int alpha)
    {
        mDrawableProperties.setAlpha(alpha);
        mCurrentDelegate.setAlpha(alpha);
    }

    @Override
    public void setColorFilter (ColorFilter colorFilter)
    {
        mDrawableProperties.setColorFilter(colorFilter);
        mCurrentDelegate.setColorFilter(colorFilter);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setHotspot (float x, float y)
    {
        mCurrentDelegate.setHotspot(x, y);
    }

    @Override
    public boolean isStateful ()
    {
        return mCurrentDelegate.isStateful();
    }

    @Override
    public Drawable getCurrent ()
    {
        return mCurrentDelegate;
    }

    @Override
    public boolean setVisible (boolean visible, boolean restart)
    {
        super.setVisible(visible, restart);
        return mCurrentDelegate.setVisible(visible, restart);
    }

    @Override
    public int getOpacity ()
    {
        return mCurrentDelegate.getOpacity();
    }

    @Override
    protected boolean onStateChange (int[] state)
    {
        return mCurrentDelegate.setState(state);
    }

    @Override
    protected boolean onLevelChange (int level)
    {
        return mCurrentDelegate.setLevel(level);
    }

    @Override
    protected void onBoundsChange (Rect bounds)
    {
        mCurrentDelegate.setBounds(bounds);
    }

    @Override
    public int getIntrinsicWidth ()
    {
        return mCurrentDelegate.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight ()
    {
        return mCurrentDelegate.getIntrinsicHeight();
    }

    @Override
    public boolean getPadding (Rect padding)
    {
        return mCurrentDelegate.getPadding(padding);
    }

    // DrawableParent methods

    @Override
    public Drawable mutate ()
    {
        mCurrentDelegate.mutate();
        return this;
    }

    @Override
    public Drawable setDrawable (Drawable newDrawable)
    {
        return setCurrent(newDrawable);
    }

    // Drawable.Callback methods

    @Override
    public Drawable getDrawable ()
    {
        return getCurrent();
    }

    @Override
    public void invalidateDrawable (Drawable who)
    {
        invalidateSelf();
    }

    @Override
    public void scheduleDrawable (Drawable who, Runnable what, long when)
    {
        scheduleSelf(what, when);
    }

    //  TransformAwareDrawable methods

    @Override
    public void unscheduleDrawable (Drawable who, Runnable what)
    {
        unscheduleSelf(what);
    }

    // TransformationCallback methods

    @Override
    public void setTransformCallback (TransformCallback transformCallback)
    {
        mTransformCallback = transformCallback;
    }

    protected void getParentTransform (Matrix transform)
    {
        if (mTransformCallback != null)
        {
            mTransformCallback.getTransform(transform);
        }
        else
        {
            transform.reset();
        }
    }

    @Override
    public void getTransform (Matrix transform)
    {
        getParentTransform(transform);
    }

    @Override
    public void getRootBounds (RectF bounds)
    {
        if (mTransformCallback != null)
        {
            mTransformCallback.getRootBounds(bounds);
        }
        else
        {
            bounds.set(getBounds());
        }
    }

    /**
     * Gets the transformed bounds of this drawable.
     * Note: bounds are not cropped (otherwise they would likely be the same as drawable's bounds).
     *
     * @param outBounds rect to fill with bounds
     */
    public void getTransformedBounds (RectF outBounds)
    {
        getParentTransform(sTempTransform);
        // IMPORTANT: {@code getBounds} should be called after {@code getParentTransform},
        // because the parent may have to change our bounds.
        outBounds.set(getBounds());
        sTempTransform.mapRect(outBounds);
    }
}
