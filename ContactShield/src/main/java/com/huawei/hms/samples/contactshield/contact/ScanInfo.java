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

public class ScanInfo {
    private int mAverageAttenuation;
    private int mMinimumAttenuation;
    private int mSecondsSinceLastScan;

    private ScanInfo(Builder builder) {
        mAverageAttenuation = builder.mAverageAttenuation;
        mMinimumAttenuation = builder.mMinimumAttenuation;
        mSecondsSinceLastScan = builder.mSecondsSinceLastScan;
    }

    /**
     * Getter
     */
    public int getAverageAttenuation() {
        return mAverageAttenuation;
    }

    /**
     * Getter
     */
    public int getMinimumAttenuation() {
        return mMinimumAttenuation;
    }

    /**
     * Getter
     */
    public int getSecondsSinceLastScan() {
        return mSecondsSinceLastScan;
    }

    @Override
    public String toString() {
        return "ScanInfo: AverageAttenuation: " + mAverageAttenuation +
                ", MinimumAttenuation: " + mMinimumAttenuation +
                ", SecondsSinceLastScan: " + mSecondsSinceLastScan;
    }

    public static class Builder {
        private int mAverageAttenuation = 0;
        private int mMinimumAttenuation = 0;
        private int mSecondsSinceLastScan = 0;

        /**
         * Constructor for builder
         */
        public Builder() {
        }

        /**
         * Build ScanInfo instance
         *
         * @return ScanInfo instance
         */
        public ScanInfo build() {
            return new ScanInfo(this);
        }

        /**
         * Setter
         */
        public Builder setAverageAttenuation(int averageAttenuation) {
            mAverageAttenuation = averageAttenuation;
            return this;
        }

        /**
         * Setter
         */
        public Builder setMinimumAttenuation(int minimumAttenuation) {
            mMinimumAttenuation = minimumAttenuation;
            return this;
        }

        /**
         * Setter
         */
        public Builder setSecondsSinceLastScan(int secondsSinceLastScan) {
            mSecondsSinceLastScan = secondsSinceLastScan;
            return this;
        }
    }
}
