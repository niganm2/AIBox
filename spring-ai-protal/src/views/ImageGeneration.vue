<template>
  <div class="image-generation" :class="{ dark: isDark }">
    <div class="container">
      <div class="chat-header">
        <PhotoIcon class="header-icon" />
        <div class="header-info">
          <h1>AI图像生成</h1>
          <p>文本一键生成创意图片</p>
        </div>
      </div>

      <div class="prompt-area">
        <textarea
          v-model="userInput"
          placeholder="请输入图片描述，例如：一只戴着墨镜的橘猫在海边散步，日落光线，电影感"
          rows="3"
          :disabled="isGenerating"
        ></textarea>
        <button
          class="generate-btn"
          @click="generateImage"
          :disabled="isGenerating || !userInput.trim()"
        >
          <template v-if="isGenerating">
            <div class="spinner"></div>
            生成中...
          </template>
          <template v-else>
            <SparklesIcon class="icon" />
            生成图片
          </template>
        </button>
      </div>

      <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

      <div class="result-area">
        <div v-if="isGenerating" class="loading-placeholder">
          <div class="spinner-large"></div>
          <p>正在创作中，请稍候...</p>
        </div>

        <div v-else-if="generatedImage" class="image-result">
          <img :src="generatedImage" alt="生成的图片" />
          <div class="image-actions">
            <a :href="imageDownloadUrl" download="ai-generated.png" class="download-btn">
              <ArrowDownTrayIcon class="icon" />
              下载图片
            </a>
            <button class="regenerate-btn" @click="generateImage" :disabled="isGenerating">
              <ArrowPathIcon class="icon" />
              重新生成
            </button>
          </div>
        </div>

        <div v-else-if="history.length > 0" class="history-grid">
          <div class="history-title">历史生成</div>
          <div class="grid">
            <div
              v-for="(item, index) in history"
              :key="index"
              class="history-item"
              @click="generatedImage = item"
            >
              <img :src="item" alt="历史图片" />
            </div>
          </div>
        </div>

        <div v-else class="empty-tip">
          <PhotoIcon class="empty-icon" />
          <p>输入描述文字，点击"生成图片"即可创作</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useDark } from '@vueuse/core'
import {
  PhotoIcon,
  SparklesIcon,
  ArrowDownTrayIcon,
  ArrowPathIcon
} from '@heroicons/vue/24/outline'
import { chatAPI } from '../services/api'

const isDark = useDark()
const userInput = ref('')
const isGenerating = ref(false)
const generatedImage = ref('')
const imageDownloadUrl = ref('')
const errorMsg = ref('')
const history = ref([])

const generateImage = async () => {
  if (isGenerating.value || !userInput.value.trim()) return
  errorMsg.value = ''
  isGenerating.value = true
  generatedImage.value = ''
  imageDownloadUrl.value = ''

  try {
    const result = await chatAPI.generateImage(userInput.value.trim())
    if (result && result.success && result.base64) {
      // 显示用 base64（同源，避免 COEP 拦截），下载用原始 URL
      generatedImage.value = result.base64
      imageDownloadUrl.value = result.url || result.base64
      history.value.unshift(result.base64)
      if (history.value.length > 12) history.value.pop()
    } else {
      errorMsg.value = result?.message || '图片生成失败，请重试'
    }
  } catch (error) {
    console.error('图片生成失败:', error)
    errorMsg.value = '图片生成失败，请检查网络后重试'
  } finally {
    isGenerating.value = false
  }
}
</script>

