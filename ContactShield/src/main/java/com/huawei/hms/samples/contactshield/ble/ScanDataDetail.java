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
package com.huawei.hms.samples.contactshield.ble;

import com.huawei.hms.samples.contactshield.util.KeyGenUtil;

/**
 * To store scan data detail during scanning period.
 *
 * @since 2020-07-14
 */
public class ScanDataDetail {
    private byte[] mDscData;
    private byte[] mSdData;
    private int mMaxRssi;
    private int mCount;
    private double mAverageRssi;
    private long mIntervalNum;

    /**
     * Constructor
     *
     * @param dscData DSC data
     * @param sdData SD Data
     * @param rssi RSSI
     */
    public ScanDataDetail(byte[] dscData, byte[] sdData, int rssi) {
        this.mDscData = dscData.clone();
        this.mSdData = sdData.clone();
        mMaxRssi = rssi;
        mCount = 1;
        mAverageRssi = rssi;
        mIntervalNum = KeyGenUtil.getIntervalNumber();
    }

    /**
     * Add RSSI
     *
     * @param rssi RSSI
     */
    public void addRssi(int rssi) {
        if (mMaxRssi < rssi) {
            // Capture the maximum rssi info
            mMaxRssi = rssi;
            mIntervalNum = KeyGenUtil.getIntervalNumber();
        }
        // Update average rssi and count
        mAverageRssi = (mAverageRssi * mCount + rssi) / (mCount + 1);
        mCount++;
    }

    /**
     * Getter
     */
    public byte[] getDscData() {
        return mDscData.clone();
    }

    /**
     * Getter
     */
    public byte[] getSdData() {
        return mSdData.clone();
    }

    /**
     * Getter
     */
    public int getMaxRssi() {
        return mMaxRssi;
    }

    /**
     * Getter
     */
    public double getAverageRssi() {
        return mAverageRssi;
    }

    /**
     * Getter
     */
    public long getIntervalNum() {
        return mIntervalNum;
    }
}
