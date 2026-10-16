package com.yule.ai.controller;

import com.yule.ai.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class CodeController {

    @Qualifier("codeChatClient")
    private final ChatClient codeChatClient;

    private final ChatHistoryRepository chatHistoryRepository;

    @RequestMapping(value = "/code", produces = "text/html;charset=UTF-8")
    public Flux<String> code(@RequestParam("prompt") String prompt,
                             @RequestParam("chatId") String chatId) {
        chatHistoryRepository.save("code", chatId);
        return codeChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content()
                .onErrorResume(e -> {
                    log.error("代码助手请求失败", e);
                    return Flux.just(resolveErrorMessage(e));
                });
    }

    static String resolveErrorMessage(Throwable e) {
        String msg = e.getMessage() != null ? e.getMessage() : "";
        if (msg.contains("429") || msg.contains("Too Many Requests")) {
            return "请求过于频繁，已触发API限流，请稍后再试。";
        }
        if (msg.contains("timeout") || msg.contains("Timeout") || msg.contains("timed out")) {
            return "请求超时，请稍后重试。";
        }
        return "抱歉，服务暂时不可用，请稍后重试。";
    }
}
