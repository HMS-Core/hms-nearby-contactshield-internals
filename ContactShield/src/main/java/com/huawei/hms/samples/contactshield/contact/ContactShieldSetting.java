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
 * ContactShieldSetting
 *
 * @since 2020-05-06
 */
public class ContactShieldSetting {
    /**
     * Default option
     */
    public static final ContactShieldSetting DEFAULT = new ContactShieldSetting.Builder().build();

    private int incubationPeriod;

    private ContactShieldSetting(ContactShieldSetting.Builder builder) {
        this.incubationPeriod = builder.incubationPeriod;
    }

    /**
     * Getter
     */
    public int getIncubationPeriod() {
        return incubationPeriod;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "ContactShieldSetting<incubationPeriod: %d>",
                this.incubationPeriod);
    }

    /**
     * ContactShieldSetting Builder
     */
    public static class Builder {
        // default incubation period is 14 days
        private int incubationPeriod = 14;

        /**
         * Build ContactShieldSetting
         */
        public ContactShieldSetting build() {
            return new ContactShieldSetting(this);
        }

        /**
         * Setter
         */
        public ContactShieldSetting.Builder setIncubationPeriod(int incubationPeriod) {
            this.incubationPeriod = ParamsRangeChecker.checkIncubationPeriodValid(incubationPeriod);
            return this;
        }
    }
}
