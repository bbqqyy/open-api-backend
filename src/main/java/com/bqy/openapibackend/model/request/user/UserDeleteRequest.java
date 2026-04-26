package com.bqy.openapibackend.model.request.user;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UserDeleteRequest {
    @Positive
    private Long id;
}
