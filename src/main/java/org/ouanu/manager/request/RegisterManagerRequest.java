package org.ouanu.manager.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.ouanu.manager.command.ManagerCreateCommand;

public record RegisterManagerRequest(
        @NotBlank @Size(min = 3, max = 20) String username,
        @Email String email,
        @Pattern(regexp = "^1[3-9]\\d{9}$") String phone,
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$") String password,
        @Column(length = 200) @Size(max = 200, message = "备注信息不能超过200个字符") String remark
) {
        public ManagerCreateCommand toCommand() {
                return new ManagerCreateCommand(username, email, phone, password, remark);
        }
}
