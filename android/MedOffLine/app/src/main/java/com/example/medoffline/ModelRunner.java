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
import androidx.annotation.NonNull;
import org.pytorch.executorch.LlamaCallback;
import org.pytorch.executorch.LlamaModule;

/** A helper class to handle all model running logic within this class. */
public class ModelRunner implements LlamaCallback {
  LlamaModule mModule = null;

  String mModelFilePath = "";
  String mTokenizerFilePath = "";
  Float mTemperature = 0.8f;

  ModelRunnerCallback mCallback = null;

  HandlerThread mHandlerThread = null;
  Handler mHandler = null;

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
    mModelFilePath = modelFilePath;
    mTokenizerFilePath = tokenizerFilePath;
    mCallback = callback;
    mTemperature = temperature;

    mModule = new LlamaModule(mModelFilePath, mTokenizerFilePath, mTemperature);
    mHandlerThread = new HandlerThread("ModelRunner");
    mHandlerThread.start();
    mHandler = new ModelRunnerHandler(mHandlerThread.getLooper(), this);

    mHandler.sendEmptyMessage(ModelRunnerHandler.MESSAGE_LOAD_MODEL);
  }

  int generate(String prompt) {
    Message msg = Message.obtain(mHandler, ModelRunnerHandler.MESSAGE_GENERATE, prompt);
    msg.sendToTarget();
    return 0;
  }

  void stop() {
    mModule.stop();
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
      int status = mModelRunner.mModule.load();
      mModelRunner.mCallback.onModelLoaded(status);
    } else if (msg.what == MESSAGE_GENERATE) {
      try {
        mModelRunner.mModule.generate((String) msg.obj, mModelRunner);
      } catch (Exception e) {
        // Handle the exception, e.g., log it and notify the callback
        e.printStackTrace();
        mModelRunner.mCallback.onError(e.getMessage() + " " + e.getCause() + " | MOL-HMSG-E-100");
      }
      try {
        mModelRunner.mCallback.onGenerationStopped();
      } catch (Exception e) {
        // Handle the exception, e.g., log it and notify the callback
        e.printStackTrace();
        mModelRunner.mCallback.onError(e.getMessage() + " " + e.getCause() + " | MOL-HMSG-E-200");
      }
    }
  }
}
