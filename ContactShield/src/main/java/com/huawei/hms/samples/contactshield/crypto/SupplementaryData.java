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

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

/**
 * Supplementary Data (SD)
 *
 * @since 2020-05-06
 */
public class SupplementaryData {
    public static final String SD_KEY = "EN-AEMK";

    @VisibleForTesting
    static byte[] getSdKey(String pkgName) throws GeneralSecurityException {
        byte[] result = Hkdf.get16ByteHkdfWithoutSalt(PeriodicKeyGenerator.getPeriodicKey(pkgName),
                SD_KEY.getBytes(StandardCharsets.UTF_8));
        return result;
    }

    /**
     * Generate supplementary data.
     *
     * @param dscIJ the J-th dsc of day I
     * @param metaData the data to be encrypted
     * @param pkgName current app which is using ContactShield
     * @return AES 128 Ctr encrypted value
     * @throws GeneralSecurityException GeneralSecurityException
     */
    public byte[] generateSupplementaryData(byte[] dscIJ, byte[] metaData, String pkgName)
            throws GeneralSecurityException {
        return EncryptDecrypt.aesCtrEncrypt(getSdKey(pkgName), dscIJ, metaData);
    }

}
