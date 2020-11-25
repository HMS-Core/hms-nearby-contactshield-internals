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

import java.util.Arrays;

/**
 * Diagnosis configuration
 *
 * @since 2020-07-02
 */
public class DiagnosisConfiguration {
    private final int mMinimumRiskValueThreshold;
    private final int[] mAttenuationRiskValues;
    private final int mAttenuationWight;
    private final int[] mDaysAfterContactedRiskValues;
    private final int mDaysAfterContactedWeight;
    private final int[] mDurationRiskValues;
    private final int mDurationWeight;
    private final int[] mInitialRiskLevelRiskValues;
    private final int mInitialRiskLevelWeight;
    private final int[] mAttenuationDurationThresholds;

    private DiagnosisConfiguration(int minimumRiskValueThreshold,
        int[] attenuationRiskValues, int attenuationWight,
        int[] daysAfterContactedRiskValues, int daysAfterContactedWeight,
        int[] durationRiskValues, int durationWeight,
        int[] initialRiskLevelRiskValues, int initialRiskLevelWeight,
        int[] attenuationDurationThresholds) {
        mMinimumRiskValueThreshold = minimumRiskValueThreshold;
        mAttenuationRiskValues = copyArray(attenuationRiskValues);
        mAttenuationWight = attenuationWight;
        mDaysAfterContactedRiskValues = copyArray(daysAfterContactedRiskValues);
        mDaysAfterContactedWeight = daysAfterContactedWeight;
        mDurationRiskValues = copyArray(durationRiskValues);
        mDurationWeight = durationWeight;
        mInitialRiskLevelRiskValues = copyArray(initialRiskLevelRiskValues);
        mInitialRiskLevelWeight = initialRiskLevelWeight;
        mAttenuationDurationThresholds = copyArray(attenuationDurationThresholds);
    }

    /**
     * Getter
     */
    public int getMinimumRiskValueThreshold() {
        return mMinimumRiskValueThreshold;
    }

    private static int[] copyArray(int[] src) {
        int[] grades = new int[src.length];
        System.arraycopy(src, 0, grades, 0, src.length);
        return grades;
    }

    /**
     * Getter
     */
    public int[] getAttenuationRiskValues() {
        return copyArray(mAttenuationRiskValues);
    }

    /**
     * Getter
     */
    public int[] getDaysAfterContactedRiskValues() {
        return copyArray(mDaysAfterContactedRiskValues);
    }

    /**
     * Getter
     */
    public int[] getDurationRiskValues() {
        return copyArray(mDurationRiskValues);
    }

    /**
     * Getter
     */
    public int[] getInitialRiskLevelRiskValues() {
        return copyArray(mInitialRiskLevelRiskValues);
    }

    /**
     * Getter
     */
    public int[] getAttenuationDurationThresholds() {
        return copyArray(mAttenuationDurationThresholds);
    }

    @Override
    public String toString() {
        return "DiagnosisConfiguration: MinimumRiskValueThreshold = " + mMinimumRiskValueThreshold +
                ", attenuationRiskValues = " + Arrays.toString(mAttenuationRiskValues) +
                ", attenuationWight = " + mAttenuationWight +
                ", daysAfterContactedRiskValues = " + Arrays.toString(mDaysAfterContactedRiskValues) +
                ", daysAfterContactedWeight = " + mDaysAfterContactedWeight +
                ", durationRiskValues = " + Arrays.toString(mDurationRiskValues) +
                ", durationWeight = " + mDurationWeight +
                ", initialRiskLevelRiskValues = " + Arrays.toString(mInitialRiskLevelRiskValues) +
                ", initialRiskLevelWeight =" + mInitialRiskLevelWeight +
                ", attenuationDurationThresholds = " + Arrays.toString(mAttenuationDurationThresholds);
    }

    /**
     * Builder for DiagnosisConfiguration
     */
    public static class Builder {
        private static final int DEFAULT_MIN_RISK_VALUE_TRESHOLD = 1;
        private static final int MIN_RISK_VALUE_THRESHOLD_LOWER_BOUND = 1;
        private static final int MIN_RISK_VALUE_THRESHOLD_UPPER_BOUND = 4096;

