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

package com.huawei.hms.samples.contactshield;

/**
 * Status code for Nearby operation
 *
 * @since 2019-11-20
 */
public final class StatusCode {
    /**
     * Operation success.
     */
    public static final int STATUS_SUCCESS = 0;

    /**
     * Operation failure.
     */
    public static final int STATUS_FAILURE = -1;

    /**
     * ContactShield app quota limited
     */
    public static final int STATUS_APP_QUOTA_LIMITED = 8100;

    /**
     * ContactShield data storage is full
     */
    public static final int STATUS_DISK_FULL = 8101;

    /**
     * ContactShield bluetooth operation error
     */
    public static final int STATUS_BLUETOOTH_OPERATION_ERROR = 8102;

    /**
     * Return debug message based on status code.
     *
     * @param statusCode Status code
     * @return Debug message for the status code.
     */
    public static String getStatusCode(int statusCode) {
        switch (statusCode) {
            case STATUS_SUCCESS: {
                return "STATUS_SUCCESS";
            }
            case STATUS_FAILURE: {
                return "STATUS_FAILURE";
            }
            case STATUS_APP_QUOTA_LIMITED: {
                return "STATUS_APP_QUOTA_LIMITED";
            }
            case STATUS_DISK_FULL: {
                return "STATUS_DISK_FULL";
            }
            case STATUS_BLUETOOTH_OPERATION_ERROR: {
                return "STATUS_BLUETOOTH_OPERATION_ERROR";
            }
            default: {
                return "Unknown status code: " + statusCode;
            }
        }
    }
}
