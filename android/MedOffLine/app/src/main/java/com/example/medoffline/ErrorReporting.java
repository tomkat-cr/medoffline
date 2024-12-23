package com.example.medoffline;

// import static java.lang.System.exit;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

class ErrorReporting {
    private final Context context;

    public ErrorReporting(Context context) {
        this.context = context;
    }

    public boolean showError(String error, String title) {
        final boolean[] retryPressed = {false};
        CountDownLatch latch = new CountDownLatch(1);

        ETLogging.getInstance().log(title + ": " + error);

        new Handler(Looper.getMainLooper()).post(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title)
                    .setMessage(error)
                    .setPositiveButton("Retry", (dialog, which) -> {
                        retryPressed[0] = true;
                        latch.countDown();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> latch.countDown())
                    .setCancelable(false)
                    .show();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return retryPressed[0];
    }

    public boolean showError(String error) {
        return showError(error, "Error");
    }
}