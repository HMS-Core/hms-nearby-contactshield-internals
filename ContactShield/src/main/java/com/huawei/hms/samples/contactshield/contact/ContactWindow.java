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
package com.huawei.hms.samples.contactshield.contact;

import com.huawei.hms.samples.contactshield.util.GlobalSettings;
import com.huawei.hms.samples.contactshield.util.ParamsRangeChecker;

import java.util.ArrayList;
import java.util.List;

/**
 * ContactWindow
 *
 * @since 2020-07-15
 */
public class ContactWindow {
    // date millis since 1970
    private long mDateMillis;
    private List<ScanInfo> mScanInfos;

    /**
     * REPORT_TYPE_CONFIRMED_TEST                1
     * REPORT_TYPE_CONFIRMED_CLINICAL_DIAGNOSIS  2
     * REPORT_TYPE_SELF_REPORT                   3
     * REPORT_TYPE_RECURSIVE                     4
     */
    private int mReportType;

    private ContactWindow(Builder builder) {
        mDateMillis = builder.mDateMillis;
        mScanInfos = builder.mScanInfos;
        mReportType = builder.mReportType;
    }

    /**
     * Getter
     */
    public long getDateMillis() {
        return mDateMillis;
    }

    /**
     * Getter
     */
    public List<ScanInfo> getScanInfos() {
        return mScanInfos;
    }

    /**
     * Getter
     */
    public int getReportType() {
        return mReportType;
    }

    @Override
    public String toString() {
        return "ContactWindow: dateMillis: " + mDateMillis
                + ", reportType: " + mReportType;
    }

    /**
     * ContactWindow builder
     */
    public static class Builder {
        private long mDateMillis = 0;
        private List<ScanInfo> mScanInfos = new ArrayList<>();
        private int mReportType = 0;

        /**
         * Build ContactWindow
         */
        public ContactWindow build() {
            return new ContactWindow(this);
        }

        /**
         * Setter
         */
        public ContactWindow.Builder setDateMillis(long dateMillis) {
            mDateMillis = ParamsRangeChecker.checkNonNegative(dateMillis);
            return this;
        }

        /**
         * Setter
         */
        public ContactWindow.Builder setScanInfos(List<ScanInfo> scanInfos) {
            if (ParamsRangeChecker.checkListNotNull(scanInfos)) {
                mScanInfos = scanInfos;
            }
            return this;
        }

        /**
         * Setter
         */
        public ContactWindow.Builder setReportType(int reportType) {
            if (ParamsRangeChecker.checkArgumentRange(reportType, 0 , GlobalSettings.REPORT_TYPE_MAX,
                    "reportType is %s, must >%s and <%s")) {
                mReportType = reportType;
            }
            return this;
        }
    }
}
