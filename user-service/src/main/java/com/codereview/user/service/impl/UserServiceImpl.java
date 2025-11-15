package com.codereview.user.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codereview.common.constant.RedisConstants;
import com.codereview.common.exception.BusinessException;
import com.codereview.common.utils.JwtUtils;
import com.codereview.common.utils.RedisUtils;
import com.codereview.user.dto.UserLoginDTO;
import com.codereview.user.dto.UserRegisterDTO;
import com.codereview.user.entity.User;
import com.codereview.user.mapper.UserMapper;
import com.codereview.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现
 * @author CodeReview
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisUtils redisUtils;

    @Override
    public void register(UserRegisterDTO dto) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User existUser = userMapper.selectOne(wrapper);
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, dto.getEmail());
        existUser = userMapper.selectOne(wrapper);
        if (existUser != null) {
            throw new BusinessException("邮箱已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(DigestUtil.md5Hex(dto.getPassword())); // MD5加密
        user.setEmail(dto.getEmail());
        user.setNickname(StrUtil.isBlank(dto.getNickname()) ? dto.getUsername() : dto.getNickname());
        user.setRole(0); // 普通用户
        user.setStatus(1); // 正常状态

        userMapper.insert(user);
        log.info("用户注册成功: {}", dto.getUsername());
    }

    @Override
    public Map<String, Object> login(UserLoginDTO dto) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证密码
        String encryptedPassword = DigestUtil.md5Hex(dto.getPassword());
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 检查账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 生成token
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());
        String token = JwtUtils.generateToken(String.valueOf(user.getId()), claims);

        // 存储到Redis
        String redisKey = RedisConstants.USER_TOKEN_KEY + user.getId();
        redisUtils.set(redisKey, token, RedisConstants.TOKEN_EXPIRE_TIME, TimeUnit.HOURS);

        // 返回用户信息和token
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("email", user.getEmail());
        result.put("role", user.getRole());

        log.info("用户登录成功: {}", dto.getUsername());
        return result;
    }

    @Override
    public void logout(String userId) {
        try {
            String redisKey = RedisConstants.USER_TOKEN_KEY + userId;
            redisUtils.delete(redisKey);
            log.info("用户登出成功: userId={}", userId);
        } catch (Exception e) {
            throw new BusinessException("登出失败");
        }
    }

    @Override
    public Map<String, Object> getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("email", user.getEmail());
        result.put("avatar", user.getAvatar());
        result.put("role", user.getRole());
        result.put("status", user.getStatus());

        return result;
    }
}
