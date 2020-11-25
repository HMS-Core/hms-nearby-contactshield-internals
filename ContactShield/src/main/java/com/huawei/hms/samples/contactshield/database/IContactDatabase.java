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

package com.huawei.hms.samples.contactshield.database;

import com.huawei.hms.samples.contactshield.contact.ContactDetail;
import com.huawei.hms.samples.contactshield.contact.ContactSketch;
import com.huawei.hms.samples.contactshield.database.table.ContactDetailData;
import com.huawei.hms.samples.contactshield.database.table.ContactSketchData;
import com.huawei.hms.samples.contactshield.database.table.ContactWindowData;
import com.huawei.hms.samples.contactshield.database.table.PdkData;
import com.huawei.hms.samples.contactshield.database.table.PdkNum;
import com.huawei.hms.samples.contactshield.database.table.ScanData;

import java.util.List;

public interface IContactDatabase {
    // APIs for ScanData
    /**
     * Insert new ScanData
     *
     * @param scanData ScanData
     */
    void insertScanData(ScanData scanData);

    /**
     * Search ScanData with given DSC and between startInterval ~ endInterval
     *
     * @param dscTarget target DSC
     * @param startInterval starting timestamp
     * @param endInterval ending timestamp
     * @return matched ScanData list
     */
    List<ScanData> getAllScanData(byte[] dscTarget, long startInterval, long endInterval);

    /**
     * Get the number of ScanData which is between startInterval ~ endInterval
     *
     * @param startInterval starting timestamp
     * @param endInterval ending timestamp
     * @return the number of matched ScanData
     */
    long getScanDataNum(long startInterval, long endInterval);

    /**
     * Delete ScanData whose timestamp is out of validTs
     *
     * @param validTs valid timestamp
     * @return number of deleted ScanData
     */
    int deleteScanData(long validTs);

    /**
     * Delete all ScanData
     */
    void deleteScanData();

    // APIs for PdkData
    /**
     * Insert new PdkData
     *
     * @param pdkData new PdkData
     */
    void insertPdkData(PdkData pdkData);

    /**
     * Get the latest PdKData for specific package
     *
     * @param pkgName package name
     * @return content of latest PdkData
     */
    List<byte[]> getLatestPdkData(String pkgName);

    /**
     * Get the timestamp of latest PdkData for specific package
     *
     * @param pkgName package name
     * @return timestamp of latest PdkData
     */
    long getMaxIntervalNum(String pkgName);

    /**
     * Get content and timestamp of all the PdkData for specific package
     *
     * @param pkgName package name
     * @return PdkNum list with contain both of content and timestamp of PdkData
     */
    List<PdkNum> getPdkData(String pkgName);

    /**
     * Delete PdkData of specific package
     *
     * @param pkgName package name
     */
    void deletePdkData(String pkgName);

    /**
     * Delete PdkData whose timestamp is out of validTs for specific package
     *
     * @param validTs valid timestamp
     * @param pkgName package name
     * @return number of deleted PdkData
     */
    int deletePdkData(long validTs, String pkgName);

    // APIs for ContactDetailData
    /**
     * Insert new ContactDetailData
     *
     * @param contactDetailData ContactDetailData list
     */
    void insertContactDetailData(ContactDetailData... contactDetailData);

    /**
     * Get ContactDetailData list for specific package + token
     *
     * @param pkgName package name
     * @param token token
     * @return ContactDetailData list
     */
    List<ContactDetail> getContactDetailList(String pkgName, String token);

    // APIs for ContactSketchData
    /**
     * Insert new ContactSketchData
     *
     * @param contactSketchData ContactSketchData
     */
    void insertContactSketchData(ContactSketchData... contactSketchData);

    /**
     * Update ContactSketchData
     *
     * @param contactSketchData ContactSketchData
     */
    void updateContactSketchData(ContactSketchData contactSketchData);

    /**
     * Get ContactSketch for specific package + token
     *
     * @param pkgName package name
     * @param token token
     * @return matched ContactSketch
     */
    ContactSketch getContactSketch(String pkgName, String token);

    /**
     * Delete ContactSketchData whose timestamp is out of validTs.
     * Note that this API should also take of deleting ContactDetailData with the same package name.
     *
     * @param pkgName package name
     * @param validTs valid timestamp
     */
    void deleteContactSketchData(String pkgName, long validTs);

    /**
     * Delete ContactSketchData for specific package.
     * Note that this API should also take of deleting ContactDetailData with the same package name.
     *
     * @param pkgName package name
     */
    void deleteContactSketchData(String pkgName);

    // APIs for ContactWindowData
    /**
     * Insert new ContactWindowData
     *
     * @param contactWindowData ContactWindowData
     */
    void insertContactWindowData(ContactWindowData contactWindowData);

    /**
     * Get ContactWindowData list for specific package
     *
     * @param pkgName package name
     * @return ContactWindowData list
     */
    List<ContactWindowData> getContactWindowData(String pkgName);

    /**
     * Delete all ContactWindowData for specific package
     *
     * @param pkgName package name
     */
    void deleteContactWindowData(String pkgName);

    /**
     * Delete stale ContactWindowData which are out of validTs
     *
     * @param pkgName package name
     * @param validTs valid timestamp
     */
    void deleteContactWindowData(String pkgName, long validTs);
}
