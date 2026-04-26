package com.bqy.openapibackend.model.request.user;

import com.bqy.openapibackend.common.PageRequest;
import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @Nullable
    private String userAccount;
    @Nullable
    private String phoneNumber;
    @Nullable
    private String email;
    @Nullable
    private String userName;
    @Nullable
    private String userProfile;


}
