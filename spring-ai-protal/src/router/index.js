import { createRouter, createWebHistory } from 'vue-router'
import GameChat from '../views/GameChat.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/ai-chat',
    name: 'AIChat',
    component: () => import('../views/AIChat.vue')
  },
  {
    path: '/comfort-simulator',
    name: 'ComfortSimulator',
    component: () => import('../views/ComfortSimulator.vue')
  },
  {
    path: '/customer-service',
    name: 'CustomerService',
    component: () => import('../views/CustomerService.vue')
  },
  {
    path: '/chat-pdf',
    name: 'ChatPDF',
    component: () => import('../views/ChatPDF.vue')
  },
  {
    path: '/game',
    name: 'game',
    component: () => import('../views/GameChat.vue')
  },
  {
    path: '/image-generation',
    name: 'ImageGeneration',
    component: () => import('../views/ImageGeneration.vue')
  },
  {
    path: '/code-assistant',
    name: 'CodeAssistant',
    component: () => import('../views/CodeAssistant.vue')
  },
  {
    path: '/text-summary',
    name: 'TextSummary',
    component: () => import('../views/TextSummary.vue')
  },
  {
    path: '/agent-assistant',
    name: 'AgentAssistant',
    component: () => import('../views/AgentAssistant.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router 