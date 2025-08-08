import tailwindcss from "@tailwindcss/vite";
export default defineNuxtConfig({
  compatibilityDate: "2024-11-01",
  css: ['./main.css'],
  devtools: {
    enabled: true,
    timeline: {
      enabled: true
    }
  },
  vite: {
    plugins: [
      tailwindcss(),
    ],
  },
  runtimeConfig: {
    // Variables que quieras exponer al cliente deben ir dentro de `public`
    public: {
      ip: process.env.IP || '127.0.0.1',
    },
  },
});
