<script setup lang="ts">
import { darkMode } from "~/composables/darkMode";

const toggleSidebar = useToggleSidebar();
const dark = darkMode();

onMounted(() => {
  let role: string | null = localStorage.getItem("role");
  if (role === null) {
    setUser("guest");
  }
  if (dark.value) {
    document.documentElement.classList.add("dark");
  } else {
    document.documentElement.classList.remove("dark");
  }
});

watch(dark, (newValue, oldValue) => {
  if (dark.value) {
    document.documentElement.classList.add("dark");
  } else {
    document.documentElement.classList.remove("dark");
  }
});
</script>

<template>
  <transition
    enter-active-class="transition-opacity duration-500"
    enter-from-class="opacity-0"
    leave-active-class="transition-opacity duration-500"
    leave-to-class="opacity-0"
    mode="out-in"
  >
    <div class="grid min-h-[100dvh] grid-rows-[1fr_auto]">
      <div class="flex h-full w-full">
        <Sidebar />
        <div
          :class="
            toggleSidebar ? 'flex h-full w-full' : 'appear flex h-full w-full'
          "
        >
          <slot></slot>
        </div>
      </div>
      <Footer />
    </div>
  </transition>
  <Notifications />
</template>
