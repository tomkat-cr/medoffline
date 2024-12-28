package com.example.medoffline;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.RequestBody;
import okhttp3.MediaType;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StatFs;
import android.util.Log;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.Map;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.json.JSONException;
import org.json.JSONObject;

public class LocalModelManagement {
    private static final String TAG = "LocalModelManagement";
    private static final int DEFAULT_TIMEOUT_MINUTES = 10;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000; // 2 seconds base delay
    private static DownloadProgressListener progressListener;

    public interface DownloadProgressListener {
        void onProgressUpdate(long bytesRead, long contentLength, boolean done);
        void onError(String error);
    }

    public static void setDownloadProgressListener(DownloadProgressListener listener) {
        progressListener = listener;
    }

    public static boolean checkInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean result = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        Log.i(TAG, ">>> checkInternetConnection: " + result);
        return result;
    }
    
    public static boolean checkWifiConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        // return activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean result = mWifi.isConnected();
        Log.i(TAG, ">>> checkWifiConnection: " + result);
        return result;
    }

    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB", "PB", "EB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static boolean checkStorageSpace(String path, long requiredSpace) {
        StatFs stat = new StatFs(path);
        long availableBytes = stat.getAvailableBytes();
        return availableBytes >= requiredSpace;
    }

    public static void downloadFileFromUrl(Context context, String url, String destPath, long requiredSpace, int timeoutMinutes) throws ExecutionException, InterruptedException, IOException {
        if (!checkInternetConnection(context)) {
            String error = "No internet connection available";
            Log.e(TAG, error);
            if (progressListener != null) progressListener.onError(error);
            throw new IOException(error);
        }

        if (!checkStorageSpace(new File(destPath).getParent(), requiredSpace)) {
            String error = "Insufficient storage space. Need at least " + readableFileSize(requiredSpace) + " free";
            Log.e(TAG, error);
            if (progressListener != null) progressListener.onError(error);
            throw new IOException(error);
        }

        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                Log.i(TAG, "Download attempt " + attempt + " of " + MAX_RETRIES);
                if (progressListener != null) {
                    progressListener.onProgressUpdate(0, 100, false);
                }
                downloadWithProgress(url, destPath, timeoutMinutes);
                return;
            } catch (IOException e) {
                lastException = e;
                String error = "Download failed (attempt " + attempt + "): " + e.getMessage();
                Log.e(TAG, error, e);
                
                // Check if it's a rate limit error
                if (e.getMessage() != null && e.getMessage().toLowerCase().contains("rate limit")) {
                    error = "Rate limit exceeded. Waiting before retry...";
                    Log.i(TAG, error);
                    if (progressListener != null) {
                        progressListener.onError(error);
                    }
                    // Wait longer for rate limit errors (exponential backoff)
                    Thread.sleep(RETRY_DELAY_MS * attempt * attempt);
                } else {
                    // Normal retry delay with exponential backoff
                    Thread.sleep(RETRY_DELAY_MS * attempt);
                }
                
                if (progressListener != null) {
                    progressListener.onError(error);
                }
            }
        }
        
        // If we get here, all attempts failed
        if (lastException != null) {
            throw new IOException("All download attempts failed. Last error: " + lastException.getMessage(), lastException);
        }
    }

    public static boolean checkFileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static void deleteDestFiles(String destPath) {
        // Clean up destination file(s) if it exists
        // File tempFile = new File(destPath);
        // if (tempFile.exists()) {
        //     tempFile.delete();
        // }
        File[] tmpFiles = new File(destPath).listFiles();
        if (tmpFiles != null) {
            for (File file : tmpFiles) {
                file.delete();
            }
        }
    }

    private static void downloadWithProgress(
        String url,
        String destPath,
        int timeoutMinutes
    ) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(timeoutMinutes, TimeUnit.MINUTES)
                .readTimeout(timeoutMinutes, TimeUnit.MINUTES)
                .writeTimeout(timeoutMinutes, TimeUnit.MINUTES)
                .retryOnConnectionFailure(true)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Connection", "keep-alive")
                .addHeader("Accept", "*/*")
                .addHeader("User-Agent", "MedOffLine-Android")
                .build();

        Response response;
        try {
            response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                String error = String.format("Download failed with code %d: %s - %s",
                        response.code(), response.message(), errorBody);
                Log.e(TAG, error);
                if (progressListener != null) {
                    progressListener.onError(error);
                }
                throw new IOException(error);
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Response body is null");
            }

            File outputFile = new File(destPath);
            File tempFile = new File(destPath + ".tmp");

            // Ensure parent directory exists
            File parentDir = outputFile.getParentFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + parentDir);
            }

            // Remove all .tmp files if they exist
            deleteDestFiles(destPath + ".tmp");

            // Download to temporary file first
            try (
                    InputStream input = body.byteStream();
                    FileOutputStream output = new FileOutputStream(tempFile)
            ) {

                byte[] buffer = new byte[8192];
                long totalBytesRead = 0;
                long contentLength = body.contentLength();
                int bytesRead;
                long lastProgressUpdate = System.currentTimeMillis();

                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastProgressUpdate > 100) { // Update progress every 100ms
                        if (progressListener != null) {
                            progressListener.onProgressUpdate(totalBytesRead, contentLength, false);
                        }
                        lastProgressUpdate = currentTime;
                    }
                }

                // Verify download completed successfully
                if (contentLength > 0 && totalBytesRead != contentLength) {
                    throw new IOException("Download incomplete: expected " + contentLength + " bytes but got " + totalBytesRead);
                }

                // Final progress update
                if (progressListener != null) {
                    progressListener.onProgressUpdate(totalBytesRead, contentLength, true);
                }
            }

            // Remove destination files if they exist
            deleteDestFiles(destPath);

            // Move temp file to final destination
            if (!tempFile.renameTo(outputFile)) {
                throw new IOException("Failed to move temporary file to final destination");
            }

        } catch (IOException e) {
            // Clean up temp file if it exists
            deleteDestFiles(destPath + ".tmp");
            throw e;
        // } finally {
        //     if (response != null) {
        //         response.close();
        //     }
        }
    }

    private static JSONObject httpRequestWithProgress(
        String url,
        String contentType,
        String body,
        Map<String, String> headers,
        int timeoutMinutes
    ) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(timeoutMinutes, TimeUnit.MINUTES)
            .readTimeout(timeoutMinutes, TimeUnit.MINUTES)
            .writeTimeout(timeoutMinutes, TimeUnit.MINUTES)
            .retryOnConnectionFailure(true)
            .build();

        RequestBody requestBody = RequestBody.create(MediaType.get(contentType), body);

        AtomicReference<Request> request = new AtomicReference<>(new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Connection", "keep-alive")
                .addHeader("Accept", "*/*")
                .addHeader("User-Agent", "MedOffLine-Android")
                .build());

        headers.forEach((key, value) -> request.set(request.get().newBuilder().addHeader(key, value).build()));

        try {
            if (progressListener != null) {
                progressListener.onProgressUpdate(0, 100, false);
            }

            Response response = null;
            response = client.newCall(request.get()).execute();

            if (progressListener != null) {
                progressListener.onProgressUpdate(50, 100, false);
            }

            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                String error = String.format("Download failed with code %d: %s - %s", 
                    response.code(), response.message(), errorBody);
                Log.e(TAG, error);
                if (progressListener != null) {
                    progressListener.onError(error);
                }
                throw new IOException(error);
            }

            // long contentLength = response.body().contentLength();
            // long totalBytesRead = 0;
            // long lastProgressUpdate = System.currentTimeMillis();

            // InputStream in = response.body().byteStream();
            // byte[] buffer = new byte[1024];
            // int bytesRead;
            // while ((bytesRead = in.read(buffer)) != -1) {
            //     totalBytesRead += bytesRead;
            //     long currentTime = System.currentTimeMillis();
            //     if (currentTime - lastProgressUpdate > 100) { // Update progress every 100ms
            //         if (progressListener != null) {
            //             progressListener.onProgressUpdate(totalBytesRead, contentLength, false);
            //         }
            //         lastProgressUpdate = currentTime;
            //     }
            // }
            // if (!response.isSuccessful()) {
            //     String errorBody = response.body() != null ? response.body().string() : "No error body";
            //     String error = String.format("Download failed with code %d: %s - %s", 
            //         response.code(), response.message(), errorBody);
            //     Log.e(TAG, error);
            //     if (progressListener != null) {
            //         progressListener.onError(error);
            //     }
            //     throw new IOException(error);
            // }
            ResponseBody responseBody = response.body();
            if (progressListener != null) {
                progressListener.onProgressUpdate(100, 100, false);
            }

            if (responseBody == null) {
                throw new IOException("Response body is null");
            }

            return new JSONObject(responseBody.string());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static void unzipGz(String gzFilePath, String destPath) throws IOException {
        Log.i(TAG, "Starting extraction of " + gzFilePath + " to " + destPath);
        File tarFile = null;
        
        try {
            new File(destPath).mkdirs();
            long startTime = System.currentTimeMillis();

            try (FileInputStream fis = new FileInputStream(gzFilePath);
                 GzipCompressorInputStream gzis = new GzipCompressorInputStream(fis);
                 TarArchiveInputStream tais = new TarArchiveInputStream(gzis)) {

                TarArchiveEntry entry;
                int filesExtracted = 0;
                
                while ((entry = tais.getNextTarEntry()) != null) {
                    if (entry.isDirectory()) continue;
                    
                    File outputFile = new File(destPath, entry.getName());
                    Log.d(TAG, "Extracting: " + outputFile.getName());
                    
                    if (!outputFile.getParentFile().exists()) {
                        outputFile.getParentFile().mkdirs();
                    }
                    
                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = tais.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                    }
                    filesExtracted++;
                }

                long duration = System.currentTimeMillis() - startTime;
                Log.i(TAG, String.format("Extraction completed: %d files in %.2f seconds", 
                    filesExtracted, duration/1000.0));
            }

            // Delete the original .tar.gz file after successful extraction
            if (!new File(gzFilePath).delete()) {
                Log.w(TAG, "Failed to delete " + gzFilePath);
            } else {
                Log.i(TAG, "Successfully deleted " + gzFilePath);
            }

        } catch (IOException e) {
            Log.e(TAG, "Error during extraction: " + e.getMessage(), e);
            throw e;
        } finally {
            if (tarFile != null && tarFile.exists()) {
                tarFile.delete();
            }
        }
    }

    public static void unzip(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        if (!dir.exists())
            dir.mkdirs(); // Create destination directory if it doesn't exist

        try (ZipInputStream zipInputStream = new ZipInputStream(
                new BufferedInputStream(Files.newInputStream(Paths.get(zipFilePath))))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                File file = new File(dir, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs(); // Create directory
                } else {
                    // Create parent directories if necessary
                    new File(file.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[2048];
                        int len;
                        while ((len = zipInputStream.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipInputStream.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean copyAssetToPrivateStorage(Context context, String assetName, String destPath) {
        try {
            File destFile = new File(destPath);
            if (!destFile.exists()) {
                // Create parent directories if they don't exist
                destFile.getParentFile().mkdirs();
                
                // Copy the file from assets to private storage
                try (InputStream in = context.getAssets().open(assetName);
                     FileOutputStream out = new FileOutputStream(destFile)) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                }
                Log.i(TAG, "Successfully copied asset " + assetName + " to " + destPath);
                return true;
            } else {
                Log.i(TAG, "Model file already exists at " + destPath);
                return true;
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy asset " + assetName + " to " + destPath + ": " + e.getMessage(), e);
            return false;
        }
    }
}