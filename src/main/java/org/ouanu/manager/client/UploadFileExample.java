package org.ouanu.manager.client;

import org.ouanu.manager.common.ResponseResult;
import org.ouanu.manager.request.ApplicationInfoRequest;
import org.ouanu.manager.response.ApplicationResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class UploadFileExample {
    public static void main(String[] args) {
        try {
//            Path singleFile = Path.of("C:\\Users\\Administrator\\Desktop\\netflix.apk");
//            String uploadResponse = FileUploadClient.uploadFile(singleFile, new String[]{});
//            System.out.println("Upload Response: " + uploadResponse);


            Path[] paths = new Path[]{
                    Path.of("F:\\desktop\\foreign\\preinstall\\AptoideTV.apk"),
                    Path.of("F:\\desktop\\foreign\\preinstall\\Disney.apk"),
                    Path.of("F:\\desktop\\foreign\\preinstall\\Game.apk"),
                    Path.of("F:\\desktop\\foreign\\preinstall\\magistv.apk"),
                    Path.of("F:\\desktop\\foreign\\preinstall\\netflix.apk"),
                    Path.of("F:\\desktop\\foreign\\preinstall\\PrimeVideo.apk"),
                    Path.of("F:\\desktop\\foreign\\preinstall\\youtube.apk"),
            };
            ResponseResult<List<ApplicationResponse>> result = FileUploadClient.uploadMultipleFiles(paths,
                    new String[]{"en", "cn", "fr", "ba"},
                    (bytesUploaded, totalBytes, percentage) ->
                    {
                        if (bytesUploaded > 0) {
                            System.out.printf("Upload Progress: %d/%d bytes (%d%%)%n", bytesUploaded, totalBytes, percentage);
                        } else {
                            System.out.printf("\rDownloaded: %d bytes", bytesUploaded);
                        }
                    }
            );

            if (result.getCode() == 200) {
                List<ApplicationResponse> data = result.getData();
                for (ApplicationResponse application : data) {
                    System.out.println();
                    System.out.println(application.toString());
                }
                Thread.sleep(2000);
                List<ApplicationInfoRequest> list = data.stream().map(ApplicationInfoRequest::fromEntity).toList();
                int size = 0;
                for (ApplicationInfoRequest applicationInfoRequest : list) {
//                    if (size > 2) break;
//                    size ++;
                    ResponseResult<String> stringResponseResult = FileUploadClient.saveOne(applicationInfoRequest);
                    System.out.println(stringResponseResult.getMessage());
                    System.out.println(stringResponseResult.getData());

                    Thread.sleep(1500);
                }


            } else {
                throw new IOException("上传失败: " + result.getMessage());
            }

//            List<ApplicationResponse> all = FileUploadClient.findAll();
//            if (all != null) {
//                for (ApplicationResponse applicationResponse : all) {
//                    System.out.println();
//                    System.out.println(applicationResponse.toString());
//                }
//            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
