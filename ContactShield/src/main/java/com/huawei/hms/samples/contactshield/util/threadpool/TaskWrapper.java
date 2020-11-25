/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.hms.samples.contactshield.util.threadpool;

import com.huawei.hms.samples.contactshield.util.ContactShieldLog;

public class TaskWrapper implements Runnable {
    private static final String TAG = "TaskWrapper";
    private Runnable mRunnable;

    public TaskWrapper(Runnable runnable) {
        mRunnable = runnable;
    }

    @Override
    public void run() {
        if (mRunnable != null) {
            try {
                mRunnable.run();
            } catch (Throwable e) {
                ContactShieldLog.e(TAG, e.getMessage());
            }
        }
    }
}
