/*
 * Copyright (c) Carlos J. Ramirez, and affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.example.medoffline;

// import static java.lang.System.exit;
import static java.util.Locale.ENGLISH;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LoadModelFromUrl {
    private final File filesDir;
    private final Context context;

    private static final Boolean USE_MODEL_DEFAULT_PATH = false;
    private static final String MODEL_DEFAULT_PATH = "/data/local/tmp/llama";

    // private static final String MODEL_URL = "https://www.kaggle.com/api/v1/models/tomkatcr/llama3.2_3b_pte/pyTorch/executorch/2/download";
    // public final ModelType DEFAULT_MODEL_TYPE = ModelType.LLAMA_3_2;

    // public final Float DEFAULT_TEMPERATURE = 0.1f;
    private static final int DEFAULT_TIMEOUT_MINUTES = 10;

    private View downloadingModelText;
    private TextView downloadProgressText;
    private ProgressBar downloadProgressBar;

    private ErrorReporting mErrorReporting;
   
    public LoadModelFromUrl(Context context, File filesDir) {
        this.filesDir = filesDir;
        this.context = context;
        this.downloadingModelText = null;
        this.downloadProgressText = null;
        this.downloadProgressBar = null;
        mErrorReporting = new ErrorReporting(context);
    }

    public void setUiElements(View downloadingModelText, TextView downloadProgressText, ProgressBar downloadProgressBar) {
        this.downloadingModelText = downloadingModelText;
        this.downloadProgressText = downloadProgressText;
        this.downloadProgressBar = downloadProgressBar;
    }

    public String getBaseModelsPath() {
        if (USE_MODEL_DEFAULT_PATH) {
            return MODEL_DEFAULT_PATH;
        }
        return filesDir.getAbsolutePath() + "/llama";
    }

    public String getFirstModelFilePath(String resourcePath, String fileExtension) {
        File modelDir = new File(resourcePath);

        ETLogging.getInstance().log("Checking directory: " + modelDir);
        if (!modelDir.exists()) {
            ETLogging.getInstance().log("Not exist, creating it...");
            boolean isCreated = modelDir.mkdirs();
            if (!isCreated) {
                throw new RuntimeException("Failed to create directory: " + modelDir);
            } else {
                ETLogging.getInstance().log("Directory created successfully: " + modelDir);
            }
        }

        // Print the modelDir files
        ETLogging.getInstance().log("Files in " + resourcePath);
        File[] files = modelDir.listFiles();
        if (files != null) {
            for (File file : files) {
                ETLogging.getInstance().log("File: " + file.getName() + ", Size: " + file.length() + " bytes");
            }
        } else {
            ETLogging.getInstance().log("No files found in " + resourcePath);
        }

        File model = Arrays.stream(modelDir.listFiles())
                // .filter(file -> file.getName().endsWith(".pte"))
                .filter(file -> file.getName().endsWith(fileExtension))
                .findFirst()
                .orElse(null);
        String modelPath = null;
        if (model != null) {
            modelPath = model.getAbsolutePath();
            ETLogging.getInstance().log("getFirstModelFilePath | modelPath: " + modelPath);
        }
        return modelPath;
    }

    public void updateDownloadProgress(long bytesRead, long contentLength) {
        // runOnUiThread(() -> {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (contentLength > 0) {
                int progress = (int) (100 * bytesRead / contentLength);
                downloadProgressBar.setProgress(progress);
                downloadProgressText.setText(String.format(ENGLISH, "Downloading: %d%%", progress));
            } else {
                downloadProgressText
                        .setText(String.format(ENGLISH, "Downloading: %.2f MB", bytesRead / (1024.0 * 1024.0)));
            }
        });
    }

    // public String getWorkingModelFilePath(String resourcePath)
    //         throws InterruptedException, ExecutionException, IOException {
    //     File modelDir = new File(resourcePath);
    //     if (!modelDir.exists()) {
    //         boolean created = modelDir.mkdirs();
    //         if (!created) {
    //             throw new IOException("Failed to create directory: " + modelDir);
    //         }
    //     }

    //     // First check if we already have the model
    //     String modelPath = getFirstModelFilePath(resourcePath, ".pte");
    //     if (modelPath != null) {
    //         Log.i("MainActivity", "Found existing model at: " + modelPath);
    //         return modelPath;
    //     }

    //     // Set up progress tracking
    //     // runOnUiThread(() -> {
    //     new Handler(Looper.getMainLooper()).post(() -> {
    //         if (downloadProgressBar != null)
    //             downloadProgressBar.setVisibility(View.VISIBLE);
    //         if (downloadProgressText != null)
    //             downloadProgressText.setVisibility(View.VISIBLE);
    //         if (downloadingModelText != null)
    //             downloadingModelText.setVisibility(View.VISIBLE);
    //     });

    //     try {
    //         // Set up progress listener
    //         LocalModelManagement.setDownloadProgressListener(new LocalModelManagement.DownloadProgressListener() {
    //             @Override
    //             public void onProgressUpdate(long bytesRead, long contentLength, boolean done) {
    //                 updateDownloadProgress(bytesRead, contentLength);
    //             }

    //             @Override
    //             public void onError(String error) {
    //                 mErrorReporting.showError(error);
    //             }
    //         });

    //         // Download and extract model
    //         String tarGzPath = resourcePath + "/model.tar.gz";
    //         LocalModelManagement.downloadZipFile(context, MODEL_URL, tarGzPath, DEFAULT_TIMEOUT_MINUTES);
    //         LocalModelManagement.unzipGz(tarGzPath, resourcePath);

    //         // Check if model was extracted successfully
    //         modelPath = getFirstModelFilePath(resourcePath, ".pte");
    //         if (modelPath == null) {
    //             throw new IOException("Model file not found after extraction");
    //         }

    //         return modelPath;

    //     } catch (Exception e) {
    //         Log.e("MainActivity", "Error during model setup", e);
    //         throw e;
    //     } finally {
    //         // runOnUiThread(() -> {
    //         new Handler(Looper.getMainLooper()).post(() -> {
    //             if (downloadProgressBar != null)
    //                 downloadProgressBar.setVisibility(View.GONE);
    //             if (downloadProgressText != null)
    //                 downloadProgressText.setVisibility(View.GONE);
    //             if (downloadingModelText != null)
    //                 downloadingModelText.setVisibility(View.GONE);
    //         });
    //     }
    // }

    public SettingsFields setDefaultParameters() {
        // Set default values for settings

        ModelType DEFAULT_MODEL_TYPE = ModelType.LLAMA_3_2;
        BackendType DEFAULT_BACKEND_TYPE = BackendType.XNNPACK;
        Float DEFAULT_TEMPERATURE = 0.1f;

        // Initialize settings
        SettingsFields mCurrentSettingsFields = new SettingsFields();
        mCurrentSettingsFields.SetInitValues();

        String resourcePath = getBaseModelsPath();

        String defaultModeModelDownloadUrl = "";
        String defaultModelPath = "";
        String defaultTokenizerPath = "";

        // Get the system prompt from the "default_system_prompt.txt" file
        AssetFileReader mAssetFileReader = new AssetFileReader(context, "default_system_prompt.txt");
        String defaultSystemPrompt = mAssetFileReader.read();
        if (!mAssetFileReader.getError().isEmpty()) {
            if (!mErrorReporting.showError(mAssetFileReader.getError())) {
                return mCurrentSettingsFields;
            }
        }

        // Get the model list from the "default_model_db.json" JSON file in the assets
        // folder to populate the modelDownloadUrl pull down menu
        ModelsConfig mModelsConfig = new ModelsConfig(context);
        List<ModelInfo> mModelInfoList = mModelsConfig.getModelsList();

        if (!mModelsConfig.getError().isEmpty()) {
            if (!mErrorReporting.showError(mModelsConfig.getError())) {
                return mCurrentSettingsFields;
            }
        }
        if (mModelInfoList.isEmpty()) {
            if (!mErrorReporting.showError("No models found")) {
                return mCurrentSettingsFields;
            }
        } else {
            // Get the 1st model from the list
            ModelInfo firstModelInfo = mModelInfoList.get(0);
            defaultModeModelDownloadUrl = firstModelInfo.getModelDownloadUrl();

            // Get the specific system prompt and append it to the default system prompt
            String specificSystemPrompt = firstModelInfo.getSystemPrompt();
            defaultSystemPrompt += (!specificSystemPrompt.isEmpty() ? "\n\n" + specificSystemPrompt : "");

            // Get the model path and tokenizer path
            defaultModelPath = getFirstModelFilePath(resourcePath, ".pte");
            if (defaultModelPath == null) {
                defaultModelPath = "";
            } else {
                defaultTokenizerPath = resourcePath + "/tokenizer.model";
            }
        }

        // Set default settings
        mCurrentSettingsFields.saveModelDownloadUrl(defaultModeModelDownloadUrl);
        mCurrentSettingsFields.saveModelPath(defaultModelPath);
        mCurrentSettingsFields.saveTokenizerPath(defaultTokenizerPath);
        mCurrentSettingsFields.saveModelType(DEFAULT_MODEL_TYPE);
        mCurrentSettingsFields.saveBackendType(DEFAULT_BACKEND_TYPE);
        mCurrentSettingsFields.saveParameters((double) DEFAULT_TEMPERATURE);
        mCurrentSettingsFields.saveSystemPrompt(defaultSystemPrompt);

        // Print the parameters:
        ETLogging.getInstance().log("Default model path: " + defaultModelPath);
        ETLogging.getInstance().log("Default tokenizer path: " + defaultTokenizerPath);
        ETLogging.getInstance().log("Default model type: " + DEFAULT_MODEL_TYPE);
        ETLogging.getInstance().log("Default backend type: " + DEFAULT_BACKEND_TYPE);
        ETLogging.getInstance().log("Default temperature: " + DEFAULT_TEMPERATURE);

        // Print mCurrentSettingsFields
        ETLogging.getInstance().log("setDefaultParameters | mCurrentSettingsFields: " + mCurrentSettingsFields);

        // Initialize shared preferences
        DemoSharedPreferences mDemoSharedPreferences;
        mDemoSharedPreferences = new DemoSharedPreferences(context);

        // Save initial settings
        mDemoSharedPreferences.addSettings(mCurrentSettingsFields);

        // Return the mCurrentSettingsFields
        return mCurrentSettingsFields;
    }
}
