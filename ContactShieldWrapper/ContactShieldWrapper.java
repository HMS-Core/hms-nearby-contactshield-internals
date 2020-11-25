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

package com.huawei.hms.contactshield.contactshieldwrapper;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration;
import com.google.android.gms.nearby.exposurenotification.ExposureInformation;
import com.google.android.gms.nearby.exposurenotification.ExposureSummary;
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.contactshield.ContactDetail;
import com.huawei.hms.contactshield.ContactShield;
import com.huawei.hms.contactshield.ContactShieldEngine;
import com.huawei.hms.contactshield.ContactShieldSetting;
import com.huawei.hms.contactshield.ContactSketch;
import com.huawei.hms.contactshield.DiagnosisConfiguration;
import com.huawei.hms.contactshield.PeriodicKey;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ContactShieldWrapper {
    private static final String TAG = "ContactShieldWrapper";

    private static volatile ContactShieldWrapper instance;

    private WeakReference<Context> mContextWeakRef;

    private ContactShieldEngine mContactShieldEngine;

    private ContactShieldWrapper(Context context) {
        mContactShieldEngine = ContactShield.getContactShieldEngine(context);
        mContextWeakRef = new WeakReference<>(context);
    }

    public static ContactShieldWrapper getInstance(Context context) {
        if (instance == null) {
            synchronized (ContactShieldWrapper.class) {
                if (instance == null) {
                    instance = new ContactShieldWrapper(context);
                }
            }
        }
        return instance;
    }

    public Task<Void> start() {
        return mContactShieldEngine.startContactShield(ContactShieldSetting.DEFAULT);
    }

    public Task<Void> stop() {
        return mContactShieldEngine.stopContactShield();
    }

    public Task<Boolean> isEnabled() {
        return mContactShieldEngine.isContactShieldRunning();
    }

    public Task<Void> provideDiagnosisKeys(List<File> files, ExposureConfiguration configuration, String token) {
        DiagnosisConfiguration diagnosisConfiguration = new DiagnosisConfiguration.Builder()
                .setMinimumRiskValueThreshold(configuration.getMinimumRiskScore())
                .setAttenuationRiskValues(configuration.getAttenuationScores())
                .setDaysAfterContactedRiskValues(configuration.getDaysSinceLastExposureScores())
                .setDurationRiskValues(configuration.getDurationScores())
                .setInitialRiskLevelRiskValues(configuration.getTransmissionRiskScores())
                .setAttenuationDurationThresholds(configuration.getDurationAtAttenuationThresholds())
                .build();
        Context context = mContextWeakRef.get();
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                new Intent(context, BackgroundContactShieldIntentService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        return mContactShieldEngine.putSharedKeyFiles(pendingIntent, files, diagnosisConfiguration, token);
    }

    public Task<List<PeriodicKey>> getTemporaryExposureKeyHistory() {
        return mContactShieldEngine.getPeriodicKey();
    }

    public Task<ContactSketch> getExposureSummary(String token) {
        return mContactShieldEngine.getContactSketch(token);
    }

    public Task<List<ContactDetail>> getExposureInformation(String token) {
        return mContactShieldEngine.getContactDetail(token);
    }

    public static List<TemporaryExposureKey> getTemporaryExposureKeyList(List<PeriodicKey> periodicKeyList) {
        List<TemporaryExposureKey> temporaryExposureKeyList = new ArrayList<>();
        for (PeriodicKey periodicKey:periodicKeyList) {
            TemporaryExposureKey temporaryExposureKey =
                    new TemporaryExposureKey.TemporaryExposureKeyBuilder()
                            .setKeyData(periodicKey.getContent())
                            .setRollingStartIntervalNumber((int) periodicKey.getPeriodicKeyValidTime())
                            .setRollingPeriod((int) periodicKey.getPeriodicKeyLifeTime())
                            .setTransmissionRiskLevel(periodicKey.getInitialRiskLevel())
                            .build();
            temporaryExposureKeyList.add(temporaryExposureKey);
        }
        return temporaryExposureKeyList;
    }

    public static ExposureSummary getExposureSummary(ContactSketch contactSketch) {
        return new ExposureSummary.ExposureSummaryBuilder()
                .setDaysSinceLastExposure(contactSketch.getDaysSinceLastHit())
                .setMatchedKeyCount(contactSketch.getNumberOfHits())
                .setMaximumRiskScore(contactSketch.getMaxRiskValue())
                .setSummationRiskScore(contactSketch.getSummationRiskValue())
                .setAttenuationDurations(contactSketch.getAttenuationDurations())
                .build();
    }

    public static List<ExposureInformation> getExposureInformationList(List<ContactDetail> contactDetailList) {
        List<ExposureInformation> exposureInformationList = new ArrayList<>();
        for (ContactDetail detail : contactDetailList) {
            ExposureInformation exposureInformation =
                    new ExposureInformation.ExposureInformationBuilder()
                            .setDateMillisSinceEpoch(detail.getDayNumber())
                            .setAttenuationValue(detail.getAttenuationRiskValue())
                            .setTransmissionRiskLevel(detail.getInitialRiskLevel())
                            .setDurationMinutes(detail.getDurationMinutes())
                            .setAttenuationDurations(detail.getAttenuationDurations())
                            .setTotalRiskScore(detail.getTotalRiskValue())
                            .build();
            exposureInformationList.add(exposureInformation);
        }
        return exposureInformationList;
    }
}
