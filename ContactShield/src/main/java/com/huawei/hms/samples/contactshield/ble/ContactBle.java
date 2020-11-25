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

/**
 * Contact Ble
 *
 * @since 2020-06-19
 */
public class ContactBle {
    private static final String TAG = "ContactBle";

    /**
     * error code, no err
     */
    public static final int ERR_OK = 0;

    /**
     * error code, parameter error
     */
    public static final int ERR_PARAM = 200;

    /**
     * error code, no bluetooth found
     */
    public static final int ERR_NO_BT_ADAPTER = 201;

    /**
     * error code, ble not enabled
     */
    public static final int ERR_BT_NOT_ENABLED = 202;

    /**
     * error code, bluetooth not support scan
     */
    public static final int ERR_BT_NOT_SUPPORT_BLE = 203;

    /**
     * error code, ble advertising failed
     */
    public static final int ERR_BLE_ADV_FAILED = 204;

    /**
     * error code, ble advertising NOT_RUNNING
     */
    public static final int ERR_BLE_ADV_NOT_RUNNING = 205;

    /**
     * error code, ble scan failed
     */
    public static final int ERR_BLE_SCAN_FAILED = 206;

    /**
     * error code, ble scan NOT_RUNNING
     */
    public static final int ERR_BLE_SCAN_NOT_RUNNING = 207;

    /**
     * error code, operation to enable bt error
     */
    public static final int ERR_TO_ENABLE_BT = 208;

    /**
     * error code, ble advertise not supported
     */
    public static final int ERR_BT_NOT_SUPPORT_BLE_ADV = 209;

    /**
     * error code, ble scan not supported
     */
    public static final int ERR_BT_NOT_SUPPORT_BLE_SCAN = 210;

    /**
     * Get error reason
     */
    public static String getBleStatusString(int status) {
        switch (status) {
            case ERR_PARAM: {
                return "Failed to create advertise data or advertise setting";
            }
            case ERR_NO_BT_ADAPTER: {
                return "Failed to get bluetooth adapter";
            }
            case ERR_BT_NOT_ENABLED: {
                return "Bluetooth adapter not enabled";
            }
            case ERR_BT_NOT_SUPPORT_BLE: {
                return "Android version lower than LOLLIPOP does not support BLE";
            }
            case ERR_BT_NOT_SUPPORT_BLE_ADV: {
                return "Bluetooth adapter does not support BLE advertising";
            }
            case ERR_BT_NOT_SUPPORT_BLE_SCAN: {
                return "Bluetooth adapter does not support BLE scan";
            }
            case ERR_TO_ENABLE_BT: {
                return "Failed to enable bluetooth adapter";
            }
            case ERR_BLE_SCAN_FAILED: {
                return "Start BLE scan failed";
            }
            case ERR_BLE_ADV_FAILED: {
                return "Start BLE advertising failed";
            }
            default: {
                return "Bluetooth operation error";
            }
        }
    }

    /**
     * Check if the device has BluetoothAdapter or BluetoothAdapter is enabled.
     * If BluetoothAdapter is not enabled, try to enable it.
     *
     * @return result code ERR_* in {@link ContactBle}
     */
    public static int checkAndEnableBtAdapter() {
        // TODO: Check and enable Bluetooth adapter according based on platfrom API.
        return ERR_OK;
    }
}
