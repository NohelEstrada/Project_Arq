<script setup lang="ts">
withDefaults(
  defineProps<{
    styles?: string;
    label?: string;
    layout?: string;
    onchange?: (event: Event) => void;
    disabled?: boolean;
    checked?: boolean;
    reverse?: boolean;
  }>(),
  {
    styles: "",
    label: "Click me",
    layout: "",
    onchange: () => {},
    disabled: false,
    checked: false,
    reverse: false,
  },
);
</script>

<template>
  <div :class="['inline-block items-center align-middle', layout]">
    <span
      v-if="reverse && label"
      class="text-primary mr-2 flex align-middle select-none"
    >
      <strong>{{ label }}</strong>
    </span>
    <label class="relative inline-flex cursor-pointer items-center gap-2">
      <input
        :checked="checked"
        type="checkbox"
        :disabled="disabled"
        @change="onchange"
        class="peer absolute h-0 w-0 opacity-0"
      />
      <span
        :class="[
          `peer-checked:border-accent peer-checked:bg-accent peer-focus:ring-accent/50 relative box-border inline-block h-5 w-10 rounded-full border-2 border-gray-400 bg-gray-300 transition-all duration-200 peer-focus:ring-2 peer-focus:outline-none peer-disabled:border-gray-300 peer-disabled:bg-gray-200 before:absolute before:top-[1px] before:left-[1px] before:h-[14px] before:w-[14px] before:rounded-full before:bg-gray-900 before:transition-transform before:duration-200 before:content-[''] peer-checked:before:translate-x-[20px]`,
          styles,
        ]"
      ></span>
      <span
        v-if="label && !reverse"
        class="text-primary inline-block items-center align-middle select-none"
      >
        <strong>{{ label }}</strong>
      </span>
    </label>
  </div>
</template>
