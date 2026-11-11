package com.haust.aispringboot.DTO.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

//接收前端登录数据
//DTO = Data Transfer Object（数据传输对象）（前端交互用）
@Data
public class UserLoginCommandDTO {
    @NotBlank(message = "用户名或邮箱不能为空")
    @Size(max = 100, message = "用户名或邮箱长度不能超过100个字符")
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(max = 50,min = 6, message = "密码长度必须在6到50个字符之间")
    private String password;
}
