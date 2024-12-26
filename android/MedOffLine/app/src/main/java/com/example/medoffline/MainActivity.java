/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.example.medoffline;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.pytorch.executorch.LlamaCallback;
import org.pytorch.executorch.LlamaModule;

public class MainActivity extends AppCompatActivity implements Runnable, LlamaCallback {
  private static final int MAX_NUM_OF_IMAGES = 5;
  private static final int REQUEST_IMAGE_CAPTURE = 1;
  private static final int CONVERSATION_HISTORY_MESSAGE_LOOKBACK = 2;

  private EditText mEditTextMessage;
  private ImageButton mSendButton;
  private ImageButton mGalleryButton;
  private ImageButton mCameraButton;
  private ImageButton mSettingsButton;
  private ListView mMessagesView;
  private View progressBar;
  private View downloadingModelText;
  private TextView mMemoryView;
  private TextView progressBarText;  // Changed from View to TextView
  private TextView downloadProgressText;
  private ProgressBar progressBarWheel;
  private ProgressBar downloadProgressBar;
  private MessageAdapter mMessageAdapter;
  private Message mResultMessage = null;
  private ConstraintLayout mMediaPreviewConstraintLayout;

  private ActivityResultLauncher<PickVisualMediaRequest> mPickGallery;
  private ActivityResultLauncher<Uri> mCameraRoll;

  private DemoSharedPreferences mDemoSharedPreferences;
  private List<Uri> mSelectedImageUri;
  private LinearLayout mAddMediaLayout;
  private Uri cameraImageUri;
  private Handler mMemoryUpdateHandler;
  private Runnable memoryUpdater;
  private int promptID = 0;
  private long startPos = 0;
  private Executor executor;
  private LlamaModule mModule = null;

  private SettingsFields mCurrentSettingsFields;
  private LoadModelFromUrl mLoadModelFromUrl = null;
  private ErrorReporting mErrorReporting = null;

  @Override
  public void onResult(String result) {
    if (result.equals(PromptFormat.getStopToken(mCurrentSettingsFields.getModelType()))) {
      return;
    }
    if (result.equals("\n\n") || result.equals("\n")) {
      if (!mResultMessage.getText().isEmpty()) {
        mResultMessage.appendText(result);
        run();
      }
    } else {
      mResultMessage.appendText(result);
      run();
    }
  }

  @Override
  public void onStats(float tps) {
    runOnUiThread(
        () -> {
          if (mResultMessage != null) {
            mResultMessage.setTokensPerSecond(tps);
            mMessageAdapter.notifyDataSetChanged();
          }
        });
  }

  private void setSendButtonState(boolean state) {
    mSendButton = findViewById(R.id.sendButton);
    if (mSendButton != null) {
      ETLogging.getInstance().log(">> setSendButtonState: " + state);
      mSendButton.setEnabled(state);
    } else {
      ETLogging.getInstance().log(">> mSendButton is null");
    }
  }

