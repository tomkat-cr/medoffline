/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.example.medoffline;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import androidx.annotation.NonNull;
import org.pytorch.executorch.LlamaCallback;
import org.pytorch.executorch.LlamaModule;

/** A helper class to handle all model running logic within this class. */
public class ModelRunner implements LlamaCallback {
  private static final int MAX_RETRIES = 3;
  private static final long RETRY_DELAY_MS = 1000;
  private static final String TAG = "ModelRunner";
  
  LlamaModule mModule = null;
  String mModelFilePath = "";
  String mTokenizerFilePath = "";
  Float mTemperature = 0.8f;
  ModelRunnerCallback mCallback = null;
  HandlerThread mHandlerThread = null;
  Handler mHandler = null;
  private volatile boolean isLoading = false;
  private int retryCount = 0;
  private long modelInitStartTime;

  /**
   * ] Helper class to separate between UI logic and model runner logic. Automatically handle
   * generate() request on worker thread.
   *
   * @param modelFilePath
   * @param tokenizerFilePath
   * @param callback
   */
  ModelRunner(
      String modelFilePath,
      String tokenizerFilePath,
      float temperature,
      ModelRunnerCallback callback) {
    Log.d(TAG, ">> Creating ModelRunner with model: " + modelFilePath);
    mModelFilePath = modelFilePath;
    mTokenizerFilePath = tokenizerFilePath;
    mCallback = callback;
    mTemperature = temperature;

    // Create handler thread first
    mHandlerThread = new HandlerThread("ModelRunner", android.os.Process.THREAD_PRIORITY_BACKGROUND);
    mHandlerThread.start();
    mHandler = new ModelRunnerHandler(mHandlerThread.getLooper(), this);

    // Initialize model on background thread with retry mechanism
    initializeModel();
  }

  private void logMemoryInfo() {
    Runtime runtime = Runtime.getRuntime();
    long maxMemory = runtime.maxMemory();
    long totalMemory = runtime.totalMemory();
    long freeMemory = runtime.freeMemory();
    
    Log.i(TAG, String.format(">> Memory - Max: %.2f MB, Total: %.2f MB, Free: %.2f MB",
        maxMemory / (1024.0 * 1024.0),
        totalMemory / (1024.0 * 1024.0),
        freeMemory / (1024.0 * 1024.0)));
  }

  private void initializeModel() {
    if (isLoading) {
        Log.w(TAG, "Model initialization already in progress");
        return;
    }
    isLoading = true;
    modelInitStartTime = System.currentTimeMillis();
    Log.d(TAG, "Starting model initialization");
    logMemoryInfo();

    mHandler.post(() -> {
        try {
            if (mModule != null) {
                Log.d(TAG, "Cleaning up previous model instance");
                mModule.stop();
                mModule = null;
                System.gc();
            }

            // Add small delay to allow cleanup
            Thread.sleep(100);
            Log.d(TAG, "Creating new LlamaModule instance");
            logMemoryInfo();
            
            mModule = new LlamaModule(mModelFilePath, mTokenizerFilePath, mTemperature);
            Log.d(TAG, "LlamaModule instance created successfully");
            
            // Start model loading
            Log.d(TAG, "Starting model load");
            mHandler.sendEmptyMessage(ModelRunnerHandler.MESSAGE_LOAD_MODEL);
            
            isLoading = false;
            retryCount = 0;
            long duration = System.currentTimeMillis() - modelInitStartTime;
            Log.i(TAG, "Model initialization completed in " + duration + "ms");
            logMemoryInfo();
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - modelInitStartTime;
            Log.e(TAG, "Model initialization failed after " + duration + "ms", e);
            logMemoryInfo();
            
            e.printStackTrace();
            retryCount++;
            
            if (retryCount < MAX_RETRIES) {
                long delay = RETRY_DELAY_MS * (1L << (retryCount - 1));
                String errorMsg = "Model initialization failed, retrying in " + (delay/1000) + " seconds...";
                Log.w(TAG, errorMsg);
                
                new Handler(Looper.getMainLooper()).post(() -> 
                    mCallback.onError(errorMsg)
                );
                
                mHandler.postDelayed(this::initializeModel, delay);
            } else {
                isLoading = false;
                String errorMsg = "Failed to initialize model after " + MAX_RETRIES + " attempts: " + e.getMessage();
                Log.e(TAG, errorMsg);
                
                new Handler(Looper.getMainLooper()).post(() -> 
                    mCallback.onError(errorMsg)
                );
            }
        }
    });
  }

  int generate(String prompt) {
    Message msg = Message.obtain(mHandler, ModelRunnerHandler.MESSAGE_GENERATE, prompt);
    msg.sendToTarget();
    return 0;
  }

  void stop() {
    Log.d(TAG, "Stopping model");
    try {
        if (mModule != null) {
            mModule.stop();
            Log.d(TAG, "Model stopped successfully");
        }
    } catch (Exception e) {
        Log.e(TAG, "Error stopping model", e);
        e.printStackTrace();
    }
  }

  @Override
  protected void finalize() throws Throwable {
    try {
        Log.d(TAG, "Finalizing ModelRunner");
        stop();
    } finally {
        super.finalize();
    }
  }

  @Override
  public void onResult(String result) {
    mCallback.onTokenGenerated(result);
  }

  @Override
  public void onStats(float tps) {
    mCallback.onStats("tokens/second: " + tps);
  }
}

class ModelRunnerHandler extends Handler {
  private static final String TAG = "ModelRunnerHandler";
  public static int MESSAGE_LOAD_MODEL = 1;
  public static int MESSAGE_GENERATE = 2;

  private final ModelRunner mModelRunner;

  public ModelRunnerHandler(Looper looper, ModelRunner modelRunner) {
    super(looper);
    mModelRunner = modelRunner;
  }

  @Override
  public void handleMessage(@NonNull android.os.Message msg) {
    if (msg.what == MESSAGE_LOAD_MODEL) {
      try {
        Log.d(TAG, "Starting model load process");
        if (mModelRunner.mModule == null) {
          throw new IllegalStateException("Model not initialized properly");
        }
        
        long startTime = System.currentTimeMillis();
        Log.d(TAG, "Calling module.load()");
        int status = mModelRunner.mModule.load();
        long duration = System.currentTimeMillis() - startTime;
        Log.i(TAG, "Model load completed in " + duration + "ms with status: " + status);
        
        new Handler(Looper.getMainLooper()).post(() -> 
          mModelRunner.mCallback.onModelLoaded(status)
        );
      } catch (Exception e) {
        Log.e(TAG, "Error during model load", e);
        e.printStackTrace();
        new Handler(Looper.getMainLooper()).post(() -> 
          mModelRunner.mCallback.onError("Error loading model: " + e.getMessage() + " | MOL-LOAD-E-100")
        );
      }
    } else if (msg.what == MESSAGE_GENERATE) {
      try {
        Log.d(TAG, "Starting text generation");
        if (mModelRunner.mModule == null) {
          throw new IllegalStateException("Model not initialized");
        }
        mModelRunner.mModule.generate((String) msg.obj, mModelRunner);
        Log.d(TAG, "Generation request sent successfully");
      } catch (Exception e) {
        Log.e(TAG, "Error during generation", e);
        e.printStackTrace();
        new Handler(Looper.getMainLooper()).post(() -> 
          mModelRunner.mCallback.onError("Generation error: " + e.getMessage() + " | MOL-GEN-E-120")
        );
      }
    }
  }
}
