package com.bqy.openapibackend.model.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserLoginRequest {
    @NotBlank(message = "账号不能为空")
    @Size(min = 4, max = 20, message = "账号必须在4到20位之间")
    private String userAccount;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码不能小于六位")
    private String userPassword;
}
