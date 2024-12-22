/*
 * Copyright (c) Carlos J. Ramirez, and affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.example.medoffline;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelsConfig {
    private String modelConfigFile;
    private final Context context;
    private String error = "";

    private final String DEFAULT_JSON_FILENAME = "default_model_db.json";

    public ModelsConfig(Context context) {
        this.context = context;
        this.modelConfigFile = DEFAULT_JSON_FILENAME;
    }

    public ModelsConfig(Context context, String modelConfigFile) {
        this.context = context;
        if (modelConfigFile.isEmpty()) {
            modelConfigFile = DEFAULT_JSON_FILENAME;
        }
        this.modelConfigFile = modelConfigFile;
    }

    // Getters and setters
    public String getModelConfigFile() { return modelConfigFile; }
    public void setModelConfigFile(String modelConfigFile) { this.modelConfigFile = modelConfigFile; }
    public String getError() { return error; }

    public List<ModelInfo> getModelsList() {
        // Read Models From the JSON file in the Assets Folder and return them as a List
        List<ModelInfo> models = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open(modelConfigFile);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                ModelInfo model = new ModelInfo(
                    obj.getString("model_id"),
                    obj.getString("model_name"),
                    obj.getString("model_type"),
                    obj.getString("model_download_url"),
                    obj.getString("model_file_name"),
                    obj.getInt("model_file_size"),
                    obj.getString("tokenizer_file_name"),
                    obj.getInt("tokenizer_file_size"),
                    obj.getString("system_prompt")
                );
                models.add(model);
            }
        } catch (IOException | JSONException e) {
            error = e.getMessage();
            // e.printStackTrace();
            // throw new RuntimeException(e);
        }
        return models;
    }

    public Map<String, ModelInfo> getModelsMap() {
        Map<String, ModelInfo> modelsMap = new HashMap<>();
        try {
            InputStream is = context.getAssets().open(modelConfigFile);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String modelId = obj.getString("model_id");
                ModelInfo model = new ModelInfo(
                    modelId,
                    obj.getString("model_name"),
                    obj.getString("model_type"),
                    obj.getString("model_download_url"),
                    obj.getString("model_file_name"),
                    obj.getInt("model_file_size"),
                    obj.getString("tokenizer_file_name"),
                    obj.getInt("tokenizer_file_size"),
                    obj.getString("system_prompt")
                );
                modelsMap.put(modelId, model);
            }
        } catch (IOException | JSONException e) {
            error = e.getMessage();
            // e.printStackTrace();
            // throw new RuntimeException(e);
        }
        return modelsMap;
    }
}
