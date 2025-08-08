<template>
  <button @click="count++">Click me</button>
  <p>Count is {{ count }}</p>
</template>

<script setup>
import { ref } from 'vue';

const count = ref(0);
</script>

