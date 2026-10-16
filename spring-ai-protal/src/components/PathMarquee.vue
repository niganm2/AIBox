<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, useSlots, cloneVNode, Fragment, Comment } from 'vue'
import type { VNode } from 'vue'

interface Props {
  path: string
  viewBox?: string
  baseVelocity?: number
  slowdownOnHover?: boolean
  draggable?: boolean
  repeat?: number
  dragSensitivity?: number
  responsive?: boolean
  grabCursor?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  viewBox: '0 0 996 330',
  baseVelocity: 8,
  slowdownOnHover: true,
  draggable: true,
  repeat: 2,
  dragSensitivity: 0.1,
  responsive: false,
  grabCursor: false
})

const slots = useSlots()

const containerRef = ref<HTMLElement | null>(null)
const pathRef = ref<SVGPathElement | null>(null)

const dragging = ref(false)
let hovered = false
let lastX = 0
let rafId = 0
let lastTime = 0
let progress = 0
let dragOffset = 0
let speedFactor = 1
let pathLength = 0
let itemEls: HTMLElement[] = []

const vbWidth = computed(() => Number(props.viewBox.split(/\s+/)[2]) || 1)
const vbHeight = computed(() => Number(props.viewBox.split(/\s+/)[3]) || 1)

// 拍平插槽节点（处理 v-for 产生的 Fragment），并复制 repeat 份
function flatten(nodes: VNode[]): VNode[] {
  const out: VNode[] = []
  for (const n of nodes) {
    if (n.type === Fragment && Array.isArray(n.children)) {
      out.push(...flatten(n.children as VNode[]))
    } else if (n.type !== Comment) {
      out.push(n)
    }
  }
  return out
}

const items = computed<VNode[]>(() => {
  const base = flatten(slots.default?.() ?? [])
  const result: VNode[] = []
  for (let r = 0; r < props.repeat; r++) {
    base.forEach((node, idx) => {
      result.push(cloneVNode(node, { key: `${r}-${idx}` }))
    })
  }
  return result
})

function tick(now: number) {
  if (!lastTime) lastTime = now
  const dt = Math.min((now - lastTime) / 1000, 0.05)
  lastTime = now

  // 悬停或拖拽时平滑减速
  const targetFactor = (hovered && props.slowdownOnHover) || dragging.value ? 0.08 : 1
  speedFactor += (targetFactor - speedFactor) * Math.min(dt * 6, 1)

  // baseVelocity 语义：每秒走过路径全长的百分比
  progress += (props.baseVelocity / 100) * speedFactor * dt

  layout()
  rafId = requestAnimationFrame(tick)
}

function layout() {
  if (!pathRef.value || !itemEls.length || !pathLength) return
  const total = itemEls.length
  const w = vbWidth.value
  const h = vbHeight.value
  for (let i = 0; i < total; i++) {
    let offset = (progress + dragOffset + i / total) % 1
    if (offset < 0) offset += 1
    const point = pathRef.value.getPointAtLength(offset * pathLength)
    const el = itemEls[i]
    el.style.left = `${(point.x / w) * 100}%`
    el.style.top = `${(point.y / h) * 100}%`
  }
}

function onPointerDown(e: PointerEvent) {
  if (!props.draggable || e.button !== 0) return
  dragging.value = true
  lastX = e.clientX
  try {
    containerRef.value?.setPointerCapture(e.pointerId)
  } catch {
    // 合成事件等无活动指针的场景下忽略
  }
}

function onPointerMove(e: PointerEvent) {
  if (!dragging.value) return
  const dx = e.clientX - lastX
  lastX = e.clientX
  dragOffset += (dx * props.dragSensitivity) / 1000
}

function onPointerUp(e: PointerEvent) {
  if (!dragging.value) return
  dragging.value = false
  try {
    containerRef.value?.releasePointerCapture(e.pointerId)
  } catch {
    // 同上
  }
}

function onMouseEnter() {
  hovered = true
}

function onMouseLeave() {
  hovered = false
}

onMounted(() => {
  pathLength = pathRef.value?.getTotalLength() ?? 0
  itemEls = Array.from(
    containerRef.value?.querySelectorAll('.path-marquee-item') ?? []
  ) as HTMLElement[]

  // 使用原生事件绑定，保证真实输入与派发事件行为一致
  const el = containerRef.value
  el?.addEventListener('pointerdown', onPointerDown)
  el?.addEventListener('pointermove', onPointerMove)
  el?.addEventListener('pointerup', onPointerUp)
  el?.addEventListener('pointercancel', onPointerUp)
  el?.addEventListener('mouseenter', onMouseEnter)
  el?.addEventListener('mouseleave', onMouseLeave)

  rafId = requestAnimationFrame(tick)
})

onBeforeUnmount(() => {
  cancelAnimationFrame(rafId)
  const el = containerRef.value
  el?.removeEventListener('pointerdown', onPointerDown)
  el?.removeEventListener('pointermove', onPointerMove)
  el?.removeEventListener('pointerup', onPointerUp)
  el?.removeEventListener('pointercancel', onPointerUp)
  el?.removeEventListener('mouseenter', onMouseEnter)
  el?.removeEventListener('mouseleave', onMouseLeave)
})
</script>

<template>
  <div
    ref="containerRef"
    class="path-marquee"
    :class="{
      'is-responsive': responsive,
      'is-grabbable': grabCursor && draggable,
      'is-dragging': dragging
    }"
  >
    <svg
      class="path-marquee-svg"
      :viewBox="viewBox"
      :preserveAspectRatio="responsive ? 'none' : 'xMidYMid meet'"
      aria-hidden="true"
    >
      <path ref="pathRef" :d="path" fill="none" class="path-marquee-path" />
    </svg>
    <div v-for="(node, i) in items" :key="(node.key as string) ?? i" class="path-marquee-item">
      <component :is="node" />
    </div>
  </div>
</template>

<style scoped>
.path-marquee {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  touch-action: pan-y;
  user-select: none;
}

.path-marquee.is-grabbable {
  cursor: grab;
}

.path-marquee.is-dragging {
  cursor: grabbing;
}

.path-marquee-svg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.path-marquee-path {
  stroke: currentColor;
  stroke-opacity: 0.12;
  stroke-width: 1.5;
  vector-effect: non-scaling-stroke;
}

.path-marquee-item {
  position: absolute;
  transform: translate(-50%, -50%);
  will-change: left, top;
}
</style>
