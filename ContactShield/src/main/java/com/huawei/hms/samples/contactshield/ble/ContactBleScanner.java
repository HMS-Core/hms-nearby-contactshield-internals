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

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;

import com.huawei.hms.samples.contactshield.util.ContactShieldLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Contact ble scanner
 *
 * @since 2020-05-13
 */
public class ContactBleScanner {
    private static final String TAG = "ContactBleScan";

    private static volatile ContactBleScanner sInstance;

    private ContactScanCallbackWrapper mScanCallbackWrapper;

    private AtomicBoolean mIsScanRunning;

    private ContactBleScanCallback mContactBleScanCallback;

    /**
     * Callback for ble scanner
     */
    public interface ContactBleScanCallback {
        /**
         * Notify scanner result
         *
         * @param result flag indicating whether scanner start successfully or not
         */
        void onScanResult(boolean result);

        /**
         * Notify ContactBeacon found event
         *
         * @param contactBeacon found ContactBeacon
         * @param rssi the rssi value of found ContactBeacon
         */
        void onFound(ContactBeacon contactBeacon, int rssi);
    }

    private ContactBleScanner() {
        mScanCallbackWrapper = new ContactScanCallbackWrapper();
        mIsScanRunning = new AtomicBoolean(false);
    }

    /**
     * Get static instance
     *
     * @return static instance
     */
    public static ContactBleScanner getInstance() {
        if (sInstance == null) {
            synchronized (ContactBleAdvertiser.class) {
                if (sInstance == null) {
                    sInstance = new ContactBleScanner();
                }
            }
        }
        return sInstance;
    }

    private int obtainBleScanner() {
        // TODO: Obtain BleScanner
        return ContactBle.ERR_OK;
    }

    private ScanSettings buildScanSettings() {
        ScanSettings.Builder settingBuilder = new ScanSettings.Builder();
        settingBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        settingBuilder.setReportDelay(0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            settingBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
            settingBuilder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
            settingBuilder.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT);
        }

        return settingBuilder.build();
    }

    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter filter = new ScanFilter.Builder().setServiceUuid(ContactBeacon.SERVICE_UID)
                .build();
        filters.add(filter);
        return filters;
    }

    /**
     * Start nearby sharing data scan process
     *
     * @return result code ERR_* in {@link ContactBle}
     */
    public int startBleScan(ContactBleScanCallback contactBleScanCallback) {
        ContactShieldLog.d(TAG, "startBleScan");
        if (mIsScanRunning.get()) {
            ContactShieldLog.d(TAG, "ble scan already started");
            return ContactBle.ERR_OK;
        }

        int ret = obtainBleScanner();
        if (ret != ContactBle.ERR_OK) {
            ContactShieldLog.e(TAG, "fail to get BleScanner" + ret);
            return ret;
        }

        ScanSettings scanSettings = buildScanSettings();
        List<ScanFilter> filters = buildScanFilters();
        ContactShieldLog.d(TAG, "Scan setting: " + scanSettings.toString());
        for (ScanFilter filter : filters) {
            ContactShieldLog.d(TAG, "Scan filter: " + filter.toString());
        }
        try {
            // TODO: to start BLE scanning
            mIsScanRunning.set(true);
            mContactBleScanCallback = contactBleScanCallback;
            ContactShieldLog.d(TAG, "start ble scan, success");
            return ContactBle.ERR_OK;
        } catch (IllegalStateException e) {
            mIsScanRunning.set(false);
            ContactShieldLog.e(TAG, e.getMessage());
        }
        return ContactBle.ERR_BLE_SCAN_FAILED;
    }

    /**
     * Stop BLE scan process
     */
    public void stopBleScan() {
        ContactShieldLog.d(TAG, "stopBleScan");
        if (mIsScanRunning.compareAndSet(true, false)) {
            // TODO: to stop BLE scanning
            ContactShieldLog.d(TAG, "stop ble scan");
        }
    }

    private void doFilter(ScanResult result) {
        if (result == null) {
            return;
        }
        ScanRecord record = result.getScanRecord();
        if (record == null) {
            return;
        }
        byte[] serviceData = record.getServiceData(ContactBeacon.SERVICE_UID);
        ContactBeacon contactBeacon = ContactBeacon.unpack(serviceData);
        if (contactBeacon == null) {
            return;
        }
        if (mContactBleScanCallback != null) {
            mContactBleScanCallback.onFound(contactBeacon, result.getRssi());
        }
    }

    /**
     * ScanCallback wrapper
     *
     * @since 2019-10-31
     */
    private class ContactScanCallbackWrapper extends ScanCallback {
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                doFilter(result);
            }
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (callbackType == ScanSettings.CALLBACK_TYPE_MATCH_LOST) {
                return;
            }
            doFilter(result);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            ContactShieldLog.e(TAG, "BLE scan failed (" + errorCode + ")");
            if (mContactBleScanCallback != null) {
                mContactBleScanCallback.onScanResult(false);
            }
        }
    }
}