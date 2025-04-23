package org.ouanu.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private String uuid;

    public AuthResponse(String token, String username, String uuid) {
        this.token = token;
        this.username = username;
        this.uuid = uuid;
    }

}