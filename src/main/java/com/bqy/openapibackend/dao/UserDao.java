package com.bqy.openapibackend.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bqy.openapibackend.mapper.UserMapper;
import com.bqy.openapibackend.model.entity.User;
import com.bqy.openapibackend.model.request.user.UserQueryRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDao extends ServiceImpl<UserMapper, User> {
    public long getCountByUserAccount(String userAccount) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserAccount, userAccount);
        return this.count(wrapper);
    }

    public boolean isUserExist(String userAccount, String userPassword) {
        return this.lambdaQuery()
                .eq(User::getUserAccount, userAccount)
                .eq(User::getUserPassword, userPassword)
                .count() > 0;
    }

    public User getLoginUser(String userAccount, String userPassword) {
//        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(User::getUserAccount,userAccount);
//        wrapper.eq(User::getUserPassword,userPassword);
//        return this.getOne(wrapper);
        return this.lambdaQuery()
                .eq(User::getUserAccount, userAccount)
                .eq(User::getUserPassword, userPassword)
                .one();
    }

    public User getUserById(Long id) {
        return this.lambdaQuery()
                .eq(User::getId, id)
                .one();
    }

    public List<User> listByRequest(UserQueryRequest request) {
        return this.lambdaQuery()
                .eq(StringUtils.isNotBlank(request.getUserAccount()), User::getUserAccount, request.getUserAccount())
                .like(StringUtils.isNotBlank(request.getEmail()), User::getEmail, request.getEmail())
                .like(StringUtils.isNotBlank(request.getUserProfile()), User::getUserProfile, request.getUserProfile())
                .like(StringUtils.isNotBlank(request.getUserName()), User::getUserName, request.getUserName())
                .like(StringUtils.isNotBlank(request.getPhoneNumber()), User::getPhoneNumber, request.getPhoneNumber())
                .list();
    }

    public Page<User> getUserPage(UserQueryRequest request) {
        return this.lambdaQuery()
                .eq(StringUtils.isNotBlank(request.getUserAccount()), User::getUserAccount, request.getUserAccount())
                .like(StringUtils.isNotBlank(request.getEmail()), User::getEmail, request.getEmail())
                .like(StringUtils.isNotBlank(request.getUserProfile()), User::getUserProfile, request.getUserProfile())
                .like(StringUtils.isNotBlank(request.getUserName()), User::getUserName, request.getUserName())
                .like(StringUtils.isNotBlank(request.getPhoneNumber()), User::getPhoneNumber, request.getPhoneNumber())
                .page(new Page<>(request.getCurrent(), request.getPageSize()));
    }
}
