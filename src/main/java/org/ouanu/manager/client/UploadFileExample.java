package org.ouanu.manager.client;

import java.io.IOException;
import java.nio.file.Path;

public class UploadFileExample {
    public static void main(String[] args) {
        try {
//            Path singleFile = Path.of("C:\\Users\\Administrator\\Desktop\\netflix.apk");
//            String uploadResponse = FileUploadClient.uploadFile(singleFile, new String[]{});
//            System.out.println("Upload Response: " + uploadResponse);

            Path[] paths = new Path[]{
                    Path.of("C:\\Users\\Administrator\\Desktop\\netflix.apk"),
                    Path.of("C:\\Users\\Administrator\\Desktop\\youtube.apk"),
                    Path.of("C:\\Users\\Administrator\\Desktop\\Game.apk"),
                    Path.of("C:\\Users\\Administrator\\Desktop\\weixin.apk"),
            };
            String s = FileUploadClient.uploadMultipleFiles(paths,
                    new String[]{},
                    (bytesUploaded, totalBytes, percentage) ->
                            System.out.printf("Upload Progress: %d/%d bytes (%d%%)%n", bytesUploaded, totalBytes, percentage)
            );
            System.out.println("Upload Response: \n" + s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
