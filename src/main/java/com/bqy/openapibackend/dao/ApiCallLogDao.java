package com.bqy.openapibackend.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bqy.openapibackend.mapper.ApiCallLogMapper;
import com.bqy.openapibackend.model.entity.ApiCallLog;
import org.springframework.stereotype.Component;

@Component
public class ApiCallLogDao extends ServiceImpl<ApiCallLogMapper, ApiCallLog> {

}
