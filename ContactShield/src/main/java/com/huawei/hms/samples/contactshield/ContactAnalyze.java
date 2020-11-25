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

package com.huawei.hms.samples.contactshield;

import com.huawei.hms.samples.contactshield.util.ContactShieldLog;
import com.huawei.hms.samples.contactshield.contact.ContactDetail;
import com.huawei.hms.samples.contactshield.contact.ContactSketch;
import com.huawei.hms.samples.contactshield.contact.ContactWindow;
import com.huawei.hms.samples.contactshield.contact.DiagnosisConfiguration;
import com.huawei.hms.samples.contactshield.contact.ScanInfo;
import com.huawei.hms.samples.contactshield.database.table.ScanData;
import com.huawei.hms.samples.contactshield.util.GlobalSettings;
import com.huawei.hms.samples.contactshield.util.KeyFileParser;
import com.huawei.hms.samples.contactshield.util.ParamsRangeChecker;
import com.huawei.hms.samples.contactshield.contact.PeriodicKey;
import com.huawei.hms.samples.contactshield.crypto.SupplementaryData;
import com.huawei.hms.samples.contactshield.crypto.EncryptDecrypt;
import com.huawei.hms.samples.contactshield.crypto.Hkdf;
import com.huawei.hms.samples.contactshield.crypto.DynamicSharingCode;
import com.huawei.hms.samples.contactshield.util.KeyGenUtil;
import com.huawei.hms.samples.contactshield.util.RiskScoreCalculator;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Contact diagnosis Analyze
 *
 * @since 2020-05-08
 */
public class ContactAnalyze {
    private static final String TAG = "ContactAnalyze";

    private static volatile ContactAnalyze instance;

    private ContactDataManage mContactDataManage = ContactDataManage.getInstance();

    /**
     * Constructor
     */
    private ContactAnalyze() {
    }

    /**
     * getInstance
     */
    public static ContactAnalyze getInstance() {
        if (instance == null) {
            synchronized (ContactAnalyze.class) {
                if (instance == null) {
                    instance = new ContactAnalyze();
                }
            }
        }
        return instance;
    }

    /**
     *  Analyze each key file in fileList with given configuration.
     *
     * @param fileList key file list
     * @param configuration Configuration for calculating diagnosis results
     * @param token Used to mark the same diagnosis.
     * @param pkgName Used to mark the same application.
     * @return if contacted with input key file list.
     */
    public boolean analyzeKeyFileList(List<File> fileList, DiagnosisConfiguration configuration,
        String token, String pkgName) {
        // The implementation of the window mode.
        if (GlobalSettings.TOKEN_WINDOW_MODE.equals(token)) {
            return analyzeKeyFileListForWindow(fileList, pkgName);
        }

        // The implementation of the contactDetail
        return analyzeKeyFileListForDetail(fileList, configuration, token, pkgName);
    }

    private boolean analyzeKeyFileListForWindow(List<File> fileList, String pkgName) {
        boolean listUpdate = false;
        for (File file : fileList) {
            List<PeriodicKey> keyList = KeyFileParser.parseFiles(file);
            if (analyzePeriodicKeyListForContactWindow(keyList, pkgName)) {
                listUpdate = true;
            }
        }

        return listUpdate;
    }

