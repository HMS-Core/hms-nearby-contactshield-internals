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

package com.huawei.hms.samples.contactshield.crypto;

import androidx.annotation.VisibleForTesting;

import com.huawei.hms.samples.contactshield.util.ContactShieldLog;
import com.huawei.hms.samples.contactshield.ContactDataManage;
import com.huawei.hms.samples.contactshield.util.KeyGenUtil;

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Periodic Key Generator
 *
 * @since 2020-05-06
 */
public class PeriodicKeyGenerator {
    private static final String  TAG = "PeriodicKeyGenerator";

    @VisibleForTesting
    static final int KEY_SIZE = 16;

    private static byte[] sPeriodicKey = null;

    /**
     * Get current PeriodicKey
     *
     * @param pkgName current app which is using ContacShield
     * @return PeriodicKey
     */
    public static synchronized byte[] getPeriodicKey(String pkgName) {
        long currentIntervalNum = KeyGenUtil.getIntervalNumber();
        long lastInterNumFromDb = ContactDataManage.getInstance().getLastIntervalNum(pkgName);

        // no data from database
        if (lastInterNumFromDb == 0) {
            generatePeriodicKey(pkgName);
        } else {
            // Restore PeriodicKey from database
            if (KeyGenUtil.checkIfIntervalSameDay(currentIntervalNum, lastInterNumFromDb)) {
                sPeriodicKey = ContactDataManage.getInstance().getLatestPeriodicKey(pkgName);
                ContactShieldLog.d(TAG, "Restore PeriodicKey from database: " + Arrays.toString(sPeriodicKey));
            } else {
                generatePeriodicKey(pkgName);
            }
        }

        if (sPeriodicKey == null) {
            ContactShieldLog.d(TAG, "PeriodicKey is null, to generate");
            generatePeriodicKey(pkgName);
        }
        return sPeriodicKey == null ? new byte[KEY_SIZE] : sPeriodicKey.clone();
    }

    /**
     * clear PeriodicKey
     */
    public static void clearPeriodicKey() {
        ContactShieldLog.d(TAG, "reset periodic key");
        sPeriodicKey = null;
    }

    private static void generatePeriodicKey(String pkgName) {
        sPeriodicKey = cRNG();
        ContactDataManage.getInstance().addPdkData(sPeriodicKey, pkgName);
        // Delete the data which is not in incubation.
        ContactDataManage.getInstance().checkAndDeleteData(pkgName);
        ContactShieldLog.d(TAG, "Generate new periodic key: " + Arrays.toString(sPeriodicKey));
    }

    /**
     * Use the secure random generator to generate a 16-byte array.
     *
     * @return a 16-byte array
     */
    static byte[] cRNG() {
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[KEY_SIZE];
        random.nextBytes(key);
        return key;
    }
}
