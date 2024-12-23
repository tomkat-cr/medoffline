/*
 * Copyright (c) Carlos J. Ramirez, and affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.example.medoffline;

public class ModelInfo {
    private String modelId;
    private String modelName;
    private String modelType;
    private String modelDownloadUrl;
    private String modelFileName;
    private int modelFileSize;
    private String tokenizerFileName;
    private int tokenizerFileSize;
    private String systemPrompt;

    public ModelInfo(String modelId, String modelName, String modelType, String modelDownloadUrl, String modelFileName, 
                     int modelFileSize, String tokenizerFileName, int tokenizerFileSize, String systemPrompt) {
        this.modelId = modelId.trim();
        this.modelName = modelName.trim();
        this.modelType = modelType.trim();
        this.modelDownloadUrl = modelDownloadUrl.trim();
        this.modelFileName = modelFileName.trim();
        this.modelFileSize = modelFileSize;
        this.tokenizerFileName = tokenizerFileName.trim();
        this.tokenizerFileSize = tokenizerFileSize;
        this.systemPrompt = systemPrompt.trim();
    }

    // Getters and setters
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getModelType() { return modelType; }
    public void setModelType(String modelType) { this.modelType = modelType; }
    public String getModelDownloadUrl() { return modelDownloadUrl; }
    public void setModelDownloadUrl(String modelDownloadUrl) { this.modelDownloadUrl = modelDownloadUrl; }
    public String getModelFileName() { return modelFileName; }
    public void setModelFileName(String modelFileName) { this.modelFileName = modelFileName; }
    public int getModelFileSize() { return modelFileSize; }
    public void setModelFileSize(int modelFileSize) { this.modelFileSize = modelFileSize; }
    public String getTokenizerFileName() { return tokenizerFileName; }
    public void setTokenizerFileName(String tokenizerFileName) { this.tokenizerFileName = tokenizerFileName; }
    public int getTokenizerFileSize() { return tokenizerFileSize; }
    public void setTokenizerFileSize(int tokenizerFileSize) { this.tokenizerFileSize = tokenizerFileSize; }
    public String getSystemPrompt() { return systemPrompt; }
    public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
}
