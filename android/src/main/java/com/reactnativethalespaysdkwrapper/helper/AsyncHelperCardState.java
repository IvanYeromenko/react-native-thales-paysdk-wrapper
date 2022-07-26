/*
 * MIT License
 *
 * Copyright (c) 2021 Thales DIS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.reactnativethalespaysdkwrapper.helper;

import androidx.annotation.NonNull;

import com.gemalto.mfs.mwsdk.dcm.DigitalizedCardStatus;
import com.gemalto.mfs.mwsdk.utils.async.AbstractAsyncHandler;
import com.gemalto.mfs.mwsdk.utils.async.AsyncResult;

public class AsyncHelperCardState extends AbstractAsyncHandler<DigitalizedCardStatus> {

    //region Defines

    public interface Delegate {
        void onSuccess(final DigitalizedCardStatus value);

        void onError(final String error);
    }

    private final Delegate mDelegate;

    //endregion

    //region Life Cycle

    public AsyncHelperCardState(@NonNull final Delegate delegate) {
        super();

        mDelegate = delegate;
    }

    //endregion


    //region MGAbstractAsyncHandler

    @Override
    public void onComplete(final AsyncResult<DigitalizedCardStatus> asyncResult) {
        if (asyncResult.isSuccessful()) {
            mDelegate.onSuccess(asyncResult.getResult());
        } else {
            mDelegate.onError(asyncResult.getErrorMessage());
        }
    }

    //endregion
}


