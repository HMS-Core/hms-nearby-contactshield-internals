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

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;

import com.huawei.hms.samples.contactshield.util.ContactShieldLog;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Contact Advertiser
 *
 * @since 2019-04-28
 */
public class ContactBleAdvertiser {
    private static final String TAG = "ContactBleAdv";

    private static volatile ContactBleAdvertiser sInstance;

    private AdvertiseCallback mAdvertiseCallback;

    private AtomicBoolean mIsAdvRunning;

    private ContactBleAdvCallback mContactBleAdvCallback;

    /**
     * Callback for ble advertiser
     */
    public interface ContactBleAdvCallback {
        /**
         * Notify advertiser result
         *
         * @param result flag indicating advertiser has started successfully or not.
         */
        void onAdvResult(boolean result);
    }

    /**
     * Constructor
     */
    private ContactBleAdvertiser() {
        mIsAdvRunning = new AtomicBoolean(false);
        mAdvertiseCallback = new ContactAdvertiseCallbackWrapper();
    }

    /**
     * Get static instance
     *
     * @return static instance
     */
    public static ContactBleAdvertiser getInstance() {
        if (sInstance == null) {
            synchronized (ContactBleAdvertiser.class) {
                if (sInstance == null) {
                    sInstance = new ContactBleAdvertiser();
                }
            }
        }
        return sInstance;
    }

    private int obtainBleAdvertiser() {
        // TODO: obtain BleAdvertiser
        return ContactBle.ERR_OK;
    }

    private AdvertiseSettings buildAdvSettings() {
        AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .setConnectable(false)
                .build();
        return advertiseSettings;
    }

    /**
     * Start BLE advertising process
     *
     * @param contactBeacon Contact beacon
     * @param contactBleAdvCallback Advertiser callback
     * @return result code ERR_* in {@link ContactBle}
     */
    public int startBleAdv(ContactBeacon contactBeacon, ContactBleAdvCallback contactBleAdvCallback) {
        ContactShieldLog.d(TAG, "start contact advertise");
        if (mIsAdvRunning.get()) {
            ContactShieldLog.d(TAG, "advertise already started");
            return ContactBle.ERR_OK;
        }

        int ret = obtainBleAdvertiser();
        if (ret != ContactBle.ERR_OK) {
            ContactShieldLog.e(TAG, "obtainBleAdvertiser ret = " + ret);
            return ret;
        }

        AdvertiseSettings advertiseSettings = buildAdvSettings();
        AdvertiseData advData = new AdvertiseData.Builder()
                .addServiceUuid(ContactBeacon.SERVICE_UID)
                .addServiceData(ContactBeacon.SERVICE_UID, contactBeacon.pack())
                .build();
        ContactShieldLog.i(TAG, "data: " + advData.toString() + ", setting: " + advertiseSettings.toString());
        try {
            // TODO: to start BLE advertising.
            mIsAdvRunning.set(true);
            mContactBleAdvCallback = contactBleAdvCallback;
            return ContactBle.ERR_OK;
        } catch (IllegalArgumentException e) {
            mIsAdvRunning.set(false);
            ContactShieldLog.e(TAG, "fail to startAdvertising " + e.getMessage());
        }

        return ContactBle.ERR_BLE_ADV_FAILED;
    }

    /**
     * Stop Ble Advertiser
     */
    public void stopBleAdv() {
        if (mIsAdvRunning.compareAndSet(true, false)) {
            ContactShieldLog.d(TAG, "stop contact advertise");
            // TODO: to stop BLE advertising
        }
    }

    /**
     * AdvertiseCallback wrapper
     *
     * @since 2019-10-31
     */
    private class ContactAdvertiseCallbackWrapper extends AdvertiseCallback {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            String successResult = "Advertise Success!! ";
            if (settingsInEffect != null) {
                successResult += settingsInEffect.toString();
            } else {
                successResult += "settingInEffect is null";
            }

            ContactShieldLog.d(TAG, successResult);
            super.onStartSuccess(settingsInEffect);
            if (mContactBleAdvCallback != null) {
                mContactBleAdvCallback.onAdvResult(true);
            }
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            ContactShieldLog.e(TAG, "BLE advertise failed (" + errorCode + ")");
            if (mContactBleAdvCallback != null) {
                mContactBleAdvCallback.onAdvResult(false);
            }
        }
    }
}
