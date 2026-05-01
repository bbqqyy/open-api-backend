package com.bqy.openapibackend.service;

import com.bqy.openapibackend.dao.UserDao;
import com.bqy.openapibackend.exception.OpzException;
import com.bqy.openapibackend.model.entity.User;
import com.bqy.openapibackend.model.vo.LoginUserVO;
import com.bqy.openapibackend.model.vo.RegisterResultVO;
import com.bqy.openapibackend.service.impl.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 * 使用 Mockito 隔离 DAO 层，专注测试 Service 业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("用户服务测试")
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpSession httpSession;

    @InjectMocks
    private UserServiceImpl userService;

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        // 登录相关测试需要：httpRequest.getSession() 返回 httpSession
        // LENIENT 模式避免注册测试报 UnnecessaryStubbing
        lenient().when(httpRequest.getSession()).thenReturn(httpSession);
    }

    // ===================== 注册测试 =====================

    @Test
    @DisplayName("注册成功：返回含 AccessKey 和 SecretKey 的结果")
    void register_validInput_success() {
        when(userDao.getCountByUserAccount("testuser")).thenReturn(0L);
        when(userDao.save(any(User.class))).thenReturn(true);

        RegisterResultVO result = userService.register(
                "testuser", "password123", "password123", "测试用户", "13800000000");

        assertNotNull(result);
        assertEquals("testuser", result.getUserAccount());
        assertNotNull(result.getAccessKey(), "应返回 AccessKey");
        assertNotNull(result.getSecretKey(), "应返回 SecretKey");
        assertTrue(result.getAccessKey().startsWith("ak_"), "AccessKey 应以 ak_ 开头");
        assertTrue(result.getSecretKey().startsWith("sk_"), "SecretKey 应以 sk_ 开头");
        verify(userDao, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("注册失败：两次密码不一致")
    void register_passwordMismatch_throwsException() {
        OpzException ex = assertThrows(OpzException.class, () ->
                userService.register("testuser", "password123", "different456",
                        "测试用户", "13800000000"));

        assertEquals("两次密码输入不一致", ex.getMessage());
        verify(userDao, never()).save(any());
    }

    @Test
    @DisplayName("注册失败：账号已存在")
    void register_accountAlreadyExists_throwsException() {
        when(userDao.getCountByUserAccount("existUser")).thenReturn(1L);

        OpzException ex = assertThrows(OpzException.class, () ->
                userService.register("existUser", "pass123", "pass123",
                        "已存在用户", "13900000000"));

        assertEquals("账号已存在，请更换", ex.getMessage());
        verify(userDao, never()).save(any());
    }

    @Test
    @DisplayName("注册时密码应以 BCrypt 格式存储（不存储明文）")
    void register_passwordStoredAsBCrypt() {
        when(userDao.getCountByUserAccount(anyString())).thenReturn(0L);

        // 捕获实际保存的 User 对象
        when(userDao.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            // 验证密码是 BCrypt 格式（以 $2a$ 开头，长度 60）
            assertTrue(savedUser.getUserPassword().startsWith("$2"), "密码应为 BCrypt 格式");
            assertEquals(60, savedUser.getUserPassword().length(), "BCrypt 密码长度应为 60");
            // 验证明文未被存储
            assertNotEquals("mypassword", savedUser.getUserPassword(), "不应存储明文密码");
            return true;
        });

        userService.register("user01", "mypassword", "mypassword", "用户01", "13700000000");
    }

    // ===================== 登录测试 =====================

    @Test
    @DisplayName("登录成功：BCrypt 密码验证")
    void login_bcryptPassword_success() {
        String rawPassword = "mypassword123";
        User mockUser = buildUser(1L, "loginUser", ENCODER.encode(rawPassword));
        when(userDao.getByUserAccount("loginUser")).thenReturn(mockUser);

        LoginUserVO result = userService.login("loginUser", rawPassword, httpRequest);

        assertNotNull(result);
        assertEquals("用户loginUser", result.getUserName());
        // 验证已写入 Session
        verify(httpSession).setAttribute(anyString(), eq(1L));
    }

    @Test
    @DisplayName("登录成功：旧版明文密码自动迁移为 BCrypt")
    void login_plainTextPassword_migratedToBCrypt() {
        String rawPassword = "plainpass";
        // 旧用户：密码以明文存储
        User mockUser = buildUser(2L, "oldUser", rawPassword);
        when(userDao.getByUserAccount("oldUser")).thenReturn(mockUser);
        when(userDao.updateById(any(User.class))).thenReturn(true);

        LoginUserVO result = userService.login("oldUser", rawPassword, httpRequest);

        assertNotNull(result, "明文密码用户应能正常登录");
        // 验证触发了密码升级（updateById 被调用）
        verify(userDao, times(1)).updateById(argThat(user -> {
            // 升级后的密码应是 BCrypt 格式
            return user.getUserPassword() != null
                    && user.getUserPassword().startsWith("$2")
                    && user.getUserPassword().length() == 60;
        }));
    }

    @Test
    @DisplayName("登录失败：账号不存在")
    void login_accountNotFound_throwsException() {
        when(userDao.getByUserAccount("nonExistUser")).thenReturn(null);

        OpzException ex = assertThrows(OpzException.class, () ->
                userService.login("nonExistUser", "password", httpRequest));

        assertEquals("账号密码错误", ex.getMessage());
    }

    @Test
    @DisplayName("登录失败：BCrypt 密码错误")
    void login_wrongBCryptPassword_throwsException() {
        User mockUser = buildUser(3L, "userA", ENCODER.encode("correctpass"));
        when(userDao.getByUserAccount("userA")).thenReturn(mockUser);

        OpzException ex = assertThrows(OpzException.class, () ->
                userService.login("userA", "wrongpass", httpRequest));

        assertEquals("账号密码错误", ex.getMessage());
    }

    @Test
    @DisplayName("登录失败：旧版明文密码输入错误")
    void login_wrongPlainTextPassword_throwsException() {
        User mockUser = buildUser(4L, "oldUser2", "storedPlainPass");
        when(userDao.getByUserAccount("oldUser2")).thenReturn(mockUser);

        OpzException ex = assertThrows(OpzException.class, () ->
                userService.login("oldUser2", "wrongPlainPass", httpRequest));

        assertEquals("账号密码错误", ex.getMessage());
        // 验证密码错误时不触发升级
        verify(userDao, never()).updateById(any());
    }

    // ===================== 辅助方法 =====================

    private User buildUser(Long id, String account, String password) {
        User user = new User();
        user.setId(id);
        user.setUserAccount(account);
        user.setUserPassword(password);
        user.setUserName("用户" + account);
        user.setUserRole("user");
        return user;
    }
}

