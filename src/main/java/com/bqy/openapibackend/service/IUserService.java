package com.bqy.openapibackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.openapibackend.model.request.user.UserQueryRequest;
import com.bqy.openapibackend.model.request.user.UserUpdateRequest;
import com.bqy.openapibackend.model.vo.LoginUserVO;
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

    long register(String userAccount, String userPassWord, String checkPassWord, String userName, String phoneNumber);

    LoginUserVO login(String userAccount, String userPassword, HttpServletRequest request);

    LoginUserVO getLoginUser(HttpServletRequest request);

    Boolean logout(HttpServletRequest request);

    List<UserVO> getUserList(UserQueryRequest request);

    Boolean deleteUser(Long id);

    Page<UserVO> getUserPage(UserQueryRequest request);

    Boolean updateUser(UserUpdateRequest request);
}
