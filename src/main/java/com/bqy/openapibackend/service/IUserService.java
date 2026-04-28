package com.bqy.openapibackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.openapibackend.model.entity.User;
import com.bqy.openapibackend.model.request.user.UserQueryRequest;
import com.bqy.openapibackend.model.request.user.UserUpdateRequest;
import com.bqy.openapibackend.model.vo.ApiKeysVO;
import com.bqy.openapibackend.model.vo.LoginUserVO;
import com.bqy.openapibackend.model.vo.RegisterResultVO;
import com.bqy.openapibackend.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * <p>
 * 用户 服务类
 * </p>
 *
 * @author bianqingyun
 * @since 2026-03-16
 */
public interface IUserService {

    /**
     * 用户注册，返回包含 AccessKey 和 SecretKey 的注册结果
     */
    RegisterResultVO register(String userAccount, String userPassWord, String checkPassWord, String userName, String phoneNumber);

    LoginUserVO login(String userAccount, String userPassword, HttpServletRequest request);

    User getLoginUser(HttpServletRequest request);

    Boolean logout(HttpServletRequest request);

    List<UserVO> getUserList(UserQueryRequest request);

    Boolean deleteUser(Long id);

    Page<UserVO> getUserPage(UserQueryRequest request);

    Boolean updateUser(UserUpdateRequest request);

    /**
     * 获取当前登录用户的 API 密钥信息
     *
     * @param request HTTP 请求
     * @return API 密钥信息（SecretKey 部分显示）
     */
    ApiKeysVO getApiKeys(HttpServletRequest request);

    /**
     * 重新生成 API 密钥
     *
     * 旧的密钥将立即失效
     *
     * @param request HTTP 请求
     * @return 新的注册结果，包含新的 AccessKey 和 SecretKey
     */
    RegisterResultVO regenerateApiKeys(HttpServletRequest request);
}
