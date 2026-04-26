package com.bqy.openapibackend.model.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Positive
    private Long id;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码不能小于六位")
    private String userPassword;

    @NotBlank(message = "电话不能为空")
    private String phoneNumber;

    private String email;

    @NotBlank(message = "用户名不能为空")
    private String userName;

    private String userAvatar;

    private String userProfile;
}
