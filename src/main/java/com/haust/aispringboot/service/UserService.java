package com.haust.aispringboot.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haust.aispringboot.DTO.command.UserLoginCommandDTO;
import com.haust.aispringboot.DTO.command.UserRegisterCommandDTO;
import com.haust.aispringboot.DTO.response.UserLoginResponseDTO;
import com.haust.aispringboot.common.Result;
import com.haust.aispringboot.entity.User;
import com.haust.aispringboot.enumClass.UserType;
import com.haust.aispringboot.exception.BusinessException;
import com.haust.aispringboot.mapper.UserMapper;
import com.haust.aispringboot.service.convert.UserConvert;
import com.haust.aispringboot.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Resource
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserLoginResponseDTO login(UserLoginCommandDTO commanDTO) {
        //构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, commanDTO.getUsername())
                .or()
                .eq(User::getEmail, commanDTO.getUsername());
        //调用MP API查询用户
        User user = userMapper.selectOne(queryWrapper);
        System.out.println(user);

        //判断用户是否存在
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        //判断密码是否正确
        String inputPassword = commanDTO.getPassword().trim();
        if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        //检查用户状态
        if (!user.isActive()) {
            throw new BusinessException("用户已被禁用,请联系管理员");
        }

        //生成JWT TOKEN
        String token = JwtTokenUtil.generateToken(user.getUsername(), user.getId(), user.getUserType());
        System.out.println(token);

        // ✅ 先转成详情DTO，再把 token + 详情 组装为登录响应
        UserLoginResponseDTO.UserDetailResponseDTO userInfo =
                UserConvert.entityToDetailResponse(user);
        return UserConvert.entityToLoginResponse(token, userInfo);
    }

    public UserLoginResponseDTO.UserDetailResponseDTO register(UserRegisterCommandDTO commanDTO) {
        System.out.println(JSONUtil.parseObj(commanDTO));
        //验证密码是否一致
        if (!commanDTO.getPassword().equals(commanDTO.getConfirmPassword())) {
            throw new BusinessException("两次输入密码不一致");
        }

        //检查用户名是否存在
        LambdaQueryWrapper<User> userNameQuery = new LambdaQueryWrapper<>();
        userNameQuery.eq(User::getUsername, commanDTO.getUsername());
        if (userMapper.selectCount(userNameQuery) > 0) {
            throw new BusinessException("用户名已存在");
        }

        //检查邮箱是否存在
        LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<>();
        emailQuery.eq(User::getEmail, commanDTO.getEmail());
        if (userMapper.selectCount(emailQuery) > 0) {
            throw new BusinessException("邮箱已存在");
        }

        //用户类型
        if(!UserType.isValidCode(commanDTO.getUserType())){
            throw new BusinessException("用户类型不存在");
        }

        //创建用户
        String password = commanDTO.getPassword().trim();
        String encodedPassword = passwordEncoder.encode(password);
        User user = UserConvert.registerCommandToEntity(commanDTO,encodedPassword);

        //插入数据库
        userMapper.insert(user);
        return UserConvert.entityToDetailResponse(user);
    }

    public UserLoginResponseDTO.UserDetailResponseDTO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if(user == null){
            throw new BusinessException("用户不存在");
        }
        return UserConvert.entityToDetailResponse(user);
    }

}