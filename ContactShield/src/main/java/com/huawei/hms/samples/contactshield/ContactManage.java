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

import android.app.PendingIntent;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;

import com.huawei.hms.samples.contactshield.ble.ContactAdvBeaconGenerator;
import com.huawei.hms.samples.contactshield.ble.ContactBeacon;
import com.huawei.hms.samples.contactshield.ble.ContactBle;
import com.huawei.hms.samples.contactshield.ble.ContactBleAdvertiser;
import com.huawei.hms.samples.contactshield.ble.ContactBleScanner;
import com.huawei.hms.samples.contactshield.util.ContactShieldLog;
import com.huawei.hms.samples.contactshield.util.threadpool.ThreadExec;
import com.huawei.hms.samples.contactshield.util.GlobalSettings;
import com.huawei.hms.samples.contactshield.util.KeyGenUtil;
import com.huawei.hms.samples.contactshield.util.ApkUtils;

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contact Manage
 *
 * @since 2019-04-28
 */
public class ContactManage {
    private static final String TAG = "ContactManage";

    private static final int ALARM_INTENT_REQUEST_CODE = 1;

    private static volatile ContactManage sInstance;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private AtomicInteger mAdvState = new AtomicInteger(ContactBle.ERR_BLE_ADV_NOT_RUNNING);

    private AtomicInteger mScanState = new AtomicInteger(ContactBle.ERR_BLE_SCAN_NOT_RUNNING);

    private AtomicBoolean mContactRunning = new AtomicBoolean(false);

    private long mLastScanTimestamp;

    private long mCurrentScanTimestamp;

    private long mNextAdvTimestamp;

    private PowerManager.WakeLock mWakeLock;

    private final SecureRandom mSecureRandom = new SecureRandom();

    private String mPkgName;

    private ContactAction mContactAction;

    private ContactBleAdvertiser.ContactBleAdvCallback mContactBleAdvCallback;

    private ContactBleScanner.ContactBleScanCallback mContactBleScanCallback;

    private ContactAdvBeaconGenerator mBeaconGenerator;

    /**
     *  Keep pendingIntent in current process, as it can't be saved in persistent storage.
     */
    private PendingIntent mPendingIntent;

    /**
     * Flag indicating whether need to notify CallResponse: It will be set to true when calling startContact(). Once
     * notify CallResponse due to success or ble failure, it wll be reset and won't notify any more in subsequent BLE
     * operation.
     */
    private AtomicBoolean mIsNeedCallResponse;

    private ContactManage() {
        mIsNeedCallResponse = new AtomicBoolean(true);
        mBeaconGenerator = new ContactAdvBeaconGenerator();
        mContactBleAdvCallback = new ContactBleAdvertiser.ContactBleAdvCallback() {
            @Override
            public void onAdvResult(boolean result) {
                onScanAndAdvResult(result);
            }
        };

        mContactBleScanCallback = new ContactBleScanner.ContactBleScanCallback() {
            @Override
            public void onScanResult(boolean result) {
                onScanAndAdvResult(result);
            }

            @Override
            public void onFound(ContactBeacon contactBeacon, int rssi) {
                ThreadExec.execNormalTask(TAG, () -> {
                    ContactDataManage.getInstance().addScanDetail(contactBeacon.getDsc(),
                            contactBeacon.getSd(), rssi);
                });
            }
        };
    }

    /**
     * Get ContactManage singleInstance
     */
    public static ContactManage getInstance() {
        if (sInstance == null) {
            synchronized (ContactManage.class) {
                if (sInstance == null) {
                    sInstance = new ContactManage();
                }
            }
        }
        return sInstance;
    }

    private void setAlarm(int delaySeconds) {
        // TODO: Set alarm to trigger onAlarmTriggered()
    }

    private void clearAlarm() {
        // TODO: Clear alarm when stop/restart Contact Shield
    }

