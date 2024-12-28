package com.example.medoffline;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

// import java.util.concurrent.CountDownLatch;

class ErrorReporting {
    private final Context context;

    private final String defaultTitle = "Error";
    private final String defaultPositiveButtonText = "Retry";
    private final String defaultNegativeButtonText = "Cancel";

    public ErrorReporting(Context context) {
        this.context = context;
    }

    public void showDialogOneOrTwoButtons(
        String error,
        String title,
        String positiveButtonText,
        String negativeButtonText,
        Runnable positiveAction,
        Runnable negativeAction
    ) {
        ETLogging.getInstance().log("showDialogOneOrTwoButtons | begin...");

        ETLogging.getInstance().log("showDialogOneOrTwoButtons | " + title + ": " + error);

        // If no buttons were set, count down the latch
        if (positiveButtonText == null && negativeButtonText == null) {
            ETLogging.getInstance().log("showDialogOneOrTwoButtons | No buttons set, stopping...");
            return;
        }

        ((android.app.Activity) context).runOnUiThread(() -> {
            AlertDialog.Builder builder=new AlertDialog.Builder(context);

            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setMessage(error);

            if (title != null && !title.isEmpty()) {
                builder.setTitle(title);
            }
            if (positiveButtonText != null && !positiveButtonText.isEmpty()) {
                builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ETLogging.getInstance().log("showErrorCancel | Positive clicked");
                        if (positiveAction != null) {
                            positiveAction.run();
                        }
                        dialog.cancel();
                    }
                });
            }
            if (negativeButtonText != null && !negativeButtonText.isEmpty()) {
                builder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ETLogging.getInstance().log("showErrorCancel | Negative clicked");
                        if (negativeAction != null) {
                            negativeAction.run();
                        }
                        dialog.cancel();
                    }
                });
            }
            AlertDialog alertdialog=builder.create();
            alertdialog.show();
        });

        ETLogging.getInstance().log("showDialogOneOrTwoButtons | finished");
        return;
    }

    // public boolean showDialogOneOrTwoButtons(String error, String title, String positiveButtonText, String negativeButtonText) {
    //     ETLogging.getInstance().log("showDialogOneOrTwoButtons | begin...");
    //     final boolean[] retryPressed = {false};
    //     CountDownLatch latch = new CountDownLatch(1);

    //     ETLogging.getInstance().log("showDialogOneOrTwoButtons | " + title + ": " + error);
        
    //     if (context instanceof android.app.Activity) {
    //         ((android.app.Activity) context).runOnUiThread(() -> {
    //             ETLogging.getInstance().log("showDialogOneOrTwoButtons | starting builder...");
    //             AlertDialog.Builder builder = new AlertDialog.Builder(context);
    //             builder.setIcon(android.R.drawable.ic_dialog_alert);
    //             builder.setMessage(error);
    //             if (title != null && !title.isEmpty()) {
    //                 builder.setTitle(title);
    //             }
    //             if (positiveButtonText != null && !positiveButtonText.isEmpty()) {
    //                 builder.setPositiveButton(positiveButtonText, (dialog, which) -> {
    //                     retryPressed[0] = true;
    //                     latch.countDown();
    //                 });
    //             }
    //             if (negativeButtonText != null && !negativeButtonText.isEmpty()) {
    //                 builder.setNegativeButton(negativeButtonText, (dialog, which) -> latch.countDown());
    //             }
    //             builder.setCancelable(false);
    //             builder.show();
    //             ETLogging.getInstance().log("showDialogOneOrTwoButtons | dialog shown");
                
    //             // If no buttons were set, count down the latch
    //             if (positiveButtonText == null && negativeButtonText == null) {
    //                 ETLogging.getInstance().log("showDialogOneOrTwoButtons | No buttons set, count down the latch...");
    //                 latch.countDown();
    //             }
    //         });
    //     } else {
    //         ETLogging.getInstance().log("showDialogOneOrTwoButtons | Error: Context is not an Activity!");
    //         latch.countDown();
    //     }

    //     try {
    //         ETLogging.getInstance().log("showDialogOneOrTwoButtons | Starting await...");
    //         latch.await();
    //     } catch (InterruptedException e) {
    //         ETLogging.getInstance().log("showDialogOneOrTwoButtons | Interrupted...");
    //         Thread.currentThread().interrupt();
    //     } catch (Exception e) {
    //         ETLogging.getInstance().log("showDialogOneOrTwoButtons | Execution exception | e: " + e.getMessage());
    //     }

    //     ETLogging.getInstance().log("showDialogOneOrTwoButtons | returning the result | retryPressed[0]: " + retryPressed[0]);
    //     return retryPressed[0];
    // }

    public void showErrorRetry(String error, String title, Runnable positiveAction, Runnable negativeAction) {
        showDialogOneOrTwoButtons(error, title, defaultPositiveButtonText, defaultNegativeButtonText, positiveAction, negativeAction);
    }

    public void showErrorCancel(String error) {
        ETLogging.getInstance().log("showErrorCancel | begin...");
        showDialogOneOrTwoButtons(error, defaultTitle, null, defaultNegativeButtonText, null, null);
    }

    public void showErrorContinue(String error) {
        showDialogOneOrTwoButtons(error, defaultTitle, "Continue", defaultNegativeButtonText, null, null);
    }

    public void showDialogOk(String error, String title) {
        showDialogOneOrTwoButtons(error, title, "Ok", null, null, null);
    }

    public void showDialogOkCancel(String error, String title, Runnable positiveAction, Runnable negativeAction) {
        showDialogOneOrTwoButtons(error, title, "Ok", defaultNegativeButtonText, positiveAction, negativeAction);
    }

    public void showDialogYesNo(String error, String title, Runnable positiveAction, Runnable negativeAction) {
        ETLogging.getInstance().log("showDialogYesNo | begin...");
        showDialogOneOrTwoButtons(error, title, "Yes", "No", positiveAction, negativeAction);
    }
}