        private static final int RISK_VALUES_COUNT = 8;
        private static final int DEFAULT_RISK_VALUE = 4;
        private static final int MIN_RISK_VALUE = 0;
        private static final int MAX_RISK_VALUE = 8;

        private static final int DEFAULT_WEIGHT_VALUE = 50;

        private static final int ATTENUATION_DURATION_THRESHOLDS_COUNT = 2;
        private static final int DEFAULT_MIN_ATTENUATION_DURATION_THRESHOLD = 50;
        private static final int DEFAULT_MAX_ATTENUATION_DURATION_THRESHOLD = 74;
        private static final int ATTENUATION_DURATION_THRESHOLD_LOWER_BOUND = 0;
        private static final int ATTENUATION_DURATION_THRESHOLD_UPPER_BOUND = 255;

        private int mMinimumRiskValueThreshold;
        private int[] mAttenuationRiskValues;
        private int mAttenuationWight;
        private int[] mDaysAfterContactedRiskValues;
        private int mDaysAfterContactedWeight;
        private int[] mDurationRiskValues;
        private int mDurationWeight;
        private int[] mInitialRiskLevelRiskValues;
        private int mInitialRiskLevelWeight;
        private int[] mAttenuationDurationThresholds;

        /**
         * Constructor for builder
         */
        public Builder() {
            mMinimumRiskValueThreshold = DEFAULT_MIN_RISK_VALUE_TRESHOLD;

            mAttenuationRiskValues = new int[RISK_VALUES_COUNT];
            Arrays.fill(mAttenuationRiskValues, DEFAULT_RISK_VALUE);
            mAttenuationWight = DEFAULT_WEIGHT_VALUE;

            mDaysAfterContactedRiskValues = new int[RISK_VALUES_COUNT];
            Arrays.fill(mDaysAfterContactedRiskValues, DEFAULT_RISK_VALUE);
            mDaysAfterContactedWeight = DEFAULT_WEIGHT_VALUE;

            mDurationRiskValues = new int[RISK_VALUES_COUNT];
            Arrays.fill(mDurationRiskValues, DEFAULT_RISK_VALUE);
            mDurationWeight = DEFAULT_WEIGHT_VALUE;

            mInitialRiskLevelRiskValues = new int[RISK_VALUES_COUNT];
            Arrays.fill(mInitialRiskLevelRiskValues, DEFAULT_RISK_VALUE);
            mInitialRiskLevelWeight = DEFAULT_WEIGHT_VALUE;

            mAttenuationDurationThresholds = new int[ATTENUATION_DURATION_THRESHOLDS_COUNT];
            mAttenuationDurationThresholds[0] = DEFAULT_MIN_ATTENUATION_DURATION_THRESHOLD;
            mAttenuationDurationThresholds[1] = DEFAULT_MAX_ATTENUATION_DURATION_THRESHOLD;
        }

        /**
         * Setter
         */
        public Builder setMinimumRiskValueThreshold(int minimumRiskValueThreshold) {
            if (ParamsRangeChecker.checkArgument(
                minimumRiskValueThreshold >= MIN_RISK_VALUE_THRESHOLD_LOWER_BOUND &&
                        minimumRiskValueThreshold <= MIN_RISK_VALUE_THRESHOLD_UPPER_BOUND,
                "minimumRiskValueThreshold(=%d) is not in valid range %d ~ %d inclusive",
                    minimumRiskValueThreshold, MIN_RISK_VALUE_THRESHOLD_LOWER_BOUND,
                    MIN_RISK_VALUE_THRESHOLD_UPPER_BOUND)) {
                mMinimumRiskValueThreshold = minimumRiskValueThreshold;
            }
            return this;
        }

