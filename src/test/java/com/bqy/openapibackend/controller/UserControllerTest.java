package com.bqy.openapibackend.controller;

import com.bqy.openapibackend.common.StatusCode;
import com.bqy.openapibackend.exception.GlobalExceptionHandler;
import com.bqy.openapibackend.exception.OpzException;
import com.bqy.openapibackend.model.vo.LoginUserVO;
import com.bqy.openapibackend.model.vo.RegisterResultVO;
import com.bqy.openapibackend.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserController 单元测试（Standalone MockMvc 模式）
 * 不依赖 Spring ApplicationContext，使用 standaloneSetup 手动装配 MockMvc
 * 验证 HTTP 接口的请求参数校验、响应结构及全局异常处理
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户 Controller 接口测试")
class UserControllerTest {

    @Mock
    private IUserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // 手动装配：Controller + 全局异常处理器，无需启动 Spring 容器
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ===================== 注册接口测试 =====================

    @Test
    @DisplayName("POST /user/register 注册成功：返回 200 和注册结果")
    void register_validInput_returns200WithResult() throws Exception {
        RegisterResultVO mockResult = RegisterResultVO.builder()
                .userId(1L)
                .userAccount("testuser")
                .userName("测试用户")
                .accessKey("ak_abc123")
                .secretKey("sk_xyz789")
                .message("注册成功")
                .build();

        when(userService.register(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockResult);

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userAccount": "testuser",
                                    "userPassWord": "password123",
                                    "checkPassWord": "password123",
                                    "userName": "测试用户",
                                    "phoneNumber": "13800000000"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userAccount").value("testuser"))
                .andExpect(jsonPath("$.data.accessKey").value("ak_abc123"))
                .andExpect(jsonPath("$.data.secretKey").value("sk_xyz789"));
    }

    @Test
    @DisplayName("POST /user/register 账号过短：返回参数错误（@Size 校验）")
    void register_shortAccount_returnsParamError() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userAccount": "ab",
                                    "userPassWord": "password123",
                                    "checkPassWord": "password123",
                                    "userName": "用户",
                                    "phoneNumber": "13800000000"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(StatusCode.PARAMS_ERROR.getCode()));
    }

    @Test
    @DisplayName("POST /user/register 密码过短：返回参数错误（@Size 校验）")
    void register_shortPassword_returnsParamError() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userAccount": "testuser",
                                    "userPassWord": "123",
                                    "checkPassWord": "123",
                                    "userName": "用户",
                                    "phoneNumber": "13800000000"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(StatusCode.PARAMS_ERROR.getCode()));
    }

    @Test
    @DisplayName("POST /user/register 账号已存在：Service 抛出业务异常，全局异常处理器捕获")
    void register_accountExists_returnsError() throws Exception {
        when(userService.register(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new OpzException(StatusCode.PARAMS_ERROR, "账号已存在，请更换"));

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userAccount": "existUser",
                                    "userPassWord": "password123",
                                    "checkPassWord": "password123",
                                    "userName": "已有用户",
                                    "phoneNumber": "13800000000"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(StatusCode.PARAMS_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value("账号已存在，请更换"));
    }

    // ===================== 登录接口测试 =====================

    @Test
    @DisplayName("POST /user/login 登录成功：返回 200 和用户信息")
    void login_validCredentials_returns200() throws Exception {
        LoginUserVO mockLoginUser = new LoginUserVO();
        mockLoginUser.setUserName("测试用户");

        when(userService.login(anyString(), anyString(), any()))
                .thenReturn(mockLoginUser);

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userAccount": "testuser",
                                    "userPassword": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userName").value("测试用户"));
    }

    @Test
    @DisplayName("POST /user/login 账号密码错误：返回业务错误码")
    void login_wrongCredentials_returnsError() throws Exception {
        when(userService.login(anyString(), anyString(), any()))
                .thenThrow(new OpzException(StatusCode.PARAMS_ERROR, "账号密码错误"));

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userAccount": "testuser",
                                    "userPassword": "wrongpass"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(StatusCode.PARAMS_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value("账号密码错误"));
    }

    @Test
    @DisplayName("POST /user/login 账号为空：返回参数校验错误")
    void login_emptyAccount_returnsParamError() throws Exception {
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userAccount": "",
                                    "userPassword": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(StatusCode.PARAMS_ERROR.getCode()));
    }

    // ===================== 登出接口测试 =====================

    @Test
    @DisplayName("GET /user/logout 登出成功：返回 true")
    void logout_loggedInUser_returnsTrue() throws Exception {
        when(userService.logout(any())).thenReturn(true);

        mockMvc.perform(get("/user/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }
}

