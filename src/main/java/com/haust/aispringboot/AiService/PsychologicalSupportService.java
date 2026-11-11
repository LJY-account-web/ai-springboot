package com.haust.aispringboot.AiService;

import com.haust.aispringboot.DTO.command.ConsultationSessionCreateDTO;
import com.haust.aispringboot.DTO.response.ConsultationMessageResponseDTO;
import com.haust.aispringboot.entity.ConsultationSession;
import com.haust.aispringboot.service.ConsultationMessageService;
import com.haust.aispringboot.service.ConsultationSessionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class PsychologicalSupportService {

    @Autowired
    @Qualifier("open-ai")
    private ChatClient chatClient;

    @Autowired
    private ConsultationSessionService consultationSessionService;

    @Autowired
    private ConsultationMessageService consultationMessageService;

    public StructOutPut.StreamChatSession startSession(Long userId, ConsultationSessionCreateDTO createDTO) {
        // 1. 创建数据库会话记录
        ConsultationSession consultationSession = consultationSessionService.createSession(userId, createDTO);

        // 2. 将初始用户消息保存到Message表
        consultationMessageService.saveUserMessage(consultationSession.getId(), createDTO.getInitialMessage(), null);

        // 3. 创建会话信息
        String sessionId = "session_" + consultationSession.getId();
        return new StructOutPut.StreamChatSession(
                sessionId,
                userId,
                createDTO.getInitialMessage(),
                System.currentTimeMillis(),
                System.currentTimeMillis() + 86400000L, // 24h
                1,
                "ACTIVE"
        );
    }

    public Flux<String> streamPsychologicalChat(String sessionId, String userMessage) {
        Long dbsessionId = extractSessionId(sessionId);
        if (dbsessionId == null) {
            return Flux.error(new RuntimeException("会话ID格式错误"));
        }

        // 1. 去重判断（是不是刚建会话时的第一条消息）
        boolean isInitialMessage = false;
        Integer messageCount = consultationMessageService.getMessageCountBySessionId(dbsessionId);
        if (messageCount == 1) {
            ConsultationMessageResponseDTO lastMessage = consultationMessageService.getLastMessageBySessionId(dbsessionId);
            if (lastMessage != null && lastMessage.getSenderType() == 1 && userMessage.equals(lastMessage.getContent())) {
                isInitialMessage = true;
            }
        }

        // 2. 如果不是初始消息，保存用户当前说的话到数据库
        if (!isInitialMessage) {
            consultationMessageService.saveUserMessage(dbsessionId, userMessage, null);
        }

        StringBuilder fullResponse = new StringBuilder();

        // 3. 直接调用 AI（这里千万别再手动 chatMemory.add 了，MessageChatMemoryAdvisor 会自动处理）
        return chatClient.prompt()
                .user(userMessage)
                // 1.0.0 版规范：必须传入 conversationId 来隔离不同用户的对话
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, sessionId))
                .stream()
                .content()
                .doOnNext(fragment -> {
                    fullResponse.append(fragment); // 收集 AI 碎片回复
                })
                .doOnComplete(() -> {
                    // 4. 对话结束，把 AI 的完整回复存进数据库
                    consultationMessageService.saveAiMessage(dbsessionId, fullResponse.toString(), "openai");
                });
    }

    // 获取参数中的数据库 sessionId
    public Long extractSessionId(String sessionId) {
        if (sessionId != null && sessionId.startsWith("session_")) {
            return Long.parseLong(sessionId.substring("session_".length()));
        }
        return null;
    }
}