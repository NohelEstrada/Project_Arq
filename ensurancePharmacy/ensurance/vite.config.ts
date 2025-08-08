import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import tailwindcss from "@tailwindcss/vite";

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), tailwindcss()],
  server: {
    host: '0.0.0.0', // Permitir conexiones externas en Docker
    port: 9008,
    watch: {
      usePolling: true, // Necesario para hot reload en Docker
    },
  },
  preview: {
    host: '0.0.0.0',
    port: 9008,
  },
});
