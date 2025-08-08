<template>
    <div class="catalog-container">
      <h2>Catálogo de Productos</h2>
  
      <!-- Grid de productos -->
      <div class="product-grid">
        <div 
          class="product-card" 
          v-for="product in productos" 
          :key="product.id"
        >
          <!-- Imagen principal del producto -->
          <img 
            :src="product.images[0]" 
            :alt="product.name" 
            class="product-image" 
          />
  
          <!-- Nombre y precio -->
          <h3>{{ product.name }}</h3>
          <p class="price">{{ product.price }}</p>
  
          <!-- Acciones: ver detalles (modal) y comprar (navega a la ruta del producto) -->
          <div class="actions">
            <button @click="openModal(product)">Ver detalles</button>
            <router-link :to="`/producto/${product.id}`" class="buy-button">
              Comprar
            </router-link>
          </div>
        </div>
      </div>
  
      <!-- Modal de detalles del producto -->
      <div v-if="showModal" class="modal-overlay">
        <div class="modal-content">
          <button class="close-button" @click="closeModal">×</button>
          
          <div v-if="selectedProduct">
            <!-- Título e imágenes del producto -->
            <h3>{{ selectedProduct.name }}</h3>
            <div class="product-images">
              <img 
                v-for="(image, index) in selectedProduct.images" 
                :key="index" 
                :src="image" 
                :alt="`Imagen ${index + 1}`" 
                class="detail-image"
              />
            </div>
  
            <!-- Detalles principales -->
            <p><strong>Descripción:</strong> {{ selectedProduct.description }}</p>
            <p><strong>Inventario:</strong> {{ selectedProduct.inventory }}</p>
            <p><strong>Ingrediente Activo:</strong> {{ selectedProduct.activeIngredient }}</p>
            <p><strong>Descripción General:</strong> {{ selectedProduct.generalDescription }}</p>
            <p><strong>Recomendación de Uso:</strong> {{ selectedProduct.usageRecommendation }}</p>
  
            <!-- Botón para ir a la página de compra/detalle completo -->
            <div class="actions">
              <router-link :to="`/producto/${selectedProduct.id}`" class="buy-button">
                Comprar
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: 'PharmacyOfertas',
    data() {
      return {
        // Aquí defines tus productos, cada uno con un arreglo de imágenes
        productos: [
          {
            id: '1',
            name: 'Botellas Médicas',
            price: '$35.00',
            // Usa require o imports si las imágenes están en /assets
            images: [
              require('@/assets/botellas.jpg'),
              require('@/assets/medicina.png')
            ],
            description: 'Detalle de Botellas Médicas',
            inventory: 100,
            activeIngredient: 'Acetaminofén',
            generalDescription: 'Producto utilizado en diversas aplicaciones médicas.',
            usageRecommendation: 'Ideal para aliviar el dolor de muela, combatir infecciones y como jarabe.'
          },
          {
            id: '2',
            name: 'Farmacia General',
            price: '$25.00',
            images: [
              require('@/assets/farmacia.png'),
              require('@/assets/mas.jpeg')
            ],
            description: 'Detalle de Farmacia General',
            inventory: 50,
            activeIngredient: 'Ibuprofeno',
            generalDescription: 'Preparado para uso general en farmacias.',
            usageRecommendation: 'Recomendado para el alivio del dolor y para tratar infecciones leves.'
          },
          {
            id: '3',
            name: 'Medicina Variada',
            price: '$50.00',
            images: [
              require('@/assets/medicina.png'),
              require('@/assets/pastillas.jpg')
            ],
            description: 'Detalle de Medicina Variada',
            inventory: 75,
            activeIngredient: 'Amoxicilina',
            generalDescription: 'Medicamento de amplio espectro.',
            usageRecommendation: 'Utilizable en casos de infecciones y para preparar jarabes.'
          }
          // ... más productos si es necesario
        ],
        showModal: false,
        selectedProduct: null
      };
    },
    methods: {
      // Abre el modal con la información de un producto
      openModal(product) {
        this.selectedProduct = product;
        this.showModal = true;
      },
      // Cierra el modal
      closeModal() {
        this.showModal = false;
        this.selectedProduct = null;
      }
    }
  };
  </script>
  
  <style scoped>
  /* Contenedor principal */
  .catalog-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
    text-align: center;
  }
  
  h2 {
    margin-bottom: 20px;
  }
  
  /* Grid de productos */
  .product-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 20px;
  }
  
  /* Tarjeta de producto */
  .product-card {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 5px rgba(0,0,0,0.1);
    padding: 15px;
    text-align: center;
    cursor: default; /* Solo se hace clic en los botones */
  }
  
  .product-image {
    width: 100%;
    height: 150px;
    object-fit: cover;
    border-radius: 8px;
    margin-bottom: 10px;
  }
  
  .price {
    color: #d9534f;
    font-weight: bold;
  }
  
  /* Acciones (botones) en la tarjeta */
  .actions {
    margin-top: 10px;
    display: flex;
    justify-content: center;
    gap: 10px;
  }
  
  button,
  .buy-button {
    padding: 8px 12px;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 600;
  }
  
  /* Botón de comprar (router-link con clase .buy-button) */
  .buy-button {
    background-color: #1e40af;
    color: #fff;
    text-decoration: none; /* Para que se vea como botón */
  }
  
  /* Modal */
  .modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0,0,0,0.5);
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  .modal-content {
    background: #fff;
    border-radius: 10px;
    padding: 20px;
    width: 90%;
    max-width: 600px;
    position: relative;
    text-align: left;
  }
  
  /* Botón de cerrar (X) */
  .close-button {
    background: none;
    border: none;
    font-size: 24px;
    color: #333;
    position: absolute;
    top: 15px;
    right: 20px;
    cursor: pointer;
  }
  
  /* Imágenes dentro del modal */
  .product-images {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
  }
  
  .detail-image {
    width: 100px;
    height: 100px;
    object-fit: cover;
    border-radius: 6px;
  }
  </style>