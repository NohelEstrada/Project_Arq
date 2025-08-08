<template>
  <Header />
  <div class="product-details-container">
    <div v-if="product">
      <h2 class="product-title">{{ product.name }}</h2>
      <!--<img :src="product.image" :alt="product.name" class="product-image" />-->
      <p class="product-description">{{ product.description }}</p>
      
      <!-- Botón para ver detalles adicionales -->
      <button @click="showModal = true" class="open-modal-btn">
        Ver detalles adicionales
      </button>
      
      <!-- Modal de información adicional -->
      <div v-if="showModal" class="modal-overlay">
        <div class="modal">
          <h3>Detalles del Producto</h3>
          <p><strong>Inventario:</strong> {{ product.stock }}</p>
          <p><strong>Principio Activo:</strong> {{ product.activeMedicament }}</p>
          <p><strong>Descripción General:</strong> {{ product.description}}</p>
          <!--<p><strong>Recomendación de Uso:</strong> {{ product.usageRecommendation }}</p>-->
          <button @click="showModal = false" class="close-modal-btn">Cerrar</button>
        </div>
      </div>

      <!-- Comments Section -->
      <section class="comments-section">
        <h3>Comentarios</h3>
        <div v-if="product.comments.length">
          <div v-for="comment in product.comments" :key="comment.id" class="comment">
            <p>{{ comment.text }}</p>
          </div>
        </div>
        <div v-else>
          <p>No hay comentarios aún.</p>
        </div>
        <form @submit.prevent="submitComment" class="comment-form">
          <textarea v-model="newComment" placeholder="Escribe tu comentario..." class="comment-input"></textarea>
          <button type="submit" class="comment-submit">Enviar</button>
        </form>
      </section>
    </div>
    <div v-else>
      <p>Producto no encontrado.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRoute } from 'vue-router';
import Header from '@/components/Header.vue';
import axios from 'axios';
import ApiService from '../services/ApiService';
const route = useRoute();
const productId = route.params.id; // Se espera que el router pase el id como string
const ip = process.env.VUE_APP_IP;

const medicines = ref([]);

const fetchMedicines = async () => {
  try {
    const response = await axios.get(ApiService.getPharmacyApiUrl("/medicines"));
    medicines.value = response.data;
    console.log("Medicinas recibidas:", medicines.value);
  } catch (error) {
    console.error('Error fetching medicines:', error);
  }
};
fetchMedicines();

// Se busca el producto cuyo idMedicine coincida con productId
const product = computed(() => {
  return medicines.value.find(p => String(p.idMedicine) === productId) || {};
});

const newComment = ref('');
const showModal = ref(false);

const submitComment = () => {
  if (newComment.value.trim() && product.value) {
    if (!product.value.comments) {
      product.value.comments = [];
    }
    product.value.comments.push({ id: Date.now(), text: newComment.value });
    newComment.value = '';
  }
};
</script>

<style scoped>
.product-details-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
  background: #f8f9fa;
  text-align: center;
  border-radius: 10px;
}

.product-title {
  font-size: 28px;
  color: #1e40af;
  margin-bottom: 20px;
}

.product-image {
  width: 100%;
  max-height: 400px;
  object-fit: cover;
  border-radius: 10px;
  margin-bottom: 20px;
}

.product-description {
  font-size: 18px;
  color: #333;
  margin-bottom: 30px;
}

.open-modal-btn {
  background: #1e40af;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 30px;
  transition: background 0.3s;
}

.open-modal-btn:hover {
  background: #1e3a8a;
}

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

.modal {
  background: #fff;
  padding: 20px 30px;
  border-radius: 10px;
  width: 90%;
  max-width: 500px;
  text-align: left;
}

.close-modal-btn {
  background: #e3342f;
  color: #fff;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  margin-top: 20px;
  transition: background 0.3s;
}

.close-modal-btn:hover {
  background: #cc1f1a;
}

.comments-section {
  text-align: left;
  margin-top: 30px;
}

.comments-section h3 {
  font-size: 22px;
  color: #1e40af;
  margin-bottom: 10px;
}

.comment {
  background: #fff;
  padding: 10px;
  border-radius: 8px;
  margin-bottom: 10px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.comment-form {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
}

.comment-input {
  resize: vertical;
  min-height: 80px;
  padding: 10px;
  font-size: 16px;
  border: 1px solid #ccc;
  border-radius: 8px;
  margin-bottom: 10px;
}

.comment-submit {
  align-self: flex-end;
  background: #1e40af;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.3s;
}

.comment-submit:hover {
  background: #1e3a8a;
}
</style>