    private ContactSketch mergeContactSketch(ContactSketch originSketch, ContactSketch newSketch) {
        if (originSketch.getNumberOfHits() == 0) {
            return newSketch;
        }

        int daysSinceLastHit = Math.min(originSketch.getDaysSinceLastHit(), newSketch.getDaysSinceLastHit());
        int maxRiskValue = Math.max(originSketch.getMaxRiskValue(), newSketch.getMaxRiskValue());
        int numberOfHits = originSketch.getNumberOfHits() + newSketch.getNumberOfHits();
        long summationRiskValue = originSketch.getSummationRiskValue() + newSketch.getSummationRiskValue();
        if (summationRiskValue > Integer.MAX_VALUE) {
            summationRiskValue = Integer.MAX_VALUE;
        }
        /* attenuationDuration array has 3 elements */
        int[] attenuationDuration = new int[3];
        for (int i = 0; i < 3; i++) {
            attenuationDuration[i] = originSketch.getAttenuationDurations()[i] +
                newSketch.getAttenuationDurations()[i];
            attenuationDuration[i] = Math.min(attenuationDuration[i], GlobalSettings.DURATION_MAX_MINUTES);
        }

        ContactSketch mergedSketch = new ContactSketch.Builder()
                .setDaysSinceLastHit(daysSinceLastHit)
                .setMaxRiskValue(maxRiskValue)
                .setNumberOfHits(numberOfHits)
                .setSummationRiskValue((int) summationRiskValue)
                .setAttenuationDurations(attenuationDuration)
                .build();
        ContactShieldLog.d(TAG, "Merge contact sketch: " + mergedSketch);
        return mergedSketch;
    }

    private boolean analyzeKeyFileListForDetail(List<File> fileList, DiagnosisConfiguration configuration,
        String token, String pkgName) {
        /* Clear stale data before every new diagnosis. */
        mContactDataManage.clearStaleSketchAndDetailData(pkgName);
        /* Prepare empty contact sketch to generate id. */
        ContactSketch originSketch = mContactDataManage.getContactSketch(pkgName, token);
        if (originSketch == null) {
            originSketch = new ContactSketch.Builder().build();
            mContactDataManage.addContactSketchData(pkgName, token, originSketch);
        }
        boolean listUpdate = false;
        for (File file : fileList) {
            List<PeriodicKey> keyList = KeyFileParser.parseFiles(file);
            List<ContactDetail> contactDetailList = analyzePeriodicKeyListForContactDetail(keyList, configuration,
                token, pkgName);
            ContactShieldLog.d(TAG, "contactDetailList size " + contactDetailList.size());
            if (contactDetailList.size() > 0) {
                ContactSketch newSketch = generateContactSketch(contactDetailList);
                ContactSketch mergeSketch = mergeContactSketch(originSketch, newSketch);
                /* Update original sketch for next round */
                originSketch = mergeSketch;
                listUpdate = true;
            }
        }
        if (listUpdate) {
            /* Must use updateContactSketchData() instead of addContactSketchData() to avoid removing detail data. */
            mContactDataManage.updateContactSketchData(pkgName, token, originSketch);
        }
        return listUpdate;
    }

    /**
     * To analyze each key in keyList
     *
     * @param keyList Periodic List
     * @return List of ContactDetail
     */
    private List<ContactDetail> analyzePeriodicKeyListForContactDetail(List<PeriodicKey> keyList,
        DiagnosisConfiguration configuration, String token, String pkgName) {
        List<ContactDetail> contactDetailList = new LinkedList<>();
        if (keyList == null || keyList.size() == 0) {
            ContactShieldLog.e(TAG, "keyList is empty");
            return contactDetailList;
        }
        long validInterval = (KeyGenUtil.getDayNumber() -
                GlobalSettings.getGlobalContactShieldSetting().getIncubationPeriod()) * KeyGenUtil.INTERVAL_TO_DAY;
        byte[] result = new byte[Hkdf.DEFAULT_SIZE];
        RiskScoreCalculator calculator = new RiskScoreCalculator(configuration);
        for (PeriodicKey key : keyList) {
            List<ScanDataWithTxPower> validScanDataList = getScanDataListOfGivenKeyFromDb(key, validInterval, result);
            if (validScanDataList.isEmpty()) {
                // The local database does not contain data that has been contacted with the current key.
                continue;
            }
            ContactDetail contactDetail = generateContactDetail(key, calculator, validScanDataList, configuration);
            if (contactDetail == null) {
                continue;
            }
            contactDetailList.add(contactDetail);
            mContactDataManage.addContactDetailData(pkgName, token, contactDetail);
        }

        return contactDetailList;
    }

