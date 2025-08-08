<script setup lang="ts">
const notifications: Ref<notification[]> = useNotifications();
</script>

<template>
  <section class="fixed top-4 right-4 space-y-4">
    <div
      v-for="(notification, index) in notifications"
      :class="[
        'bg-background animate-appear-slow rounded-lg',
        notification.type === 'loading'
          ? 'text-accent hover:text-h-accent'
          : '',
        notification.type === 'success'
          ? 'text-success hover:text-h-success'
          : '',
        notification.type === 'error' ? 'text-error hover:text-h-error' : '',
      ]"
      :key="index"
    >
      <h2
        class="bg-h-background mb-2 flex rounded-t-lg border-b-2 px-4 pt-2 pb-2 text-lg font-bold"
      >
        {{ notification.title }}
        <button class="ms-auto">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            height="24px"
            viewBox="0 -960 960 960"
            width="24px"
            fill="currentColor"
          >
            <path
              d="m256-200-56-56 224-224-224-224 56-56 224 224 224-224 56 56-224 224 224 224-56 56-224-224-224 224Z"
            />
          </svg>
        </button>
      </h2>

      <p class="text-secondary hover:text-h-secondary px-4 pb-2">
        {{ notification.description }}
      </p>
    </div>
  </section>
</template>
