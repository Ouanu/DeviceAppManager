package org.ouanu.manager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    @NotBlank(message = "Username/Phone Number cannot be blank.")
    private String usernameOrPhoneNumber;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}