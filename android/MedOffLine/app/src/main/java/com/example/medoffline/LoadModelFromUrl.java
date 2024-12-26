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
    private static final String MODEL_DEFAULT_PATH = "/data/local/tmp/medoffline";

    private static final int DEFAULT_TIMEOUT_MINUTES = 10;
    private static final long MIN_STORAGE_SPACE = 2L * 1024 * 1024 * 1024; // 2GB

    public static Float DEFAULT_TEMPERATURE = 0.1f;

    // public static String DEFAULT_MODEL_CONFIG_DOWNLOAD_URL = "http://192.168.1.100/get_model_config";
    // public static String DEFAULT_MODEL_CONFIG_DOWNLOAD_URL = "https://medoffline.aclics.com/get_model_config";
    public static String DEFAULT_MODEL_CONFIG_DOWNLOAD_URL = "https://www.carlosjramirez.com/downloads/medoffline_default_model_db.json";

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

    public void genericDownloadFile(SettingsFields mCurrentSettingsFields, String url, String targetPath, long minStorageSpace)
        throws InterruptedException, ExecutionException, IOException {

        String errorMessage = "";
        String resourcePath = new File(targetPath).getParent();

        ETLogging.getInstance().log("genericDownloadFile | started..." + 
            "\n | url: " + url +
            "\n | targetPath: " + targetPath + 
            "\n | minStorageSpace: " + minStorageSpace +
            "\n | resourcePath: " + resourcePath);

        // If target directory does not exist, create it
        File fileObject = new File(resourcePath);
        if (!fileObject.exists()) {
            boolean created = fileObject.mkdirs();
            if (!created) {
                throw new IOException("Failed to create directory: " + resourcePath);
            }
        }

        // Set up progress tracking
        new Handler(Looper.getMainLooper()).post(() -> {
            if (downloadProgressBar != null)
                downloadProgressBar.setVisibility(View.VISIBLE);
            if (downloadProgressText != null)
                downloadProgressText.setVisibility(View.VISIBLE);
            if (downloadingModelText != null)
                downloadingModelText.setVisibility(View.VISIBLE);
        });

        try {
            // Set up progress listener
            LocalModelManagement.setDownloadProgressListener(new LocalModelManagement.DownloadProgressListener() {
                @Override
                public void onProgressUpdate(long bytesRead, long contentLength, boolean done) {
                    updateDownloadProgress(bytesRead, contentLength);
                }

                @Override
                public void onError(String error) {
                    throw new RuntimeException(error);
                }
            });

            // Download and extract model
            LocalModelManagement.downloadFileFromUrl(
                context, url, targetPath,
                minStorageSpace, DEFAULT_TIMEOUT_MINUTES);

            // Check if model configuration was extracted successfully
            if (!LocalModelManagement.checkFileExists(targetPath)) {
                errorMessage = "File " + targetPath + " not found after extraction";
            }
        } catch (Exception e) {
            errorMessage = e.getMessage();
        } finally {
            new Handler(Looper.getMainLooper()).post(() -> {
                if (downloadProgressBar != null)
                    downloadProgressBar.setVisibility(View.GONE);
                if (downloadProgressText != null)
                    downloadProgressText.setVisibility(View.GONE);
                if (downloadingModelText != null)
                    downloadingModelText.setVisibility(View.GONE);
            });
        }

        if (errorMessage.isEmpty()) {
            return;
        }
        ETLogging.getInstance().log(errorMessage);
        throw new RuntimeException(errorMessage);
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
                // throw new RuntimeException("Failed to create directory: " + modelDir);
                String errorMessage = "Failed to create directory: " + modelDir;
                mErrorReporting.showError(errorMessage);
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

    public ModelsConfig getModelsConfig() {
        String alternateConfigPath = getModelConfigPath() + "/model_config.json";
        if (!LocalModelManagement.checkFileExists(alternateConfigPath)) {
          alternateConfigPath = "";
        }
        ModelsConfig modelsConfig = new ModelsConfig(context, alternateConfigPath);
        return modelsConfig;
    }

    public void downloadModel(SettingsFields mCurrentSettingsFields, DemoSharedPreferences mDemoSharedPreferences)
        throws InterruptedException, ExecutionException, IOException {

        ETLogging.getInstance().log("downloadModel | started...");
        
        String modelToDownload = mCurrentSettingsFields.getModelToDownload();
        String resourcePath = getBaseModelsPath();

        ModelsConfig modelsConfig = getModelsConfig();

        ModelInfo modelInfo = modelsConfig.getModelData(modelToDownload);
        if (modelInfo == null) {
            throw new IOException("Model info not found for model: " + modelToDownload);
        }
        String ModelUrl = modelInfo.getModelDownloadUrl();
        if (ModelUrl == null) {
            throw new IOException("Model URL not found for model: " + modelToDownload);
        }

        String tarGzPath = resourcePath + "/model.tar.gz";

        // Download the model .tar file
        genericDownloadFile(mCurrentSettingsFields, ModelUrl, tarGzPath, MIN_STORAGE_SPACE);

        // Extract the .tar file
        LocalModelManagement.unzipGz(tarGzPath, resourcePath);

        // Check if model was extracted successfully
        // String modelPath = getFirstModelFilePath(resourcePath, ".pte");
        String modelPath = resourcePath + "/" + modelInfo.getModelFileName();
        if (!LocalModelManagement.checkFileExists(modelPath)) {
            throw new IOException("Model file '" + modelPath + "' not found after extraction");
        }

        // Check if tokenizer exists and rename it to .bin if it's tokenizer.model
        renameTokenizerFile(modelPath);

        // Get the tokenizer path
        String tokenizerPath = getTokenizerPath(modelPath);

        // Save model and tokenizer paths in the settings
        mCurrentSettingsFields.saveModelPath(modelPath);
        mCurrentSettingsFields.saveTokenizerPath(tokenizerPath);

        // Save model path to SharedPreferences
        mDemoSharedPreferences.addSettings(mCurrentSettingsFields);
    }

    public String getModelConfigPath() {
        if (USE_MODEL_DEFAULT_PATH) {
            return MODEL_DEFAULT_PATH;
        }
        return filesDir.getAbsolutePath() + "/model_configs";
    }

    public void downloadModelConfig(SettingsFields mCurrentSettingsFields)
        throws InterruptedException, ExecutionException, IOException {

        ETLogging.getInstance().log("downloadModelConfig | started...");

        String modelConfigDownloadUrl = mCurrentSettingsFields.getModelDownloadUrl();
        String resourcePath = getModelConfigPath();
        String targetPath = resourcePath + "/model_config.json";
        long minStorageSpace = 1L * 1024 * 1024; // 1 MB

        // Download and extract model configuration JSON file
        genericDownloadFile(mCurrentSettingsFields, modelConfigDownloadUrl, targetPath, minStorageSpace);

        // Check if model configuration was extracted successfully
        if (!LocalModelManagement.checkFileExists(targetPath)) {
            String errorMessage = "Model configuration file not found after extraction";
            ETLogging.getInstance().log(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    public SettingsFields setDefaultParameters() {
        // Set default values for settings

        ModelType DEFAULT_MODEL_TYPE = ModelType.LLAMA_3_2;
        BackendType DEFAULT_BACKEND_TYPE = BackendType.XNNPACK;

        // Initialize settings
        SettingsFields mCurrentSettingsFields = new SettingsFields();
        mCurrentSettingsFields.SetInitValues();

        String resourcePath = getBaseModelsPath();

        String defaultModeModelDownloadUrl = DEFAULT_MODEL_CONFIG_DOWNLOAD_URL;
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
            // defaultModeModelDownloadUrl = firstModelInfo.getModelDownloadUrl();

            // Get the specific system prompt and append it to the default system prompt
            String specificSystemPrompt = firstModelInfo.getSystemPrompt();
            defaultSystemPrompt += (!specificSystemPrompt.isEmpty() ? "\n\n" + specificSystemPrompt : "");

            // Get the model path and tokenizer path
            defaultModelPath = getFirstModelFilePath(resourcePath, ".pte");
            if (defaultModelPath == null) {
                defaultModelPath = "";
            } else {
                defaultTokenizerPath = getTokenizerPath(defaultModelPath);
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

        // Save initial settings
        saveSettings(mCurrentSettingsFields);

        // Return the mCurrentSettingsFields
        return mCurrentSettingsFields;
    }

    public String getTokenizerPath(String modelPath) {
        String resourcePath = getBaseModelsPath();
        String tokenizerPath = resourcePath + "/tokenizer.model";
        // Verify if tokenizer file exists
        File tokenizerFile = new File(tokenizerPath);
        if (!tokenizerFile.exists()) {
            tokenizerPath = modelPath.replace(".pte", ".bin");
            tokenizerFile = new File(tokenizerPath);
            if (!tokenizerFile.exists()) {
                String errorMessage = "Error: tokenizer file does not exist";
                Log.e("LoadModelFromUrl | getTokenizerPath", errorMessage);
                mErrorReporting.showError(errorMessage);
            }
        }
        return tokenizerPath;
    }

    private void renameTokenizerFile(String modelPath) {
        String resourcePath = getBaseModelsPath();
        String tokenizerPath = resourcePath + "/tokenizer.model";
        File tokenizerFile = new File(tokenizerPath);
        if (tokenizerFile.exists()) {
            String newTokenizerPath = modelPath.replace(".pte", ".bin");
            File newTokenizerFile = new File(newTokenizerPath);
            if (!newTokenizerFile.exists()) {
                tokenizerFile.renameTo(newTokenizerFile);
            }
        }
    }

    private void saveSettings(SettingsFields updatedSettingsFields) {
        // Save settings to shared preferences
        DemoSharedPreferences mDemoSharedPreferences = new DemoSharedPreferences(context);
        mDemoSharedPreferences.addSettings(updatedSettingsFields);
    }
}