<style scoped lang="scss">
.image-generation {
  min-height: calc(100vh - 80px);
  background: var(--bg-color);
  color: var(--text-color);
  transition: background-color 0.3s, color 0.3s;

  .container {
    max-width: 900px;
    margin: 0 auto;
    padding: 2rem;
  }

  .chat-header {
    display: flex;
    align-items: center;
    gap: 1rem;
    margin-bottom: 2rem;

    .header-icon {
      width: 44px;
      height: 44px;
      color: #326ce5;
    }

    .header-info {
      h1 {
        font-size: 1.4rem;
        font-weight: 700;
        margin: 0;
      }

      p {
        font-size: 0.9rem;
        color: #888;
        margin: 0.25rem 0 0;

        .dark & {
          color: #9aa0aa;
        }
      }
    }
  }

  .prompt-area {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    margin-bottom: 1.5rem;

    textarea {
      padding: 1rem;
      border: 1px solid rgba(0, 0, 0, 0.1);
      border-radius: 12px;
      background: var(--bg-color);
      color: var(--text-color);
      font-size: 1rem;
      resize: vertical;
      outline: none;
      transition: border-color 0.2s;
      min-height: 80px;

      &:focus {
        border-color: #326ce5;
      }

      &:disabled {
        opacity: 0.6;
      }

      .dark & {
        border-color: rgba(255, 255, 255, 0.1);
      }
    }

    .generate-btn {
      align-self: flex-end;
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.7rem 1.5rem;
      background: #326ce5;
      color: white;
      border: none;
      border-radius: 10px;
      cursor: pointer;
      font-size: 1rem;
      font-weight: 600;
      transition: background 0.2s;

      &:hover:not(:disabled) {
        background: #2858c4;
      }

      &:disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }

      .icon {
        width: 20px;
        height: 20px;
      }

      .spinner {
        width: 18px;
        height: 18px;
        border: 2px solid rgba(255, 255, 255, 0.3);
        border-top-color: white;
        border-radius: 50%;
        animation: spin 0.8s linear infinite;
      }
    }
  }

  .error-msg {
    background: rgba(239, 68, 68, 0.1);
    color: #ef4444;
    padding: 0.75rem 1rem;
    border-radius: 8px;
    margin-bottom: 1rem;
    font-size: 0.9rem;
  }

  .result-area {
    min-height: 300px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .loading-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1rem;
    color: #888;

    .dark & {
      color: #9aa0aa;
    }

    .spinner-large {
      width: 48px;
      height: 48px;
      border: 4px solid rgba(50, 108, 229, 0.2);
      border-top-color: #326ce5;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }
  }

  .image-result {
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1rem;

    img {
      max-width: 100%;
      max-height: 60vh;
      border-radius: 12px;
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
    }

    .image-actions {
      display: flex;
      gap: 1rem;

      .download-btn,
      .regenerate-btn {
        display: flex;
        align-items: center;
        gap: 0.4rem;
        padding: 0.6rem 1.2rem;
        border-radius: 8px;
        cursor: pointer;
        font-size: 0.9rem;
        text-decoration: none;
        transition: all 0.2s;
        border: none;
      }

      .download-btn {
        background: #40c9a2;
        color: white;

        &:hover {
          background: #35b08e;
        }
      }

      .regenerate-btn {
        background: transparent;
        color: #326ce5;
        border: 1px solid #326ce5;

        &:hover {
          background: rgba(50, 108, 229, 0.1);
        }

        &:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }
      }

      .icon {
        width: 18px;
        height: 18px;
      }
    }
  }

  .history-grid {
    width: 100%;

    .history-title {
      font-size: 0.95rem;
      font-weight: 600;
      margin-bottom: 1rem;
      color: #666;

      .dark & {
        color: #aaa;
      }
    }

    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
      gap: 1rem;
    }

    .history-item {
      aspect-ratio: 1;
      border-radius: 10px;
      overflow: hidden;
      cursor: pointer;
      transition: transform 0.2s;
      border: 2px solid transparent;

      &:hover {
        transform: scale(1.03);
        border-color: #326ce5;
      }

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
  }

  .empty-tip {
    text-align: center;
    color: #aaa;

    .empty-icon {
      width: 64px;
      height: 64px;
      margin-bottom: 1rem;
      opacity: 0.4;
    }

    p {
      font-size: 1rem;
    }
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 768px) {
  .image-generation {
    .container {
      padding: 1rem;
    }
  }
}
</style>