    /**
     * Analyze PeriodicKey list for ContactWindow mode
     *
     * @param keyList Periodic Key List
     * @return List of ContactWindow
     */
    private boolean analyzePeriodicKeyListForContactWindow(List<PeriodicKey> keyList, String pkgName) {
        boolean isUpdate = false;
        if (keyList == null || keyList.size() == 0) {
            ContactShieldLog.e(TAG, "keyList is empty");
            return isUpdate;
        }
        long validInterval = (KeyGenUtil.getDayNumber() -
                GlobalSettings.getGlobalContactShieldSetting().getIncubationPeriod()) * KeyGenUtil.INTERVAL_TO_DAY;
        byte[] result = new byte[Hkdf.DEFAULT_SIZE];
        for (PeriodicKey key : keyList) {
            List<ScanDataWithTxPower> validScanDataList = getScanDataListOfGivenKeyFromDb(key, validInterval, result);
            if (validScanDataList.isEmpty()) {
                // The local database does not contain data that has been contacted with the current key.
                ContactShieldLog.d(TAG, "No valid ScanData from DB");
                continue;
            }
            List<ContactWindow> resultList = generateContactWindowListOfGivenKey(key, validScanDataList);
            if (!resultList.isEmpty()) {
                mContactDataManage.addContactWindows(pkgName, resultList);
                isUpdate = true;
            }
        }

        return isUpdate;
    }

    private List<ScanDataWithTxPower> getScanDataListOfGivenKeyFromDb(PeriodicKey key, long validInterval,
        byte[] result) {
        // Based on the validity period of the key, search the data of the current day in the ScanData table.
        long startInterval = key.getPeriodicKeyValidTime();
        long endInterval = startInterval + key.getPeriodicKeyLifeTime() - 1;
        // Check whether the key is in the incubation period.
        // If the key is out of the incubation period, no need to handle it.
        if (startInterval < validInterval) {
            ContactShieldLog.d(TAG, "This key is not in incubation period" + validInterval);
            return Collections.emptyList();
        }
        List<ScanDataWithTxPower> validScanDataList = new LinkedList<>();
        byte[] sdKey = generateSdKey(key);
        if (sdKey == null || sdKey.length == 0) {
            ContactShieldLog.e(TAG, "sdKey:" + Arrays.toString(sdKey));
            return validScanDataList;
        }
        // Generate a target DSCs list. The maximum number of DSCs is 144 for each key.
        List<byte[]> targetDscList = getTargetDsc(key, result);
        for (byte[] dscTarget : targetDscList) {
            // For each DSC, search the database for the same scanning record.
            List<ScanData> scanDataList = ContactDataManage.getInstance()
                    .searchScanDataWithGivenDsc(dscTarget, startInterval, endInterval);
            if (scanDataList.size() == 0) {
                // If no matching record is found, check the next target DSC.
                continue;
            }
            ContactShieldLog.d(TAG, "scanDataList size:" + scanDataList.size());
            // If the records are valid, decrypt and extract TxPower for future use.
            for (ScanData data: scanDataList) {
                byte[] sdData = EncryptDecrypt.aesCtrDecrypt(sdKey, dscTarget, data.getSd());
                ContactShieldLog.d(TAG, "Dsc matched:" + Arrays.toString(dscTarget)
                        + ", Decrypt sd: " + Arrays.toString(sdData));
                if (sdData == null || sdData.length != 4) {
                    ContactShieldLog.d(TAG, "Invalid sd data, continue");
                    continue;
                }
                // Note: The version number contained in sdData is not checked for version compatibility.
                // Parse the sdData and use the Tx carried in the scanned data.
                validScanDataList.add(new ScanDataWithTxPower(data, sdData[1]));
                ContactShieldLog.d(TAG, "validScanDataList size:" + validScanDataList.size());
            }
        }
        return validScanDataList;
    }

