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

/**
 * RiskLevel
 *
 * @since 2020-05-06
 */
public @interface RiskLevel {
    int RISK_LEVEL_INVALID = 0;
    int RISK_LEVEL_LOWEST = 1;
    int RISK_LEVEL_LOW = 2;
    int RISK_LEVEL_MEDIUM_LOW = 3;
    int RISK_LEVEL_MEDIUM = 4;
    int RISK_LEVEL_MEDIUM_HIGH = 5;
    int RISK_LEVEL_HIGH = 6;
    int RISK_LEVEL_EXT_HIGH = 7;
    int RISK_LEVEL_HIGHEST = 8;
}
