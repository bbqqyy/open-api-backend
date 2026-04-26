package com.bqy.openapibackend.model.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterRequest {
    @Size(min = 4, max = 20, message = "账号必须在4到20位之间")
    @NotBlank(message = "账号不能为空")
    private String userAccount;
    @NotBlank(message = "用户名不能为空")
    private String userName;
    @Size(min = 6, message = "密码不能小于六位")
    @NotBlank(message = "密码不能为空")
    private String userPassWord;
    @NotBlank(message = "重复输入的密码不能为空")
    private String checkPassWord;
    @NotBlank(message = "电话不能为空")
    private String phoneNumber;
}
