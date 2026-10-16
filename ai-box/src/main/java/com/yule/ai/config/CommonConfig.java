package com.yule.ai.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.yule.ai.constants.SystemConstants;
import com.yule.ai.tools.CourseTools;
import com.yule.ai.tools.MallTools;
import com.yule.ai.tools.MathTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


@Component
public class CommonConfig {

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(15) // 最多保留15条消息，超出自动删掉最早的
                .build();
    }

    /**
     * 多模态 ChatClient —— 只给 ChatController 用（支持图片）
     * 显式覆盖模型为 qwen3.8-omni-flash，不受 YAML qwen-turbo 影响
     */
    @Bean("multimodalChatClient")
    public ChatClient multimodalChatClient(ChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient
                .builder(chatModel)
                .defaultOptions(DashScopeChatOptions.builder()
                        .withModel("qwen3.8-omni-flash")
                        .withMultiModel(true)
                        .build())
                .defaultSystem("你是一个热心、可爱的智能助手，你的名字是小怡怡，请以小怡怡的身份和语气回答问题")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    /**
     * 纯文本 ChatClient —— 给 Game/Service/PDF 等用
     * 走 YAML 默认模型 qwen-turbo
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient
                .builder(chatModel)
                .defaultSystem("你是一个热心、可爱的智能助手，你的名字是小怡怡，请以小怡怡的身份和语气回答问题")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Bean
    public ChatClient gameChatClient(ChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient
                .builder(chatModel)
                .defaultSystem(SystemConstants.GAME_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Bean
    public ChatClient serviceChatClient(ChatModel chatModel, ChatMemory chatMemory, CourseTools courseTools) {
        return ChatClient
                .builder(chatModel)
                .defaultSystem(SystemConstants.SERVICE_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(courseTools)
                .build();
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    public ChatClient pdfChatClient(ChatModel chatModel, ChatMemory chatMemory, VectorStore vectorStore) {
        return ChatClient.builder(chatModel)
                .defaultSystem("请根据上下文回答问题，遇到上下文没有的问题，不要随意捏造。")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        RetrievalAugmentationAdvisor.builder()
                                .documentRetriever(VectorStoreDocumentRetriever.builder()
                                        .vectorStore(vectorStore)
                                        .similarityThreshold(0.5d)
                                        .topK(2)
                                        .build())
                                .build()
                )
                .build();
    }

    /**
     * AI代码助手 ChatClient —— 使用通义千问代码模型
     */
    @Bean("codeChatClient")
    public ChatClient codeChatClient(ChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient
                .builder(chatModel)
                .defaultOptions(DashScopeChatOptions.builder()
                        .withModel("qwen3-coder-flash")
                        .build())
                .defaultSystem(SystemConstants.CODE_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    /**
     * AI文本摘要 ChatClient
     */
    @Bean("summaryChatClient")
    public ChatClient summaryChatClient(ChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient
                .builder(chatModel)
                .defaultOptions(DashScopeChatOptions.builder()
                        .withModel("qwen-turbo")
                        .withTemperature(0.3)
                        .build())
                .defaultSystem(SystemConstants.SUMMARY_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    /**
     * 智能Agent助手 ChatClient —— 支持 Function Calling 工具调用
     */
    @Bean("agentChatClient")
    public ChatClient agentChatClient(ChatModel chatModel, ChatMemory chatMemory, MathTools mathTools, MallTools mallTools) {
        return ChatClient
                .builder(chatModel)
                .defaultOptions(DashScopeChatOptions.builder()
                        .withModel("qwen-turbo")
                        .build())
                .defaultSystem(SystemConstants.AGENT_SYSTEM_PROMPT)
                .defaultTools(mathTools, mallTools)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

}
