/*
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.imagepipeline.nativecode;

/**
 * This is the class responsible to return the WebpTranscoder if any
 */
public class WebpTranscoderFactory
{

    public static boolean sWebpTranscoderPresent = false;
    private static WebpTranscoder sWebpTranscoder;

    static
    {
        try
        {
            sWebpTranscoder = (WebpTranscoder) Class.forName("com.facebook.imagepipeline.nativecode.WebpTranscoderImpl").newInstance();
            sWebpTranscoderPresent = true;
        }
        catch (Throwable e)
        {
            sWebpTranscoderPresent = false;
        }
    }

    public static WebpTranscoder getWebpTranscoder ()
    {
        return sWebpTranscoder;
    }

}