    private void updateAttenuationDurationResult(DiagnosisConfiguration configuration, int signal, int[] attenDuration,
        int duration) {
        int[] thresholds = configuration.getAttenuationDurationThresholds();
        if (signal < thresholds[0]) {
            attenDuration[0] += duration;
            attenDuration[0] = Math.min(attenDuration[0], GlobalSettings.DURATION_MAX_MINUTES);
            return;
        }
        if (signal >= thresholds[1]) {
            attenDuration[2] += duration;
            attenDuration[2] = Math.min(attenDuration[2], GlobalSettings.DURATION_MAX_MINUTES);
            return;
        }
        attenDuration[1] += duration;
        attenDuration[1] = Math.min(attenDuration[1], GlobalSettings.DURATION_MAX_MINUTES);
    }

    private List<byte[]> getTargetDsc(PeriodicKey key, byte[] result) {
        byte[] dscKey = generateDscKey(key, result);
        List<byte[]> targetDscList = new ArrayList<>();
        if (dscKey == null || dscKey.length == 0) {
            ContactShieldLog.e(TAG, "dscKey:" + Arrays.toString(dscKey));
            return targetDscList;
        }

        // Calculate the DSC list based on the input parameter PeriodicKey.
        long startNumber = key.getPeriodicKeyValidTime();
        long keyDuration = key.getPeriodicKeyLifeTime();
        if (startNumber < 0 || keyDuration <= 0) {
            ContactShieldLog.d(TAG, "Invalid KeyValidTime or KeyLifeTime." + key.toString());
            return targetDscList;
        }
        // keyDuration must in range [0,143]
        keyDuration = Math.max(0, Math.min(GlobalSettings.PDK_ROLLING_PERIOD, keyDuration));
        for (long i = 0; i < keyDuration; i++) {
            byte[] eninJ = KeyGenUtil.getEncodeIntervalNumberByteArray(startNumber + i);
            targetDscList.add(EncryptDecrypt.aesEcbEncrypt(dscKey, DynamicSharingCode.generatePaddedData(eninJ)));
        }
        return targetDscList;
    }

    private List<ContactWindow> generateContactWindowListOfGivenKey(PeriodicKey key,
        List<ScanDataWithTxPower> validScanDataList) {
        List<ContactWindow> contactWindowList = new LinkedList<>();
        long dateMillis = KeyGenUtil.intervalNum2DayMillis(key.getPeriodicKeyValidTime());
        List<ScanInfo> scanInfoList = new LinkedList<>();
        // The unit of duration is second, which is used to partition different ContactWindows.
        int duration = 0;
        for (ScanDataWithTxPower scanDataWithTxPower : validScanDataList) {
            ScanData scanData = scanDataWithTxPower.getScanData();
            if (scanData.getAverageRssi() == 0) {
                // Matched the earlier version data in the database, the window mode is not supported.
                continue;
            }
            int minimumAttenuation = scanDataWithTxPower.getDecryptTxPower() - scanData.getRssi()
                    - GlobalSettings.getCalibration().getRssiCorrection();
            int averageAttenuation = scanDataWithTxPower.getDecryptTxPower() - scanData.getAverageRssi()
                    - GlobalSettings.getCalibration().getRssiCorrection();

            duration += scanData.getSecondsSinceLastScan();
            // Each ContactWindow can save ScanInfo for a maximum of 30 minutes.
            if (duration > GlobalSettings.CONTACT_WINDOW_SECOND) {
                contactWindowList.add(new ContactWindow.Builder()
                        .setDateMillis(dateMillis)
                        .setReportType(key.getReportType())
                        .setScanInfos(scanInfoList)
                        .build());
                scanInfoList = new LinkedList<>();
                duration = scanData.getSecondsSinceLastScan();
            }

            scanInfoList.add(new ScanInfo.Builder()
                    .setAverageAttenuation(averageAttenuation)
                    .setMinimumAttenuation(minimumAttenuation)
                    .setSecondsSinceLastScan(scanData.getSecondsSinceLastScan())
                    .build());
        }
        // The remaining ScanInfo information within 30 minutes forms the last ContactWindow.
        if (!scanInfoList.isEmpty()) {
            contactWindowList.add(new ContactWindow.Builder()
                    .setDateMillis(dateMillis)
                    .setReportType(key.getReportType())
                    .setScanInfos(scanInfoList)
                    .build());
        }

        ContactShieldLog.d(TAG, "generate contactWindowList，size：" + contactWindowList.size());
        return contactWindowList;
    }

