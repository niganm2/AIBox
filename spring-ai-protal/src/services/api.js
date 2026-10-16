const BASE_URL = "http://localhost:8080";

export const chatAPI = {
  // 发送聊天消息
  async sendMessage(data, chatId) {
    try {
      const url = new URL(`${BASE_URL}/ai/chat`);
      if (chatId) {
        url.searchParams.append("chatId", chatId);
      }

      const response = await fetch(url, {
        method: "POST",
        body:
          data instanceof FormData
            ? data
            : new URLSearchParams({ prompt: data }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return response.body.getReader();
    } catch (error) {
      console.error("API Error:", error);
      throw error;
    }
  },

  // 获取聊天历史列表
  async getChatHistory(type = "chat") {
    // 添加类型参数
    try {
      const response = await fetch(`${BASE_URL}/ai/history/${type}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const chatIds = await response.json();
      // 转换为前端需要的格式
      return chatIds.map((id) => ({
        id,
        title:
          type === "pdf"
            ? `PDF对话 ${id.slice(-6)}`
            : type === "service"
              ? `咨询 ${id.slice(-6)}`
              : `对话 ${id.slice(-6)}`,
      }));
    } catch (error) {
      console.error("API Error:", error);
      return [];
    }
  },

  // 批量删除对话记录
  async deleteChats(chatIds, type = "chat") {
    try {
      const response = await fetch(`${BASE_URL}/ai/history/${type}`, {
        method: "DELETE",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(chatIds),
      });
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return true;
    } catch (error) {
      console.error("API Error:", error);
      return false;
    }
  },

  // 获取特定对话的消息历史
  async getChatMessages(chatId, type = "chat") {
    // 添加类型参数
    try {
      const response = await fetch(`${BASE_URL}/ai/history/${type}/${chatId}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const messages = await response.json();
      // 添加时间戳
      return messages.map((msg) => ({
        ...msg,
        timestamp: new Date(), // 由于后端没有提供时间戳，这里临时使用当前时间
      }));
    } catch (error) {
      console.error("API Error:", error);
      return [];
    }
  },

  // 发送游戏消息
  async sendGameMessage(prompt, chatId) {
    try {
      const response = await fetch(
        `${BASE_URL}/ai/game?prompt=${encodeURIComponent(prompt)}&chatId=${chatId}`,
        {
          method: "GET",
        },
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return response.body.getReader();
    } catch (error) {
      console.error("API Error:", error);
      throw error;
    }
  },

  // 发送客服消息
  async sendServiceMessage(prompt, chatId) {
    try {
      const response = await fetch(
        `${BASE_URL}/ai/service?prompt=${encodeURIComponent(prompt)}&chatId=${chatId}`,
        {
          method: "GET",
        },
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return response.body.getReader();
    } catch (error) {
      console.error("API Error:", error);
      throw error;
    }
  },

  // 发送 PDF 问答消息
  async sendPdfMessage(prompt, chatId) {
    try {
      const response = await fetch(
        `${BASE_URL}/ai/pdf/chat?prompt=${encodeURIComponent(prompt)}&chatId=${chatId}`,
        {
          method: "GET",
          // 确保使用流式响应
          signal: AbortSignal.timeout(30000), // 30秒超时
        },
      );

      if (!response.ok) {
        throw new Error(`API error: ${response.status}`);
      }

      // 返回可读流
      return response.body.getReader();
    } catch (error) {
      console.error("API Error:", error);
      throw error;
    }
  },

  // AI图像生成
  async generateImage(prompt) {
    try {
      const response = await fetch(
        `${BASE_URL}/ai/image?prompt=${encodeURIComponent(prompt)}`,
      );
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return await response.json();
    } catch (error) {
      console.error("API Error:", error);
      throw error;
    }
  },

  // AI代码助手
  async sendCodeMessage(prompt, chatId) {
    try {
      const response = await fetch(
        `${BASE_URL}/ai/code?prompt=${encodeURIComponent(prompt)}&chatId=${chatId}`,
        {
          method: "GET",
        },
      );
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.body.getReader();
    } catch (error) {
      console.error("API Error:", error);
      throw error;
    }
  },

  // AI文本摘要
  async sendSummaryMessage(prompt, chatId) {
    try {
      const response = await fetch(
        `${BASE_URL}/ai/summary?prompt=${encodeURIComponent(prompt)}&chatId=${chatId}`,
        {
          method: "GET",
        },
      );
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.body.getReader();
    } catch (error) {
      console.error("API Error:", error);
      throw error;
    }
  },

  // 智能Agent助手
  async sendAgentMessage(prompt, chatId) {
    try {
      const response = await fetch(
        `${BASE_URL}/ai/agent?prompt=${encodeURIComponent(prompt)}&chatId=${chatId}`,
        {
          method: "GET",
        },
      );
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.body.getReader();
    } catch (error) {
      console.error("API Error:", error);
      throw error;
    }
  },
};
