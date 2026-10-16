package com.yule.ai.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * PDF 会话与文件绑定表
 */
@Data
@TableName("pdf_chat_file")
public class PdfChatFile implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    private String chatId;

    /**
     * PDF 文件名
     */
    private String fileName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
