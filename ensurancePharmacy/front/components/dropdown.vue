<script setup>
import { ref, onMounted, onBeforeUnmount } from "vue";

const isOpen = ref(false);
const dropdownRef = ref(null);

const toggleDropdown = () => {
  isOpen.value = !isOpen.value;
};

const handleClickOutside = (event) => {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target)) {
    isOpen.value = false;
  }
};

onMounted(() => {
  document.addEventListener("click", handleClickOutside);
});

onBeforeUnmount(() => {
  document.removeEventListener("click", handleClickOutside);
});
</script>

<template>
  <div class="relative inline-block text-left" ref="dropdownRef">
    <button
      @click="toggleDropdown"
      class="bg-background text-primary hover:bg-h-background inline-flex w-full justify-center rounded-full px-4 py-2 text-sm font-medium font-semibold shadow-sm focus:outline-none"
    >
      Options
      <svg
        v-if="isOpen"
        class="ms-2"
        xmlns="http://www.w3.org/2000/svg"
        height="24px"
        viewBox="0 -960 960 960"
        width="24px"
        fill="currentColor"
      >
        <path d="M480-360 280-560h400L480-360Z" />
      </svg>
      <svg
        v-if="!isOpen"
        class="ms-2"
        xmlns="http://www.w3.org/2000/svg"
        height="24px"
        viewBox="0 -960 960 960"
        width="24px"
        fill="currentColor"
      >
        <path d="m280-400 200-200 200 200H280Z" />
      </svg>
    </button>

    <transition
      enter-active-class="transition ease-out duration-100"
      enter-from-class="transform opacity-0 scale-95"
      enter-to-class="transform opacity-100 scale-100"
      leave-active-class="transition ease-in duration-75"
      leave-from-class="transform opacity-100 scale-100"
      leave-to-class="transform opacity-0 scale-95"
    >
      <div
        v-if="isOpen"
        class="bg-background ring-opacity-5 absolute left-0 z-100 mt-2 w-56 origin-top-right rounded-md ring-1 shadow-lg ring-black"
      >
        <div
          class="py-1"
          role="menu"
          aria-orientation="vertical"
          aria-labelledby="options-menu"
        >
          <slot></slot>
        </div>
      </div>
    </transition>
  </div>
</template>
