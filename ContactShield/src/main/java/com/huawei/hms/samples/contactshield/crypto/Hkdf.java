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

import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Hkdf
 *
 * @since 2020-05-06
 */
public class Hkdf {
    public static final int DEFAULT_SIZE = 16;

    private static final String ALG = "HmacSha256";

    /**
     * get 16-Byte Hkdf Without Salt
     *
     * @param ikm input keying material
     * @param info optional context
     *
     * @return size pseudorandom bytes
     * @throws GeneralSecurityException Exception
     */
    public static byte[] get16ByteHkdfWithoutSalt(final byte[] ikm, final byte[] info) throws GeneralSecurityException {
        byte[] digest = getDigest(ikm, info);
        byte[] hkdfResult = new byte[DEFAULT_SIZE];
        System.arraycopy(digest, 0, hkdfResult, 0, DEFAULT_SIZE);

        return hkdfResult;
    }

    /**
     * get 16-Byte Hkdf Without Salt
     *
     * @param ikm input keying material
     * @param info optional context
     * @param result result will be saved
     *
     * @return size pseudorandom bytes
     * @throws GeneralSecurityException Exception
     */
    public static byte[] get16ByteHkdfWithoutSaltToGivenArray(final byte[] ikm, final byte[] info, byte[] result)
            throws GeneralSecurityException {
        if (result == null || result.length != DEFAULT_SIZE) {
            return get16ByteHkdfWithoutSalt(ikm, info);
        }
        byte[] digest = getDigest(ikm, info);
        System.arraycopy(digest, 0, result, 0, DEFAULT_SIZE);

        return result;
    }

    private static byte[] getDigest(final byte[] ikm, final byte[] info) throws GeneralSecurityException  {
        Mac mac = Mac.getInstance(ALG);
        mac.init(new SecretKeySpec(new byte[mac.getMacLength()], ALG));
        byte[] prk = mac.doFinal(ikm);
        mac.init(new SecretKeySpec(prk, ALG));
        byte[] digest = new byte[0];

        mac.update(digest);
        mac.update(info);
        mac.update((byte) 1);
        digest = mac.doFinal();
        return digest;
    }

}