  private void setLocalModel(String modelPath, String tokenizerPath, float temperature) {
    Message modelLoadingMessage = new Message("Loading model...", false, MessageType.SYSTEM, 0);
    ETLogging.getInstance().log(">> Loading model: " + modelPath);
    ETLogging.getInstance().log("   with tokenizer " + tokenizerPath);
    runOnUiThread(
        () -> {
          setSendButtonState(false);
          mMessageAdapter.add(modelLoadingMessage);
          mMessageAdapter.notifyDataSetChanged();
        });
    if (mModule != null) {
      ETLogging.getInstance().log("Start deallocating existing module instance");
      mModule.resetNative();
      mModule = null;
      ETLogging.getInstance().log("Completed deallocating existing module instance");
    }
    long runStartTime = System.currentTimeMillis();
    int loadResult = -99;
    String modelLoadError = "";
    String modelInfo = "";
    try {
      mModule =
          new LlamaModule(
              ModelUtils.getModelCategory(
                mCurrentSettingsFields.getModelType(),
                mCurrentSettingsFields.getBackendType()
              ),
              modelPath,
              tokenizerPath,
              temperature);
      loadResult = mModule.load();
    } catch (Exception e) {
      modelLoadError = "Error: " + e.getMessage();
      ETLogging.getInstance().log(modelLoadError);
      e.printStackTrace();
      mErrorReporting.showError(modelLoadError);
      return;
    }
    long loadDuration = System.currentTimeMillis() - runStartTime;
    if (loadResult != 0) {
      modelInfo = "*Model could not load (Error Code: " + loadResult + ")*";
      if (!modelLoadError.isEmpty()) {
        modelInfo += "\n" + modelLoadError;
      }
      loadDuration = 0;
      mErrorReporting.showError(modelInfo, "Model Load failed");
    } else {
      String[] segments = modelPath.split("/");
      String pteName = segments[segments.length - 1];
      segments = tokenizerPath.split("/");
      String tokenizerName = segments[segments.length - 1];
      modelInfo =
          "Successfully loaded model. "
              + pteName
              + " and tokenizer "
              + tokenizerName
              + " in "
              + (float) loadDuration / 1000
              + " sec."
              + " You can send text or image for inference";

      mCurrentSettingsFields.saveIsModelLoaded(true);
      mDemoSharedPreferences.addSettings(mCurrentSettingsFields);
      onModelRunStopped();

      if (mCurrentSettingsFields.getModelType() == ModelType.LLAVA_1_5) {
        ETLogging.getInstance().log("Llava start prefill prompt");
        startPos = mModule.prefillPrompt(PromptFormat.getLlavaPresetPrompt(), 0, 1, 0);
        ETLogging.getInstance().log("Llava completes prefill prompt");
      }
    }
    // Show model load status and info in the log
    String modelLoggingInfo =
        modelLoadError
            + "Model path: "
            + modelPath
            + "\nTokenizer path: "
            + tokenizerPath
            + "\nBackend: "
            + mCurrentSettingsFields.getBackendType().toString()
            + "\nModelType: "
            + ModelUtils.getModelCategory(
                mCurrentSettingsFields.getModelType(), mCurrentSettingsFields.getBackendType())
            + "\nTemperature: "
            + temperature
            + "\nModel loaded time: "
            + loadDuration
            + " ms";
    ETLogging.getInstance().log("Load complete. " + modelLoggingInfo);
    // Show model info in the conversation
    Message modelLoadedMessage = new Message(modelInfo, false, MessageType.SYSTEM, 0);
    runOnUiThread(
        () -> {
          setSendButtonState(true);
          mMessageAdapter.remove(modelLoadingMessage);
          mMessageAdapter.add(modelLoadedMessage);
          mMessageAdapter.notifyDataSetChanged();
        });
  }

  private void loadLocalModelAndParameters(
      String modelFilePath, String tokenizerFilePath, float temperature) {
      Runnable runnable =
        new Runnable() {
          @Override
          public void run() {
            setLocalModel(modelFilePath, tokenizerFilePath, temperature);
          }
        };
    new Thread(runnable).start();
  }

  private String getConversationHistory() {
    String conversationHistory = "";

    ArrayList<Message> conversations =
        mMessageAdapter.getRecentSavedTextMessages(CONVERSATION_HISTORY_MESSAGE_LOOKBACK);
    if (conversations.isEmpty()) {
      return conversationHistory;
    }

    int prevPromptID = conversations.get(0).getPromptID();
    String conversationFormat =
        PromptFormat.getConversationFormat(mCurrentSettingsFields.getModelType());
    String format = conversationFormat;
    for (int i = 0; i < conversations.size(); i++) {
      Message conversation = conversations.get(i);
      int currentPromptID = conversation.getPromptID();
      if (currentPromptID != prevPromptID) {
        conversationHistory = conversationHistory + format;
        format = conversationFormat;
        prevPromptID = currentPromptID;
      }
      if (conversation.getIsSent()) {
        format = format.replace(PromptFormat.USER_PLACEHOLDER, conversation.getText());
      } else {
        format = format.replace(PromptFormat.ASSISTANT_PLACEHOLDER, conversation.getText());
      }
    }
    conversationHistory = conversationHistory + format;

    return conversationHistory;
  }

  private String getTotalFormattedPrompt(String conversationHistory, String rawPrompt) {
    if (conversationHistory.isEmpty()) {
      return mCurrentSettingsFields.getFormattedSystemAndUserPrompt(rawPrompt);
    }

    return mCurrentSettingsFields.getFormattedSystemPrompt()
        + conversationHistory
        + mCurrentSettingsFields.getFormattedUserPrompt(rawPrompt);
  }

  private void onModelRunStarted() {
    if (mSendButton != null) mSendButton.setClickable(false);
    if (mSendButton != null) mSendButton.setImageResource(R.drawable.baseline_stop_24);
    if (mSendButton != null) mSendButton.setOnClickListener(
        view -> {
          mModule.stop();
        });
  }

