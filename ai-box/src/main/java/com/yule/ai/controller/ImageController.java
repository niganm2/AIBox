package com.yule.ai.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class ImageController {

    private final ImageModel imageModel;

    @RequestMapping(value = "/image")
    public Map<String, Object> generateImage(@RequestParam("prompt") String prompt) {
        Map<String, Object> result = new HashMap<>();
        try {
            ImageResponse response = imageModel.call(new ImagePrompt(prompt));
            if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
                result.put("success", false);
                result.put("message", "图片生成失败，请重试");
                return result;
            }

            String imageUrl = response.getResults().get(0).getOutput().getUrl();
            log.debug("生成图片URL: {}", imageUrl);

            // 下载图片并转 base64，避免前端跨域/COEP 拦截
            byte[] imageBytes = downloadImage(imageUrl);
            if (imageBytes == null || imageBytes.length == 0) {
                result.put("success", false);
                result.put("message", "图片下载失败，请重试");
                return result;
            }

            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            result.put("success", true);
            result.put("url", imageUrl);
            result.put("base64", "data:image/png;base64," + base64);
        } catch (Exception e) {
            log.error("图片生成异常", e);
            result.put("success", false);
            result.put("message", "图片生成异常：" + e.getMessage());
        }
        return result;
    }

    private byte[] downloadImage(String imageUrl) {
        try (InputStream in = URI.create(imageUrl).toURL().openStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            return out.toByteArray();
        } catch (Exception e) {
            log.error("下载图片失败: {}", imageUrl, e);
            return null;
        }
    }
}
