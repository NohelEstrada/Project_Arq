<!-- eslint-disable vue/multi-word-component-names -->
<!-- Catalog.vue (o como se llame tu componente) -->
<template>
  <div class="catalog-container">
    <!-- Banner y título -->
    <div class="catalog-header">
      <h2 class="title">Catálogo de Medicamentos</h2>
      <p class="subtitle">Encuentra los medicamentos que necesitas</p>
    </div>

    <!-- Filtros con diseño moderno -->
    <div class="filters-container">
      <div class="search-box">
        <i class="search-icon">🔍</i>
        <input
          type="text"
          v-model="searchQuery"
          placeholder="Buscar medicamento..."
          class="search-input"
        />
      </div>
      
      <div class="filter-group">
        <input
          type="text"
          v-model="activeIngredientFilter"
          placeholder="Principio activo"
          class="filter-input"
        />
        <input
          type="text"
          v-model="brandFilter"
          placeholder="Marca"
          class="filter-input"
        />
        <div class="price-filters">
          <input
            type="number"
            v-model.number="minPrice"
            placeholder="Precio mín"
            class="filter-input price-input"
          />
          <span class="price-separator">-</span>
          <input
            type="number"
            v-model.number="maxPrice"
            placeholder="Precio máx"
            class="filter-input price-input"
          />
        </div>
      </div>
    </div>
  
    <!-- Grid de productos con nuevo diseño -->
    <div class="product-grid">
      <div v-for="product in filteredProducts" :key="product.idMedicine" class="product-card">
        <div class="product-content">
          <div class="product-badge" v-if="product.prescription">Requiere receta</div>
          <h3 class="product-name">{{ product.name }}</h3>
          <p class="product-active">{{ product.activeMedicament }}</p>
          <p class="product-price">Q{{ product.price.toFixed(2) }}</p>
          
          <div class="product-actions">
            <button class="details-button" @click="openModal(product)">
              <span class="icon">ⓘ</span> Detalles
            </button>
            <router-link :to="`/producto/${product.idMedicine}`" class="buy-button">
              <span class="icon">🛒</span> Comprar
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- Indicador de carga -->
    <div v-if="products.length === 0" class="loading-container">
      <div class="loading-spinner"></div>
      <p>Cargando medicamentos...</p>
    </div>
  </div>

  <!-- Modal de detalles del producto con diseño moderno -->
  <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">
      <button class="close-modal" @click="closeModal">✖</button>
      
      <div class="modal-body">
        <div class="modal-header">
          <h2 class="modal-title">{{ selectedProduct.name }}</h2>
          <div class="modal-price">Q{{ selectedProduct.price ? selectedProduct.price.toFixed(2) : '0.00' }}</div>
          <div class="product-badge large" v-if="selectedProduct.prescription">Requiere receta médica</div>
        </div>
        
        <div class="info-grid">
          <div class="info-item">
            <h4>Principio Activo</h4>
            <p>{{ selectedProduct.activeMedicament || 'No disponible' }}</p>
          </div>
          
          <div class="info-item">
            <h4>Marca</h4>
            <p>{{ selectedProduct.brand || 'No disponible' }}</p>
          </div>
          
          <div class="info-item">
            <h4>Concentración</h4>
            <p>{{ selectedProduct.concentration || 'No disponible' }}</p>
          </div>
          
          <div class="info-item">
            <h4>Presentación</h4>
            <p>{{ selectedProduct.presentacion || 'No disponible' }}</p>
          </div>
          
          <div class="info-item">
            <h4>Stock Disponible</h4>
            <p class="stock-indicator" :class="{'low-stock': selectedProduct.stock < 10}">
              {{ selectedProduct.stock }} unidades
            </p>
          </div>
          
          <div class="info-item full-width">
            <h4>Descripción</h4>
            <p class="description-text">{{ selectedProduct.description || 'No hay descripción disponible para este producto.' }}</p>
          </div>
        </div>
        
        <div class="modal-actions">
          <router-link :to="`/producto/${selectedProduct.idMedicine}`" class="modal-buy-button">
            Comprar Ahora
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import axios from "axios";



const products = ref([]);
import ApiService from '../services/ApiService';
const fetchProduct = async () => {
  try {
    const response = await axios.get(ApiService.getPharmacyApiUrl("/medicines"));
    products.value = response.data;
    console.log(products.value);
  } catch (error) {
    console.error('Error fetching medicines:', error);
  }
};
fetchProduct();

// Manejo de modal
const showModal = ref(false);
const selectedProduct = ref({});

const openModal = (product) => {
  selectedProduct.value = product;
  showModal.value = true;
  // Prevenir el scroll en el fondo cuando el modal está abierto
  document.body.style.overflow = 'hidden';
};

