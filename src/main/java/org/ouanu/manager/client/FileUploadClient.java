package org.ouanu.manager.client;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUploadClient {
    private static final String UPLOAD_URL = "http://localhost:8081/api/app/upload";
    private static final String MULTI_UPLOAD_URL = "http://localhost:8081/api/app/multi-upload";

    // 单文件上传带进度显示
    public static String uploadFile(Path filePath, String[] banRegions, UploadProgressListener listener) throws IOException {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        HttpURLConnection connection = (HttpURLConnection) new URL(UPLOAD_URL).openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        // 获取文件总大小
        long fileSize = Files.size(filePath);
        long totalBytes = fileSize + calculateFormDataSize(boundary, filePath, banRegions);

        try (OutputStream os = connection.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true);
             InputStream fileInput = Files.newInputStream(filePath)) {

            // 写入文件部分头
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                    .append(filePath.getFileName().toString()).append("\"\r\n");
            writer.append("Content-Type: ").append(Files.probeContentType(filePath)).append("\r\n\r\n");
            writer.flush();

            // 写入文件内容（带进度回调）
            long bytesWritten = 0;
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInput.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
                bytesWritten += bytesRead;

                // 计算当前总上传字节数（包括表单数据）
                long currentTotalUploaded = bytesWritten + calculateFormDataBytesWritten(boundary, filePath, bytesWritten, fileSize);
                int percentage = (int) ((currentTotalUploaded * 100) / totalBytes);

                // 回调进度
                if (listener != null) {
                    listener.onProgress(currentTotalUploaded, totalBytes, percentage);
                }
            }
            os.flush();

            // 写入ban_regions参数
            writer.append("\r\n--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"ban_regions\"\r\n\r\n");
            writer.append(String.join(",", banRegions)).append("\r\n");

            // 结束标记
            writer.append("\r\n").append("--").append(boundary).append("--\r\n");
            writer.flush();

            // 表单数据部分已全部上传
            if (listener != null) {
                listener.onProgress(totalBytes, totalBytes, 100);
            }
        }

        // 读取响应
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            return response.toString();
        }
    }

    // 计算表单数据部分的总大小
    private static long calculateFormDataSize(String boundary, Path filePath, String[] banRegions) {
        String fileName = filePath.getFileName().toString();
        String fileHeader = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n" +
                "Content-Type: " + getContentType(filePath) + "\r\n\r\n";

        String regionsHeader = "\r\n--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"ban_regions\"\r\n\r\n" +
                String.join(",", banRegions) + "\r\n";

        String footer = "\r\n--" + boundary + "--\r\n";

        return fileHeader.length() + regionsHeader.length() + footer.length();
    }

    // 计算当前已写入的表单数据字节数
    private static long calculateFormDataBytesWritten(String boundary, Path filePath, long fileBytesWritten, long totalFileBytes) {
        String fileName = filePath.getFileName().toString();
        String fileHeader = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n" +
                "Content-Type: " + getContentType(filePath) + "\r\n\r\n";

        if (fileBytesWritten < totalFileBytes) {
            return fileHeader.length();
        } else {
            return fileHeader.length() + 2; // +2 for the \r\n after file content
        }
    }

    private static String getContentType(Path filePath) {
        try {
            return Files.probeContentType(filePath);
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    // 多文件上传带进度显示
    public static String uploadMultipleFiles(Path[] filePaths, String[] banRegions, UploadProgressListener listener) throws IOException {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        HttpURLConnection connection = (HttpURLConnection) new URL(MULTI_UPLOAD_URL).openConnection();

        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        // 计算总大小
        long totalBytes = calculateMultiFormDataSize(boundary, filePaths, banRegions);

        try (OutputStream os = connection.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"), true)) {

            long totalBytesWritten = 0;

            for (Path filePath : filePaths) {
                // 写入文件头
                String fileHeader = "--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"files\"; filename=\"" + filePath.getFileName().toString() + "\"\r\n" +
                        "Content-Type: " + getContentType(filePath) + "\r\n\r\n";

                writer.append(fileHeader);
                writer.flush();
                totalBytesWritten += fileHeader.length();

                // 更新进度
                updateProgress(listener, totalBytesWritten, totalBytes);

                // 写入文件内容
                try (InputStream fileInput = Files.newInputStream(filePath)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fileInput.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                        totalBytesWritten += bytesRead;
                        updateProgress(listener, totalBytesWritten, totalBytes);
                    }
                }

                // 写入文件分隔符
                writer.append("\r\n");
                writer.flush();
                totalBytesWritten += 2;
                updateProgress(listener, totalBytesWritten, totalBytes);
            }

            // 写入ban_regions参数
            String regionsHeader = "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"ban_regions\"\r\n\r\n" +
                    String.join(",", banRegions) + "\r\n";

            writer.append(regionsHeader);
            writer.flush();
            totalBytesWritten += regionsHeader.length();
            updateProgress(listener, totalBytesWritten, totalBytes);

            // 写入结束标记
            String footer = "--" + boundary + "--\r\n";
            writer.append(footer);
            writer.flush();
            totalBytesWritten += footer.length();

            // 最终进度更新
            if (listener != null) {
                listener.onProgress(totalBytes, totalBytes, 100);
            }
        }

        // 读取响应
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            return response.toString();
        }
    }

    // 计算多文件上传表单数据总大小
    private static long calculateMultiFormDataSize(String boundary, Path[] filePaths, String[] banRegions) throws IOException {
        long totalSize = 0;

        for (Path filePath : filePaths) {
            String fileHeader = "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"files\"; filename=\"" + filePath.getFileName().toString() + "\"\r\n" +
                    "Content-Type: " + getContentType(filePath) + "\r\n\r\n";

            totalSize += fileHeader.length() + Files.size(filePath) + 2; // +2 for \r\n
        }

        String regionsHeader = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"ban_regions\"\r\n\r\n" +
                String.join(",", banRegions) + "\r\n";

        String footer = "--" + boundary + "--\r\n";

        return totalSize + regionsHeader.length() + footer.length();
    }

    private static void updateProgress(UploadProgressListener listener, long bytesWritten, long totalBytes) {
        if (listener != null) {
            int percentage = (int) ((bytesWritten * 100) / totalBytes);
            listener.onProgress(bytesWritten, totalBytes, percentage);
        }
    }

    public interface UploadProgressListener {
        /**
         * 上传进度回调
         * @param bytesUploaded 已上传字节数
         * @param totalBytes 总字节数
         * @param percentage 上传百分比(0-100)
         */
        void onProgress(long bytesUploaded, long totalBytes, int percentage);
    }
}
