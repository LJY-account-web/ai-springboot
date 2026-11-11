package com.haust.aispringboot.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.haust.aispringboot.DTO.command.ConsultationSessionCreateDTO;
import com.haust.aispringboot.entity.ConsultationSession;
import com.haust.aispringboot.entity.User;
import com.haust.aispringboot.mapper.ConsultationSessionMapper;
import com.haust.aispringboot.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ConsultationSessionService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ConsultationSessionMapper consultationSessionMapper;

    public ConsultationSession createSession(Long userId, ConsultationSessionCreateDTO createDTO) {
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user != null) {
            // 创建会话记录
            ConsultationSession session = ConsultationSession.builder()
                    .userId(userId)
                    .sessionTitle(createDTO.getSessionTitle())
                    .startedAt(LocalDateTime.now())
                    .build();
            //如果未提供标题
            if(StrUtil.isBlank(createDTO.getSessionTitle())){
                session.setSessionTitle("小暖AI助手 — " + DateUtil.format(LocalDateTime.now(),"MM-dd HH:mm"));
            }
            // 插入会话记录
            consultationSessionMapper.insert(session);
            return session;
        }
        return null;
    }

}