const closeModal = () => {
  showModal.value = false;
  // Restaurar el scroll cuando se cierra el modal
  document.body.style.overflow = 'auto';
};

// Filtros
const searchQuery = ref('');
const activeIngredientFilter = ref('');
const brandFilter = ref('');
const minPrice = ref(null);
const maxPrice = ref(null);

const filteredProducts = computed(() => {
  return products.value.filter((product) => {
    const matchesName = !searchQuery.value || product.name.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchesIngredient = !activeIngredientFilter.value || product.activeMedicament.toLowerCase().includes(activeIngredientFilter.value.toLowerCase());
    const matchesBrand = !brandFilter.value || product.brand.toLowerCase().includes(brandFilter.value.toLowerCase());
    const matchesMinPrice = !minPrice.value || product.price >= minPrice.value;
    const matchesMaxPrice = !maxPrice.value || product.price <= maxPrice.value;

    return (
      matchesName &&
      matchesIngredient &&
      matchesBrand &&
      matchesMinPrice &&
      matchesMaxPrice
    );
  });
});
</script>

<style scoped>
/* Estilos generales */
.catalog-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem;
  background-color: #f8fafc;
  min-height: 100vh;
}

/* Encabezado del catálogo */
.catalog-header {
  text-align: center;
  margin-bottom: 2.5rem;
}

.title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #1e40af;
  margin-bottom: 0.5rem;
}

.subtitle {
  font-size: 1.2rem;
  color: #64748b;
}

/* Contenedor de filtros */
.filters-container {
  background-color: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  margin-bottom: 2rem;
}

.search-box {
  position: relative;
  margin-bottom: 1rem;
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
}

.search-input {
  width: 100%;
  padding: 1rem 1rem 1rem 3rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
}

.filter-group {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.filter-input {
  flex: 1;
  min-width: 180px;
  padding: 0.8rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.9rem;
}

.price-filters {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.price-input {
  width: 100px;
  min-width: auto;
}

.price-separator {
  color: #64748b;
}

/* Grid de productos */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}

.product-card {
  background-color: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  padding: 1.5rem;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.product-content {
  padding: 1.2rem;
}

.product-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 0.5rem;
  height: 2.6rem;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-active {
  font-size: 0.9rem;
  color: #64748b;
  margin-bottom: 0.8rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  font-size: 1.3rem;
  font-weight: 700;
  color: #16a34a;
  margin-bottom: 1rem;
}

.product-actions {
  display: flex;
  gap: 0.5rem;
}

.details-button, .buy-button {
  padding: 0.7rem 1rem;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.3rem;
  font-size: 0.9rem;
  text-decoration: none;
}

.details-button {
  background-color: #f1f5f9;
  color: #334155;
  flex: 1;
}

.details-button:hover {
  background-color: #e2e8f0;
}

.buy-button {
  background-color: #1e40af;
  color: white;
  flex: 1;
}

.buy-button:hover {
  background-color: #1e3a8a;
}

.icon {
  font-size: 1rem;
}

/* Indicador de carga */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #64748b;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e2e8f0;
  border-top: 4px solid #1e40af;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-content {
  background-color: white;
  border-radius: 12px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  padding: 2rem;
}

.close-modal {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: rgba(0, 0, 0, 0.1);
  color: #334155;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
  z-index: 10;
}

.close-modal:hover {
  background-color: rgba(0, 0, 0, 0.2);
}

.modal-body {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.modal-header {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.modal-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 0.5rem;
}

.modal-price {
  font-size: 1.8rem;
  font-weight: 700;
  color: #16a34a;
  margin-bottom: 2rem;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-item h4 {
  font-size: 0.9rem;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 0.5rem;
  text-transform: uppercase;
}

.info-item p {
  font-size: 1.1rem;
  color: #1e293b;
}

.full-width {
  grid-column: 1 / -1;
}

.description-text {
  line-height: 1.6;
}

.stock-indicator {
  font-weight: 600;
}

.low-stock {
  color: #ef4444;
}

.modal-actions {
  display: flex;
  gap: 1rem;
}

.modal-buy-button {
  background-color: #1e40af;
  color: white;
  border: none;
  padding: 1rem 1.5rem;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s ease;
  text-decoration: none;
  display: inline-block;
}

.modal-buy-button:hover {
  background-color: #1e3a8a;
}

.product-badge {
  display: inline-block;
  background-color: rgba(239, 68, 68, 0.9);
  color: white;
  padding: 0.3rem 0.6rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.product-badge.large {
  font-size: 0.85rem;
  padding: 0.5rem 0.8rem;
  margin-top: 0.5rem;
}

/* Responsive */
@media (max-width: 768px) {
  .modal-body {
    padding: 1.5rem;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>