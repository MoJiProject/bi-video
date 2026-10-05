import { onBeforeUnmount, onMounted } from 'vue'

let lockCount = 0
let savedStyles = null

function applyStyles(styles) {
  savedStyles = {}
  for (const key of Object.keys(styles)) {
    savedStyles[key] = document.body.style[key]
    document.body.style[key] = styles[key]
  }
}

function restoreStyles() {
  if (!savedStyles) return
  for (const key of Object.keys(savedStyles)) {
    if (savedStyles[key]) document.body.style[key] = savedStyles[key]
    else document.body.style.removeProperty(key)
  }
  savedStyles = null
}

export function useBodyLayout(styles) {
  onMounted(() => {
    lockCount += 1
    if (lockCount === 1) applyStyles(styles)
  })

  onBeforeUnmount(() => {
    lockCount = Math.max(0, lockCount - 1)
    if (lockCount === 0) restoreStyles()
  })
}
