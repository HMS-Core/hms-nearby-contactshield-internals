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

import java.util.Collections;
import java.util.List;

public class ContactDatabaseImpl implements IContactDatabase {
    public ContactDatabaseImpl() {
    }

    @Override
    public void insertScanData(ScanData scanData) {
    }

    @Override
    public List<ScanData> getAllScanData(byte[] dscTarget, long startInterval, long endInterval) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public long getScanDataNum(long startInterval, long endInterval) {
        return 0;
    }

    @Override
    public int deleteScanData(long validTs) {
        return 0;
    }

    @Override
    public void deleteScanData() {
    }

    @Override
    public void insertPdkData(PdkData pdkData) {
    }

    @Override
    public List<byte[]> getLatestPdkData(String pkgName) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public long getMaxIntervalNum(String pkgName) {
        return 0;
    }

    @Override
    public List<PdkNum> getPdkData(String pkgName) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void deletePdkData(String pkgName) {
    }

    @Override
    public int deletePdkData(long validTs, String pkgName) {
        return 0;
    }

    @Override
    public void insertContactDetailData(ContactDetailData... contactDetailData) {
    }

    @Override
    public List<ContactDetail> getContactDetailList(String pkgName, String token) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void insertContactSketchData(ContactSketchData... contactSketchData) {
    }

    @Override
    public void updateContactSketchData(ContactSketchData contactSketchData) {
    }

    @Override
    public ContactSketch getContactSketch(String pkgName, String token) {
        return null;
    }

    @Override
    public void deleteContactSketchData(String pkgName, long validTs) {
    }

    @Override
    public void deleteContactSketchData(String pkgName) {
    }

    @Override
    public void insertContactWindowData(ContactWindowData contactWindowData) {
    }

    @Override
    public List<ContactWindowData> getContactWindowData(String pkgName) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void deleteContactWindowData(String pkgName) {
    }

    @Override
    public void deleteContactWindowData(String pkgName, long validTs) {
    }
}