    private Runnable mStopScan = new Runnable() {
        @Override
        public void run() {
            ThreadExec.execSeqTask(TAG, () -> {
                if (!mContactRunning.get()) {
                    return;
                }
                ContactBleScanner.getInstance().stopBleScan();
                ContactShieldLog.d(TAG, "Stop scan success");
                mScanState.compareAndSet(ContactBle.ERR_OK, ContactBle.ERR_BLE_SCAN_NOT_RUNNING);
                ContactDataManage.getInstance().flushScanDataToDb(getSecondsSinceLastScan());

                int delaySeconds;
                if (ApkUtils.isScreenOn()) {
                    delaySeconds = getRandom(GlobalSettings.MIN_SCAN_PERIOD_SECONDS,
                            GlobalSettings.MAX_SCAN_PERIOD_SECONDS);
                } else {
                    delaySeconds = GlobalSettings.POWER_SAVE_SCAN_PERIOD_SECONDS;
                }
                ContactShieldLog.d(TAG, "Schedule next scan in " + delaySeconds + " seconds");
                setAlarm(delaySeconds);
                if (mWakeLock != null && mWakeLock.isHeld()) {
                    mWakeLock.release();
                    ContactShieldLog.d(TAG, "Release wakelock.");
                }
            });
        }
    };

    private int getRandom(int min, int max) {
        int range = max - min + 1;
        return mSecureRandom.nextInt(range) + min;
    }

    /**
     * On ContactShield alarm triggered.
     */
    public void onAlarmTriggered() {
        ThreadExec.execSeqTask(TAG, () -> {
            /* Serialize operation to avoid thread-safe issue. */
            if (!mContactRunning.get()) {
                return;
            }
            if (mPkgName != null && mContactAction != null) {
                ContactShieldLog.d(TAG, "OnContactShield Alarm triggered");
                startScanAdv(mPkgName, mContactAction);
            }
        });
    }

    private boolean startScanAdv(String pkgName, ContactAction action) {
        boolean advRet = false;
        boolean scanRet = startScan(action);
        if (!scanRet) {
            return false;
        }

        if (mNextAdvTimestamp == 0 || mNextAdvTimestamp < KeyGenUtil.getSecondsSinceEpoch()) {
            advRet = startAdv(pkgName, action);
            if (advRet) {
                int nextAdvDelay = getRandom(GlobalSettings.MIN_ADV_PERIOD_SECONDS,
                        GlobalSettings.MAX_ADV_PERIOD_SECONDS);
                ContactShieldLog.d(TAG, "Next adv in " + nextAdvDelay + " seconds");
                mNextAdvTimestamp = KeyGenUtil.getSecondsSinceEpoch() + nextAdvDelay;
            } else {
                stopScan();
            }
        } else {
            // Don't need to adv, so just set it to true.
            advRet = true;
        }

        return advRet;
    }

    private void doStart() {
        if (!mContactRunning.get()) {
            clearAlarm();

            // TODO: Prepare wakelock
            mWakeLock = null;
            mLastScanTimestamp = 0;
            mCurrentScanTimestamp = 0;
            mNextAdvTimestamp = 0;
            boolean ret = startScanAdv(mPkgName, mContactAction);
            if (!ret) {
                // startAdv()/startScan() will report failure, so don't have to call action.call() here.
                mContactRunning.set(false);
            }
            return;
        }

        // Already running
        if (mIsNeedCallResponse.compareAndSet(true, false)) {
            mContactAction.call(StatusCode.STATUS_SUCCESS, "ContactShield service is already running");
        }
    }

    private void doStop() {
        stopAdv();
        stopScan();
        clearAlarm();
        mContactRunning.set(false);
    }

    /**
     * Start Contact
     */
    public void startContact(String pkgName, ContactAction action) {
        ThreadExec.execSeqTask(TAG, () -> {
            ContactShieldLog.i(TAG, "startContact");
            int ret = ContactBle.checkAndEnableBtAdapter();
            if (ret == ContactBle.ERR_NO_BT_ADAPTER || ret == ContactBle.ERR_TO_ENABLE_BT ||
                    ret == ContactBle.ERR_BT_NOT_SUPPORT_BLE) {
                action.call(StatusCode.STATUS_BLUETOOTH_OPERATION_ERROR, ContactBle.getBleStatusString(ret));
                return;
            }

            mIsNeedCallResponse.set(true);
            mPkgName = pkgName;
            mContactAction = action;

            if (ret == ContactBle.ERR_BT_NOT_ENABLED) {
                ContactShieldLog.i(TAG, "bluetooth adapter is not enabled, wait a minute for enable automatically");
                return;
            }

            doStart();
        });
    }

