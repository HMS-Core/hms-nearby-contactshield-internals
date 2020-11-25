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

import com.huawei.hms.samples.contactshield.contact.RiskLevel;

import java.util.List;
import java.util.Locale;

/**
 * ParamsRangeChecker
 *
 * @since 2020-05-09
 */
public class ParamsRangeChecker {
    private static final String TAG = "ParamsRangeChecker";

    /**
     * Max attenuationRiskValue
     */
    public static final int ATTEN_MAX = 255;

    /**
     * The actual attenuation range is 0 ~ 255
     */
    public static final int ATTEN_REAL_VALUE_MAX = 255;

    /**
     * max diagnose period incubationPeriod
     */
    public static final int INCUBATION_PERIOD_MAX = 60;

    /**
     * max risk value
     */
    private static final int RISK_VALUE_MAX = 4096;

    /**
     * checkRiskLevelValid
     */
    public static int checkRiskLevelValid(int riskLevel) {
        if (riskLevel < RiskLevel.RISK_LEVEL_INVALID ||
                riskLevel > RiskLevel.RISK_LEVEL_HIGHEST) {
            ContactShieldLog.e(TAG, "Risk level must between 0 and 8");
            return riskLevel < 0 ? RiskLevel.RISK_LEVEL_INVALID : RiskLevel.RISK_LEVEL_HIGHEST;
        }
        return riskLevel;
    }

    /**
     * checkIsRiskLevelValid
     */
    public static boolean checkIsRiskLevelValid(int riskLevel) {
        if (riskLevel < RiskLevel.RISK_LEVEL_INVALID ||
                riskLevel > RiskLevel.RISK_LEVEL_HIGHEST) {
            ContactShieldLog.e(TAG, "Risk level must between 0 and 8");
            return false;
        }
        return true;
    }

    /**
     * checkRiskValueValid
     *
     * @param riskValue Value be checked
     * @return valid value
     */
    public static int checkRiskValueValid(int riskValue) {
        if (riskValue < 0 || riskValue > RISK_VALUE_MAX) {
            ContactShieldLog.e(TAG, "Risk level must between 0 and " + RISK_VALUE_MAX);
            return riskValue < 0 ? 0 : RISK_VALUE_MAX;
        }
        return riskValue;
    }

    /**
     * checkRiskLevelArrayValid
     */
    public static int[] checkRiskLevelArrayValid(int[] riskLevelArray) {
        if (riskLevelArray == null || riskLevelArray.length != 8) {
            ContactShieldLog.e(TAG, "Set null array or invalid length");
            return new int[8];
        }
        for (int i = 0; i < riskLevelArray.length; ++i) {
            riskLevelArray[i] = checkRiskLevelValid(riskLevelArray[i]);
        }
        return riskLevelArray;
    }

    /**
     * checkRiskLevelValid
     */
    public static int checkSummationRiskLevelValid(int riskLevel) {
        if (riskLevel < 0) {
            ContactShieldLog.e(TAG, "riskLevel < 0");
            return 0;
        }
        return riskLevel;
    }

    /**
     * checkRiskLevelArrayValid
     */
    public static void checkAttenuationDurationsValid(int[] attenDurations) {
        for (int i = 0; i < attenDurations.length; i++) {
            if (attenDurations[i] > GlobalSettings.DURATION_MAX_MINUTES || attenDurations[i] < 0) {
                ContactShieldLog.e(TAG, "Params must between 0 and " + GlobalSettings.DURATION_MAX_MINUTES);
            }
            attenDurations[i] = Math.max(Math.min(attenDurations[i], GlobalSettings.DURATION_MAX_MINUTES), 0);
        }
    }

    /**
     * checkRiskLevelValid
     */
    public static int checkAttenuationRiskValueValid(int attenuationRiskValue) {
        if (attenuationRiskValue > ATTEN_MAX || attenuationRiskValue < 0) {
            ContactShieldLog.e(TAG, "Params must between 0 and " + ATTEN_MAX);
            return attenuationRiskValue < 0 ? 0 : ATTEN_MAX;
        }
        return attenuationRiskValue;
    }

    /**
     * checkRiskLevelArrayValid
     */
    public static int[] checkNormalizeArrayValid(int[] nArray) {
        return checkRiskLevelArrayValid(nArray);
    }

    /**
     * checkNonNegative
     */
    public static int checkNonNegative(int digit) {
        if (digit < 0) {
            ContactShieldLog.e(TAG, "Params must be nonegtive");
            return 0;
        }
        return digit;
    }

    /**
     * checkNonNegative
     */
    public static long checkNonNegative(long digit) {
        if (digit < 0) {
            ContactShieldLog.e(TAG, "Params must be nonegtive");
            return 0;
        }
        return digit;
    }

    /**
     * checkIsNegative
     */
    public static boolean checkIsNegative(long digit) {
        if (digit < 0) {
            ContactShieldLog.e(TAG, "Params must be nonegtive");
            return true;
        }
        return false;
    }

    /**
     * checkWeight
     */
    public static int checkWeight(int digit) {
        if (digit < 0 || digit > 100) {
            ContactShieldLog.e(TAG, "Weight must between 0 and 100");
            return digit < 0 ? 0 : 100;
        }
        return digit;
    }

    /**
     * check not null
     */
    public static boolean checkByteArrayValid(byte[] btArray) {
        if (btArray == null || btArray.length != 16) {
            ContactShieldLog.e(TAG, "Set null array or invalid length");
            return false;
        }
        return true;
    }

    /**
     * checkIncubationPeriodValid
     */
    public static int checkIncubationPeriodValid(int incubationPeriod) {
        if (incubationPeriod < 1 || incubationPeriod > INCUBATION_PERIOD_MAX) {
            ContactShieldLog.e(TAG, "IncubationPeriod must between 1 and " + INCUBATION_PERIOD_MAX);
            return incubationPeriod < 1 ? 1 : INCUBATION_PERIOD_MAX;
        }
        return incubationPeriod;
    }

    /**
     * check list not null
     */
    public static boolean checkListNotNull(List<?> list) {
        if (list == null) {
            ContactShieldLog.e(TAG, "list cannot be null");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Check if argument is in range
     */
    public static boolean checkArgumentRange(int arg, int min, int max, String str) {
        if (arg < min || arg > max) {
            ContactShieldLog.e(TAG, String.format(Locale.ENGLISH, str, arg, min, max));
            return false;
        }
        return true;
    }

    /**
     * Check if argument is not null
     */
    public static <T> boolean checkNotNull(T arg, String str, Object... objArr) {
        if (arg == null) {
            ContactShieldLog.e(TAG, String.format(Locale.ENGLISH, str, objArr));
            return false;
        }
        return true;
    }

    /**
     * Check if argument is true
     */
    public static boolean checkArgument(boolean arg, String str, Object... objArr) {
        if (!arg) {
            ContactShieldLog.e(TAG, String.format(Locale.ENGLISH, str, objArr));
            return false;
        }
        return true;
    }
}
