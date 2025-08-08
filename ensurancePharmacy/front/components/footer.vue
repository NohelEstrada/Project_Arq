<script setup lang="ts">
const links = [
  { name: "My Insurance.S.A.", href: "/" },
  { name: "My Website. All rights reserved.", href: "/" },
  { name: "About Us", href: "/about" },
  { name: "Services", href: "/services" },
  { name: "Contact", href: "/contact" },
  { name: "Privacy Policy", href: "/privacy" },
];

const edit = useEdit();

</script>
<template>
  <footer v-if="!edit"  class="bg-background py-8">
    <section
      class="text-primary hover:text-h-primary container mx-auto flex flex-col items-center justify-between px-4 md:flex-row"
    >
      <div class="mb-4 md:mb-0">
        <h1 class="text-primary flex justify-center text-xl font-bold">
          {{ links[0].name }}
        </h1>
        <p class="text-primary hover:text-h-secondary text-sm">
          {{ links[1].name }}
        </p>
      </div>
      <nav>
        <ul class="flex flex-nowrap space-x-6">
          <li
            class="hover:text-h-accent text-center"
          >
            <a
              :href="links[2].href"
              class="text-primary hover:text-accent transition-colors duration-200"
            >
              {{ links[2].name }}
            </a>
          </li>
          <li
            class="hover:text-h-accent text-center"
          >
            <a
              :href="links[3].href"
              class="text-primary hover:text-accent transition-colors duration-200"
            >
              {{ links[3].name }}
            </a>
          </li>
          <li
            class="hover:text-h-accent text-center"
          >
            <a
              :href="links[4].href"
              class="text-primary hover:text-accent transition-colors duration-200"
            >
              {{ links[4].name }}
            </a>
          </li>
          <li
            class="hover:text-h-accent text-center"
          >
            <a
              :href="links[5].href"
              class="text-primary hover:text-accent transition-colors duration-200"
            >
              {{ links[5].name }}
            </a>
          </li>
        </ul>
      </nav>
    </section>
  </footer>

  <footer v-if="edit" class="bg-background py-8">
    <section class="responsive-grid mx-8">
      <div class="mb-8">
        <span class="text-primary font-semibold">Title</span>
        <input type="text" class="field" :defaultValue="links[0].name"/>
      </div>
      <div class="mb-8">
        <span class="text-primary font-semibold">Rights</span>
        <input type="text" class="field" :defaultValue="links[1].name" />
      </div>
      <div class="mb-8">
        <span class="text-primary font-semibold">About Us</span>
        <input type="text" class="field" :defaultValue="links[2].name" />
      </div>
      <div class="mb-8">
        <span class="text-primary font-semibold">Services</span>
        <input type="text" class="field" :defaultValue="links[3].name" />
      </div>
      <div class="mb-8">
        <span class="text-primary font-semibold">Contact</span>
        <input type="text" class="field" :defaultValue="links[4].name" />
      </div>
      <div class="mb-8">
        <span class="text-primary font-semibold">Privacy</span>
        <input type="text" class="field" :defaultValue="links[5].name" />
      </div>
    </section>
    <button class="btn mx-auto flex justify-center">Save</button>
  </footer>
</template>
