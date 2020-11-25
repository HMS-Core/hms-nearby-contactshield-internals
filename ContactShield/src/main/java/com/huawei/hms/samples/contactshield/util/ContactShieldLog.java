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

package com.huawei.hms.samples.contactshield.util;

import android.util.Log;

/**
 * log interface for nearby kit
 *
 * @since 2019-10-31
 */
public class ContactShieldLog {
    private static final String GLOBAL_TAG = "ContactShield_";

    /**
     * Constructor
     */
    private ContactShieldLog() {}

    public static void d(String tag, String msg) {
        Log.d(GLOBAL_TAG + tag, msg);
    }

    public static void d(String tag, String msg, Throwable tr) {
        Log.d(GLOBAL_TAG + tag, msg, tr);
    }

    public static void i(String tag, String msg) {
        Log.i(GLOBAL_TAG + tag, msg);
    }

    public static void w(String tag, String msg) {
        Log.w(GLOBAL_TAG + tag, msg);
    }

    public static void e(String tag, String msg) {
        Log.e(GLOBAL_TAG + tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        Log.e(GLOBAL_TAG + tag, msg, tr);
    }
}
