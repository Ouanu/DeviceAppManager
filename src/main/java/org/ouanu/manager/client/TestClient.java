package org.ouanu.manager.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.ouanu.manager.dto.ResponseResult;
import org.ouanu.manager.dto.UserDto;
import org.ouanu.manager.model.User;
import org.ouanu.manager.record.LoginRequest;
import org.ouanu.manager.record.ManagerRegisterRequest;
import org.ouanu.manager.record.RegisterRequest;
import org.ouanu.manager.record.TokenResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TestClient {
    private static final String BASE_URL = "http://127.0.0.1:8081/api/auth"; // 替换为你的服务器地址
    private static final String BASE_MANAGER_URL = "http://127.0.0.1:8081/api/manager"; // 替换为你的服务器地址
    private static TestClient client = new TestClient();
    private static RestTemplate restTemplate = new RestTemplate();
    public static void main(String[] args) {
        TestClient client1 = new TestClient();
        String s = client1.testLogin();
//        client1.testManagerRegister("Manager", "M3123131312", "manager@example.com", "13800138003");
        client1.testGetUsers();
    }

//    private String testListUsers(String token) {
//
//    }

    private void testGetUsers() {
        try {
            // 1. 首先获取登录token
            LoginRequest loginRequest = new LoginRequest("Manager", "M3123131312");
            ResponseResult<TokenResponse> loginResponse = client.login(loginRequest);

            if (loginResponse == null || loginResponse.getCode() != 200) {
                System.out.println("登录失败: " +
                        (loginResponse != null ? loginResponse.getMessage() : "未知错误"));
                return;
            }
            String token = loginResponse.getData().token();
            System.out.println("登录成功, token: " + token);

            // 2. 使用获取的token访问用户列表
            List<UserDto> users = client.getAllUsers("Bearer " + token);
            System.out.println("成功获取用户列表:");
            users.forEach(user -> System.out.printf(
                    "ID: %d, Username: %s, Role: %s, Phone: %s\n",
                    user.getId(), user.getUsername(), user.getRole(), user.getPhone()));

        } catch (Exception e) {
            System.err.println("发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<UserDto> getAllUsers(String authToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // 直接解析为List<UserDto>
            ResponseEntity<List<UserDto>> response = restTemplate.exchange(
                    BASE_MANAGER_URL + "/list",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<List<UserDto>>() {}
            );

            return response.getBody();
        } catch (HttpClientErrorException e) {
//            log.error("HTTP客户端错误: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("获取用户列表失败: " + e.getStatusText());
        } catch (HttpServerErrorException e) {
//            log.error("HTTP服务端错误: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("服务端错误: " + e.getStatusText());
        } catch (Exception e) {
//            log.error("请求异常", e);
            throw new RuntimeException("请求异常: " + e.getMessage());
        }
    }


    private String testLogin() {
        try {
            LoginRequest loginRequest = new LoginRequest("newuser", "A3123131312");
            ResponseResult<TokenResponse> loginResponse = client.login(loginRequest);

            if (loginResponse != null && loginResponse.getCode() == 200) {
                System.out.println("success,token: " + loginResponse.getData().token());
                return loginResponse.getData().token();
            } else {
                System.out.println("failure: " +
                        (loginResponse != null ? loginResponse.getMessage() : "unknown error"));
                return "";
            }
        } catch (Exception e) {
            System.err.println("An exception has occurred: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    private void testRegister(String username, String password, String email, String phone) {
        try {
//            RegisterRequest registerRequest = new RegisterRequest("newuser", "newuser@qq.com", "13800138000", "A3123131312");
//            RegisterRequest registerRequest = new RegisterRequest("newuser2", "newuser2@qq.com", "13800138002", "B3123131312");
            RegisterRequest registerRequest = new RegisterRequest(username, email, phone, password);
            ResponseResult<String> registerResponse = client.register(registerRequest);

            if (registerResponse.getCode() == 201) {
                System.out.println("Register succeed");
            } else {
                System.out.println("Register failure: " + registerResponse.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void testManagerRegister(String username, String password, String email, String phone) {
        try {
            ManagerRegisterRequest registerRequest = new ManagerRegisterRequest(username, email, phone, password);
            ResponseResult<String> registerResponse = client.managerRegister(registerRequest);

            if (registerResponse.getCode() == 201) {
                System.out.println("Register succeed");
            } else {
                System.out.println("Register failure: " + registerResponse.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final Gson gson = new Gson();

    public ResponseResult<TokenResponse> login(LoginRequest request) throws IOException {
        URL url = new URL(BASE_URL + "/login");
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
            String response = readResponse(connection, responseCode);

            // 使用正确的TypeToken
            return gson.fromJson(response,
                    new TypeToken<ResponseResult<TokenResponse>>(){}.getType());
        } catch (Exception e) {
            System.err.println("Parsing response failed: " + e.getMessage());
            throw new IOException("Failed to process the login request: ", e);
        } finally {
            connection.disconnect();
        }
    }

    private String readResponse(HttpURLConnection conn, int responseCode) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        responseCode < 400 ?
                                conn.getInputStream() :
                                conn.getErrorStream(),
                        StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining());
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

    public ResponseResult<String> managerRegister(ManagerRegisterRequest request) throws IOException {
        URL url = new URL(BASE_MANAGER_URL + "/register");
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
