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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Thread pool wrapper to catch all the exception
 *
 * @since 2020-07-13
 */
public final class ThreadExec {
    private static final long DEFAULT_KEEP_ALIVE_TIME = 60L;

    private static final int THREAD_MIN_DEFAULT = 1;
    private static final int THREAD_MAX_LIMIT = 16;

    private static ThreadPoolExecutor sCoreExecutor;

    private static ThreadPoolExecutor sSeqTaskExecutor;

    static {
        initExecutor();
    }

    private static synchronized void initExecutor() {
        sSeqTaskExecutor = new ThreadPoolExecutor(0, THREAD_MIN_DEFAULT, DEFAULT_KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        sSeqTaskExecutor.allowCoreThreadTimeOut(true);

        sCoreExecutor = new ThreadPoolExecutor(THREAD_MIN_DEFAULT, THREAD_MAX_LIMIT, DEFAULT_KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        sCoreExecutor.allowCoreThreadTimeOut(true);
    }

    /**
     * Run normal task
     *
     * @param task task
     */
    public static void execNormalTask(String moduleName, Runnable task) {
        sCoreExecutor.execute(new TaskWrapper(task));
    }

    /**
     * Run sequence task
     *
     * @param task task
     */
    public static void execSeqTask(String moduleName, Runnable task) {
        sSeqTaskExecutor.execute(new TaskWrapper(task));
    }
}