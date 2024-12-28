# CHANGELOG

All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/) and [Keep a Changelog](http://keepachangelog.com/).



## Unreleased
---

### New

### Changes

### Fixes

### Breaks


## 0.0.3 (2024-12-28)
---

### New
Android app: add "non-Wifi connection" warning when loading models.
Android app: add a progress percent indicator for the http request that downloads models.
Android app: add load updated configuration file from URL, as an alternative for the "default_model_db.json".
Android app: add the "medoffline_default_model_db.json" file to test updated models configuration file.
Android app: add "genericDownloadFile" to share the logic between the models and config files download.
Android app: add getModelData() to ModelsConfig class to have the complete selected model object.
Android app: add version number to main android app screen.
Android app: add a warning message about model answers in the main activity chat layout.
Android app: add warning messages on download APK and android app footer mentioning that "this App is a prototype".
Android app: change memory usage in Gb.
Webpage: add the URL of the video presentation in english.
Webpage: add 1B model in the Source Code page.
Webpage: add APK download instructions page replaces the direct APK download button in the main webpage.
README: add cover image for the english documentation.

### Changes
Android app: automatic model download and load at startup disabled and moved to the Settings.
Android app: update URL for model configuration download to use "carlosjramirez.com" instead of "medoffline.aclics.com".
Android app: code clean up.
Android app: new logo for the android app icon.
Android app: ModelsConfig class accepts alternative config file in directories different than the assets dir.
Android app: "tokenizer.model" file renamed to "<model_file_name>.bin"
Android app: "showError" method renamed to "showDialogOneOrTwoButtons" and "showErrorCancel", "showErrorContinue", "showDialogOk", "showDialogOkCancel", "showDialogYesNo" method were added.
Android app: 'buildgradle.kts" file was replaced by "build.gradle", so it was removed.
Website: new logo on the footer of all the pages.
Version number changed to "0.0.3" in all required places.

### Fixes
Android app: fix model load error and add error handling.
Android app: fix "Failed to move temporary file to final destination" error message when the downloaded tar file is decompressed.
Android app: assign default values for settings moved to the onResume method.
Android app: PerfTest was using the old tokenizer name "tokenizer.model".
Android app: fix the showError dialog because it wasn't working on the MainActivity.


## 0.0.2 (2024-11-30)
---

### New
Landing page: add source code, presentations, team and invalid URL pages.

### Changes
All README.md sections translated to spanish and english.


## 0.0.1 (2024-11-22)
---

### New
Project started for the [Hackathon Llama Impact Pan-LATAM](https://lablab.ai/event/hackathon-llama-impact-pan-latam-es) organized by [Lablab.ai](https://lablab.ai) [GS-155].
