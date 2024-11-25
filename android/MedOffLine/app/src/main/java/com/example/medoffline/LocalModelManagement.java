package com.example.medoffline;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.io.IOException;
import java.util.zip.GZIPInputStream;

import android.os.AsyncTask;
import android.os.Build;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

public class LocalModelManagement {

    // public static void main(String[] args) {
    //     LocalModelManagement localModelManagement = new LocalModelManagement();

    public static void downloadZipFile(String url, String destPath) throws ExecutionException, InterruptedException {
        new DownloadZipFileTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, destPath).get();
    }

    private static class DownloadZipFileTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String url = params[0];
            String destPath = params[1];

            System.out.println(">> Downloading " + url + " to " + destPath);

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                File outputFile = new File(destPath);

                System.out.println(">> Writting downloaded file " + url + " to " + destPath);

                try (InputStream inputStream = response.body().byteStream();
                     FileOutputStream outputStream = new FileOutputStream(outputFile)) {

                    byte[] buffer = new byte[2048];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    System.out.println(">> DONE! Downloaded " + url + " to " + destPath);
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
//            if (success) {
//                future.complete(true);
//            } else {
//                future.completeExceptionally(new IOException("Download failed"));
//            }
        }
    }

    public static void unzipGz(String tarGzFilePath, String destinationDir) throws IOException {
        System.out.println("Unzipping " + tarGzFilePath + " to " + destinationDir);
        File destDir = new File(destinationDir);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try (InputStream fileInputStream = new FileInputStream(new File(tarGzFilePath));
                 GzipCompressorInputStream gzipInputStream = new GzipCompressorInputStream(fileInputStream);
                 TarArchiveInputStream tarInputStream = new TarArchiveInputStream(gzipInputStream)) {

                TarArchiveEntry entry;
                while ((entry = tarInputStream.getNextTarEntry()) != null) {
                    File outputFile = new File(destinationDir, entry.getName());
                    if (entry.isDirectory()) {
                        outputFile.mkdirs();
                    } else {
                        // Ensure parent directory exists
                        outputFile.getParentFile().mkdirs();
                        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = tarInputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, len);
                            }
                        }
                    }
                }
            }
        // }
    }
    
    public static void unzip(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        if (!dir.exists()) dir.mkdirs(); // Create destination directory if it doesn't exist
    
        try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFilePath)))) {
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
}    