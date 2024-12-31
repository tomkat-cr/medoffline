# CHANGELOG

All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/) and [Keep a Changelog](http://keepachangelog.com/).



## Unreleased
---

### New

### Changes

### Fixes

### Breaks


## 0.0.4 (2024-12-30)
---

### New
Implement API keys to GS BE Core [GS-159].
Implement backoffice [GS-147].

### Changes
README: fix the english video presentation URL, and links to Kaggle updated to use the 1B model [GS-147].



## 0.0.3 (2024-12-29)
---

### New
Android app: add "non-Wifi connection" warning when loading models.
Android app: add a progress percent indicator for the http request that downloads models.
Android app: add load updated configuration file from URL, as an alternative for the "default_model_db.json".
Android app: add the "medoffline_default_model_db.json" file to test updated models configuration file.
Android app: add "genericDownloadFile" to share the logic between the models and config files download.
Android app: add getModelData() to ModelsConfig class to have the complete selected model object.
Android app: add version number to main android app screen.
Android app: add option to delete downloaded configuration.
Android app: add option to delete downloaded models.
Android app: add a warning message about model answers in the main activity chat layout.
Android app: add warning message to android app footer mentioning that "this App is a prototype".
Android app: change memory usage in Gb.
Webpage: add the URL of the video presentation in english.
Webpage: add 1B model in the Source Code page.
Webpage: APK download instructions page replaces the direct APK download button in the main webpage.
Webpage: add a warning message on download APK mentioning that "this App is a prototype".
README: add cover image for the english documentation.

### Changes
Android app: automatic model download and load at startup disabled and moved to the Settings.
Android app: update URL for model configuration download to use "carlosjramirez.com" instead of "medoffline.aclics.com".
Android app: new logo for the android app icon.
Android app: ModelsConfig class accepts alternative config file in directories different than the assets dir.
Android app: "tokenizer.model" file renamed to "<model_file_name>.bin"
Android app: "showError" method renamed to "showDialogOneOrTwoButtons" and "showErrorCancel", "showErrorContinue", "showDialogOk", "showDialogOkCancel", "showDialogYesNo" method were added.
Android app: code clean up.
Android app: 'buildgradle.kts" file was replaced by "build.gradle", so it was removed.
Android app: the advanced settings collapse was implemented and elements are collapsed by default.
Android app: when a model is downloaded, automatically is loaded and makes it the selected model.
Android app: calculate the disk space required based on the size of the model+tokenizer x 2.
Android app: removed "local_download" from the models configuration.
Android app: the blue stripe with the name of the App was added to all screens.
Android app: add constant ACTIVITY_DEBUG = false; to clear the logs.
Android app: model download timeout was increased: DEFAULT_TIMEOUT_MINUTES = 20.
Website: new logo on the footer of all the pages.
Version number changed to "0.0.3" in all required places.

### Fixes
Android app: fix model load error and add error handling.
Android app: fix "Failed to move temporary file to final destination" error message when the downloaded tar file is decompressed.
Android app: assign default values for settings moved to the onResume method.
Android app: PerfTest was using the old tokenizer name "tokenizer.model".
Android app: fix the showError dialog because it wasn't working on the MainActivity.
Android app: fix the default temperature value as double.
Android app: fix model size of 1B in model configuration.
Android app: set the model download/load flags to false when the operation is cancelled.
Android app: the download/upload buttons for models and/or configurations are processed even there's no changes to other settings.
Android app: disable the chat message sending button when a model is loading/unloading.
[GS-147]

## 0.0.2 (2024-11-30)
---

### New
Landing page: add source code, presentations, team and invalid URL pages.

### Changes
All README.md sections translated to spanish and english.
[GS-147]


## 0.0.1 (2024-11-22)
---

### New
Project started for the [Hackathon Llama Impact Pan-LATAM](https://lablab.ai/event/hackathon-llama-impact-pan-latam-es) organized by [Lablab.ai](https://lablab.ai) [GS-156].
