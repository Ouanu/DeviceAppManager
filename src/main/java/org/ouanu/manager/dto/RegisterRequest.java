package org.ouanu.manager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Username cannot be blank.")
    @Size(min = 7, max = 20, message = "Usernames should be between 7 and 20 characters long.")
    private String username;

    @NotBlank(message = "Password cannot be blank.")
    @Size(min = 8, max = 30, message = "Password length should be between 8 and 30.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,}$",
             message = "The password must contain at least one number, one lowercase letter, one uppercase letter, and one special character")
    private String password;

    @Pattern(regexp = "^(\\+\\d{1,4})?[\\s\\-]?(\\(\\d{1,4}\\)|\\d{1,4})[\\s\\-]?\\d{3,10}$",
         message = "Please enter a valid phone number.")
    private String phoneNumber;

}