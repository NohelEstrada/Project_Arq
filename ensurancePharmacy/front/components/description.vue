<script setup lang="ts">
withDefaults(defineProps<{ name: string; show: boolean }>(), {
  name: "prescription",
  show: false,
});
</script>
<template>
  <dialog
    class="card text-primary z-50 m-1 max-w-['150px'] px-2 py-1"
    :open="show"
  >
    {{ name }}
  </dialog>
</template>
