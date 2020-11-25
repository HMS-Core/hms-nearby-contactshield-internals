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

import java.util.Arrays;
import java.util.Locale;

/**
 * PeriodicKey
 *
 * @since 2020-04-30
 */
public class PeriodicKey {
    private byte[] content;

    private long periodicKeyValidTime;

    private long periodicKeyLifeTime;

    // Determined by cp
    private int initialRiskLevel;

    /**
     * REPORT_TYPE_UNKNOW                        0
     * REPORT_TYPE_CONFIRMED_TEST                1
     * REPORT_TYPE_CONFIRMED_CLINICAL_DIAGNOSIS  2
     * REPORT_TYPE_SELF_REPORT                   3
     * REPORT_TYPE_RECURSIVE                     4
     * REPORT_TYPE_REVOKED                       5
     */
    private int reportType;

    private PeriodicKey(Builder builder) {
        this.content = builder.content;
        this.periodicKeyValidTime = builder.periodicKeyValidTime;
        this.periodicKeyLifeTime = builder.periodicKeyLifeTime;
        this.initialRiskLevel = builder.initialRiskLevel;
        this.reportType = builder.reportType;
    }

    /**
     * Getter
     */
    public byte[] getContent() {
        return content.clone();
    }

    /**
     * Getter
     */
    public long getPeriodicKeyValidTime() {
        return periodicKeyValidTime;
    }

    /**
     * Getter
     */
    public long getPeriodicKeyLifeTime() {
        return periodicKeyLifeTime;
    }

    /**
     * Getter
     */
    public int getInitialRiskLevel() {
        return initialRiskLevel;
    }

    /**
     * Getter
     */
    public int getReportType() {
        return reportType;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "PeriodicKey<content: %s, periodicKeyValidTime: %s," +
                        " periodicKeyLifeTime: %s, initialRiskLevel: %d, reportType: %d>",
                Arrays.toString(this.content),
                this.periodicKeyValidTime,
                this.periodicKeyLifeTime,
                this.initialRiskLevel,
                this.reportType);
    }

    /**
     * PeriodKey Builder
     */
    public static class Builder {
        private byte[] content = new byte[16];

        private long periodicKeyValidTime = 0;

        private long periodicKeyLifeTime = 0;

        private int initialRiskLevel = 0;

        private int reportType = 0;

        /**
         * Build PeriodKey
         */
        public PeriodicKey build() {
            return new PeriodicKey(this);
        }

        /**
         * Setter
         */
        public PeriodicKey.Builder setContent(byte[] content) {
            if (ParamsRangeChecker.checkByteArrayValid(content)) {
                this.content = content.clone();
            }
            return this;
        }

        /**
         * Setter
         */
        public Builder setPeriodicKeyValidTime(long periodicKeyValidTime) {
            this.periodicKeyValidTime = ParamsRangeChecker.checkNonNegative(periodicKeyValidTime);
            return this;
        }

        /**
         * Setter
         */
        public Builder setPeriodicKeyLifeTime(long periodicKeyLifeTime) {
            this.periodicKeyLifeTime = ParamsRangeChecker.checkNonNegative(periodicKeyLifeTime);
            return this;
        }

        /**
         * Setter
         */
        public Builder setInitialRiskLevel(int initialRiskLevel) {
            this.initialRiskLevel = ParamsRangeChecker.checkRiskLevelValid(initialRiskLevel);
            return this;
        }

        /**
         * Setter
         */
        public PeriodicKey.Builder setReportType(int reportType) {
            if (ParamsRangeChecker.checkArgumentRange(reportType, 0 , GlobalSettings.REPORT_TYPE_MAX,
                    "reportType is %s, must >=%s and <=%s")) {
                this.reportType = reportType;
            }
            return this;
        }
    }
}