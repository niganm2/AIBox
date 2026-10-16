<template>
  <div class="home" :class="{ dark: isDark }">
    <PathMarquee
      class="marquee-bg"
      :path="marqueePath"
      view-box="0 0 996 330"
      :base-velocity="3"
      :repeat="2"
      :drag-sensitivity="0.1"
      slowdown-on-hover
      draggable
      responsive
      grab-cursor
    >
      <div
        v-for="(image, index) in marqueeImages"
        :key="index"
        class="marquee-item"
      >
        <img :src="image" :alt="`背景图 ${index + 1}`" draggable="false" />
      </div>
    </PathMarquee>
    <div class="container">
      <h1 class="title">智盒</h1>
      <div class="cards-grid">
        <component
          :is="app.route ? 'router-link' : 'div'"
          v-for="app in aiApps"
          :key="app.id"
          :to="app.route"
          class="card"
          :class="{ 'card-placeholder': !app.route }"
        >
          <div class="card-content">
            <component :is="app.icon" class="icon" :class="app.iconClass" />
            <h2>{{ app.title }}</h2>
            <p class="desc">{{ app.description }}</p>
            <span v-if="!app.route" class="coming-soon">敬请期待</span>
          </div>
        </component>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useDark } from "@vueuse/core";
import PathMarquee from "../components/PathMarquee.vue";
import {
  ChatBubbleLeftRightIcon,
  HeartIcon,
  UserGroupIcon,
  DocumentTextIcon,
  PhotoIcon,
  CodeBracketIcon,
  NewspaperIcon,
  CpuChipIcon,
} from "@heroicons/vue/24/outline";

const isDark = useDark();

const marqueePath =
  "M1 209.434C58.5872 255.935 387.926 325.938 482.583 209.434C600.905 63.8051 525.516 -43.2211 427.332 19.9613C329.149 83.1436 352.902 242.723 515.041 267.302C644.752 286.966 943.56 181.94 995 156.5";

// 开发环境走 vite 代理（避免 COEP 拦截跨域图片），生产环境直连
const imgBase = import.meta.env.DEV
  ? "/text-to-image"
  : "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image";

const marqueeImages = [
  `${imgBase}?prompt=futuristic%20humanoid%20AI%20robot%20head%20with%20glowing%20blue%20circuit%20patterns%2C%20dark%20background%2C%20studio%20lighting%2C%20photorealistic%2C%20high%20detail&image_size=square`,
  `${imgBase}?prompt=misty%20mountain%20landscape%20at%20golden%20sunrise%2C%20layers%20of%20fog%2C%20cinematic%20photography%2C%20photorealistic&image_size=square`,
  `${imgBase}?prompt=colorful%20abstract%20liquid%20ink%20swirl%2C%20vibrant%20gradient%20colors%2C%20macro%20photography%2C%20high%20detail&image_size=square`,
  `${imgBase}?prompt=cute%20ginger%20cat%20wearing%20round%20sunglasses%2C%20studio%20portrait%2C%20soft%20lighting%2C%20photorealistic&image_size=square`,
  `${imgBase}?prompt=cyberpunk%20city%20street%20at%20night%2C%20neon%20signs%2C%20rain%20reflections%2C%20cinematic%2C%20photorealistic&image_size=square`,
  `${imgBase}?prompt=astronaut%20floating%20in%20colorful%20nebula%2C%20stars%20and%20cosmic%20dust%2C%20digital%20art%2C%20vibrant&image_size=square`,
  `${imgBase}?prompt=tropical%20beach%20with%20turquoise%20water%20and%20palm%20trees%2C%20aerial%20drone%20view%2C%20photorealistic&image_size=square`,
  `${imgBase}?prompt=macro%20photo%20of%20a%20dewdrop%20on%20a%20green%20leaf%2C%20soft%20bokeh%20background%2C%20photorealistic&image_size=square`,
];

const aiApps = ref([
  {
    id: 1,
    title: "AI聊天",
    description: "多模态对话，图文音频交互",
    route: "/ai-chat",
    icon: ChatBubbleLeftRightIcon,
  },
  {
    id: 2,
    title: "哄哄模拟器",
    description: "AI趣味陪伴对话",
    route: "/game",
    icon: HeartIcon,
    iconClass: "heart-icon",
  },
  {
    id: 3,
    title: "智能客服",
    description: "全天候智能咨询",
    route: "/customer-service",
    icon: UserGroupIcon,
  },
  {
    id: 4,
    title: "ChatPDF",
    description: "文档智能问答",
    route: "/chat-pdf",
    icon: DocumentTextIcon,
  },
  {
    id: 5,
    title: "AI图像生成",
    description: "文本一键生成创意图片",
    model: "通义万相-文生图",
    route: "/image-generation",
    icon: PhotoIcon,
  },
  {
    id: 6,
    title: "AI代码助手",
    description: "代码编写、排错与解释",
    model: "通义千问-Code",
    route: "/code-assistant",
    icon: CodeBracketIcon,
  },
  {
    id: 7,
    title: "AI文本摘要",
    description: "长文本快速提炼核心要点",
    model: "通义千问-Qwen2.5",
    route: "/text-summary",
    icon: NewspaperIcon,
  },
  {
    id: 8,
    title: "智能Agent助手",
    description: "内置时间计算、数学运算工具",
    model: "通义千问-Qwen2.5（支持Function Calling工具调用）",
    route: "/agent-assistant",
    icon: CpuChipIcon,
  },
]);
</script>

