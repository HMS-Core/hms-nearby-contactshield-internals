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

import com.huawei.hms.samples.contactshield.database.ContactDatabaseImpl;
import com.huawei.hms.samples.contactshield.database.IContactDatabase;
import com.huawei.hms.samples.contactshield.util.ContactShieldLog;
import com.huawei.hms.samples.contactshield.util.threadpool.ThreadExec;
import com.huawei.hms.samples.contactshield.ble.ContactBeacon;
import com.huawei.hms.samples.contactshield.ble.ScanDataDetail;
import com.huawei.hms.samples.contactshield.contact.ContactDetail;
import com.huawei.hms.samples.contactshield.contact.ContactSketch;
import com.huawei.hms.samples.contactshield.contact.ContactWindow;
import com.huawei.hms.samples.contactshield.contact.PeriodicKey;
import com.huawei.hms.samples.contactshield.contact.ScanInfo;
import com.huawei.hms.samples.contactshield.crypto.PeriodicKeyGenerator;
import com.huawei.hms.samples.contactshield.database.table.ContactDetailData;
import com.huawei.hms.samples.contactshield.database.table.ContactSketchData;
import com.huawei.hms.samples.contactshield.database.table.ContactWindowData;
import com.huawei.hms.samples.contactshield.database.table.ContactWindowBase;
import com.huawei.hms.samples.contactshield.database.table.PdkData;
import com.huawei.hms.samples.contactshield.database.table.PdkNum;
import com.huawei.hms.samples.contactshield.database.table.ScanData;
import com.huawei.hms.samples.contactshield.database.table.ScanInfoData;
import com.huawei.hms.samples.contactshield.util.GlobalSettings;
import com.huawei.hms.samples.contactshield.util.KeyGenUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ContactDataManage
 *
 * @since 2019-04-28
 */
public class ContactDataManage {
    private static final String TAG = "ContactDataManage";

    private static volatile ContactDataManage contactDataInstance;

    private final Map<String, ScanDataDetail> scanDataDetailMap = new HashMap<>();

    // Lock to protect scanDataDetailMap
    private final Object mLock = new Object();

    private AtomicLong storageRemainOfDailyScan;

    private IContactDatabase contactDatabase;

    /**
     * Constructor
     */
    private ContactDataManage() {
        storageRemainOfDailyScan = new AtomicLong(Long.MAX_VALUE);
        contactDatabase = new ContactDatabaseImpl();
    }

    /**
     * Get singleton instance
     */
    public static ContactDataManage getInstance() {
        if (contactDataInstance == null) {
            synchronized (ContactDataManage.class) {
                if (contactDataInstance == null) {
                    contactDataInstance = new ContactDataManage();
                }
            }
        }
        return contactDataInstance;
    }

    public void clearScanDscMap() {
        synchronized (mLock) {
            scanDataDetailMap.clear();
        }
    }

    private static boolean isValidDscId(byte[] localDsc) {
        if (localDsc == null || localDsc.length != ContactBeacon.DSC_LENGTH) {
            return false;
        }
        return true;
    }

    /**
     * Search ScanData with given DSC
     *
     * @return matched value
     */
    public List<ScanData> searchScanDataWithGivenDsc(byte[] dscTarget, long startInterval, long endInterval) {
        List<ScanData> scanDataList = contactDatabase.getAllScanData(dscTarget, startInterval, endInterval);
        if (scanDataList == null || scanDataList.size() == 0) {
            return Collections.emptyList();
        }
        return scanDataList;
    }

    /**
     * Add scan detail data to cache
     *
     * @param dscData DSC data
     * @param sdData SD data
     * @param rssi RSSI value for this scan record
     * @return Flag indicating operation result, true for success and false for failure.
     */
    public boolean addScanDetail(byte[] dscData, byte[] sdData, int rssi) {
        if (!isValidDscId(dscData)) {
            ContactShieldLog.i(TAG, "addScanDetail failure. localDsc: " + Arrays.toString(dscData));
            return false;
        }

        String mapKey = Arrays.toString(dscData);
        synchronized (mLock) {
            ScanDataDetail scanDataDetail = scanDataDetailMap.get(mapKey);
            if (scanDataDetail == null) {
                scanDataDetail = new ScanDataDetail(dscData, sdData, rssi);
                // Should limit the size of scanDscMap
                scanDataDetailMap.put(mapKey, scanDataDetail);
            } else {
                scanDataDetail.addRssi(rssi);
            }
        }
        ContactShieldLog.i(TAG, "addScanDetail done. localDsc: " + Arrays.toString(dscData));
        return true;
    }

