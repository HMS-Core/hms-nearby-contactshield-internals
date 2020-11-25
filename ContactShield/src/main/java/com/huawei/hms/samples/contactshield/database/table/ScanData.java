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

package com.huawei.hms.samples.contactshield.database.table;

/**
 * ScanData
 *
 * @since 2019-05-28
 */
public class ScanData {
    private int id;
    private long intervalNum;
    private byte[] dsc = new byte[0];
    private byte[] sd = new byte[0];
    /* Represent max RSSI during ScanInfo after v1.5 */
    private int rssi;
    private int averageRssi;
    private int secondsSinceLastScan;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getIntervalNum() {
        return intervalNum;
    }

    public void setIntervalNum(long intervalNum) {
        this.intervalNum = intervalNum;
    }

    public byte[] getDsc() {
        return dsc.clone();
    }

    public void setDsc(byte[] dsc) {
        this.dsc = dsc.clone();
    }

    public byte[] getSd() {
        return sd.clone();
    }

    public void setSd(byte[] sd) {
        this.sd = sd.clone();
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getAverageRssi() {
        return averageRssi;
    }

    public void setAverageRssi(int averageRssi) {
        this.averageRssi = averageRssi;
    }

    public int getSecondsSinceLastScan() {
        return secondsSinceLastScan;
    }

    public void setSecondsSinceLastScan(int secondsSinceLastScan) {
        this.secondsSinceLastScan = secondsSinceLastScan;
    }
}
