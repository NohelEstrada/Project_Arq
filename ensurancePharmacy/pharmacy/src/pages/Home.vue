<!-- eslint-disable vue/multi-word-component-names -->
<template>
    <div class="dashboard">
      <!-- Sidebar Izquierdo -->
      <aside class="sidebar">
        <div class="contact-icons">
          <button class="icon-btn">💬</button>
          <button class="icon-btn">📞</button>
          <button class="icon-btn">📱</button>
        </div>
        <ul class="sidebar-menu">
          <li>Equipo Médico</li>
          <router-link to="/aseguradoras" class="nav-item">Aseguradoras Afiliadas</router-link>
          <li>Canje de puntos</li>
          <li>Pagos</li>
        </ul>
      </aside>
  
      <!-- Contenido Principal -->
      <main class="content">
        <!-- Carrusel de Ofertas -->
        <section class="carousel">
          <img :src="carouselImages[currentImage]" class="carousel-img" />
          <button @click="prevImage" class="carousel-btn left">◀</button>
          <button @click="nextImage" class="carousel-btn right">▶</button>
        </section>
  
        <!-- Sección de Redes Sociales y Ofertas -->
        <section class="offers">
          <h2 class="section-title">Redes Sociales</h2>
          <div class="offers-container">
            <div v-for="(offer, index) in offers" :key="index" class="offer-card">
              <img :src="offer.image" :alt="offer.title" class="offer-img" />
              <p class="offer-title">{{ offer.title }}</p>
            </div>
          </div>
        </section>
      </main>
    </div>
  </template>
  
  <script setup>
  import { ref } from "vue";
  // Import local images for the carousel
  import carousel1 from '@/assets/botellas.jpg';
  import carousel2 from '@/assets/farmacia.png';
  import carousel3 from '@/assets/mas.jpeg';
  
  // Import local images for offers
  import omronImage from '@/assets/muletas.jpg';
  import sukrolImage from '@/assets/pastillas.jpg';
  import isdinImage from '@/assets/botellas.jpg';
  
  // Lista de imágenes del carrusel (URLs)
  const carouselImages = ref([
    carousel1,
    carousel2,
    carousel3
  ]);
  
  // Lista de ofertas con imágenes desde URL
  const offers = ref([
    { title: "Omron - Presión Arterial", image: omronImage },
    { title: "Sukrol - Suplemento Cerebral", image: sukrolImage },
    { title: "10% Descuento en ISDIN", image: isdinImage }
  ]);
  
  // Estado del carrusel
  const currentImage = ref(0);
  
  // Funciones para cambiar imágenes del carrusel
  const prevImage = () => {
    currentImage.value = (currentImage.value - 1 + carouselImages.value.length) % carouselImages.value.length;
  };
  
  const nextImage = () => {
    currentImage.value = (currentImage.value + 1) % carouselImages.value.length;
  };
  </script>
  
  <style scoped>
  /* Layout principal */
  .dashboard {
    display: flex;
    height: 100vh;
    background-color: #f8f9fa;
  }
  
  /* Sidebar */
  .sidebar {
    width: 250px;
    background: #729ef1;
    color: white;
    padding: 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  
  .contact-icons {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
  }
  
  .icon-btn {
    background: white;
    color: #002366;
    padding: 8px;
    border-radius: 50%;
    cursor: pointer;
  }
  
  .sidebar-menu {
    list-style: none;
    padding: 0;
  }
  
  .sidebar-menu li {
    padding: 10px;
    border-bottom: 1px solid white;
    text-align: center;
  }
  
  /* Contenido Principal */
  .content {
    flex-grow: 1;
    padding: 20px;
  }
  
  /* Carrusel */
  .carousel {
    position: relative;
    width: 100%;
    max-width: 900px;
    margin: 0 auto;
  }
  
  .carousel-img {
    width: 100%;
    height: 400px; /* Fixed height */
    object-fit: cover;
    border-radius: 10px;
  }
  
  .carousel-btn {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    background: rgba(0, 0, 0, 0.5);
    color: white;
    border: none;
    padding: 10px;
    cursor: pointer;
  }
  
  .left {
    left: 10px;
  }
  .right {
    right: 10px;
  }
  
  /* Sección de Ofertas */
  .offers {
    margin-top: 20px;
  }
  
  .section-title {
    text-align: center;
    font-size: 24px;
    color: #002366;
    margin-bottom: 10px;
  }
  
  .offers-container {
    display: flex;
    justify-content: center;
    gap: 15px;
  }
  
  .offer-card {
    background: white;
    padding: 10px;
    text-align: center;
    border-radius: 5px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  }
  
  .offer-img {
    width: 100%;
    height: 200px; /* Fixed height */
    object-fit: cover;
    border-radius: 5px;
  }
  
  .offer-title {
    font-weight: bold;
    margin-top: 5px;
  }
  </style>