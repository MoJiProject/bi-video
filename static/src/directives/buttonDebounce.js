// throttle.js
export default {
  mounted(el, binding) {
    // 节流时长（默认 1000ms）
    const delay = typeof binding.value === 'number' ? binding.value : 1000
    let isThrottled = false

    // 让非交互元素（如 div）默认显示手型
    if (window.getComputedStyle(el).cursor === 'auto') {
      el.style.cursor = 'pointer'
    }

    // 自动注入样式（只会注入一次）
    if (!document.querySelector('#vue3-throttle-styles')) {
      const style = document.createElement('style')
      style.id = 'vue3-throttle-styles'
      style.textContent = `
        .throttle-disabled {
          pointer-events: none !important;
          opacity: 0.6;
          user-select: none;
        }
      `
      document.head.appendChild(style)
    }

    // 事件处理函数
    const clickHandler = (e) => {
      if (isThrottled) {
        // 禁止后续任何同元素上的监听器执行
        e.stopImmediatePropagation?.()
        e.preventDefault?.()
        return
      }

      isThrottled = true
      el.classList.add('throttle-disabled')

      setTimeout(() => {
        isThrottled = false
        el.classList.remove('throttle-disabled')
      }, delay)
    }

    el.addEventListener('click', clickHandler)

    // 保存清理函数，供 unmounted 使用
    el._throttleCleanup = () => {
      el.removeEventListener('click', clickHandler)
      delete el._throttleCleanup
    }
  },
  unmounted(el) {
    if (el._throttleCleanup) el._throttleCleanup()
  }
}