    /**
     * Flush scan detail data to database
     *
     * @param secondsSinceLastScan seconds since last scan
     */
    public void flushScanDataToDb(int secondsSinceLastScan) {
        ContactShieldLog.d(TAG, "Flush scan data to db. seconds since last scan: " + secondsSinceLastScan);
        ThreadExec.execNormalTask(TAG, () -> {
            // Update database storage limit, to avoid inserting too many records within single day.
            boolean res = storageRemainOfDailyScan.compareAndSet(Long.MAX_VALUE,
                    GlobalSettings.DAILY_SCAN_DATA_COUNT_LIMIT - getTodayScanItemCount());
            if (res) {
                ContactShieldLog.d(TAG, "Get Storage remain from DB, " + storageRemainOfDailyScan.get());
            }

            synchronized (mLock) {
                for (Map.Entry<String, ScanDataDetail> entry : scanDataDetailMap.entrySet()) {
                    if (storageRemainOfDailyScan.get() < 0) {
                        ContactShieldLog.i(TAG, "Storage is limited, discard this scan result.");
                        break;
                    }
                    ScanDataDetail scanDataDetail = entry.getValue();
                    ScanData scanData = new ScanData();
                    scanData.setDsc(scanDataDetail.getDscData());
                    scanData.setSd(scanDataDetail.getSdData());
                    scanData.setRssi(scanDataDetail.getMaxRssi());
                    scanData.setIntervalNum(scanDataDetail.getIntervalNum());
                    scanData.setAverageRssi((int) Math.round(scanDataDetail.getAverageRssi()));
                    scanData.setSecondsSinceLastScan(secondsSinceLastScan);
                    contactDatabase.insertScanData(scanData);
                    storageRemainOfDailyScan.decrementAndGet();
                    ContactShieldLog.d(TAG, "Storage remain is " + storageRemainOfDailyScan.get());
                }
                scanDataDetailMap.clear();
            }
        });
    }

    /**
     * Add pdk to PdkData table
     *
     * @param pdk pdk
     */
    public void addPdkData(byte[] pdk, String pkgName) {
        PdkData pdkData = new PdkData();
        pdkData.setIntervalNum(KeyGenUtil.getIntervalNumber());
        pdkData.setPdk(pdk);
        pdkData.setAppId(pkgName);
        contactDatabase.insertPdkData(pdkData);
        ContactShieldLog.i(TAG, "add pdk to db:" + Arrays.toString(pdk));
    }

    /**
     * Get latest PeriodicKey
     *
     * @param pkgName package name
     * @return the latest PeriodKey
     */
    public byte[] getLatestPeriodicKey(String pkgName) {
        List<byte[]> pdkDataList = contactDatabase.getLatestPdkData(pkgName);
        if (pdkDataList == null || pdkDataList.size() == 0) {
            ContactShieldLog.d(TAG, "getLatestPeriodicKey: list is null or size is 0.");
            return new byte[0];
        }

        return pdkDataList.get(0);
    }

    /**
     * Get interval number of latest PeriodKey
     *
     * @param pkgName package name
     * @return interval number of latest PeriodKey
     */
    public long getLastIntervalNum(String pkgName) {
        return contactDatabase.getMaxIntervalNum(pkgName);
    }

    /**
     * Get historical period key list.
     * New period key(pdk) will be generated per day and save to database. Pdk out of incubation period will be removed.
     * With such mechanism, all pdk in database is guaranteed to be valid with incubation period.
     *
     * @param pkgName package name
     * @return Historical period key.
     */
    public List<PeriodicKey> getHisPeriodKey(String pkgName) {
        List<PeriodicKey> periodicKeyList = new ArrayList<>();

        List<PdkNum> pdkNumList = contactDatabase.getPdkData(pkgName);
        if (pdkNumList == null || pdkNumList.size() == 0) {
            ContactShieldLog.d(TAG, "getHisPeriodKey: list is null or size is 0.");
            return periodicKeyList;
        }

        for (PdkNum pdkNum : pdkNumList) {
            byte[] pdk = pdkNum.getPdk();
            if (pdk.length != 16) {
                continue;
            }
            // Timestamp when PeriodKey was generated since epoch.
            long validInterval = pdkNum.getIntervalNum();
            // Valid duration of PeriodKey: 144 - timestamp when PeriodKey generated since midnight
            long expireInterval = GlobalSettings.PDK_ROLLING_PERIOD -
                    validInterval % GlobalSettings.PDK_ROLLING_PERIOD;
            PeriodicKey periodicKey = new PeriodicKey.Builder()
                    .setPeriodicKeyValidTime(validInterval)
                    .setPeriodicKeyLifeTime(expireInterval)
                    .setContent(pdk)
                    .build();
            periodicKeyList.add(periodicKey);
        }
        return periodicKeyList;
    }

