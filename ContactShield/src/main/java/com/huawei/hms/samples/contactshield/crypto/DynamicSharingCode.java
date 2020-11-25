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
import com.huawei.hms.samples.contactshield.util.KeyGenUtil;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * Dynamic Sharing Code (DSC)
 *
 * @since 2020-05-06
 */
public class DynamicSharingCode {
    private static final String TAG = "DynamicSharingCode";

    public static final String DSC_KEY = "EN-RPIK";

    public static final String DSC_TEXT = "EN-RPI";

    @VisibleForTesting
    static final int PAD_ENIN_OFFSET = 12;

    @VisibleForTesting
    static final int PAD_UTF_LENGTH = 6;

    @VisibleForTesting
    static final int PAD_ENIN_LENGTH = 4;

    @VisibleForTesting
    static final int PAD_LENGTH = 16;

    private byte[] mCurrentPdk;

    private byte[] mCurrentDsc;

    /**
     * To generate current DynamicSharingCode
     *
     * @param pkgName current app which is using ContactShield
     * @return DynamicSharingCode
     * @throws GeneralSecurityException the GeneralSecurityException
     */
    public byte[] generateDynamicSharingCode(String pkgName) throws GeneralSecurityException {
        byte[] encodeIntervalNumberByteArray =
                KeyGenUtil.getEncodeIntervalNumberByteArray(KeyGenUtil.getIntervalNumber());
        mCurrentDsc = EncryptDecrypt.aesEcbEncrypt(getDscKey(pkgName),
                generatePaddedData(encodeIntervalNumberByteArray));
        return mCurrentDsc.clone();
    }

    @VisibleForTesting
    byte[] getDscKey(String pkgName) throws GeneralSecurityException {
        mCurrentPdk = PeriodicKeyGenerator.getPeriodicKey(pkgName);
        byte[] result = Hkdf.get16ByteHkdfWithoutSalt(mCurrentPdk,
                DSC_KEY.getBytes(StandardCharsets.UTF_8));

        return result;
    }

    /**
     * To generate PaddedData with encode IntervalNumber
     *
     * @param encodeIntervalNumberByteArray encode IntervalNumber
     * @return byte array of PaddedData
     */
    public static byte[] generatePaddedData(byte[] encodeIntervalNumberByteArray) {
        byte[] paddedData = new byte[PAD_LENGTH];
        Arrays.fill(paddedData, (byte) 0);
        byte[] dscTextBytes = DSC_TEXT.getBytes(StandardCharsets.UTF_8);
        if (dscTextBytes.length != PAD_UTF_LENGTH) {
            ContactShieldLog.e(TAG, "length of dscText Bytes is " + dscTextBytes.length
                    + ", not expected " + PAD_UTF_LENGTH);
            return paddedData;
        }
        System.arraycopy(dscTextBytes, 0, paddedData, 0, dscTextBytes.length);

        if (encodeIntervalNumberByteArray == null || encodeIntervalNumberByteArray.length != PAD_ENIN_LENGTH) {
            ContactShieldLog.e(TAG, "IntervalNumberByteArray is null or length of encode IntervalNumberByteArray is"
                    + " not expected " + PAD_ENIN_LENGTH);
            return paddedData;
        }
        System.arraycopy(encodeIntervalNumberByteArray, 0, paddedData, PAD_ENIN_OFFSET, PAD_ENIN_LENGTH);
        return paddedData;
    }

    /**
     * get Current Dsc
     *
     * @return Dsc value
     */
    public byte[] getCurrentDsc() {
        if (mCurrentDsc == null) {
            return new byte[0];
        }
        return mCurrentDsc.clone();
    }

    /**
     * get Current PeriodicKey
     *
     * @return Periodic Key value
     */
    public byte[] getCurrentPdk() {
        if (mCurrentPdk == null) {
            return new byte[0];
        }
        return mCurrentPdk.clone();
    }
}