    /**
     * Stop Contact
     */
    public void stopContact() {
        ThreadExec.execSeqTask(TAG, () -> {
            doStop();
        });
    }

    /**
     * Restart BLE advertising
     */
    public void restartAdvertising() {
        ThreadExec.execSeqTask(TAG, () -> {
            if (mPkgName == null || mContactAction == null) {
                return;
            }
            if (!mContactRunning.get()) {
                return;
            }
            stopAdv();
            boolean ret = startAdv(mPkgName, mContactAction);
            if (ret) {
                // Prepare next adv
                int nextAdvDelay = getRandom(GlobalSettings.MIN_ADV_PERIOD_SECONDS,
                        GlobalSettings.MAX_ADV_PERIOD_SECONDS);
                ContactShieldLog.d(TAG, "Restart ADV. Next adv in " + nextAdvDelay + " seconds");
                mNextAdvTimestamp = KeyGenUtil.getSecondsSinceEpoch() + nextAdvDelay;
            }
        });
    }

    /**
     * Handling for BLE Scanner/Advertiser result.
     *
     * @param isSuccess isSuccess
     */
    private void onScanAndAdvResult(boolean isSuccess) {
        ThreadExec.execSeqTask(TAG, () -> {
            if (isSuccess) {
                mContactRunning.compareAndSet(false, true);
                if (mContactAction != null && mIsNeedCallResponse.compareAndSet(true, false)) {
                    ContactShieldLog.d(TAG, "call onScanAndAdvResult:ContactShield service start success");
                    mContactAction.call(StatusCode.STATUS_SUCCESS, "ContactShield service start success");
                }
                return;
            }

            // stop contact if BLE scanning or advertising fail.
            doStop();
            if (mContactAction != null && mIsNeedCallResponse.compareAndSet(true, false)) {
                ContactShieldLog.d(TAG, "call onScanAndAdvResult:Start Contact Shield error");
                mContactAction.call(StatusCode.STATUS_BLUETOOTH_OPERATION_ERROR, "Start Contact Shield error");
            }
        });
    }

    public PendingIntent getPendingIntent() {
        return mPendingIntent;
    }

    public void setPendingIntent(PendingIntent mPendingIntent) {
        this.mPendingIntent = mPendingIntent;
    }

    private boolean startAdv(String pkgName, ContactAction action) {
        // Stop advertising old data the start advertising new data
        ContactBleAdvertiser.getInstance().stopBleAdv();
        ContactBeacon contactBeacon = mBeaconGenerator.generate(pkgName);
        if (contactBeacon == null) {
            if (mIsNeedCallResponse.compareAndSet(true, false)) {
                action.call(StatusCode.STATUS_BLUETOOTH_OPERATION_ERROR, "Failed to generate advertiser beacon");
            }
            return false;
        }
        int result = ContactBleAdvertiser.getInstance().startBleAdv(contactBeacon, mContactBleAdvCallback);
        if (result == ContactBle.ERR_OK) {
            mAdvState.compareAndSet(ContactBle.ERR_BLE_ADV_NOT_RUNNING, ContactBle.ERR_OK);
            ContactShieldLog.i(TAG, "Contact data adv success");
            return true;
        } else {
            // If BLE advertising failed, return error reason to CP
            ContactShieldLog.i(TAG, "Contact Ble adv failed, err info:" + result);
            if (mIsNeedCallResponse.compareAndSet(true, false)) {
                action.call(StatusCode.STATUS_BLUETOOTH_OPERATION_ERROR, ContactBle.getBleStatusString(result));
            }
            return false;
        }
    }

    private void stopAdv() {
        ContactBleAdvertiser.getInstance().stopBleAdv();
        mAdvState.compareAndSet(ContactBle.ERR_OK, ContactBle.ERR_BLE_ADV_NOT_RUNNING);
    }