        private boolean checkGrades(int[] grades, String message) {
            if (!ParamsRangeChecker.checkArgument(grades != null, "%s must be not null", message)) {
                return false;
            }

            if (!ParamsRangeChecker.checkArgument(grades.length == RISK_VALUES_COUNT,
                    "%s.length(=%d) must contains %d elements", message, grades.length, RISK_VALUES_COUNT)) {
                return false;
            }
            for (int i = 0; i < grades.length; i++) {
                if (!ParamsRangeChecker.checkArgument(grades[i] >= MIN_RISK_VALUE && grades[i] <= MAX_RISK_VALUE,
                    "%s[%d](=%d) is not in valid range %d ~ %d inclusive",
                    message, i, grades[i], MIN_RISK_VALUE, MAX_RISK_VALUE)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Setter
         */
        public Builder setAttenuationRiskValues(int... attenuationRiskValues) {
            if (checkGrades(attenuationRiskValues, "attenuationRiskValues")) {
                mAttenuationRiskValues = copyArray(attenuationRiskValues);
            }
            return this;
        }

        /**
         * Setter
         */
        public Builder setDaysAfterContactedRiskValues(int... daysAfterContactedRiskValues) {
            if (checkGrades(daysAfterContactedRiskValues, "daysAfterContactedRiskValues")) {
                mDaysAfterContactedRiskValues = copyArray(daysAfterContactedRiskValues);
            }
            return this;
        }

        /**
         * Setter
         */
        public Builder setDurationRiskValues(int... durationRiskValues) {
            if (checkGrades(durationRiskValues, "durationRiskValues")) {
                mDurationRiskValues = copyArray(durationRiskValues);
            }
            return this;
        }

        /**
         * Setter
         */
        public Builder setInitialRiskLevelRiskValues(int... initialRiskLevelRiskValues) {
            if (checkGrades(initialRiskLevelRiskValues, "InitialRiskLevelRiskValues")) {
                mInitialRiskLevelRiskValues = copyArray(initialRiskLevelRiskValues);
            }
            return this;
        }

        /**
         * Setter
         */
        public Builder setAttenuationDurationThresholds(int... attenuationDurationThresholds) {
            if (!ParamsRangeChecker.checkNotNull(attenuationDurationThresholds,
                    "attenuationDurationThresholds must not be null")) {
                return this;
            }
            if (!ParamsRangeChecker.checkArgument(
                    attenuationDurationThresholds.length == ATTENUATION_DURATION_THRESHOLDS_COUNT,
                "attenuationDurationThresholds.length(=%d) must contains %d elements",
                attenuationDurationThresholds.length, ATTENUATION_DURATION_THRESHOLDS_COUNT)) {
                return this;
            }
            for (int i = 0; i < attenuationDurationThresholds.length; i++) {
                if (!ParamsRangeChecker.checkArgument(
                        attenuationDurationThresholds[i] >= ATTENUATION_DURATION_THRESHOLD_LOWER_BOUND
                        && attenuationDurationThresholds[i] <= ATTENUATION_DURATION_THRESHOLD_UPPER_BOUND,
                    "attenuationDurationThresholds[%d](=%d) is not in valid range %d ~ %d inclusive",
                    i, attenuationDurationThresholds[i], ATTENUATION_DURATION_THRESHOLD_LOWER_BOUND,
                    ATTENUATION_DURATION_THRESHOLD_UPPER_BOUND)) {
                    return this;
                }
            }
            if (!ParamsRangeChecker.checkArgument(attenuationDurationThresholds[0] <= attenuationDurationThresholds[1],
                    "attenuationDurationThresholds[0](=%d) must be <= attenuationDurationThresholds[1](=%d)",
                    attenuationDurationThresholds[0], attenuationDurationThresholds[1])) {
                return this;
            }
            mAttenuationDurationThresholds = copyArray(attenuationDurationThresholds);
            return this;
        }

        /**
         * Generate DiagnosisConfiguration
         */
        public DiagnosisConfiguration build() {
            return new DiagnosisConfiguration(mMinimumRiskValueThreshold,
                    mAttenuationRiskValues, mAttenuationWight,
                    mDaysAfterContactedRiskValues, mDaysAfterContactedWeight,
                    mDurationRiskValues, mDurationWeight,
                    mInitialRiskLevelRiskValues, mInitialRiskLevelWeight,
                    mAttenuationDurationThresholds);
        }
    }
}
