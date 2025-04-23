package org.ouanu.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserDTO {
    private String username;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private String uuid;

}