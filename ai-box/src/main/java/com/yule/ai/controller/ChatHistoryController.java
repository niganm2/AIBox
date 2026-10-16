package com.yule.ai.controller;

import com.yule.ai.entity.vo.MessageVO;
import com.yule.ai.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ai/history")
public class ChatHistoryController {

    private final ChatHistoryRepository chatHistoryRepository;

    private final ChatMemory chatMemory;

    /**
     * 获取聊天记录
     * @param type
     * @return
     */
    @GetMapping("/{type}")
    public List<String> getChatIds(@PathVariable("type") String type) {
        return chatHistoryRepository.getChatIds(type);
    }

    @GetMapping("/{type}/{chatId}")
    public List<MessageVO> getChatHistory(@PathVariable("type") String type, @PathVariable("chatId") String chatId) {
        List<Message> messages = chatMemory.get(chatId);
        if (messages == null){
            return List.of();
        }
        return messages.stream().map(MessageVO::new).toList();
    }

    /**
     * 批量删除会话记录（同时清除记忆）
     * @param type 业务类型
     * @param chatIds 会话ID列表
     */
    @DeleteMapping("/{type}")
    public void deleteChats(@PathVariable("type") String type, @RequestBody List<String> chatIds) {
        if (chatIds == null || chatIds.isEmpty()) {
            return;
        }
        for (String chatId : chatIds) {
            chatHistoryRepository.delete(type, chatId);
            chatMemory.clear(chatId);
        }
    }
}
