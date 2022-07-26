/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reactnativethalespaysdkwrapper.util;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

/**
 * PaymentExecutor thread executor implementation
 */
public class PaymentExecutor {
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public void execute(@NonNull Runnable command) {
        mainThreadHandler.post(command);
    }

    public void execute(@NonNull Runnable command, long delayMs) {
        mainThreadHandler.postDelayed(command, delayMs);
    }

    public void cancel(@NonNull Runnable command) {
        mainThreadHandler.removeCallbacks(command);
    }

    public void cancelAllPendingOps() {
        mainThreadHandler.removeCallbacksAndMessages(null);
    }
}