    private ContactDetail generateContactDetail(PeriodicKey key, RiskScoreCalculator calculator,
        List<ScanDataWithTxPower> validScanDataList, DiagnosisConfiguration configuration) {
        long startNumber = key.getPeriodicKeyValidTime();
        long dayNum = startNumber / GlobalSettings.PDK_ROLLING_PERIOD;
        // Calculate duration in Minutes
        int duration = 0;
        // The value of signal must be in range [0, 255].
        int minSignal = Integer.MAX_VALUE;
        int[] attenuationDurationOfCurrentKey = new int[3];
        for (ScanDataWithTxPower scanDataWithTxPower : validScanDataList) {
            ScanData scanData = scanDataWithTxPower.getScanData();
            int signal = scanDataWithTxPower.getDecryptTxPower() - scanData.getRssi()
                    - GlobalSettings.getCalibration().getRssiCorrection();
            minSignal = Math.min(minSignal, signal);
            if (scanData.getAverageRssi() == 0) {
                // AverageRssi is 0 indicates the data is scanned by an earlier version, so the step is 5 minutes.
                updateAttenuationDurationResult(configuration, signal, attenuationDurationOfCurrentKey,
                        GlobalSettings.DURATION_STEP);
                duration += GlobalSettings.DURATION_STEP;
            } else {
                // AverageRssi is not 0 indicates that the data is scanned by the new version.
                // The step is scanData.getSecondsSinceLastScan().
                updateAttenuationDurationResult(configuration, signal, attenuationDurationOfCurrentKey,
                        scanData.getSecondsSinceLastScan() / KeyGenUtil.SECOND_TO_MINUTE);
                duration += scanData.getSecondsSinceLastScan() / KeyGenUtil.SECOND_TO_MINUTE;
            }
        }
        duration = Math.min(duration, GlobalSettings.DURATION_MAX_MINUTES);
        int signal = Math.min(Math.max(minSignal, 0), ParamsRangeChecker.ATTEN_REAL_VALUE_MAX);
        long durLastDayNum = Math.max(KeyGenUtil.getDayNumber() - KeyGenUtil.interval2DayNumber(startNumber), 0);
        int totalRiskValue = calculator.calcTotalRiskValue(signal, (int) durLastDayNum, duration,
            key.getInitialRiskLevel());
        if (totalRiskValue < configuration.getMinimumRiskValueThreshold()) {
            ContactShieldLog.d(TAG, "total risk value (" + totalRiskValue + ") less than threshold. discard it");
            return null;
        }
        ContactShieldLog.d(TAG, "matched target dsc, call onContact.");

        return new ContactDetail.Builder()
                .setDayNumber(dayNum)
                .setAttenuationRiskValue(signal)
                .setDurationMinutes(duration)
                .setTotalRiskValue(totalRiskValue)
                .setInitialRiskLevel(key.getInitialRiskLevel())
                .setAttenuationDurations(attenuationDurationOfCurrentKey)
                .build();
    }

    private byte[] generateDscKey(PeriodicKey key, byte[] result) {
        byte[] dscKey = null;
        try {
            dscKey = Hkdf.get16ByteHkdfWithoutSaltToGivenArray(key.getContent(),
                    DynamicSharingCode.DSC_KEY.getBytes(StandardCharsets.UTF_8), result);
        }catch (GeneralSecurityException e) {
            ContactShieldLog.e(TAG, "DscKey Expand exception:" + e.getMessage());
        }
        return dscKey;
    }

