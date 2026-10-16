package com.yule.ai.controller;

import com.yule.ai.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class ChatController {

    @Qualifier("multimodalChatClient")
    private final ChatClient chatClient;

    private final ChatHistoryRepository chatHistoryRepository;

    //流式输出
    @RequestMapping(value = "/chat", produces = "text/html;charset=UTF-8")
    public Flux<String> chat(@RequestParam("prompt") String prompt,
                             @RequestParam("chatId") String chatId,
                             @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        //1.保存会话 ID
        chatHistoryRepository.save("chat", chatId);
        //2.请求模型
        if (files == null || files.isEmpty()) {
            //没有附件，纯文本对话
            return textChat(prompt, chatId);
        } else {
            //有附件，多模态对话
            return multiModelChat(prompt, chatId, files);
        }

    }

    //多模态对话
    private Flux<String> multiModelChat(String prompt, String chatId, List<MultipartFile> files) {
        //解析多媒体
        List<Media> medias = files.stream()
                .map(file -> {
                    String contentType = file.getContentType();
                    if (contentType == null || contentType.isBlank()) {
                        contentType = "application/octet-stream";
                    }
                    return new Media(
                            MimeType.valueOf(contentType),
                            file.getResource()
                    );
                })
                .toList();
        //多模态对话
        return chatClient.prompt()
                .user(p -> p.text(prompt).media(medias.toArray(Media[]::new)))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    //纯文本对话
    private Flux<String> textChat(String prompt, String chatId) {
        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

}
