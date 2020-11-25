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

/**
 * KeyGenUtil
 *
 * @since 2020-05-12
 */
public class KeyGenUtil {
    /**
     * Interval number to day number
     */
    public static final int INTERVAL_TO_DAY = 6 * 24;

    /**
     * Milliseconds to seconds
     */
    public static final long MILLI_SECS_TO_SECS = 1000L;

    /**
     * Seconds to minute
     */
    public static final int SECOND_TO_MINUTE = 60;

    /**
     * Milliseconds to day
     */
    public static final int MILLI_SECS_TO_DAYS = 60 * 60 * 24 * 1000;

    private static final int SECS_TO_DAYS = 60 * 60 * 24;

    private static final int SECS_TO_10MINS = 60 * 10;

    /**
     * Calculate dayNumber since Epoch.
     *
     * @return day number
     */
    public static long getDayNumber() {
        return getSecondsSinceEpoch() / SECS_TO_DAYS;
    }

    /**
     * Calculate interval number since Epoch.
     *
     * @return interval number
     */
    public static long getIntervalNumber() {
        return getSecondsSinceEpoch() / SECS_TO_10MINS;
    }

    /**
     * IntervalNumber is encoded as a 32-bit (uint32_t) unsigned little-endian value.
     *
     * @return little-endian value
     */
    public static byte[] getEncodeIntervalNumberByteArray(long intervalNumber) {
        return intToByteLittleEndian((int)getUint32(intervalNumber));
    }

    /**
     * Interval number to day number
     *
     * @return day number
     */
    public static long interval2DayNumber(long intervalNum) {
        return intervalNum / INTERVAL_TO_DAY;
    }

    /**
     * Interval number to day number in milliseconds
     *
     * @return day number in milliseconds
     */
    public static long intervalNum2DayMillis(long intervalNum) {
        return interval2DayNumber(intervalNum) * MILLI_SECS_TO_DAYS;
    }

    /**
     * Get database delete interval number with the given gap value.
     *
     * @return delete interval number
     */
    public static long getDeleteTimeInterval(int dayGap) {
        return (getDayNumber() - dayGap) * INTERVAL_TO_DAY;
    }

    /**
     * Check if two interval numbers are in the same day
     *
     * @return true is same day.
     */
    public static boolean checkIfIntervalSameDay(long lastInterval, long currentInterval) {
        return interval2DayNumber(lastInterval) == interval2DayNumber(currentInterval);
    }

    /**
     * Returns the total seconds since the Epoch.
     *
     * @return total seconds
     */
    public static long getSecondsSinceEpoch() {
        return System.currentTimeMillis() / 1000L;
    }

    private static byte[] intToByteLittleEndian(int in) {
        byte[] result = new byte[4];
        result[3] = (byte) (in >> 24 & 0xff);
        result[2] = (byte) (in >> 16 & 0xff);
        result[1] = (byte) (in >> 8 & 0xff);
        result[0] = (byte) (in >> 0 & 0xff);
        return result;
    }

    private static long getUint32(long val) {
        return val & 0x00000000ffffffff;
    }
}
