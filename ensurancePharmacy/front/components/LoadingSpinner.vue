<script setup lang="ts">
defineProps({
  size: {
    type: String,
    default: 'md',
    validator: (value: string) => ['sm', 'md', 'lg'].includes(value)
  },
  message: {
    type: String,
    default: 'Cargando...'
  },
  showMessage: {
    type: Boolean,
    default: true
  }
});
</script>

<template>
  <div class="w-full h-full  flex items-center justify-center space-y-4 p-4">
    <!-- Spinner with customizable size -->
    <div 
      :class="[
        'animate-spin rounded-full border-t-transparent absolute',
        size === 'sm' ? 'h-6 w-6 border-2' : 
        size === 'lg' ? 'h-12 w-12 border-4' : 'h-8 w-8 border-3',
        'border-primary'  // Using your primary color variable from main.css
      ]"
      role="status"
      aria-label="loading"
    ></div>
    
    <!-- Optional loading message -->
    <p v-if="showMessage" class="text-gray-600 font-medium">{{ message }}</p>
  </div>
</template> 