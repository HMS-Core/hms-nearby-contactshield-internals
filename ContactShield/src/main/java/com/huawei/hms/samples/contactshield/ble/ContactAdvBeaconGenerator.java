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

import com.huawei.hms.samples.contactshield.util.ContactShieldLog;
import com.huawei.hms.samples.contactshield.crypto.DynamicSharingCode;
import com.huawei.hms.samples.contactshield.crypto.SupplementaryData;
import com.huawei.hms.samples.contactshield.util.GlobalSettings;

import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * Contact adv beacon generator
 *
 * @since 2020-06-31
 */
public class ContactAdvBeaconGenerator {
    private static final String TAG = "ContactAdvData";
    private DynamicSharingCode mDynamicSharingCode;
    private SupplementaryData mSupplementaryData;

    public ContactAdvBeaconGenerator() {
        mDynamicSharingCode = new DynamicSharingCode();
        mSupplementaryData = new SupplementaryData();
    }

    public ContactBeacon generate(String pkgName) {
        // Generate DSC data
        byte[] dscData;
        try {
            dscData = mDynamicSharingCode.generateDynamicSharingCode(pkgName);
        } catch (GeneralSecurityException e) {
            ContactShieldLog.w(TAG, "contact DSC data exception:" + e.getMessage());
            return null;
        }
        ContactShieldLog.i(TAG, "contact DSC data,dscData.length=" + dscData.length + ":"
                + Arrays.toString(dscData));

        // Fill up metadata
        byte[] metaData = new byte[ContactBeacon.SD_LENGTH];
        Arrays.fill(metaData, (byte) 0);
        metaData[0] = ContactBeacon.SD_VERSION;
        metaData[1] = (byte) GlobalSettings.getCalibration().getTx();

        // Generate sdData
        byte[] sdData;
        try {
            sdData = mSupplementaryData.generateSupplementaryData(dscData, metaData, pkgName);
        } catch (GeneralSecurityException e) {
            ContactShieldLog.w(TAG, "contact SD data exception:" + e.getMessage());
            return null;
        }
        ContactShieldLog.i(TAG, "contact SD data, sdData.length=" + sdData.length + ":"
                + Arrays.toString(sdData));
        return new ContactBeacon(dscData, sdData);
    }
}
