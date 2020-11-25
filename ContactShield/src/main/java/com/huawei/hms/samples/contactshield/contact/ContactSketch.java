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
 * ContactSketch
 *
 * @since 2020-04-30
 */
public class ContactSketch {
    private int daysSinceLastHit;

    private int numberOfHits;

    private int maxRiskValue;

    private int summationRiskValue;

    // in minutes
    private int[] attenuationDurations;

    private ContactSketch(ContactSketch.Builder builder) {
        this.daysSinceLastHit = builder.daysSinceLastHit;
        this.numberOfHits = builder.numberOfHits;
        this.maxRiskValue = builder.maxRiskValue;
        this.summationRiskValue = builder.summationRiskValue;
        this.attenuationDurations = builder.attenuationDurations;
    }

    /**
     * Getter
     */
    public int getDaysSinceLastHit() {
        return daysSinceLastHit;
    }

    /**
     * Getter
     */
    public int getNumberOfHits() {
        return numberOfHits;
    }

    /**
     * Getter
     */
    public int getMaxRiskValue() {
        return maxRiskValue;
    }

    /**
     * Getter
     */
    public int getSummationRiskValue() {
        return summationRiskValue;
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
        return String.format(Locale.ENGLISH, "ContactSketch<daysSinceLastHit: %d, " +
                        "numberOfHits: %d, maxRiskValue: %d, summationRiskValue: %d, attenuationDurations: %d, %d, %d>",
                this.daysSinceLastHit,
                this.numberOfHits,
                this.maxRiskValue,
                this.summationRiskValue,
                this.attenuationDurations[0],
                this.attenuationDurations[1],
                this.attenuationDurations[2]
        );
    }

    /**
     * ContactSketch Builder
     */
    public static class Builder {
        private int daysSinceLastHit = 0;

        private int numberOfHits = 0;

        private int maxRiskValue = 0;

        private int summationRiskValue = 0;

        /* attenuationDurations is an int array,length is 3 */
        private int[] attenuationDurations = new int[3];

        /**
         * Build Contact Sketch
         */
        public ContactSketch build() {
            return new ContactSketch(this);
        }

        /**
         * Setter
         */
        public ContactSketch.Builder setDaysSinceLastHit(int daysSinceLastHit) {
            this.daysSinceLastHit = ParamsRangeChecker.checkNonNegative(daysSinceLastHit);
            return this;
        }

        /**
         * Setter
         */
        public ContactSketch.Builder setNumberOfHits(int numberOfHits) {
            this.numberOfHits = ParamsRangeChecker.checkNonNegative(numberOfHits);
            return this;
        }

        /**
         * Setter
         */
        public ContactSketch.Builder setMaxRiskValue(int maxRiskValue) {
            this.maxRiskValue = ParamsRangeChecker.checkRiskValueValid(maxRiskValue);
            return this;
        }

        /**
         * Setter
         */
        public ContactSketch.Builder setSummationRiskValue(int summationRiskValue) {
            this.summationRiskValue = ParamsRangeChecker.checkNonNegative(summationRiskValue);
            return this;
        }

        /**
         * Setter
         */
        public ContactSketch.Builder setAttenuationDurations(int[] attenDurations) {
            if (attenDurations == null || attenDurations.length != 3) {
                return this;
            }
            ParamsRangeChecker.checkAttenuationDurationsValid(attenDurations);
            System.arraycopy(attenDurations, 0, this.attenuationDurations, 0, 3);
            return this;
        }
    }
}
