package org.ouanu.manager.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DownloadFileExample {
    public static void main(String[] args) {
        try {
            String fileName= "com.netflix.ninja.apk";
            File downloadDir = new File("./tmp/downloads");
            if (!downloadDir.exists() || !downloadDir.isDirectory()) {
                if (!downloadDir.mkdirs()) {
                    throw new FileNotFoundException("./tmp/downloads does not exists.");
                }
            }
            Path savePath = Paths.get(downloadDir.getPath(), fileName);

            Files.createDirectories(savePath.getParent());

            String result = AdvancedFileDownloadClient.downloadFileWithProgress(
                    fileName,
                    savePath,
                    (bytesDownloaded, totalBytes, progress) -> {
                        if (totalBytes > 0) {
                            System.out.printf("\rDownload Progress: %d/%d bytes (%d%%)",
                                    bytesDownloaded, totalBytes, progress);
                        } else {
                            System.out.printf("\rDownloaded: %d bytes", bytesDownloaded);
                        }
                    });
            System.out.println(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
