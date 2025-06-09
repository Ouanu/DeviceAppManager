package org.ouanu.manager.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.ouanu.manager.common.ResponseResult;
import org.ouanu.manager.dto.UserDto;
import org.ouanu.manager.request.*;
import org.ouanu.manager.response.UserResponse;
import org.ouanu.manager.response.UserTokenResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class TestClient {
    private static final String BASE_URL = "http://127.0.0.1:8081/api/auth"; // 替换为你的服务器地址
    private static final String BASE_MANAGER_URL = "http://127.0.0.1:8081/api/manager"; // 替换为你的服务器地址
    private static final String BASE_DEVICE_URL = "http://127.0.0.1:8081/api/device"; // 替换为你的服务器地址
    private static TestClient client = new TestClient();
    private static RestTemplate restTemplate = new RestTemplate();
    private static List<UserDto> dtos = new ArrayList<>();

    public static void main(String[] args) {
        TestClient client1 = new TestClient();

        client1.testDeviceRegister(
                "38:7a:cc:0f:4c:0e",
                "Test Device",
                "default",
                "57522640-ffbb-4232-86e3-966f655b371b",
                "57522640-ffbb-4232-86e3-966f655b371b",
                "Only for test"
        );

//        String s = client1.testLogin("newuser1", "B3123131311");
//        UserResponse user = client1.getUser(s);
//        System.out.println("user = " + user);
//        if (user != null) {
//            System.out.println("username = " + user.getUsername() + " uuid = " + user.getUuid() + " email = " + user.getEmail());
//        } else {
//            System.out.println("User wasn't exists");
//        }
//        client1.testManagerRegister("Manager", "M3123131310", "manager@example.com", "13800138000");
//        client1.testRegister("newuser1", "B3123131311", "newuser1@qq.com", "13800138001");
//        client1.testRegister("newuser2", "B3123131312", "newuser2@qq.com", "13800138002");
//        client1.testRegister("newuser3", "B3123131313", "newuser3@qq.com", "13800138003");
//        client1.testRegister("newuser4", "B3123131314", "newuser4@qq.com", "13800138004");
//        String token = client1.testGetUsers();
//        if (!dtos.isEmpty()) {
//            UserDto dto = dtos.get(1);
//            try {
//                System.out.println("delete user = " + dto.getUsername() + " uuid = " + dto.getUuid());
//                client1.testDeleteUser(token, dto.getUuid(), false);
//            } catch (Exception e) {
//                System.out.println(e);
//            }
//        }
//        client1.testGetUsers();

    }

//    private String testListUsers(String token) {
//
//    }

    public UserResponse getUser(String authToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + authToken);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // 构建带参数的URI
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL + "/me");

            // 直接解析为List<UserDto>
            ResponseEntity<ResponseResult<UserResponse>> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<ResponseResult<UserResponse>>() {
                    }
            );
            if (response.getBody() != null) {
                return response.getBody().getData();
            } else {
                return null;
            }
        } catch (HttpClientErrorException e) {
//            log.error("HTTP客户端错误: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            System.out.println("Failed to get user: " + e.getStatusText());
            return null;
        } catch (HttpServerErrorException e) {
//            log.error("HTTP服务端错误: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            System.out.println("Server error: " + e.getStatusText());
            return null;
        } catch (Exception e) {
//            log.error("请求异常", e);
            System.out.println("Request exception: " + e.getMessage());
            return null;
        }
    }

    private void testDeleteUser(String token, String uuid, boolean isHardDelete) throws IOException {
//        LoginRequest loginRequest = new LoginRequest("Manager", "M3123131310");
//            LoginRequest loginRequest = new LoginRequest("newuser", "A3123131312");
//        ResponseResult<TokenResponse> loginResponse = client.login(loginRequest);
        boolean deleteResponse = client.delete(token, uuid, isHardDelete);
        System.out.println("Delete result = " + deleteResponse);
    }

    private boolean delete(String authToken, String uuid, boolean isHardDelete) throws IOException {
        URL url;
        if (isHardDelete) url = new URL(BASE_MANAGER_URL + "/hard_delete");
        else url = new URL(BASE_MANAGER_URL + "/delete");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        DeleteUserOrManagerRequest request = new DeleteUserOrManagerRequest(uuid);
        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + authToken);
            connection.setDoOutput(true);

            String requestBody = gson.toJson(request);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            String response = readResponse(connection, responseCode);

            // 使用正确的TypeToken
            ResponseResult<Boolean> result = gson.fromJson(response,
                    new TypeToken<ResponseResult<Boolean>>() {
                    }.getType());
            return result.getCode() == 200;
        } catch (Exception e) {
            System.err.println("Parsing response failed: " + e.getMessage());
//            throw new IOException("Failed to process the login request: ", e);
            return false;
        } finally {
            connection.disconnect();
        }
    }

    private String testGetUsers() {
        try {
            // 1. 首先获取登录token
//            LoginRequest loginRequest = new LoginRequest("Manager", "M3123131310");
            LoginRequest loginRequest = new LoginRequest("newuser1", "B3123131311");
            ResponseResult<UserTokenResponse> loginResponse = client.login(loginRequest);

            if (loginResponse == null || loginResponse.getCode() != 200) {
                System.out.println("Login failure: -----" +
                        (loginResponse != null ? loginResponse.getMessage() : "未知错误"));
                return "";
            }
            String token = loginResponse.getData().token();
            System.out.println("Login succeed, token: " + token);

            // 2. 使用获取的token访问用户列表
            HashMap<String, String> map = new HashMap<>();
//            map.put("phone", "1380013800");
            map.put("email", "@");
            List<UserDto> users = client.getAllUsers("Bearer " + token, map);
            users.forEach(user -> {
                System.out.println("Get user's list:");
                System.out.printf(
                        "ID: %d, Username: %s, Role: %s, Phone: %s\n, Email: %s\n, CreateTime: %s\n, LastModifiedTime: %s\n",
                        user.getId(), user.getUsername(), user.getRole(), user.getPhone(), user.getEmail(), user.getCreateTime().toString(), user.getLastModifiedTime().toString());
                dtos.add(user);
            });
            return loginResponse.getData().token();
        } catch (Exception e) {
            System.err.println("Wrong: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    public List<UserDto> getAllUsers(String authToken, Map<String, String> params) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // 构建带参数的URI
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_MANAGER_URL + "/list");
            if (params != null) {
                params.forEach(builder::queryParam);
            }

            // 直接解析为List<UserDto>
            ResponseEntity<List<UserDto>> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {
                    }
            );

            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 403) {
                System.out.println("Client error: Access denied.");
            } else {
                System.out.println("Client error: " + e.getMessage());
            }
            return new ArrayList<>();
        } catch (HttpServerErrorException e) {
//            log.error("HTTP服务端错误: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            System.out.println("Server error: " + e.getStatusText());
            return new ArrayList<>();
        } catch (Exception e) {
//            log.error("请求异常", e);
            System.out.println("Request exception: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    private String testLogin(String username, String password) {
        try {
            LoginRequest loginRequest = new LoginRequest(username, password);
            ResponseResult<UserTokenResponse> loginResponse = client.login(loginRequest);

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
            RegisterUserRequest registerUserRequest = new RegisterUserRequest(username, email, phone, password, "Test");
            ResponseResult<String> registerResponse = client.register(registerUserRequest);

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
            RegisterManagerRequest registerRequest = new RegisterManagerRequest(username, email, phone, password, "manager");
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


    private void testDeviceRegister(String uuid, String deviceName, String deviceGroup, String userUuid, String signature, String remark) {
        try {
            RegisterDeviceRequest deviceRequest = new RegisterDeviceRequest(uuid, deviceName, deviceGroup, userUuid, signature, remark);
//            RegisterManagerRequest registerRequest = new RegisterManagerRequest(username, email, phone, password, "manager");
//            ResponseResult<String> registerResponse = client.managerRegister(registerRequest);
            ResponseResult<String> registerResponse = client.deviceRegister(deviceRequest);


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

    public ResponseResult<UserTokenResponse> login(LoginRequest request) throws IOException {
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
                    new TypeToken<ResponseResult<UserTokenResponse>>() {
                    }.getType());
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

    public ResponseResult<String> register(RegisterUserRequest request) throws IOException {
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
                return gson.fromJson(response, new TypeToken<ResponseResult<String>>() {
                }.getType());
            }
        } finally {
            connection.disconnect();
        }
    }

    public ResponseResult<String> managerRegister(RegisterManagerRequest request) throws IOException {
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
                return gson.fromJson(response, new TypeToken<ResponseResult<String>>() {
                }.getType());
            }
        } finally {
            connection.disconnect();
        }
    }

    public ResponseResult<String> deviceRegister(RegisterDeviceRequest request) throws IOException {
        URL url = new URL(BASE_DEVICE_URL + "/register");
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
                return gson.fromJson(response, new TypeToken<ResponseResult<String>>() {
                }.getType());
            }
        } finally {
            connection.disconnect();
        }
    }

}