    /**
     * Add contact detail data
     *
     * @param pkgName package name
     * @param token token
     * @param detail contact detail
     */
    public void addContactDetailData(String pkgName, String token, ContactDetail detail) {
        ContactDetailData result = new ContactDetailData();
        result.setPkgName(pkgName);
        result.setToken(token);
        result.setContactDetail(detail);
        contactDatabase.insertContactDetailData(result);
    }

    /**
     * Get contact detail list
     *
     * @param pkgName package name
     * @param token token
     * @return contact detail list
     */
    public List<ContactDetail> getContactDetailList(String pkgName, String token) {
        List<ContactDetail> contactDetailList = contactDatabase.getContactDetailList(pkgName,
            token);
        if (contactDetailList == null || contactDetailList.size() == 0) {
            return Collections.emptyList();
        }
        return contactDetailList;
    }

    private void manipulateContactSketchData(String pkgName, String token, ContactSketch contactSketch, boolean toAdd) {
        ContactSketchData contactSketchData = new ContactSketchData();
        contactSketchData.setPkgName(pkgName);
        contactSketchData.setToken(token);
        contactSketchData.setContactSketch(contactSketch);
        contactSketchData.setLastUpdateTimestamp(KeyGenUtil.getIntervalNumber());
        if (toAdd) {
            contactDatabase.insertContactSketchData(contactSketchData);
        } else {
            contactDatabase.updateContactSketchData(contactSketchData);
        }
    }

    /**
     * Add ContactSketch data
     *
     * @param pkgName package name
     * @param token token
     * @param contactSketch contact sketch
     */
    public void addContactSketchData(String pkgName, String token, ContactSketch contactSketch) {
        manipulateContactSketchData(pkgName, token, contactSketch, true);
    }

    /**
     * Update ContactSketch data
     *
     * @param pkgName package name
     * @param token token
     * @param contactSketch contact sketch
     */
    public void updateContactSketchData(String pkgName, String token, ContactSketch contactSketch) {
        manipulateContactSketchData(pkgName, token, contactSketch, false);
    }

    /**
     * Get contact sketch
     *
     * @param pkgName package name
     * @param token token
     * @return contact sketch
     */
    public ContactSketch getContactSketch(String pkgName, String token) {
        return contactDatabase.getContactSketch(pkgName, token);
    }

    /**
     * Clear stale ContactSketch and ContactDetail data
     *
     * @param pkgName package name
     */
    public void clearStaleSketchAndDetailData(String pkgName) {
        long dataToDeleteInterval = KeyGenUtil.getDeleteTimeInterval(GlobalSettings
                .getGlobalContactShieldSetting().getIncubationPeriod());
        contactDatabase.deleteContactSketchData(pkgName, dataToDeleteInterval);
    }

    private List<ScanInfo> toScanInfoList(List<ScanInfoData> scanInfoDataList) {
        List<ScanInfo> scanInfoList = new ArrayList<>();
        if (scanInfoDataList != null) {
            for (ScanInfoData scanInfoData : scanInfoDataList) {
                scanInfoList.add(scanInfoData.getScanInfo());
            }
        }
        return scanInfoList;
    }

    private List<ScanInfoData> fromScanInfoList(List<ScanInfo> scanInfoList) {
        List<ScanInfoData> scanInfoDataList = new ArrayList<>();
        for (ScanInfo scanInfo : scanInfoList) {
            scanInfoDataList.add(new ScanInfoData(scanInfo));
        }
        return scanInfoDataList;
    }

