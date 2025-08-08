<script setup lang="ts">
defineProps({
  show: Boolean,
});
</script>

<template>
  <div
    v-if="show"
    class="fixed inset-0 z-50 flex h-full w-full items-center justify-center rounded-lg backdrop-blur-md"
  >
    <dialog
      class="bg-background relative z-60 w-full max-w-md rounded-lg px-4 py-2 shadow-xl"
      :open="show"
    >
      <button @click="$emit('update:show', false)">
        <svg
          class="text-primary"
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
      <div class="flex-col px-6">
        <slot></slot>
      </div>
    </dialog>
  </div>
</template>
