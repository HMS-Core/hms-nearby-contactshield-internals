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

import com.huawei.hms.samples.contactshield.contact.ContactShieldSetting;

/**
 * GlobalSettings
 *
 * @since 2020-06-22
 */
public class GlobalSettings {
    /**
     * Max number of intervals(10 minutes) for the life time of PeriodKey
     */
    public static final int PDK_ROLLING_PERIOD = 144;

    /**
     * Max duration to keep BLE scan data in database, default to 60 days.
     */
    public static final int BLE_SCAN_DATA_DURATION = 60;

    /**
     * Step value to calculate contacted duration in minutes when generate ContactDetail data.
     */
    public static final int DURATION_STEP = 5;

    /**
     * Max contacted duration, default to 30 minutes
     */
    public static final int DURATION_MAX_MINUTES = 30;

    /**
     * Max number of scan data per day to be saved in database, which is around 100 MB:
     *   1896 * 1024 * 54B(scan data size)
     */
    public static final long DAILY_SCAN_DATA_COUNT_LIMIT = 1896L * 1024;

    /**
     * Max duration since last scan, default to 300 seconds
     */
    public static final int MAX_DURATION_SINCE_LAST_SCAN = 5 * 60;

    /**
     * Continues BLE scan duration, default to 4000 milliseconds
     */
    public static final int BLE_SCAN_DURATION = 4 * 1000;

    /**
     * Max report type value
     */
    public static final int REPORT_TYPE_MAX = 5;

    /**
     * Token string for window mode
     */
    public static final String TOKEN_WINDOW_MODE = "TOKEN_WINDOW_MODE";

    /**
     * Max duration that one ContactWindow covers in window mode, 30 minutes(1800 seconds)
     */
    public static final int CONTACT_WINDOW_SECOND = 30 * 60;

    /**
     * Quota to call putSharedKeyFiles(tokenA) per day, default to 60 times
     */
    public static final int PUT_TOKEN_A_MAX_COUNT = 60;

    /**
     * Period (24 hours) during which calling putSharedKeyFiles(tokenA) is limited with quota.
     */
    public static final int PUT_TOKEN_A_PERIOD = 24 * 60 * 60;

    /**
     * Quota to call putSharedKeyFiles(normal token) per day, default to 200 times
     */
    public static final int PUT_NORMAL_TOKEN_MAX_COUNT = 200;

    /**
     * Period (24 hours) during which calling putSharedKeyFiles(normal token) is limited with quota
     */
    public static final int PUT_NORMAL_TOKEN_PERIOD = 24 * 60 * 60;

    /**
     * Minimum advertising period, default to 610 seconds
     */
    public static final int MIN_ADV_PERIOD_SECONDS = 610;

    /**
     * Maximum advertising period, default to 690 seconds
     */
    public static final int MAX_ADV_PERIOD_SECONDS = 690;

    /**
     * Minimum scanning period, default to 160 seconds
     */
    public static final int MIN_SCAN_PERIOD_SECONDS = 160;

    /**
     * Maximum scanning period, default to 240 seconds
     */
    public static final int MAX_SCAN_PERIOD_SECONDS = 240;

    /**
     * Scanning period when for power saving mode(screen off), default to 540 seconds.
     */
    public static final int POWER_SAVE_SCAN_PERIOD_SECONDS = 540;

    /**
     * Minimum storage requirement, default to 10 MB
     */
    public static final long MINIMUM_STORAGE_SPACE = 10 * 1024 * 1024L;

    /**
     * Incubation period, default to 14 days
     */
    public static final int DEFAULT_INCUBATION_PERIOD = 14;

    /**
     * Broadcast action when BLE scanner in idle state is timeout
     */
    public static final String INTENT_ACTION_CONTACT_SHIELD_PERIOD_TIMEOUT =
        "com.huawei.hms.contactshield.CONTACT_SHIELD_PERIOD_TIMEOUT";

    /**
     * Local calibration for current phone model
     */
    private static Calibration sCalibration = Calibration.DEFAULT;

    /**
     * set calibration
     *
     * @param calibration calibration
     */
    public static void setCalibration(Calibration calibration) {
        sCalibration = calibration;
    }

    /**
     * get calibration
     *
     * @return sCalibration
     */
    public static Calibration getCalibration() {
        return sCalibration;
    }

    /**
     * getGlobalContactShieldSetting
     */
    public static ContactShieldSetting getGlobalContactShieldSetting() {
        return ContactShieldSetting.DEFAULT;
    }
}
