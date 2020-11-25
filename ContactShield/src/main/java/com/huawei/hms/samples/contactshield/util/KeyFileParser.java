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

import androidx.annotation.NonNull;

import com.huawei.hms.samples.TEKSignatureListOuterClass;
import com.huawei.hms.samples.TemporaryExposureKeyExportOuterClass;
import com.huawei.hms.samples.contactshield.contact.PeriodicKey;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class KeyFileParser {
    private static final String TAG = "KeyFileParser";
    private static final String SIG_FILENAME = "export.sig";
    private static final String EXPORT_FILENAME = "export.bin";
    private static final int READ_LINE_LENGTH = 16;
    private static final int STREAM_EOF = -1;
    private static final int STREAM_BUFFER_SIZE = 4096;

    public static List<PeriodicKey> parseFiles(File keyFile) {
        List<PeriodicKey> keyList = new ArrayList<>();
        FileContent fileContent = readFile(keyFile);
        if (fileContent == null) {
            return keyList;
        }
        for (TemporaryExposureKeyExportOuterClass.TemporaryExposureKey k : fileContent.export.getKeysList()) {
            // Discard key if invalid parameter is found.
            if (!checkFileKeyValid(k)) {
                continue;
            }
            PeriodicKey periodicKey = new PeriodicKey.Builder()
                    .setContent(k.getKeyData().toByteArray())
                    .setPeriodicKeyValidTime(k.getRollingStartIntervalNumber())
                    .setPeriodicKeyLifeTime(k.getRollingPeriod())
                    .setInitialRiskLevel(k.getTransmissionRiskLevel())
                    .setReportType(k.getReportType().getNumber())
                    .build();
            keyList.add(periodicKey);
        }

        return keyList;
    }

    private static byte[] inputStreamToByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        /* Memory usage to be optimised for large file */
        byte[] buffer = new byte[STREAM_BUFFER_SIZE];
        int n;
        while ((n = input.read(buffer)) != STREAM_EOF) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

    private static FileContent readFile(File file) {
        FileContent fileContent = null;
        ZipFile zip;
        try {
            zip = new ZipFile(file);
        } catch (IOException e) {
            ContactShieldLog.e(TAG, "ZIP file io exception");
            return null;
        }

        ZipEntry signatureEntry = zip.getEntry(SIG_FILENAME);
        ZipEntry exportEntry = zip.getEntry(EXPORT_FILENAME);

        try {
            if (signatureEntry != null && exportEntry != null) {
                byte[] sigData = inputStreamToByteArray(zip.getInputStream(signatureEntry));
                byte[] bodyData = inputStreamToByteArray(zip.getInputStream(exportEntry));

                if (bodyData.length > READ_LINE_LENGTH) {
                    byte[] header = Arrays.copyOf(bodyData, READ_LINE_LENGTH);
                    byte[] exportData = Arrays.copyOfRange(bodyData, READ_LINE_LENGTH, bodyData.length);

                    String headerString = new String(header, "UTF-8");
                    TEKSignatureListOuterClass.TEKSignatureList signature =
                            TEKSignatureListOuterClass.TEKSignatureList.parseFrom(sigData);
                    TemporaryExposureKeyExportOuterClass.TemporaryExposureKeyExport export =
                            TemporaryExposureKeyExportOuterClass.TemporaryExposureKeyExport.parseFrom(exportData);
                    fileContent = new FileContent(headerString, export, signature);
                }
            }
        } catch (IOException e) {
            ContactShieldLog.e(TAG, "Get input stream exception");
        }

        try {
            zip.close();
        } catch (IOException e) {
            ContactShieldLog.w(TAG, "Close zip file exception");
        }
        return fileContent;
    }

    private static boolean checkFileKeyValid(TemporaryExposureKeyExportOuterClass.TemporaryExposureKey key) {
        return ParamsRangeChecker.checkByteArrayValid(key.getKeyData().toByteArray()) &&
                !ParamsRangeChecker.checkIsNegative(key.getRollingStartIntervalNumber()) &&
                !ParamsRangeChecker.checkIsNegative(key.getRollingPeriod()) &&
                ParamsRangeChecker.checkIsRiskLevelValid(key.getTransmissionRiskLevel()) &&
                ParamsRangeChecker.checkArgumentRange(key.getReportType().getNumber(), 0 ,
                        GlobalSettings.REPORT_TYPE_MAX, "reportType is %s, must >=%s and <=%s");
    }

    private static class FileContent {
        private final String header;
        private final TemporaryExposureKeyExportOuterClass.TemporaryExposureKeyExport export;
        private final TEKSignatureListOuterClass.TEKSignatureList signature;

        FileContent(String header, TemporaryExposureKeyExportOuterClass.TemporaryExposureKeyExport export,
                    TEKSignatureListOuterClass.TEKSignatureList signature) {
            this.export = export;
            this.header = header;
            this.signature = signature;
        }

        @NonNull
        @Override
        public String toString() {
            return "FileContent[header=" + header +
                    ", TemporaryExposureKeyExport=" + export.toString() +
                    ", signature=" + signature.toString() + "]";
        }
    }
}
