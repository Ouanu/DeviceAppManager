package org.ouanu.manager.dto;

import lombok.Data;

@Data
public class UserRegister {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String remark;
}
