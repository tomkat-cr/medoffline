/*
 * Copyright (c) Carlos J. Ramirez, and affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.example.medoffline;

import android.content.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AssetFileReader {

    private final Context context;
    private final String fileName;
    private String error = "";

    public AssetFileReader(Context context, String fileName) {
        this.context = context;
        this.fileName = fileName;
    }

    public String read() {
        String content;
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            content = stringBuilder.toString();
            reader.close();
        } catch (IOException e) {
            error = e.getMessage();
            // e.printStackTrace();
            content = "";
        }
        return content;
    }

    public String getError() {
        return error;
    }
}