<style scoped lang="scss">
.home {
  position: relative;
  min-height: 100vh;
  padding: 2rem;
  background: var(--bg-color);
  transition: background-color 0.3s;
  overflow: hidden;

  .marquee-bg {
    position: absolute;
    inset: 0;
    z-index: 0;
    color: var(--text-color);
    opacity: 0.55;
    mask-image: linear-gradient(
      to bottom,
      transparent,
      #000 12%,
      #000 88%,
      transparent
    );
    -webkit-mask-image: linear-gradient(
      to bottom,
      transparent,
      #000 12%,
      #000 88%,
      transparent
    );

    .dark & {
      opacity: 0.35;
    }
  }

  .marquee-item {
    width: 96px;
    height: 96px;
    border-radius: 14px;
    overflow: hidden;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.18);
    transition: transform 0.3s ease-in-out;

    &:hover {
      transform: scale(1.5);
    }

    img {
      display: block;
      width: 100%;
      height: 100%;
      object-fit: cover;
      pointer-events: none;
    }
  }

  .container {
    position: relative;
    z-index: 1;
    max-width: 1600px;
    margin: 0 auto;
    padding: 0 2rem;
    // 让走马灯背景能接收悬停/拖拽事件，仅卡片恢复交互
    pointer-events: none;

    .card {
      pointer-events: auto;
    }
  }

  .title {
    text-align: center;
    font-size: 3rem;
    font-weight: 800;
    letter-spacing: 0.08em;
    margin-bottom: 4rem;
    background: linear-gradient(45deg, #326ce5, #40c9a2);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    animation: fadeIn 1s ease-out;
  }

  .cards-grid {
    display: grid;
    grid-template-columns: repeat(1, 1fr);
    gap: 2rem;
    justify-items: center;
    padding: 1rem;

    @media (min-width: 768px) {
      grid-template-columns: repeat(2, 1fr);
    }

    @media (min-width: 1200px) {
      grid-template-columns: repeat(4, 1fr);
    }
  }

  .card {
    position: relative;
    width: 100%;
    max-width: 320px;
    background: rgba(255, 255, 255, 0.78);
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
    border-radius: 16px;
    padding: 2rem;
    text-decoration: none;
    color: inherit;
    border: 1px solid rgba(0, 0, 0, 0.04);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
    overflow: hidden;
    transition:
      transform 0.2s ease,
      box-shadow 0.2s ease;

    .dark & {
      background: rgba(32, 35, 43, 0.78);
      border: 1px solid rgba(255, 255, 255, 0.06);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
    }

    &:hover {
      transform: translateY(-6px);
      box-shadow: 0 12px 28px rgba(50, 108, 229, 0.15);

      .dark & {
        box-shadow: 0 12px 28px rgba(0, 0, 0, 0.45);
      }
    }

    .card-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      min-height: 160px;
      justify-content: center;
    }

    .icon {
      width: 48px;
      height: 48px;
      margin-bottom: 1rem;
      color: #326ce5;

      &.heart-icon {
        color: #40c9a2;
        animation: pulse 1.5s ease-in-out infinite;
      }
    }

    h2 {
      font-size: 1.5rem;
      font-weight: 700;
      margin-bottom: 0.5rem;
    }

    p {
      color: #666;
      font-size: 1rem;

      .dark & {
        color: #9aa0aa;
      }
    }

    .coming-soon {
      position: absolute;
      top: 0.75rem;
      right: 0.75rem;
      padding: 0.2rem 0.6rem;
      font-size: 0.7rem;
      color: #326ce5;
      background: rgba(50, 108, 229, 0.1);
      border-radius: 999px;

      .dark & {
        background: rgba(50, 108, 229, 0.2);
      }
    }
  }

  .card-placeholder {
    cursor: default;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

@media (max-width: 768px) {
  .home {
    padding: 1rem;

    .container {
      padding: 0 1rem;
    }

    .title {
      font-size: 2rem;
    }

    .card {
      max-width: 100%;
    }
  }
}
</style>
