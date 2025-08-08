<script setup lang="ts">
const slides: { image: string; alt: string }[] = [
  { image: "/attend.jpg", alt: "Seguro de Auto" },
  {
    image: "https://images.pexels.com/photos/3760067/pexels-photo-3760067.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    alt: "Seguro de Hogar",
  },
  {
    image: "https://images.pexels.com/photos/928199/pexels-photo-928199.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
    alt: "Seguro de Vida",
  },
];

const activeIndex: Ref<number> = ref(0);

const next: () => void = (): void => {
  activeIndex.value = (activeIndex.value + 1) % slides.length;
};
const prev: () => void = (): void => {
  activeIndex.value = (activeIndex.value - 1 + slides.length) % slides.length;
};

const goToSlide: (index: number) => void = (index: number): void => {
  activeIndex.value = index;
};
</script>
<template>
  <div class="relative aspect-21/9 overflow-hidden">
    <div
      class="flex transition-transform duration-500 ease-out"
      :style="{ transform: `translateX(-${activeIndex * 100}%)` }"
    >
      <div
        v-for="(slide, index) in slides"
        :key="index"
        class="min-w-full bg-[var(--primary-color)]"
      >
        <img
          :src="slide.image"
          :alt="slide.alt"
          class="object-fit h-full w-full"
        />
        <div
          class="absolute bottom-0 left-1/2 z-50 flex -translate-x-1/2 transform space-x-2 p-4"
        >
          <span
            v-for="(_, index) in slides"
            :key="index"
            @click="goToSlide(index)"
            class="h-3 w-3 cursor-pointer rounded-full"
            :class="{
              'bg-white': activeIndex === index,
              'bg-gray-400': activeIndex !== index,
            }"
          ></span>
        </div>
      </div>
    </div>
    <button
      @click="prev"
      class="button bg-opacity-50 absolute top-1/2 left-0 m-2 flex -translate-y-1/2 transform justify-center p-0 text-[var(--first)] focus:outline-none"
    >
      <svg
        stroke="#121827"
        fill="currentColor"
        stroke-width="0"
        viewBox="0 0 24 24"
        height="40px"
        width="40px"
        xmlns="http://www.w3.org/2000/svg"
      >
        <polyline
          fill="none"
          stroke-width="2"
          points="7 2 17 12 7 22"
          transform="matrix(-1 0 0 1 24 0)"
        ></polyline>
      </svg>
    </button>
    <button
      @click="next"
      class="button bg-opacity-50 absolute top-1/2 right-0 m-2 -translate-y-1/2 transform p-0 text-[var(--first)] focus:outline-none"
    >
      <svg
        stroke="#121827"
        fill="currentColor"
        stroke-width="0"
        viewBox="0 0 24 24"
        height="40px"
        width="40px"
        xmlns="http://www.w3.org/2000/svg"
      >
        <polyline
          fill="none"
          stroke-width="2"
          points="7 2 17 12 7 22"
        ></polyline>
      </svg>
    </button>
  </div>
</template>
