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
public class SummaryController {

    @Qualifier("summaryChatClient")
    private final ChatClient summaryChatClient;

    private final ChatHistoryRepository chatHistoryRepository;

    @RequestMapping(value = "/summary", produces = "text/html;charset=UTF-8")
    public Flux<String> summary(@RequestParam("prompt") String prompt,
                                @RequestParam("chatId") String chatId) {
        chatHistoryRepository.save("summary", chatId);
        return summaryChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content()
                .onErrorResume(e -> {
                    log.error("文本摘要请求失败", e);
                    return Flux.just(CodeController.resolveErrorMessage(e));
                });
    }
}
