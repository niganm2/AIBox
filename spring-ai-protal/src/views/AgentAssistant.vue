<template>
  <div class="agent-assistant" :class="{ dark: isDark }">
    <div class="chat-container">
      <div class="chat-header">
        <CpuChipIcon class="header-icon" />
        <div class="header-info">
          <h1>智能Agent助手</h1>
          <p>内置时间计算、数学运算工具</p>
        </div>
        <button class="new-chat-btn" @click="startNewChat">
          <PlusIcon class="icon" />
          新对话
        </button>
      </div>

      <div class="messages" ref="messagesRef">
        <div v-if="currentMessages.length === 0" class="welcome">
          <CpuChipIcon class="welcome-icon" />
          <h2>你好，我是智能Agent助手</h2>
          <p>
            我可以调用工具帮你计算时间和数学问题。试试问我：现在几点？或
            128除以8等于多少？
          </p>
        </div>
        <ChatMessage
          v-for="(message, index) in currentMessages"
          :key="index"
          :message="message"
          :is-stream="isStreaming && index === currentMessages.length - 1"
        />
      </div>

      <div class="input-area">
        <textarea
          v-model="userInput"
          @keydown.enter.prevent="sendMessage"
          placeholder="试试问我：现在几点？2024年1月1日到今天有多少天？128除以8等于多少？"
          rows="1"
          ref="inputRef"
        ></textarea>
        <button
          class="send-button"
          @click="sendMessage"
          :disabled="isStreaming || !userInput.trim()"
        >
          <PaperAirplaneIcon class="icon" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from "vue";
import { useDark } from "@vueuse/core";
import {
  CpuChipIcon,
  PaperAirplaneIcon,
  PlusIcon,
} from "@heroicons/vue/24/outline";
import ChatMessage from "../components/ChatMessage.vue";
import { chatAPI } from "../services/api";

const isDark = useDark();
const messagesRef = ref(null);
const inputRef = ref(null);
const userInput = ref("");
const isStreaming = ref(false);
const currentChatId = ref(null);
const currentMessages = ref([]);

const generateChatId = () =>
  "agent_" + Date.now() + "_" + Math.random().toString(36).slice(2, 8);

const startNewChat = () => {
  currentChatId.value = generateChatId();
  currentMessages.value = [];
};

const adjustTextareaHeight = () => {
  const textarea = inputRef.value;
  if (textarea) {
    textarea.style.height = "auto";
    textarea.style.height = Math.min(textarea.scrollHeight, 200) + "px";
  }
};

const scrollToBottom = async () => {
  await nextTick();
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight;
  }
};

const sendMessage = async () => {
  if (isStreaming.value || !userInput.value.trim()) return;
  if (!currentChatId.value) currentChatId.value = generateChatId();

  const messageContent = userInput.value.trim();
  currentMessages.value.push({
    role: "user",
    content: messageContent,
    timestamp: new Date(),
  });
  userInput.value = "";
  adjustTextareaHeight();
  await scrollToBottom();

  const assistantMessage = {
    role: "assistant",
    content: "",
    timestamp: new Date(),
  };
  currentMessages.value.push(assistantMessage);
  isStreaming.value = true;

  try {
    const reader = await chatAPI.sendAgentMessage(
      messageContent,
      currentChatId.value,
    );
    const decoder = new TextDecoder();
    let accumulatedContent = "";
    while (true) {
      try {
        const { value, done } = await reader.read();
        if (done) break;
        accumulatedContent += decoder.decode(value);
        await nextTick(() => {
          const lastIndex = currentMessages.value.length - 1;
          currentMessages.value.splice(lastIndex, 1, {
            ...assistantMessage,
            content: accumulatedContent,
          });
        });
        await scrollToBottom();
      } catch (readError) {
        console.error("读取流错误:", readError);
        break;
      }
    }
  } catch (error) {
    console.error("发送消息失败:", error);
    const errMsg = error.message || "网络请求失败，请检查后端服务是否启动";
    const lastIndex = currentMessages.value.length - 1;
    currentMessages.value.splice(lastIndex, 1, {
      ...assistantMessage,
      content: "抱歉，发生了错误：" + errMsg,
    });
  } finally {
    isStreaming.value = false;
  }
};

onMounted(() => {
  startNewChat();
  adjustTextareaHeight();
});
</script>

<style scoped lang="scss">
.agent-assistant {
  height: calc(100vh - 80px);
  display: flex;
  background: var(--bg-color);
  color: var(--text-color);
  transition:
    background-color 0.3s,
    color 0.3s;

  .chat-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    max-width: 900px;
    margin: 0 auto;
    width: 100%;
  }

  .chat-header {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 1.2rem 2rem;
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);

    .dark & {
      border-bottom-color: rgba(255, 255, 255, 0.06);
    }

    .header-icon {
      width: 40px;
      height: 40px;
      color: #326ce5;
    }

    .header-info {
      flex: 1;

      h1 {
        font-size: 1.25rem;
        font-weight: 700;
        margin: 0;
      }

      p {
        font-size: 0.85rem;
        color: #888;
        margin: 0;

        .dark & {
          color: #9aa0aa;
        }
      }
    }

    .new-chat-btn {
      display: flex;
      align-items: center;
      gap: 0.4rem;
      padding: 0.5rem 1rem;
      background: #326ce5;
      color: white;
      border: none;
      border-radius: 8px;
      cursor: pointer;
      font-size: 0.9rem;
      transition: background 0.2s;

      &:hover {
        background: #2858c4;
      }

      .icon {
        width: 18px;
        height: 18px;
      }
    }
  }

  .messages {
    flex: 1;
    overflow-y: auto;
    padding: 1.5rem 2rem;
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
  }

  .welcome {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    margin: auto 0;
    color: #888;

    .dark & {
      color: #9aa0aa;
    }

    .welcome-icon {
      width: 64px;
      height: 64px;
      color: #326ce5;
      margin-bottom: 1rem;
      opacity: 0.7;
    }

    h2 {
      font-size: 1.3rem;
      font-weight: 700;
      margin: 0 0 0.5rem;
      color: var(--text-color);
    }

    p {
      font-size: 0.95rem;
      margin: 0;
      max-width: 400px;
    }
  }

  .input-area {
    padding: 1rem 2rem 1.5rem;
    border-top: 1px solid rgba(0, 0, 0, 0.06);
    display: flex;
    align-items: flex-end;
    gap: 0.75rem;

    .dark & {
      border-top-color: rgba(255, 255, 255, 0.06);
    }

    textarea {
      flex: 1;
      padding: 0.75rem 1rem;
      border: 1px solid rgba(0, 0, 0, 0.1);
      border-radius: 12px;
      background: var(--bg-color);
      color: var(--text-color);
      font-size: 0.95rem;
      resize: none;
      outline: none;
      transition: border-color 0.2s;
      max-height: 200px;

      &:focus {
        border-color: #326ce5;
      }

      .dark & {
        border-color: rgba(255, 255, 255, 0.1);
      }
    }

    .send-button {
      width: 44px;
      height: 44px;
      border-radius: 12px;
      background: #326ce5;
      color: white;
      border: none;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: background 0.2s;
      flex-shrink: 0;

      &:hover:not(:disabled) {
        background: #2858c4;
      }

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }

      .icon {
        width: 20px;
        height: 20px;
      }
    }
  }
}

@media (max-width: 768px) {
  .agent-assistant {
    .chat-header {
      padding: 1rem;
    }
    .messages {
      padding: 1rem;
    }
    .input-area {
      padding: 0.75rem 1rem 1rem;
    }
  }
}
</style>