    /**
     * Add ContactWindow list
     *
     * @param pkgName package name
     * @param contactWindowList ContactWindow list
     */
    public void addContactWindows(String pkgName, List<ContactWindow> contactWindowList) {
        for (ContactWindow contactWindow : contactWindowList) {
            ContactWindowBase contactWindowBase = new ContactWindowBase();
            contactWindowBase.setDateMillis(contactWindow.getDateMillis());
            contactWindowBase.setReportType(contactWindow.getReportType());
            contactWindowBase.setPkgName(pkgName);
            contactWindowBase.setLastUpdateTimestamp(KeyGenUtil.getIntervalNumber());

            List<ScanInfoData> scanInfoDataList = fromScanInfoList(contactWindow.getScanInfos());

            ContactWindowData contactWindowData = new ContactWindowData();
            contactWindowData.setContactWindowBase(contactWindowBase);
            contactWindowData.setScanInfoDataList(scanInfoDataList);

            contactDatabase.insertContactWindowData(contactWindowData);
        }
    }

    /**
     * Get ContactWindow list
     *
     * @param pkgName package name
     * @return ContactWindow list
     */
    public List<ContactWindow> getContactWindows(String pkgName) {
        List<ContactWindow> contactWindowList = new ArrayList<>();
        List<ContactWindowData> contactWindowDataList =
            contactDatabase.getContactWindowData(pkgName);
        if (contactWindowDataList != null) {
            for (ContactWindowData contactWindowData : contactWindowDataList) {
                ContactWindowBase contactWindowBase = contactWindowData.getContactWindowBase();
                List<ScanInfo> scanInfoList = toScanInfoList(contactWindowData.getScanInfoDataList());
                ContactWindow contactWindow = new ContactWindow.Builder()
                        .setDateMillis(contactWindowBase.getDateMillis())
                        .setReportType(contactWindowBase.getReportType())
                        .setScanInfos(scanInfoList)
                        .build();
                contactWindowList.add(contactWindow);
            }
        }
        return contactWindowList;
    }

    /**
     * Clear data from database when uninstalling package
     *
     * @param pkgName package name
     */
    public void clearData(String pkgName) {
        ContactShieldLog.d(TAG, "start to clear data.");
        contactDatabase.deletePdkData(pkgName);
        contactDatabase.deleteContactSketchData(pkgName);
        contactDatabase.deleteContactWindowData(pkgName);
        ContactShieldLog.d(TAG, "finish to clear data.");
        PeriodicKeyGenerator.clearPeriodicKey();
    }

    /**
     * Clear cache data
     */
    public void clearCacheData() {
        ContactShieldLog.d(TAG, "clear cache data.");
        synchronized (mLock) {
            scanDataDetailMap.clear();
        }
    }

    /**
     * Get today's scan item count
     */
    private long getTodayScanItemCount() {
        long startIn = KeyGenUtil.getDayNumber() * GlobalSettings.PDK_ROLLING_PERIOD;
        long endIn = startIn + GlobalSettings.PDK_ROLLING_PERIOD;

        return contactDatabase.getScanDataNum(startIn, endIn);
    }

    /**
     * Check if storage is limited.
     *
     * @return flag indicating if limited
     */
    public boolean isStorageLimited() {
        return storageRemainOfDailyScan.get() < 0;
    }

    /**
     * Delete data periodically
     *
     * @param pkgName package name
     */
    public void checkAndDeleteData(String pkgName) {
        // The scan data will be kept in database for GlobalSettings.BLE_SCAN_DATA_DURATION days.
        long scanDataToDeleteInterval = KeyGenUtil.getDeleteTimeInterval(GlobalSettings.BLE_SCAN_DATA_DURATION);
        // Other data, such as PeriodKey, diagnosis result, etc, will be kept in database for "incubationPeriod" days.
        long dataToDeleteInterval = KeyGenUtil.getDeleteTimeInterval(GlobalSettings
            .getGlobalContactShieldSetting().getIncubationPeriod());
        contactDatabase.deleteScanData(scanDataToDeleteInterval);
        contactDatabase.deletePdkData(dataToDeleteInterval, pkgName);
        contactDatabase.deleteContactSketchData(pkgName, dataToDeleteInterval);
        contactDatabase.deleteContactWindowData(pkgName, dataToDeleteInterval);
        synchronized (mLock) {
            scanDataDetailMap.clear();
        }
        storageRemainOfDailyScan.set(Long.MAX_VALUE);
    }

    /**
     * Clear period keys and scan data
     */
    public void clearPdkAndScanData(String pkgName) {
        contactDatabase.deletePdkData(pkgName);
        contactDatabase.deleteScanData();
    }
}
