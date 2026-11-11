package com.haust.aispringboot.DTO.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

//前端 → 后端（收进来的参数，带校验注解）
@Data
public class ConsultationSessionCreateDTO {
    @NotBlank(message = "会话标题不能为空")
    @Size(max = 200,message = "会话标题长度不能超过200个字符")
       private String sessionTitle;
    @NotBlank(message = "会话初始消息不能为空")
    @Size(max = 2000,message = "会话初始消息长度不能超过2000个字符")
    private String initialMessage;

}
