package org.ouanu.manager.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class AdvancedFileDownloadClient {
    private static final String DOWNLOAD_URL = "http://localhost:8081/api/app/download/";

    public interface DownloadProgressListener {
        void onProgress(long bytesDownloaded, long totalBytes, int progress);
    }

    public static String downloadFileWithProgress(String packageName, Path savePath, DownloadProgressListener listener) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(DOWNLOAD_URL + packageName).openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            long fileSize = connection.getContentLengthLong();
            String fileName = getFileNameFromHeader(connection);

            try(InputStream ins = connection.getInputStream();
                OutputStream os = Files.newOutputStream(savePath)) {

                byte[] buffer = new byte[4096];
                long bytesDownloaded = 0;
                int bytesRead;

                while ((bytesRead = ins.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                    bytesDownloaded += bytesRead;

                    if (listener != null) {
                        int progress = fileSize > 0 ? (int) ((bytesDownloaded * 100) / fileSize) : -1;
                        listener.onProgress(bytesDownloaded, fileSize, progress);
                    }
                }
                return "\nFile download succeed: " + (fileName != null ? fileName : savePath.getFileName());
            }
        } else {
            handleErrorResponse(responseCode, packageName);
            return null;
        }
    }

    private static String getFileNameFromHeader(HttpURLConnection connection) {
        String contentDisposition = connection.getHeaderField("Content-Disposition");
        if (contentDisposition != null) {
            for (String part : contentDisposition.split(";")) {
                if (part.trim().startsWith("file=")) {
                    return part.substring(part.indexOf('=') + 1).replace("\"", "");
                }
            }
        }
        return null;
    }

    private static void handleErrorResponse(int responseCode, String packageName) throws IOException {
        switch (responseCode) {
            case HttpURLConnection.HTTP_NOT_FOUND ->
                throw new FileNotFoundException("File does not exists: " + packageName);
            case HttpURLConnection.HTTP_UNAUTHORIZED ->
                throw new IOException("Unauthorized.");
            case HttpURLConnection.HTTP_FORBIDDEN ->
                throw new IOException("Forbidden.");
            default ->
                throw new IOException("Server error: " + responseCode);
        }
    }
}
