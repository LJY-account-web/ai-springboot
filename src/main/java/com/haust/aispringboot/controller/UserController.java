package com.haust.aispringboot.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.haust.aispringboot.DTO.command.UserLoginCommandDTO;
import com.haust.aispringboot.DTO.command.UserRegisterCommandDTO;
import com.haust.aispringboot.DTO.response.UserLoginResponseDTO;
import com.haust.aispringboot.common.Result;
import com.haust.aispringboot.service.UserService;
import com.haust.aispringboot.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Resource
    private UserService userService;

    //登录接口
    @PostMapping("/login")
    public Result<UserLoginResponseDTO> login(@Valid @RequestBody UserLoginCommandDTO commanDTO) {

        //调用服务层登录方法
        UserLoginResponseDTO result = userService.login(commanDTO);
        System.out.println(result);
        return Result.ok(result);
    }

    //注册接口
    @PostMapping("/add")
    public Result<UserLoginResponseDTO.UserDetailResponseDTO> register(@Valid @RequestBody UserRegisterCommandDTO commanDTO) {
        //调用服务层注册方法
        UserLoginResponseDTO.UserDetailResponseDTO result = userService.register(commanDTO);
        return Result.ok(result);
    }

    //获取当前用户信息
    @GetMapping("/current")
    public Result<UserLoginResponseDTO.UserDetailResponseDTO> getCurrentUser() {
        //如何从token中获取用户ID
        String Token = JwtTokenUtil.getCurrentToken();
        DecodedJWT jwt = JwtTokenUtil.verifyToken(Token);
        Long userId = jwt.getClaim("userId").asLong();
        //调用服务层获取当前用户信息
        UserLoginResponseDTO.UserDetailResponseDTO result = userService.getUserById(userId);
        return Result.ok(result);

    }


}