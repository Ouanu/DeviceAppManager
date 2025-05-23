package org.ouanu.manager.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.ouanu.manager.dto.ResponseResult;
import org.ouanu.manager.record.LoginRequest;
import org.ouanu.manager.record.RegisterRequest;
import org.ouanu.manager.record.TokenResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class TestClient {
    private static final String BASE_URL = "http://127.0.0.1:8081/api/auth"; // 替换为你的服务器地址
    private static TestClient client = new TestClient();
    public static void main(String[] args) {
        try {
            LoginRequest loginRequest = new LoginRequest("newuser", "A3123131312");
            ResponseResult<TokenResponse> loginResponse = client.login(loginRequest);

            if (loginResponse.getCode() == 200) {
                System.out.println("success，token: " + loginResponse.getData().token());
            } else {
                System.out.println("failure: " + loginResponse.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
////            RegisterRequest registerRequest = new RegisterRequest("newuser", "newuser@qq.com", "13800138000", "A3123131312");
//            RegisterRequest registerRequest = new RegisterRequest("newuser2", "newuser2@qq.com", "13800138002", "B3123131312");
//            ResponseResult<String> registerResponse = client.register(registerRequest);
//
//            if (registerResponse.getCode() == 201) {
//                System.out.println("注册成功");
//            } else {
//                System.out.println("注册失败: " + registerResponse.getMessage());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }
    private static final Gson gson = new Gson();

    public ResponseResult<TokenResponse> login(LoginRequest request) throws IOException {
        URL url = new URL(BASE_URL + "/login");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "Java Client");
//            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            connection.setDoOutput(true);

            // 设置超时
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            // 调试：打印请求信息
            System.out.println("Sending request to: " + url);
            System.out.println("Request body: " + gson.toJson(request));

            try (OutputStream os = connection.getOutputStream()) {
                String requestBody = gson.toJson(request);
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            String responseBody;

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            responseCode < 400 ?
                                    connection.getInputStream() :
                                    connection.getErrorStream(),
                            StandardCharsets.UTF_8))) {
                responseBody = br.lines().collect(Collectors.joining());
            }

            // 调试：打印响应信息
            System.out.println("Response code: " + responseCode);
            System.out.println("Response body: " + responseBody);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                return gson.fromJson(responseBody,
                        new TypeToken<ResponseResult<TokenResponse>>(){}.getType());
            } else {
                throw new IOException("HTTP error code: " + responseCode + ", body: " + responseBody);
            }
        } finally {
            connection.disconnect();
        }
    }


    public ResponseResult<String> register(RegisterRequest request) throws IOException {
        URL url = new URL(BASE_URL + "/register");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            String requestBody = gson.toJson(request);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            responseCode < 400 ?
                                    connection.getInputStream() :
                                    connection.getErrorStream(),
                            StandardCharsets.UTF_8))) {
                String response = br.lines().collect(Collectors.joining());
                return gson.fromJson(response, new TypeToken<ResponseResult<String>>(){}.getType());
            }
        } finally {
            connection.disconnect();
        }
    }


}
