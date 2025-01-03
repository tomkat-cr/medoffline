/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.example.medoffline;

public class SettingsFields {

  public String getModelFilePath() {
    return modelFilePath;
  }

  public String getModelToDownload() {
    return modelToDownload;
  }

  public String getModelDownloadUrl() {
    return modelDownloadUrl;
  }

  public String getTokenizerFilePath() {
    return tokenizerFilePath;
  }

  public double getTemperature() {
    return temperature;
  }

  public String getSystemPrompt() {
    return systemPrompt;
  }

  public ModelType getModelType() {
    return modelType;
  }

  public BackendType getBackendType() {
    return backendType;
  }

  public String getUserPrompt() {
    return userPrompt;
  }

  public String getFormattedSystemAndUserPrompt(String prompt) {
    return getFormattedSystemPrompt() + getFormattedUserPrompt(prompt);
  }

  public String getFormattedSystemPrompt() {
    return PromptFormat.getSystemPromptTemplate(modelType)
        .replace(PromptFormat.SYSTEM_PLACEHOLDER, systemPrompt);
  }

  public String getFormattedUserPrompt(String prompt) {
    return userPrompt.replace(PromptFormat.USER_PLACEHOLDER, prompt);
  }

  public boolean getIsClearChatHistory() {
    return isClearChatHistory;
  }

  public boolean getIsLoadModel() {
    return isLoadModel;
  }

  public boolean getIsDownloadModel() {
    return isDownloadModel;
  }

  public boolean getIsDownloadModelConfig() {
    return isDownloadModelConfig;
  }

  public boolean getIsModelLoaded() {
    return isModelLoaded;
  }

  private String modelFilePath;
  private String modelToDownload;
  private String modelDownloadUrl;
  private String tokenizerFilePath;
  private double temperature;
  private String systemPrompt;
  private String userPrompt;
  private boolean isClearChatHistory;
  private boolean isLoadModel;
  private boolean isDownloadModel;
  private boolean isDownloadModelConfig;
  private ModelType modelType;
  private BackendType backendType;
  private boolean isModelLoaded;

  private static final boolean ACTIVITY_DEBUG = false;

  public SettingsFields() {
    if (ACTIVITY_DEBUG) ETLogging.getInstance().log("SettingsFields # 1 | No parameters");
  }

  public SettingsFields(SettingsFields settingsFields) {
    if (ACTIVITY_DEBUG) ETLogging.getInstance().log("SettingsFields # 2 | With parameter settingsFields: " + settingsFields);
    this.modelFilePath = settingsFields.modelFilePath;
    this.modelToDownload = settingsFields.modelToDownload;
    this.modelDownloadUrl = settingsFields.modelDownloadUrl;
    this.tokenizerFilePath = settingsFields.tokenizerFilePath;
    this.temperature = settingsFields.temperature;
    this.systemPrompt = settingsFields.getSystemPrompt();
    this.userPrompt = settingsFields.getUserPrompt();
    this.isClearChatHistory = settingsFields.getIsClearChatHistory();
    this.isLoadModel = settingsFields.getIsLoadModel();
    this.isDownloadModel = settingsFields.getIsDownloadModel();
    this.isDownloadModelConfig = settingsFields.getIsDownloadModelConfig();
    this.modelType = settingsFields.modelType;
    this.backendType = settingsFields.backendType;
    this.isModelLoaded = settingsFields.isModelLoaded;
  }

  public void SetInitValues() {
    ModelType DEFAULT_MODEL = ModelType.LLAMA_3_2;
    BackendType DEFAULT_BACKEND = BackendType.XNNPACK;
    this.modelFilePath = "";
    this.modelToDownload = "";
    this.modelDownloadUrl = "";
    this.tokenizerFilePath = "";
    this.temperature = SettingsActivity.TEMPERATURE_MIN_VALUE;
    this.systemPrompt = "";
    this.userPrompt = PromptFormat.getUserPromptTemplate(DEFAULT_MODEL);
    this.isClearChatHistory = false;
    this.isLoadModel = false;
    this.isDownloadModel = false;
    this.isDownloadModelConfig = false;
    this.modelType = DEFAULT_MODEL;
    this.backendType = DEFAULT_BACKEND;
    this.isModelLoaded = false;
  }

