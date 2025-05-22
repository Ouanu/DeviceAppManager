package org.ouanu.manager.record

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.ouanu.manager.service.UserService.UserCreateCommand



@JvmRecord
data class LoginRequest(val username: String?, val password: String?)

@JvmRecord
data class TokenResponse(val token: String?)

class RegisterRequest(
    @field:NotBlank @field:Size(min = 3, max = 20) @NotBlank @Size(min = 3, max = 20) val username: String?,
    @field:Email @Email val email: String?,
    @field:Pattern(regexp = "^1[3-9]\\d{9}$") @Pattern(regexp = "^1[3-9]\\d{9}$") val phone: String?,
    @field:Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$") @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$") val password: String?
) {
    fun toCommand(): UserCreateCommand {
        return UserCreateCommand(username, email, phone, password)
    }
}