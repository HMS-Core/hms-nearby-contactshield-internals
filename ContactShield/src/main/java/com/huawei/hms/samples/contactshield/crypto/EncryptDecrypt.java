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

import com.huawei.hms.samples.contactshield.util.ContactShieldLog;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES Encrypt Decrypt
 *
 * @since 2020-05-06
 */
public class EncryptDecrypt {
    private static final String TAG = "EncryptDecrypt";

    /**
     * AES encrypt
     *
     * @param encryptKey key
     * @param text  text to be encrypted
     * @return encrypted text
     */
    public static byte[] aesEcbEncrypt(byte[] encryptKey, byte[] text) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            SecretKeySpec secretKey = new SecretKeySpec(encryptKey, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(text);
        } catch (GeneralSecurityException e) {
            ContactShieldLog.e(TAG, "Encrypt Exception : " + e.getMessage());
        }
        return new byte[0];
    }

    /**
     * AES decrypt
     *
     * @param encryptKey key
     * @param text encrypted text
     * @return decrypted text
     */
    public static byte[] aesEcbDecrypt(byte[] encryptKey, byte[] text) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            SecretKeySpec secretKey = new SecretKeySpec(encryptKey, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(text);
        } catch (GeneralSecurityException e) {
            ContactShieldLog.e(TAG, "Decrypt Exception : " + e.getMessage());
        }
        return new byte[0];
    }

    /**
     * AES 128 Ctr Encrypt
     *
     * @param encryptKey Key
     * @param IvPara Iv Parameter Spec
     * @param text text to be encrypted
     * @return encrypted text
     */
    public static byte[] aesCtrEncrypt(byte[] encryptKey, byte[] IvPara, byte[] text) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            SecretKeySpec secretKey = new SecretKeySpec(encryptKey, "AES");
            IvParameterSpec iv = new IvParameterSpec(IvPara);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            return cipher.doFinal(text);
        } catch (GeneralSecurityException e) {
            ContactShieldLog.e(TAG, "aes128Ctr Encrypt Exception : " + e.getMessage());
        }
        return new byte[0];
    }

    /**
     * AES 128 Ctr decrypt
     *
     * @param encrypKey Key
     * @param IvPara Iv Parameter Spec
     * @param text encrypted text
     * @return decrypted text
     */
    public static byte[] aesCtrDecrypt(byte[] encrypKey, byte[] IvPara, byte[] text) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            SecretKeySpec secretKey = new SecretKeySpec(encrypKey, "AES");
            IvParameterSpec iv = new IvParameterSpec(IvPara);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            return cipher.doFinal(text);
        } catch (GeneralSecurityException e) {
            ContactShieldLog.e(TAG, "aes128Ctr Decrypt Exception : " + e.getMessage());
        }
        return new byte[0];
    }

}
