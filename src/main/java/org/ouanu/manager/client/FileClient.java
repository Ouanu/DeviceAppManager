package org.ouanu.manager.client;

import org.ouanu.manager.apk.ApkManifestReader;
import org.ouanu.manager.dto.UserDto;
import org.ouanu.manager.response.ApplicationResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class FileClient {
    private static final String BASE_APP_URL = "http://127.0.0.1:8081/api/app"; // 替换为你的服务器地址
    public static void main(String[] args) {
        try {
            HttpHeaders headers = new HttpHeaders();
//            headers.set("Authorization", authToken);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // 构建带参数的URI
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_APP_URL + "/list");

            RestTemplate restTemplate = new RestTemplate();
            // 直接解析为List<UserDto>
            ResponseEntity<List<ApplicationResponse>> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {
                    }
            );
            Handler handler = new ConsoleHandler();
            handler.setEncoding("UTF-8");
            Logger logger = Logger.getLogger(ApkManifestReader.class.getName());
            logger.addHandler(handler);
            logger.setUseParentHandlers(false);
            List<ApplicationResponse> body = response.getBody();
            for (ApplicationResponse applicationResponse : body) {
                System.out.println();
                System.out.println("PackageName:" + applicationResponse.getPackageName());
                System.out.println("Label:" + applicationResponse.getLabel());
                System.out.println("FileName:" + applicationResponse.getFileName());
                System.out.println("VersionName:" + applicationResponse.getVersionName());
                System.out.println("VersionCode:" + applicationResponse.getVersionCode());
                System.out.println("Size:" + applicationResponse.getSize());
                String[] split = applicationResponse.getAppNames().split(",");
                logger.info("AppNames:" + Arrays.toString(split));
                System.out.println("UploadTime:" + applicationResponse.getUploadTime());
                System.out.println();
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 403) {
                System.out.println("Client error: Access denied.");
            } else {
                System.out.println("Client error: " + e.getMessage());
            }
        } catch (HttpServerErrorException e) {
//            log.error("HTTP服务端错误: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            System.out.println("Server error: " + e.getStatusText());
        } catch (Exception e) {
//            log.error("请求异常", e);
            System.out.println("Request exception: " + e.getMessage());
        }
    }
}