  public void showOwnData() {
    ETLogging.getInstance().log(
      "SettingsFields # showOwnData:" +
      "\n | this.modelFilePath: " + this.modelFilePath +
      "\n | this.modelToDownload: " + this.modelToDownload +
      "\n | this.modelDownloadUrl: " + this.modelDownloadUrl +
      "\n | this.tokenizerFilePath: " + this.tokenizerFilePath +
      "\n | this.temperature: " + this.temperature +
      "\n | this.systemPrompt: " + this.systemPrompt +
      "\n | this.userPrompt: " + this.userPrompt +
      "\n | this.isClearChatHistory: " + this.isClearChatHistory +
      "\n | this.isLoadModel: " + this.isLoadModel +
      "\n | this.isDownloadModel: " + this.isDownloadModel +
      "\n | this.isDownloadModelConfig: " + this.isDownloadModelConfig +
      "\n | this.modelType: " + this.modelType +
      "\n | this.backendType: " + this.backendType +
      "\n | this.isModelLoaded: " + this.isModelLoaded);
  }

  public void saveModelPath(String modelFilePath) {
    this.modelFilePath = modelFilePath;
  }

  public void saveModelToDownload(String modelToDownload) {
    this.modelToDownload = modelToDownload;
  }

  public void saveModelDownloadUrl(String modelDownloadUrl) {
    this.modelDownloadUrl = modelDownloadUrl;
  }

  public void saveTokenizerPath(String tokenizerFilePath) {
    this.tokenizerFilePath = tokenizerFilePath;
  }

  public void saveModelType(ModelType modelType) {
    this.modelType = modelType;
  }

  public void saveBackendType(BackendType backendType) {
    this.backendType = backendType;
  }

  public void saveParameters(Double temperature) {
    this.temperature = temperature;
  }

  public void savePrompts(String systemPrompt, String userPrompt) {
    this.systemPrompt = systemPrompt;
    this.userPrompt = userPrompt;
  }

  public void saveSystemPrompt(String systemPrompt) {
    this.systemPrompt = systemPrompt;
  }

  public void saveIsClearChatHistory(boolean needToClear) {
    this.isClearChatHistory = needToClear;
  }

  public void saveLoadModelAction(boolean shouldLoadModel) {
    this.isLoadModel = shouldLoadModel;
  }

  public void saveDownloadModelAction(boolean shouldDownloadModel) {
    this.isDownloadModel = shouldDownloadModel;
  }

  public void saveDownloadModelConfigAction(boolean shouldDownloadModelConfig) {
    this.isDownloadModelConfig = shouldDownloadModelConfig;
  }

  public void saveIsModelLoaded(boolean isModelLoaded) {
    this.isModelLoaded = isModelLoaded;
  }

  public boolean equals(SettingsFields anotherSettingsFields) {
    if (this == anotherSettingsFields) return true;
    return modelFilePath.equals(anotherSettingsFields.modelFilePath)
        && modelToDownload.equals(anotherSettingsFields.modelToDownload)
        && modelDownloadUrl.equals(anotherSettingsFields.modelDownloadUrl)
        && tokenizerFilePath.equals(anotherSettingsFields.tokenizerFilePath)
        && temperature == anotherSettingsFields.temperature
        && systemPrompt.equals(anotherSettingsFields.systemPrompt)
        && userPrompt.equals(anotherSettingsFields.userPrompt)
        && isClearChatHistory == anotherSettingsFields.isClearChatHistory
        && isLoadModel == anotherSettingsFields.isLoadModel
        && modelType == anotherSettingsFields.modelType
        && backendType == anotherSettingsFields.backendType
        && isDownloadModel == anotherSettingsFields.isDownloadModel
        && isModelLoaded == anotherSettingsFields.isModelLoaded
        && isDownloadModelConfig == anotherSettingsFields.isDownloadModelConfig;
  }
}
