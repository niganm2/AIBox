package com.yule.ai.controller;

import com.yule.ai.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class CustomerServiceController {

    @Qualifier("serviceChatClient")
    private final ChatClient serviceChatClient;

    private final ChatHistoryRepository chatHistoryRepository;
    //流式输出
    @RequestMapping(value = "/service", produces = "text/html;charset=UTF-8")
    public Flux<String> service(@RequestParam("prompt") String prompt,
                                @RequestParam("chatId") String chatId) {
        //1.保存会话 ID
        chatHistoryRepository.save("service", chatId);
        //2.请求模型
        return serviceChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

}
