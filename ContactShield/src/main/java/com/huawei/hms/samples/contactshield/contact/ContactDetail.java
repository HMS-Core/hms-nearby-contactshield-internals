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

import com.huawei.hms.samples.contactshield.util.ParamsRangeChecker;

import java.util.Locale;

/**
 * Contact Detail
 *
 * @since 2020-04-30
 */
public class ContactDetail {
    private long dayNumber;

    private int durationMinutes;

    private int attenuationRiskValue;

    @RiskLevel
    private int initialRiskLevel;

    private int totalRiskValue;

    // in minutes
    private int[] attenuationDurations;

    private ContactDetail(ContactDetail.Builder builder) {
        this.dayNumber = builder.dayNumber;
        this.durationMinutes = builder.durationMinutes;
        this.attenuationRiskValue = builder.attenuationRiskValue;
        this.initialRiskLevel = builder.initialRiskLevel;
        this.totalRiskValue = builder.totalRiskValue;
        this.attenuationDurations = builder.attenuationDurations;
    }

    /**
     * Getter
     */
    public long getDayNumber() {
        return dayNumber;
    }

    /**
     * Getter
     */
    public int getDurationMinutes() {
        return durationMinutes;
    }

    /**
     * Getter
     */
    public int getAttenuationRiskValue() {
        return attenuationRiskValue;
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
    public int getTotalRiskValue() {
        return totalRiskValue;
    }

    /**
     * Getter of attenuationDurations, in minutes
     *
     * @return value of attenuationDurations
     */
    public int[] getAttenuationDurations() {
        return attenuationDurations.clone();
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "ContactDetail<dayNumber: %s, durationMinutes: %d, " +
                        "attenuationRiskValue: %d, initialRiskLevel: %d, totalRiskValue: %d, " +
                        "attenuationDurations: %d, %d, %d>",
                this.dayNumber,
                this.durationMinutes,
                this.attenuationRiskValue,
                this.initialRiskLevel,
                this.totalRiskValue,
                this.attenuationDurations[0],
                this.attenuationDurations[1],
                this.attenuationDurations[2]);
    }

    /**
     * ContactDetail Builder
     */
    public static class Builder {
        private long dayNumber;

        private int durationMinutes;

        private int attenuationRiskValue;

        @RiskLevel
        private int initialRiskLevel;

        private int totalRiskValue;

        /* attenuationDurations is an int array,length is 3 */
        private int[] attenuationDurations = new int[3];

        /**
         * Build ContactDetail
         */
        public ContactDetail build() {
            return new ContactDetail(this);
        }

        /**
         * Setter
         */
        public ContactDetail.Builder setDayNumber(long dayNumberSinceEpoch) {
            this.dayNumber = ParamsRangeChecker.checkNonNegative(dayNumberSinceEpoch);
            return this;
        }

        /**
         * Setter
         */
        public ContactDetail.Builder setDurationMinutes(int durationMinutes) {
            this.durationMinutes = ParamsRangeChecker.checkNonNegative(durationMinutes);
            return this;
        }

        /**
         * Setter
         */
        public ContactDetail.Builder setAttenuationRiskValue(int attenuationRiskValue) {
            this.attenuationRiskValue = ParamsRangeChecker.checkAttenuationRiskValueValid(attenuationRiskValue);
            return this;
        }

        /**
         * Setter
         */
        public ContactDetail.Builder setInitialRiskLevel(int initialRiskLevel) {
            this.initialRiskLevel = ParamsRangeChecker.checkRiskLevelValid(initialRiskLevel);
            return this;
        }

        /**
         * Setter
         */
        public ContactDetail.Builder setTotalRiskValue(int totalRiskValue) {
            this.totalRiskValue = ParamsRangeChecker.checkRiskValueValid(totalRiskValue);
            return this;
        }

        /**
         * Setter
         */
        public ContactDetail.Builder setAttenuationDurations(int[] attenDurations) {
            if (attenDurations == null || attenDurations.length != 3) {
                return this;
            }
            ParamsRangeChecker.checkAttenuationDurationsValid(attenDurations);
            System.arraycopy(attenDurations, 0, this.attenuationDurations, 0, 3);
            return this;
        }
    }
}
