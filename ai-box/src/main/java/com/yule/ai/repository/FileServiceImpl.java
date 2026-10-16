package com.yule.ai.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yule.ai.entity.po.PdfChatFile;
import com.yule.ai.mapper.PdfChatFileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileRepository {

    private final VectorStore vectorStore;
    private final PdfChatFileMapper pdfChatFileMapper;

    @Override
    public boolean save(String chatId, Resource resource) {
        // 1. 保存文件到本地磁盘
        String filename = resource.getFilename();
        File target = new File(Objects.requireNonNull(filename));
        if (!target.exists()) {
            try {
                Files.copy(resource.getInputStream(), target.toPath());
            } catch (IOException e) {
                log.error("Failed to save PDF resource.", e);
                return false;
            }
        }

        // 2. 保存 chatId → fileName 绑定关系到 MySQL（存在则更新，不存在则新增）
        PdfChatFile existing = pdfChatFileMapper.selectOne(
                new LambdaQueryWrapper<PdfChatFile>().eq(PdfChatFile::getChatId, chatId)
        );
        if (existing != null) {
            existing.setFileName(filename);
            pdfChatFileMapper.updateById(existing);
        } else {
            PdfChatFile bind = new PdfChatFile();
            bind.setChatId(chatId);
            bind.setFileName(filename);
            pdfChatFileMapper.insert(bind);
        }

        // 3. 写入向量库
        writeToVectorStore(resource, chatId);
        return true;
    }

    @Override
    public Resource getFile(String chatId) {
        PdfChatFile bind = pdfChatFileMapper.selectOne(
                new LambdaQueryWrapper<PdfChatFile>().eq(PdfChatFile::getChatId, chatId)
        );
        if (bind == null) {
            log.warn("chatId [{}] 未绑定任何 PDF 文件", chatId);
            return new FileSystemResource("");
        }
        return new FileSystemResource(bind.getFileName());
    }

    private void writeToVectorStore(Resource resource, String chatId) {
        // 1. 创建 PDF 读取器
        PagePdfDocumentReader reader = new PagePdfDocumentReader(
                resource,
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                        .withPagesPerDocument(1)
                        .build()
        );
        // 2. 读取 PDF 文档，拆分为 Document
        List<Document> documents = reader.read();
        String fileName = resource.getFilename();
        documents.forEach(document -> {
            document.getMetadata().put("chat_id", chatId);
            if (fileName != null) {
                document.getMetadata().put("file_name", fileName);
            }
        });
        // 3. 写入向量库
        vectorStore.add(documents);
    }
}
