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

import android.os.ParcelUuid;

import com.huawei.hms.samples.contactshield.util.ContactShieldLog;

/**
 * Contact beacon
 *
 * @since 2020-06-31
 */
public class ContactBeacon {
    private static final String TAG = "ContactBeacon";

    /**
     * Contact Service UUID:0xFD6F
     */
    public static final String UUID = "0000fd6f-0000-1000-8000-00805f9b34fb";

    /**
     * Contact Service UUID:0xFD6F
     */
    public static final ParcelUuid SERVICE_UID = ParcelUuid.fromString("0000fd6f-0000-1000-8000-00805f9b34fb");

    /**
     * SD_VERSION
     */
    public static final byte SD_VERSION = 0b01000000;

    /**
     * Contact Service data length
     */
    public static final int DATA_LENGTH = 20;

    /**
     * offset of uuid Dynamic Sharing Code
     */
    public static final byte DSC_OFFSET = 0;

    /**
     * Dynamic Sharing Code (DSC) length (bytes)
     */
    public static final byte DSC_LENGTH = 16;

    /**
     * offset of Supplementary Data (SD)
     */
    public static final byte SD_OFFSET = 16;

    /**
     * Supplementary Data (SD) length (bytes)
     */
    public static final byte SD_LENGTH = 4;

    private byte[] mDsc = new byte[0];

    private byte[] mSd = new byte[0];

    /**
     * Constructor
     */
    public ContactBeacon(byte[] dsc, byte[] sd) {
        if (dsc != null) {
            mDsc = dsc.clone();
        }
        if (sd != null) {
            mSd = sd.clone();
        }
    }

    public byte[] getDsc() {
        return mDsc.clone();
    }

    public byte[] getSd() {
        return mSd.clone();
    }

    public void setDsc(byte[] dsc) {
        if (dsc != null && dsc.length == DSC_LENGTH) {
            mDsc = dsc.clone();
        }
    }

    public void setSd(byte[] sd) {
        if (sd != null && sd.length == SD_LENGTH) {
            mSd = sd.clone();
        }
    }

    /**
     * Pack contact beacon to buffer
     *
     * @return buffer
     */
    public byte[] pack() {
        if (mDsc.length != DSC_LENGTH || mSd.length != SD_LENGTH) {
            ContactShieldLog.w(TAG, "Invalid DSC or SD");
            return new byte[0];
        }
        byte[] data = new byte[DATA_LENGTH];
        System.arraycopy(mDsc, 0, data, DSC_OFFSET, DSC_LENGTH);
        System.arraycopy(mSd, 0, data, SD_OFFSET, SD_LENGTH);
        return data;
    }

    /**
     * Extract contact beacon from buffer
     *
     * @param data input buffer
     * @return contact beacon
     */
    public static ContactBeacon unpack(byte[] data) {
        if (data == null || data.length < DATA_LENGTH) {
            return null;
        }

        byte[] dsc = new byte[DSC_LENGTH];
        System.arraycopy(data, 0, dsc, 0, DSC_LENGTH);
        byte[] sd = new byte[SD_LENGTH];
        System.arraycopy(data, SD_OFFSET, sd, 0, SD_LENGTH);
        return new ContactBeacon(dsc, sd);
    }
}
