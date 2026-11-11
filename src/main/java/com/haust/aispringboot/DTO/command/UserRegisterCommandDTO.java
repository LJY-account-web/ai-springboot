package com.haust.aispringboot.DTO.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;

import java.time.LocalDate;

//接收前端注册数据
@Data
public class UserRegisterCommandDTO{
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3,max = 50,message = "用户名长度必须在3-50之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",message = "用户名只能包含字母、数字和下划线")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式错误")
    @Size(max = 100,message = "邮箱长度不能超过100个字符")
    private String email;

    private String nickname;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6,max = 50,message = "密码长度必须在6-50之间")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    @Size(min = 6,max = 50,message = "确认密码长度必须在6-50之间")
    private String confirmPassword;

    @Pattern(regexp = "^$|^1[3456789]\\d{9}$", message = "手机号格式错误")
    private String phone;

    private Integer gender;
    private Integer userType = 1;
    private LocalDate birthday;
}
