package com.yule.ai.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MysqlChatHistoryRepository implements ChatHistoryRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(String type, String chatId) {
        // 利用唯一索引 (type, chat_id) 去重，重复则忽略
        jdbcTemplate.update(
                "INSERT IGNORE INTO chat_history (type, chat_id) VALUES (?, ?)",
                type, chatId
        );
    }

    @Override
    public List<String> getChatIds(String type) {
        return jdbcTemplate.queryForList(
                "SELECT chat_id FROM chat_history WHERE type = ?",
                String.class, type
        );
    }

    @Override
    public void delete(String type, String chatId) {
        jdbcTemplate.update(
                "DELETE FROM chat_history WHERE type = ? AND chat_id = ?",
                type, chatId
        );
    }
}
