<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="producto-detalle">
    <div v-if="product" class="producto-container">
      <div class="producto-header">
        <h1>{{ product.name }}</h1>
        <span class="badge" v-if="product.stock > 0">Disponible</span>
        <span class="badge out-of-stock" v-else>Agotado</span>
      </div>

      <div class="producto-content">
        <div class="producto-imagen">
          <img
            v-if="product.images && product.images.length"
            :src="product.images[0]"
            alt="Imagen principal del producto"
            class="main-image"
          />
          <div v-else class="placeholder-image">
            <i class="fas fa-prescription-bottle-alt"></i>
          </div>
        </div>

        <div class="producto-info">
          <div class="precio-box">
            <h2 class="precio">Q{{ product.price }}</h2>
            <div class="inventario" :class="{ 'low-stock': product.stock < 10 }">
              {{ product.stock }} unidades disponibles
            </div>
          </div>

          <div class="detalles-box">
            <h3>Detalles del producto</h3>
            <div class="detalle-item">
              <span class="label">Ingrediente Activo:</span>
              <span class="value">{{ product.activeMedicament }}</span>
            </div>
            <div class="detalle-item">
              <span class="label">Concentración:</span>
              <span class="value">{{ product.concentration }}</span>
            </div>
            <div class="detalle-item">
              <span class="label">Presentación:</span>
              <span class="value">{{ product.presentacion }}</span>
            </div>
            <div class="detalle-item">
              <span class="label">Marca:</span>
              <span class="value">{{ product.brand }}</span>
            </div>
          </div>

          <div class="descripcion-box">
            <h3>Descripción</h3>
            <p>{{ product.description }}</p>
          </div>

          <div class="compra-box">
            <div class="cantidad-selector">
              <button @click="decrementQuantity" :disabled="quantity <= 1" class="qty-btn">-</button>
              <input
                type="number"
                v-model.number="quantity"
                :min="1"
                :max="product.stock"
                class="qty-input"
              />
              <button @click="incrementQuantity" :disabled="quantity >= product.stock" class="qty-btn">+</button>
            </div>
            <button
              @click="redirectToCheckout"
              :disabled="!quantity || quantity < 1 || quantity > product.stock || product.stock === 0"
              class="comprar-btn"
            >
              Comprar ahora
            </button>
          </div>
        </div>
      </div>

      <div class="comentarios-section">
        <h3>Comentarios</h3>
        <Comentarios :initialComments="productComments" />
      </div>
    </div>

    <div v-else class="loading-container">
      <div class="loading-spinner"></div>
      <p>Cargando producto...</p>
    </div>
  </div>
</template>

<script>
import Comentarios from '@/components/Comentarios.vue';
import axios from "axios";
import { useUserStore } from '@/stores/userStore';
import ApiService from '../services/ApiService';

export default {
  name: "ProductoDetalle",
  components: {
    Comentarios
  },
  props: ["id"],
  data() {
    return {
      product: null,
      quantity: 1,
      productComments: [
        { idComments: 1, user: { name: 'Juan' }, commentText: '¡Muy buen producto!' },
        { idComments: 2, user: { name: 'Maria' }, commentText: 'Me ayudó mucho, gracias!' }
      ]
    };
  },
  mounted() {
    this.fetchProductDetails();
  },
  methods: {
    fetchProductDetails() {
      const routeId = this.$route.params.id;
      axios.get(ApiService.getPharmacyApiUrl("/medicines"))
        .then(response => {
          const products = response.data;
          console.log(this.$route.params.id);
          this.product = products.find(prod => prod.idMedicine === Number(routeId));
        })
        .catch(error => {
          console.error('Error fetching medicines:', error);
        });
    },
    incrementQuantity() {
      if (this.quantity < this.product.stock) {
        this.quantity++;
      }
    },
    decrementQuantity() {
      if (this.quantity > 1) {
        this.quantity--;
      }
    },
    redirectToCheckout() {
      // Verificar si el usuario está logueado
      const userStore = useUserStore();
      if (!userStore.user) {
        // Si no está logueado, redirigir a login
        this.$router.push('/login');
        return;
      }
      
      // Redirigir a la página de verificación de compra
      this.$router.push({
        name: 'VerificarCompra',
        params: { id: this.product.idMedicine },
        query: { quantity: this.quantity }
      });
    },
    
    purchaseProduct() {
      const userStore = useUserStore();
      const userId = userStore.user.idUser;

      // Obtener todas las órdenes para el usuario
      axios.get(ApiService.getPharmacyApiUrl(`/orders?userId=${userId}`))
        .then(response => {
          console.log('Response from orders GET:', response.data);
          const orders = response.data;
          const orderInProgress = orders.find(order => order.status === 'En progreso');
          if (orderInProgress) {
            console.log('Found order in progress:', orderInProgress);
            return orderInProgress;
          } else {
            return axios.post(ApiService.getPharmacyApiUrl("/orders"), {
              user: { idUser: userId },
              status: 'En progreso'
            }).then(response => {
              console.log('Response from orders POST:', response.data);
              return response.data;
            });
          }
        })
        .then(order => {
          console.log('ORDEN:', order);
          return axios.get(ApiService.getPharmacyApiUrl(`/order_medicines?id=${order.idOrder}%2C${this.product.idMedicine}`))
              .then(response => {
                let items = response.data;
                if (!Array.isArray(items)) items = items ? [items] : [];
                return items;
              })
              .catch(error => {
                if (error.response?.status === 404) return [];    // no existe → crear
                throw error;
              })
              .then(orderMedicines => {
                const existing = orderMedicines.find(om => om.medicine.idMedicine === this.product.idMedicine);
                const payload = {
                  orders: order,
                  medicine: { idMedicine: this.product.idMedicine },
                  quantity: this.quantity,
                  cost: this.product.price,
                  total: this.product.price * this.quantity
                };
                return existing
                    ? axios.put(ApiService.getPharmacyApiUrl("/order_medicines"), payload)
                    : axios.post(ApiService.getPharmacyApiUrl("/order_medicines"), payload);
              });
        })
        .then(response => {
          console.log('Orden y medicamento añadidos o actualizados:', response.data);
        })
        .catch(error => {
          console.error('Error en el proceso de la orden:', error);
        });
    }
  }
};
</script>

