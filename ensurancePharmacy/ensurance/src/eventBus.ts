import { reactive } from 'vue'

export type EventCallback = (...args: any[]) => void

export interface EventBus {
  on(event: string, callback: EventCallback): void
  emit(event: string, ...args: any[]): void
  off(event: string, callback: EventCallback): void
}

export default reactive<EventBus>({
  on(event: string, callback: EventCallback) {
    window.addEventListener(event, (e: Event) => {
      if (e instanceof CustomEvent) {
        callback(...e.detail)
      }
    })
  },
  emit(event: string, ...args: any[]) {
    window.dispatchEvent(new CustomEvent(event, { detail: args }))
  },
  off(event: string, callback: EventCallback) {
    window.removeEventListener(event, callback as EventListener)
  }
}) 