    private int getSecondsSinceLastScan() {
        if (mLastScanTimestamp == 0 || mCurrentScanTimestamp <= mLastScanTimestamp) {
            return 0;
        }
        long duration = mCurrentScanTimestamp - mLastScanTimestamp;
        // Round to 60 seconds
        double factor = ((double) duration) / KeyGenUtil.SECOND_TO_MINUTE;
        duration = Math.round(factor) * KeyGenUtil.SECOND_TO_MINUTE;
        if (duration > GlobalSettings.MAX_DURATION_SINCE_LAST_SCAN) {
            duration = GlobalSettings.MAX_DURATION_SINCE_LAST_SCAN;
        }
        return (int)duration;
    }

    private boolean startScan(ContactAction action) {
        // Stop previous scanning task
        ContactBleScanner.getInstance().stopBleScan();
        // Clear previous scanning data cache.
        ContactDataManage.getInstance().clearScanDscMap();

        mLastScanTimestamp = mCurrentScanTimestamp;
        mCurrentScanTimestamp = KeyGenUtil.getSecondsSinceEpoch();
        int result = ContactBleScanner.getInstance().startBleScan(mContactBleScanCallback);
        // If BLE scanning fails, return error reason to CP.
        if (result != ContactBle.ERR_OK) {
            ContactShieldLog.d(TAG, "start scan failed. result=" + result);
            ContactShieldLog.i(TAG, "Start BLE scan error");
            if (mIsNeedCallResponse.compareAndSet(true, false)) {
                action.call(StatusCode.STATUS_BLUETOOTH_OPERATION_ERROR, ContactBle.getBleStatusString(result));
            }
            return false;
        }
        ContactShieldLog.d(TAG, "start scan success. result=" + result);
        mScanState.compareAndSet(ContactBle.ERR_BLE_SCAN_NOT_RUNNING, ContactBle.ERR_OK);

        // Schedule BLE stop task in GlobalSettings.SCAN_PERIOD milli seconds
        mHandler.postDelayed(mStopScan, GlobalSettings.BLE_SCAN_DURATION);
        ContactShieldLog.d(TAG, "schedule stop scan in " + GlobalSettings.BLE_SCAN_DURATION);

        if (mWakeLock != null) {
            mWakeLock.acquire();
            ContactShieldLog.d(TAG, "Acquire wakelock to record scan results.");
        }
        return true;
    }

    private void stopScan() {
        mHandler.removeCallbacks(mStopScan);
        ContactBleScanner.getInstance().stopBleScan();
        mScanState.compareAndSet(ContactBle.ERR_OK, ContactBle.ERR_BLE_SCAN_NOT_RUNNING);
        ContactDataManage.getInstance().flushScanDataToDb(getSecondsSinceLastScan());
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            ContactShieldLog.d(TAG, "Release wakelock.");
        }
    }

    /**
     * Check if contact shield is running
     *
     * @return Running state
     */
    public boolean isRunning() {
        return mAdvState.get() == ContactBle.ERR_OK || mScanState.get() == ContactBle.ERR_OK;
    }

    /**
     * Stop contact task when Location or Bluetooth switch is turned off.
     */
    public void stopOnSwitchOff() {
        ThreadExec.execSeqTask(TAG, () -> {
            if (!mContactRunning.get()) {
                ContactShieldLog.d(TAG, "ContactShield BLE task is not running");
                return;
            }
            doStop();
        });
    }

    /**
     * Restart contact task when both Location and Bluetooth switches are turned on.
     */
    public void restartOnSwitchOn() {
        ThreadExec.execSeqTask(TAG, () -> {
            ContactShieldLog.d(TAG, "Restart ContactShield BLE task");
            if (mPkgName == null || mContactAction == null) {
                ContactShieldLog.e(TAG, "restart contact failed");
                return;
            }

            if (mContactRunning.get()) {
                ContactShieldLog.d(TAG, "stop before restart contact");
                doStop();
            }

            doStart();
        });
    }
}