<style scoped>
.producto-detalle {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.producto-header {
  display: flex;
  align-items: center;
  margin-bottom: 2rem;
}

.producto-header h1 {
  font-size: 2rem;
  color: #2c3e50;
  margin: 0;
  flex-grow: 1;
}

.badge {
  background-color: #4caf50;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
}

.badge.out-of-stock {
  background-color: #f44336;
}

.producto-content {
  display: flex;
  gap: 2rem;
  margin-bottom: 2rem;
}

.producto-imagen {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f9f9f9;
  border-radius: 12px;
  overflow: hidden;
  min-height: 300px;
}

.main-image {
  max-width: 100%;
  max-height: 400px;
  object-fit: contain;
}

.placeholder-image {
  font-size: 5rem;
  color: #ddd;
}

.producto-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.precio-box {
  background-color: #f8f9fa;
  padding: 1rem;
  border-radius: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.precio {
  font-size: 1.8rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.inventario {
  font-size: 0.9rem;
  color: #6c757d;
}

.inventario.low-stock {
  color: #f44336;
}

.detalles-box, .descripcion-box {
  background-color: #f8f9fa;
  padding: 1.5rem;
  border-radius: 10px;
}

.detalles-box h3, .descripcion-box h3 {
  color: #2c3e50;
  font-size: 1.2rem;
  margin-top: 0;
  margin-bottom: 1rem;
  border-bottom: 1px solid #e1e4e8;
  padding-bottom: 0.5rem;
}

.detalle-item {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 0;
  border-bottom: 1px solid #f1f1f1;
}

.detalle-item .label {
  font-weight: 600;
  color: #555;
}

.descripcion-box p {
  color: #555;
  line-height: 1.6;
}

.compra-box {
  margin-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.cantidad-selector {
  display: flex;
  align-items: center;
  max-width: 150px;
}

.qty-btn {
  width: 40px;
  height: 40px;
  background-color: #f1f1f1;
  border: none;
  border-radius: 50%;
  font-size: 1.2rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.qty-btn:hover:not(:disabled) {
  background-color: #e1e1e1;
}

.qty-btn:disabled {
  color: #aaa;
  cursor: not-allowed;
}

.qty-input {
  width: 50px;
  text-align: center;
  font-size: 1rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 0.5rem;
  margin: 0 0.5rem;
}

.comprar-btn {
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 1rem 1.5rem;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
  width: 100%;
  max-width: 400px;
  align-self: center;
}

.comprar-btn:hover:not(:disabled) {
  background-color: #2980b9;
}

.comprar-btn:disabled {
  background-color: #b3b3b3;
  cursor: not-allowed;
}

.comentarios-section {
  margin-top: 2rem;
  padding: 1.5rem;
  background-color: #f8f9fa;
  border-radius: 10px;
}

.comentarios-section h3 {
  color: #2c3e50;
  font-size: 1.2rem;
  margin-top: 0;
  margin-bottom: 1rem;
  border-bottom: 1px solid #e1e4e8;
  padding-bottom: 0.5rem;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.loading-spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-left-color: #3498db;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Media queries for responsiveness */
@media (max-width: 768px) {
  .producto-content {
    flex-direction: column;
  }

  .producto-imagen {
    min-height: 250px;
  }

  .main-image {
    max-height: 300px;
  }
}
</style>