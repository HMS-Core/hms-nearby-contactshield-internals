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

package com.huawei.hms.samples.contactshield.util;

import com.huawei.hms.samples.contactshield.contact.DiagnosisConfiguration;

/**
 * RiskScoreCalculator
 *
 * @since 2020-05-08
 */
public class RiskScoreCalculator {
    private static final int MAX_TOTAL_RISK_VALUE = 4096;

    private static final int MAX_RISK_VALUES_INDEX = 7;

    private static final int MINUTES_PER_DURATION = 5;

    private static final int DAYS_PER_RANGE = 2;

    private DiagnosisConfiguration mDiagnosisConfiguration;

    public RiskScoreCalculator(DiagnosisConfiguration diagnosisConfiguration) {
        mDiagnosisConfiguration = diagnosisConfiguration;
    }

    /**
     * calculate risk level
     *
     * @return risk level
     */
    public int calcTotalRiskValue(int attenuation, int daysSinceLastExposure, int duration, int transmissionRisk) {
        int transRiskScore = ParamsRangeChecker.checkRiskLevelValid(transmissionRisk)
                <= 1 ? 0 : transmissionRisk - 1;
        int totalRiskValue = mDiagnosisConfiguration.getAttenuationRiskValues()[getAttenuationIndex(attenuation)]
                * mDiagnosisConfiguration.getDaysAfterContactedRiskValues()[getDaysExposureIndex(daysSinceLastExposure)]
                * mDiagnosisConfiguration.getDurationRiskValues()[getDurationIndex(duration)]
                * mDiagnosisConfiguration.getInitialRiskLevelRiskValues()[transRiskScore];

        totalRiskValue = Math.min(Math.max(totalRiskValue, 0), MAX_TOTAL_RISK_VALUE);
        return totalRiskValue;
    }

    /**
     * Get index according to attenuation value:
     *   0 when Attenuation > 73
     *   1 when 73 >= Attenuation > 63
     *   2 when 63 >= Attenuation > 51
     *   3 when 51 >= Attenuation > 33
     *   4 when 33 >= Attenuation > 27
     *   5 when 27 >= Attenuation > 15
     *   6 when 15 >= Attenuation > 10
     *   7 when 10 >= Attenuation
     */
    private static int getAttenuationIndex(int attenuationScore) {
        int val = ParamsRangeChecker.checkNonNegative(attenuationScore);
        if (val > 73) {
            return 0;
        } else if (val > 63) {
            return 1;
        } else if (val > 51) {
            return 2;
        } else if (val > 33) {
            return 3;
        } else if (val > 27) {
            return 4;
        } else if (val > 15) {
            return 5;
        } else if (val > 10) {
            return 6;
        } else {
            return 7;
        }
    }

    /**
     * Get index according to contact duration:
     *   0 when Duration == 0
     *   1 when Duration <= 5
     *   2 when Duration <= 10
     *   3 when Duration <= 15
     *   4 when Duration <= 20
     *   5 when Duration <= 25
     *   6 when Duration <= 30
     *   7 when Duration > 30
     */
    private static int getDurationIndex(int durationScore) {
        int val = ParamsRangeChecker.checkNonNegative(durationScore);
        int durationIndex = val / MINUTES_PER_DURATION;
        if (val % MINUTES_PER_DURATION == 0) {
            return Math.min(durationIndex, MAX_RISK_VALUES_INDEX);
        } else {
            return Math.min(durationIndex + 1, MAX_RISK_VALUES_INDEX);
        }
    }

    /**
     * Get index according to days after contacted:
     *   0 when Days >= 14
     *   1 when Days >= 12
     *   2 when Days >= 10
     *   3 when Days >= 8
     *   4 when Days >= 6
     *   5 when Days >= 4
     *   6 when Days >= 2
     *   7 when Days >= 0
     */
    private static int getDaysExposureIndex(int exporeDays) {
        int val = ParamsRangeChecker.checkNonNegative(exporeDays);
        int result = MAX_RISK_VALUES_INDEX - val / DAYS_PER_RANGE;
        if (result < 0) {
            return 0;
        }
        if (result > MAX_RISK_VALUES_INDEX) {
            return MAX_RISK_VALUES_INDEX;
        }
        return result;
    }
}
