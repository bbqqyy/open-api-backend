package com.bqy.openapibackend.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiApplyVO {
    private Long id;
    private Long apiId;
    private String apiName;
    /** 申请人ID */
    private Long userId;
    /** 申请人名称 */
    private String applicantName;
    /** API拥有者ID */
    private Long ownerId;
    /** API拥有者名称 */
    private String ownerName;
    private String status;
    /** 申请时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