  private void onModelRunStopped() {
    ETLogging.getInstance().log("onModelRunStopped | Stating model run...");

    if (mSendButton != null) mSendButton.setClickable(true);
    if (mSendButton != null) mSendButton.setImageResource(R.drawable.baseline_send_24);
    if (mSendButton != null) mSendButton.setOnClickListener(
        view -> {
          ETLogging.getInstance().log("onModelRunStopped | mSendButton clicked");
          try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
          } catch (Exception e) {
            ETLogging.getInstance().log("Keyboard dismissal error: " + e.getMessage());
          }
          addSelectedImagesToChatThread(mSelectedImageUri);
          String finalPrompt;
          String rawPrompt = mEditTextMessage.getText().toString();
          if (ModelUtils.getModelCategory(
                  mCurrentSettingsFields.getModelType(), mCurrentSettingsFields.getBackendType())
              == ModelUtils.VISION_MODEL) {
            finalPrompt = mCurrentSettingsFields.getFormattedSystemAndUserPrompt(rawPrompt);
          } else {
            finalPrompt = getTotalFormattedPrompt(getConversationHistory(), rawPrompt);
          }
          // We store raw prompt into message adapter, because we don't want to show the extra
          // tokens from system prompt
          mMessageAdapter.add(new Message(rawPrompt, true, MessageType.TEXT, promptID));
          mMessageAdapter.notifyDataSetChanged();
          mEditTextMessage.setText("");
          mResultMessage = new Message("", false, MessageType.TEXT, promptID);
          mMessageAdapter.add(mResultMessage);
          // Scroll to bottom of the list
          mMessagesView.smoothScrollToPosition(mMessageAdapter.getCount() - 1);
          // After images are added to prompt and chat thread, we clear the imageURI list
          // Note: This has to be done after imageURIs are no longer needed by LlamaModule
          mSelectedImageUri = null;
          promptID++;
          Runnable runnable =
              new Runnable() {
                @Override
                public void run() {
                  Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);
                  ETLogging.getInstance().log("starting runnable generate()");
                  runOnUiThread(
                      new Runnable() {
                        @Override
                        public void run() {
                          onModelRunStarted();
                        }
                      });
                  long generateStartTime = System.currentTimeMillis();
                  if (ModelUtils.getModelCategory(
                          mCurrentSettingsFields.getModelType(),
                          mCurrentSettingsFields.getBackendType())
                      == ModelUtils.VISION_MODEL) {
                    mModule.generateFromPos(
                        finalPrompt,
                        ModelUtils.VISION_MODEL_SEQ_LEN,
                        startPos,
                        MainActivity.this,
                        false);
                  } else if (mCurrentSettingsFields.getModelType() == ModelType.LLAMA_GUARD_3) {
                    String llamaGuardPromptForClassification =
                        PromptFormat.getFormattedLlamaGuardPrompt(rawPrompt);
                    ETLogging.getInstance()
                        .log("Running inference.. prompt=" + llamaGuardPromptForClassification);
                    mModule.generate(
                        llamaGuardPromptForClassification,
                        llamaGuardPromptForClassification.length() + 64,
                        MainActivity.this,
                        false);
                  } else {
                    ETLogging.getInstance().log("Running inference.. prompt=" + finalPrompt);
                    mModule.generate(
                        finalPrompt,
                        (int) (finalPrompt.length() * 0.75) + 64,
                        MainActivity.this,
                        false);
                  }

                  long generateDuration = System.currentTimeMillis() - generateStartTime;
                  mResultMessage.setTotalGenerationTime(generateDuration);
                  runOnUiThread(
                      new Runnable() {
                        @Override
                        public void run() {
                          onModelRunStopped();
                        }
                      });
                  ETLogging.getInstance().log("Inference completed");
                }
              };
          executor.execute(runnable);
        });

        // TODO this doesn't work, it stops with the error:
        // FATAL EXCEPTION: Thread-2: android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
        // mMessageAdapter.notifyDataSetChanged();
  }

  @Override
  public void run() {
    runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            mMessageAdapter.notifyDataSetChanged();
          }
        });
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    if (mAddMediaLayout != null && mAddMediaLayout.getVisibility() == View.VISIBLE) {
      mAddMediaLayout.setVisibility(View.GONE);
    } else {
      // Default behavior of back button
      finish();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mMemoryUpdateHandler.removeCallbacks(memoryUpdater);
    // This is to cover the case where the app is shutdown when user is on MainActivity but
    // never clicked on the logsActivity
    ETLogging.getInstance().saveLogs();
  }

  private boolean checkPermissions() {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
            == PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    ETLogging.getInstance().log("onCreate | started...");

    super.onCreate(savedInstanceState);

    mLoadModelFromUrl = new LoadModelFromUrl(this, getFilesDir());
    mErrorReporting = new ErrorReporting(this);

    mCurrentSettingsFields = new SettingsFields();
    mCurrentSettingsFields.SetInitValues();

    // Set theme
    setTheme(R.style.AppTheme);

    // Set layout
    setContentView(R.layout.activity_main);

    // Request necessary permissions
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (!checkPermissions()) {
        ETLogging.getInstance().log("onCreate | requestPermissions...");
        requestPermissions(
          new String[] {
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
          },
          1
        );
      }
    }

    // Initialize shared preferences
    if (mDemoSharedPreferences == null) {
      mDemoSharedPreferences = new DemoSharedPreferences(getBaseContext());
    }

    ETLogging.getInstance().log("onCreate | initializing UI...");

    View mainContent = findViewById(R.id.main_content);
    if (mainContent != null) mainContent.setVisibility(View.VISIBLE);

    // Set initial visibility
    progressBar = findViewById(R.id.progressBar);
    progressBarText = findViewById(R.id.progressBarText);
    progressBarWheel = findViewById(R.id.progressBarWheel);
    downloadingModelText = findViewById(R.id.downloadingModelText);
    downloadProgressBar = findViewById(R.id.downloadProgressBar);
    downloadProgressText = findViewById(R.id.downloadProgressText);

    if (progressBar != null) progressBar.setVisibility(View.GONE);
    if (progressBarText != null) progressBarText.setVisibility(View.GONE);
    if (progressBarWheel != null) progressBarWheel.setVisibility(View.GONE);
    if (downloadingModelText != null) downloadingModelText.setVisibility(View.GONE);
    if (downloadProgressBar != null) downloadProgressBar.setVisibility(View.GONE);
    if (downloadProgressText != null) downloadProgressText.setVisibility(View.GONE);

    // Initialize UI components
    mEditTextMessage = findViewById(R.id.editTextMessage);
    mSendButton = findViewById(R.id.sendButton);
    mMessagesView = findViewById(R.id.messages_view);
    mMessageAdapter = new MessageAdapter(this, R.layout.sent_message, new ArrayList<Message>());
    mMessagesView.setAdapter(mMessageAdapter);
    mMemoryUpdateHandler = new Handler(Looper.getMainLooper());

    // Setup media button
    setupMediaButton();

    // Setup gallery picker
    setupGalleryPicker();

    // Setup camera
    setupCameraRoll();
  
    // Setup memory settings
    startMemoryUpdate();

    // Setup settings (gear) button
    mSettingsButton = findViewById(R.id.settings);
    if (mSettingsButton != null) {
        mSettingsButton.setOnClickListener(view -> {
            Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(myIntent);
        });
    }

    // Setup logs button
    setupShowLogsButton();

    setSendButtonState(mCurrentSettingsFields.getIsModelLoaded());
    if (mCurrentSettingsFields.getIsModelLoaded()) {
      onModelRunStopped();
    } else {
      ETLogging.getInstance().log("onCreate | onModelRunStopped not called");
    }

    ETLogging.getInstance().log("onCreate | Initialize executor for background tasks...");

    // Initialize executor for background tasks
    executor = Executors.newSingleThreadExecutor();

    ETLogging.getInstance().log("onCreate | executor for background tasks was initialized");
  }

  @Override
  protected void onPause() {
    super.onPause();
    mDemoSharedPreferences.addMessages(mMessageAdapter);
  }

  @Override
  protected void onResume() {
    // Check for if settings parameters have changed

    super.onResume();

    Gson gson = new Gson();
    String settingsFieldsJSON = mDemoSharedPreferences.getSettings();

    ETLogging.getInstance().log("onResume | settingsFieldsJSON: " + settingsFieldsJSON);

    SettingsFields updatedSettingsFields;
    if (settingsFieldsJSON.isEmpty()) {
      ETLogging.getInstance().log("onResume | call setDefaultParameters...");
      updatedSettingsFields = mLoadModelFromUrl.setDefaultParameters();
    } else {
      ETLogging.getInstance().log("onResume | load settings from shared preferences...");
      updatedSettingsFields =
          gson.fromJson(settingsFieldsJSON, SettingsFields.class);
    }

    ETLogging.getInstance().log("onResume | updatedSettingsFields: " + updatedSettingsFields);

    if (updatedSettingsFields == null) {
      // Added this check, because gson.fromJson can return null
      ETLogging.getInstance().log("onResume | gson.fromJson returned null...");
      askUserToSelectModel();
      return;
    }

    // ETLogging.getInstance().log("1) onResume | updatedSettingsFields.showOwnData()");
    // updatedSettingsFields.showOwnData();
    // ETLogging.getInstance().log("2) onResume | mCurrentSettingsFields.showOwnData()");
    // mCurrentSettingsFields.showOwnData();

    boolean isUpdated = !mCurrentSettingsFields.equals(updatedSettingsFields);

    boolean isLoadModel = updatedSettingsFields.getIsLoadModel();
    boolean isDownloadModel = updatedSettingsFields.getIsDownloadModel();
    boolean isDownloadModelConfig = updatedSettingsFields.getIsDownloadModelConfig();

    setBackendMode(updatedSettingsFields.getBackendType());

    ETLogging.getInstance().log("onResume | isUpdated: " + isUpdated + ", isLoadModel: " + isLoadModel);

    if (isUpdated) {
      if (isDownloadModelConfig) {
        ETLogging.getInstance().log("onResume | Calls checkForDownloadModelConfig()");
        downloadModelConfig(mCurrentSettingsFields);
      } else if (isLoadModel || isDownloadModel) {
        // If users change the model file, but not pressing loadModelButton, we won't load the new
        // model
        ETLogging.getInstance().log("onResume | Calls checkForUpdateAndReloadModel()");
        mCurrentSettingsFields = new SettingsFields(updatedSettingsFields);
        checkForUpdateAndReloadModel(mCurrentSettingsFields);
      } else {
        ETLogging.getInstance().log("onResume | askUserToSelectModel # 1");
        askUserToSelectModel();
      }

      if (mCurrentSettingsFields.getIsModelLoaded()) {
        onModelRunStopped();
      } else {
        ETLogging.getInstance().log("onResume | onModelRunStopped not called");
      }

      ETLogging.getInstance().log("onResume | setSendButtonState = " + mCurrentSettingsFields.getIsModelLoaded());
      setSendButtonState(mCurrentSettingsFields.getIsModelLoaded());

      ETLogging.getInstance().log("onResume | Calls checkForClearChatHistory()");
      checkForClearChatHistory(updatedSettingsFields);

      // Update current to point to the latest
      ETLogging.getInstance().log("onResume | Update current to point to the latest");
      mCurrentSettingsFields = new SettingsFields(updatedSettingsFields);
    }
  }

  private void setBackendMode(BackendType backendType) {
    if (backendType.equals(BackendType.XNNPACK) || backendType.equals(BackendType.QUALCOMM)) {
      setXNNPACKMode();
    } else if (backendType.equals(BackendType.MEDIATEK)) {
      setMediaTekMode();
    }
  }

  private void setXNNPACKMode() {
    if (requireViewById(R.id.addMediaButton) != null) requireViewById(R.id.addMediaButton).setVisibility(View.VISIBLE);
  }

  private void setMediaTekMode() {
    if (requireViewById(R.id.addMediaButton) != null) requireViewById(R.id.addMediaButton).setVisibility(View.GONE);
  }

  private void checkForClearChatHistory(SettingsFields updatedSettingsFields) {
    if (updatedSettingsFields.getIsClearChatHistory()) {
      mMessageAdapter.clear();
      mMessageAdapter.notifyDataSetChanged();
      mDemoSharedPreferences.removeExistingMessages();
      // changing to false since chat history has been cleared.
      updatedSettingsFields.saveIsClearChatHistory(false);
      mDemoSharedPreferences.addSettings(updatedSettingsFields);
    }
  }

  private void checkForUpdateAndReloadModel(SettingsFields updatedSettingsFields) {
    String modelToDownload = updatedSettingsFields.getModelToDownload();

    String modelPath = updatedSettingsFields.getModelFilePath();
    String tokenizerPath = updatedSettingsFields.getTokenizerFilePath();
    double temperature = updatedSettingsFields.getTemperature();

    // Log the parameters
    ETLogging.getInstance().log("checkForUpdateAndReloadModel | Model path: " + modelPath);
    ETLogging.getInstance().log("checkForUpdateAndReloadModel | Tokenizer path: " + tokenizerPath);
    ETLogging.getInstance().log("checkForUpdateAndReloadModel | Temperature: " + temperature);
    ETLogging.getInstance().log("checkForUpdateAndReloadModel | Is load model: " + updatedSettingsFields.getIsLoadModel());
    ETLogging.getInstance().log("checkForUpdateAndReloadModel | Is download model: " + updatedSettingsFields.getIsDownloadModel());
    ETLogging.getInstance().log("checkForUpdateAndReloadModel | Is model loaded: " + updatedSettingsFields.getIsModelLoaded());

    if (!modelToDownload.isEmpty()) {
      if (updatedSettingsFields.getIsDownloadModel()) {
        Thread thread = new Thread(new Runnable() {
          @Override
          public void run() {
            mLoadModelFromUrl.setUiElements(downloadingModelText, downloadProgressText, downloadProgressBar);
            try {
              mLoadModelFromUrl.downloadModel(updatedSettingsFields, mDemoSharedPreferences);
            } catch (Exception e) {
              String errorMessage = "Model download exception: " + e.getMessage();
              ETLogging.getInstance().log(errorMessage);
              // e.printStackTrace();
              mErrorReporting.showError(e.getMessage(), "Model download exception");
              return;
            }
            updatedSettingsFields.saveDownloadModelAction(false);
            updatedSettingsFields.saveLoadModelAction(true);
            mDemoSharedPreferences.addSettings(updatedSettingsFields);
          }
        });
        thread.start(); 
        modelPath = updatedSettingsFields.getModelFilePath();
        tokenizerPath = updatedSettingsFields.getTokenizerFilePath();
      }
    }

    if (!modelPath.isEmpty() && !tokenizerPath.isEmpty()) {
      if (updatedSettingsFields.getIsLoadModel()
          || !modelPath.equals(mCurrentSettingsFields.getModelFilePath())
          || !tokenizerPath.equals(mCurrentSettingsFields.getTokenizerFilePath())
          || temperature != mCurrentSettingsFields.getTemperature()) {
        loadLocalModelAndParameters(
            updatedSettingsFields.getModelFilePath(),
            updatedSettingsFields.getTokenizerFilePath(),
            (float) updatedSettingsFields.getTemperature());
        updatedSettingsFields.saveLoadModelAction(false);
        mDemoSharedPreferences.addSettings(updatedSettingsFields);
      }
    } else {
      askUserToSelectModel();
    }
  }


  private void downloadModelConfig(SettingsFields updatedSettingsFields) {
    ETLogging.getInstance().log(">> Download Model Config start...");
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        mLoadModelFromUrl.setUiElements(downloadingModelText, downloadProgressText, downloadProgressBar);
        try {
          mLoadModelFromUrl.downloadModelConfig(updatedSettingsFields);
        } catch (Exception e) {
          String errorMessage = "Model configuration download exception: " + e.getMessage();
          ETLogging.getInstance().log(errorMessage);
          // e.printStackTrace();
          mErrorReporting.showError(e.getMessage(), "Model configuration download exception");
          return;
        }
        updatedSettingsFields.saveDownloadModelConfigAction(false);
        mDemoSharedPreferences.addSettings(updatedSettingsFields);
        mErrorReporting.showError("Model configuration downloaded. Go to Settings to load the new models.", "Configuration Download");
      }
    });
    thread.start();
  }

  private void askUserToSelectModel() {
    String askLoadModel =
        "To get started, select the AI Model from the gear icon in the top right corner";
    Message askLoadModelMessage = new Message(askLoadModel, false, MessageType.SYSTEM, 0);
    ETLogging.getInstance().log(askLoadModel);
    runOnUiThread(
        () -> {
          mMessageAdapter.add(askLoadModelMessage);
          mMessageAdapter.notifyDataSetChanged();
        });
  }

  private void setupShowLogsButton() {
    ImageButton showLogsButton = requireViewById(R.id.showLogsButton);
    showLogsButton.setOnClickListener(
        view -> {
          Intent myIntent = new Intent(MainActivity.this, LogsActivity.class);
          MainActivity.this.startActivity(myIntent);
        });
  }

  private void setupMediaButton() {
    mAddMediaLayout = requireViewById(R.id.addMediaLayout);
    if (mAddMediaLayout != null) mAddMediaLayout.setVisibility(View.GONE); // We hide this initially

    ImageButton addMediaButton = requireViewById(R.id.addMediaButton);
    addMediaButton.setOnClickListener(
        view -> {
          if (mAddMediaLayout != null) mAddMediaLayout.setVisibility(View.VISIBLE);
        });

    mGalleryButton = requireViewById(R.id.galleryButton);
    mGalleryButton.setOnClickListener(
        view -> {
          // Launch the photo picker and let the user choose only images.
          mPickGallery.launch(
              new PickVisualMediaRequest.Builder()
                  .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                  .build());
        });
    mCameraButton = requireViewById(R.id.cameraButton);
    mCameraButton.setOnClickListener(
        view -> {
          Log.d("CameraRoll", "Check permission");
          if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
              != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                MainActivity.this,
                new String[] {Manifest.permission.CAMERA},
                REQUEST_IMAGE_CAPTURE);
          } else {
            launchCamera();
          }
        });
  }

  private void setupCameraRoll() {
    // Registers a camera roll activity launcher.
    mCameraRoll =
        registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            result -> {
              if (result && cameraImageUri != null) {
                Log.d("CameraRoll", "Photo saved to uri: " + cameraImageUri);
                if (mAddMediaLayout != null) mAddMediaLayout.setVisibility(View.GONE);
                List<Uri> uris = new ArrayList<>();
                uris.add(cameraImageUri);
                showMediaPreview(uris);
              } else {
                // Delete the temp image file based on the url since the photo is not successfully
                // taken
                if (cameraImageUri != null) {
                  ContentResolver contentResolver = MainActivity.this.getContentResolver();
                  contentResolver.delete(cameraImageUri, null, null);
                  Log.d("CameraRoll", "No photo taken. Delete temp uri");
                }
              }
            });
    mMediaPreviewConstraintLayout = requireViewById(R.id.mediaPreviewConstraintLayout);
    ImageButton mediaPreviewCloseButton = requireViewById(R.id.mediaPreviewCloseButton);
    mediaPreviewCloseButton.setOnClickListener(
        view -> {
          if (mMediaPreviewConstraintLayout != null) mMediaPreviewConstraintLayout.setVisibility(View.GONE);
          mSelectedImageUri = null;
        });

    ImageButton addMoreImageButton = requireViewById(R.id.addMoreImageButton);
    addMoreImageButton.setOnClickListener(
        view -> {
          Log.d("addMore", "clicked");
          if (mMediaPreviewConstraintLayout != null) mMediaPreviewConstraintLayout.setVisibility(View.GONE);
          // Direct user to select type of input
          if (mCameraButton != null) mCameraButton.callOnClick();
        });
  }

  private String updateMemoryUsage() {
    ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
    ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
    if (activityManager == null) {
      return "---";
    }
    activityManager.getMemoryInfo(memoryInfo);
    long totalMem = memoryInfo.totalMem / (1024 * 1024);
    long availableMem = memoryInfo.availMem / (1024 * 1024);
    long usedMem = totalMem - availableMem;
    return usedMem + "MB";
  }

  private void startMemoryUpdate() {
    mMemoryView = requireViewById(R.id.ram_usage_live);
    memoryUpdater =
        new Runnable() {
          @Override
          public void run() {
            if (mMemoryView != null) mMemoryView.setText(updateMemoryUsage());
            mMemoryUpdateHandler.postDelayed(this, 1000);
          }
        };
    mMemoryUpdateHandler.post(memoryUpdater);
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQUEST_IMAGE_CAPTURE && grantResults.length != 0) {
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        launchCamera();
      } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
        Log.d("CameraRoll", "Permission denied");
      }
    }
  }

  private void launchCamera() {
    ContentValues values = new ContentValues();
    values.put(MediaStore.Images.Media.TITLE, "New Picture");
    values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
    values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera/");
    cameraImageUri =
        MainActivity.this
            .getContentResolver()
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    mCameraRoll.launch(cameraImageUri);
  }

  private void setupGalleryPicker() {
    // Registers a photo picker activity launcher in single-select mode.
    mPickGallery =
        registerForActivityResult(
            new ActivityResultContracts.PickMultipleVisualMedia(MAX_NUM_OF_IMAGES),
            uris -> {
              if (!uris.isEmpty()) {
                Log.d("PhotoPicker", "Selected URIs: " + uris);
                if (mAddMediaLayout != null) mAddMediaLayout.setVisibility(View.GONE);
                for (Uri uri : uris) {
                  MainActivity.this
                      .getContentResolver()
                      .takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                showMediaPreview(uris);
              } else {
                Log.d("PhotoPicker", "No media selected");
              }
            });

    mMediaPreviewConstraintLayout = requireViewById(R.id.mediaPreviewConstraintLayout);
    ImageButton mediaPreviewCloseButton = requireViewById(R.id.mediaPreviewCloseButton);
    mediaPreviewCloseButton.setOnClickListener(
        view -> {
          if (mMediaPreviewConstraintLayout != null) mMediaPreviewConstraintLayout.setVisibility(View.GONE);
          mSelectedImageUri = null;
        });

    ImageButton addMoreImageButton = requireViewById(R.id.addMoreImageButton);
    addMoreImageButton.setOnClickListener(
        view -> {
          Log.d("addMore", "clicked");
          if (mMediaPreviewConstraintLayout != null) mMediaPreviewConstraintLayout.setVisibility(View.GONE);
          // Direct user to select type of input
          if (mGalleryButton != null) mGalleryButton.callOnClick();
        });
  }

  private List<ETImage> getProcessedImagesForModel(List<Uri> uris) {
    List<ETImage> imageList = new ArrayList<>();
    if (uris != null) {
      uris.forEach(
          (uri) -> {
            imageList.add(new ETImage(this.getContentResolver(), uri));
          });
    }
    return imageList;
  }

  private void showMediaPreview(List<Uri> uris) {
    if (mSelectedImageUri == null) {
      mSelectedImageUri = uris;
    } else {
      mSelectedImageUri.addAll(uris);
    }

    if (mSelectedImageUri.size() > MAX_NUM_OF_IMAGES) {
      mSelectedImageUri = mSelectedImageUri.subList(0, MAX_NUM_OF_IMAGES);
      Toast.makeText(
              this, "Only max " + MAX_NUM_OF_IMAGES + " images are allowed", Toast.LENGTH_SHORT)
          .show();
    }
    Log.d("mSelectedImageUri", mSelectedImageUri.size() + " " + mSelectedImageUri);

    if (mMediaPreviewConstraintLayout != null) mMediaPreviewConstraintLayout.setVisibility(View.VISIBLE);

    List<ImageView> imageViews = new ArrayList<ImageView>();

    // Pre-populate all the image views that are available from the layout (currently max 5)
    imageViews.add(requireViewById(R.id.mediaPreviewImageView1));
    imageViews.add(requireViewById(R.id.mediaPreviewImageView2));
    imageViews.add(requireViewById(R.id.mediaPreviewImageView3));
    imageViews.add(requireViewById(R.id.mediaPreviewImageView4));
    imageViews.add(requireViewById(R.id.mediaPreviewImageView5));

    // Hide all the image views (reset state)
    for (int i = 0; i < imageViews.size(); i++) {
      if (imageViews.get(i) != null) imageViews.get(i).setVisibility(View.GONE);
    }

    // Only show/render those that have proper Image URIs
    for (int i = 0; i < mSelectedImageUri.size(); i++) {
      if (imageViews.get(i) != null) imageViews.get(i).setVisibility(View.VISIBLE);
      if (imageViews.get(i) != null) imageViews.get(i).setImageURI(mSelectedImageUri.get(i));
    }

    // For LLava, we want to call prefill_image as soon as an image is selected
    // Llava only support 1 image for now
    if (mCurrentSettingsFields.getModelType() == ModelType.LLAVA_1_5) {
      List<ETImage> processedImageList = getProcessedImagesForModel(mSelectedImageUri);
      if (!processedImageList.isEmpty()) {
        mMessageAdapter.add(
            new Message("Llava - Starting image Prefill.", false, MessageType.SYSTEM, 0));
        mMessageAdapter.notifyDataSetChanged();
        Runnable runnable =
            () -> {
              Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);
              ETLogging.getInstance().log("Starting runnable prefill image");
              ETImage img = processedImageList.get(0);
              ETLogging.getInstance().log("Llava start prefill image");
              startPos =
                  mModule.prefillImages(
                      img.getInts(),
                      img.getWidth(),
                      img.getHeight(),
                      ModelUtils.VISION_MODEL_IMAGE_CHANNELS,
                      startPos);
            };
        executor.execute(runnable);
      }
    }
  }

  private void addSelectedImagesToChatThread(List<Uri> selectedImageUri) {
    if (selectedImageUri == null) {
      return;
    }
    if (mMediaPreviewConstraintLayout != null) mMediaPreviewConstraintLayout.setVisibility(View.GONE);
    for (int i = 0; i < selectedImageUri.size(); i++) {
      Uri imageURI = selectedImageUri.get(i);
      Log.d("image uri ", "test " + imageURI.getPath());
      mMessageAdapter.add(new Message(imageURI.toString(), true, MessageType.IMAGE, 0));
    }
    mMessageAdapter.notifyDataSetChanged();
  }

  private void populateExistingMessages(String existingMsgJSON) {
    Gson gson = new Gson();
    Type type = new TypeToken<ArrayList<Message>>() {}.getType();
    ArrayList<Message> savedMessages = gson.fromJson(existingMsgJSON, type);
    for (Message msg : savedMessages) {
      mMessageAdapter.add(msg);
    }
    mMessageAdapter.notifyDataSetChanged();
  }

  private int setPromptID() {
    return mMessageAdapter.getMaxPromptID() + 1;
  }
}