    private byte[] generateSdKey(PeriodicKey key) {
        byte[] sdKey = null;
        try {
            sdKey = Hkdf.get16ByteHkdfWithoutSalt(key.getContent(),
                    SupplementaryData.SD_KEY.getBytes(StandardCharsets.UTF_8));
        }catch (GeneralSecurityException e) {
            ContactShieldLog.e(TAG, "SdKey Expand exception:" + e.getMessage());
        }
        return sdKey;
    }

    /**
     * Get contact detail list
     *
     * @param token token
     * @param pkgName package name
     * @return contact detail list
     */
    public List<ContactDetail> getDetailList(String token, String pkgName) {
        return mContactDataManage.getContactDetailList(pkgName, token);
    }

    /**
     * Get contact sketch
     *
     * @param token token
     * @param pkgName package name
     * @return contact sketch
     */
    public ContactSketch getContactSketch(String token, String pkgName) {
        ContactSketch contactSketch = mContactDataManage.getContactSketch(pkgName, token);
        if (contactSketch == null) {
            contactSketch = new ContactSketch.Builder().build();
        }
        return contactSketch;
    }

    /**
     * get contact windows
     *
     * @param pkgName package name
     * @return List of ContactWindows
     */
    public List<ContactWindow> getContactWindows(String pkgName) {
        return mContactDataManage.getContactWindows(pkgName);
    }

    private ContactSketch generateContactSketch(List<ContactDetail> contactDetailList) {
        if (contactDetailList == null || contactDetailList.size() == 0) {
            ContactShieldLog.i(TAG, "GetContactSketch: contact detail list is null.");
            return new ContactSketch.Builder().build();
        }
        long summationRiskLevel = 0;
        int[] attenDuration = new int[3];
        long latestDate = 0;
        int maxRiskScore = 0;
        ContactShieldLog.d(TAG, "Generating contact sketch");
        for (ContactDetail contactDetail: contactDetailList) {
            summationRiskLevel += contactDetail.getTotalRiskValue();
            for (int i = 0; i < 3; i++) {
                attenDuration[i] += contactDetail.getAttenuationDurations()[i];
                attenDuration[i] = Math.min(attenDuration[i], GlobalSettings.DURATION_MAX_MINUTES);
            }
            latestDate = Math.max(contactDetail.getDayNumber(), latestDate);
            maxRiskScore = Math.max(contactDetail.getTotalRiskValue(), maxRiskScore);
        }
        int size = contactDetailList.size();
        long durLastDayNums = KeyGenUtil.getDayNumber() - latestDate;
        ContactShieldLog.i(TAG, "GetContactSketch: daysSinceLastHit，maxRiskLevel, numberOfHits:"
                + durLastDayNums + " " + maxRiskScore + " " + size);
        return new ContactSketch.Builder()
                .setDaysSinceLastHit(Math.max(0, (int) durLastDayNums))
                .setMaxRiskValue(maxRiskScore)
                .setNumberOfHits(size)
                .setSummationRiskValue(summationRiskLevel > Integer.MAX_VALUE ?
                        Integer.MAX_VALUE : (int) summationRiskLevel)
                .setAttenuationDurations(attenDuration)
                .build();
    }

    /**
     * This structure encapsulates the valid scanning data selected from the database and the decrypted TxPower
     * for subsequent calculation of risk values and windows.
     */
    private static class ScanDataWithTxPower {
        // mDecryptTxPower: stores the TxPower decrypted from the SD of ScanData.
        private int mDecryptTxPower;
        private ScanData mScanData;

        public ScanDataWithTxPower(ScanData scanData, int txPower) {
            mScanData = scanData;
            mDecryptTxPower = txPower;
        }

        /**
         * get Decrypt TxPower
         *
         * @return value of mDecryptTxPower
         */
        public int getDecryptTxPower() {
            return mDecryptTxPower;
        }

        /**
         * get ScanData
         *
         * @return value of mScanData
         */
        public ScanData getScanData() {
            return mScanData;
        }
    }
}
