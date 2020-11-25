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

/**
 * Calibration
 *
 * @since 2020-07-01
 */
public class Calibration {
    /**
     * Default option
     */
    public static final Calibration DEFAULT = new Calibration(null, null, -5, -19, 1);

    private String mOem;
    private String mModel;
    private int mRssiCorrection;
    private int mTx;
    private int mCalibrationConfidence;

    public Calibration(String oem, String model, int rssiCorrection, int tx, int calibrationConfidence) {
        mOem = oem;
        mModel = model;
        mRssiCorrection = rssiCorrection;
        mTx = tx;
        mCalibrationConfidence = calibrationConfidence;
    }

    /**
     * getModel
     *
     * @return mModel
     */
    public String getModel() {
        return mModel;
    }

    /**
     * getModel
     *
     * @return mModel
     */
    public int getTx() {
        return mTx;
    }

    public int getRssiCorrection() {
        return mRssiCorrection;
    }

    @Override
    public String toString() {
        return "mOem:" + mOem + ", mModel:" + mModel + ", mRssiCorrection:" + mRssiCorrection
                + ", mTx:" + mTx + ", mCalibrationConfidence:" + mCalibrationConfidence;
    }
}
