<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="cart-container">
    <h1>Carrito de Compras</h1>

    <!-- Si no existe una orden en progreso -->
    <div v-if="!currentOrder">
      <p>No tienes una orden en progreso.</p>
    </div>

    <!-- Si existe una orden en progreso, se muestran los ítems -->
    <div v-else>
      <table>
        <thead>
        <tr>
          <th>Medicamento</th>
          <th>Cantidad</th>
          <th>Costo Unitario</th>
          <th>Total</th>
          <th>Eliminar</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="item in cartItems" :key="item.medicine.idMedicine">
          <td>{{ item.medicine.name }}</td>
          <td>{{ item.quantity }}</td>
          <td>{{ item.cost }}</td>
          <td>{{ item.total }}</td>
          <td>
            <button @click="removeItem(item)">X</button>
          </td>
        </tr>
        </tbody>
      </table>
      <button @click="openModal">Completar compra</button>

      <div v-if="isModalOpen" class="modal">
        <div class="modal-content">
          <h2>Completar compra</h2>
          <input v-model="cardNumber" placeholder="Número de tarjeta" />
          <input v-model="cardExpiry" @input="handleExpiryInput" placeholder="MM/YY" />
          <input v-model="cardCvv" placeholder="CVV" />
          <button @click="completePurchase">Confirmar Compra</button>
          <button @click="closeModal">Cancelar</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="CartView">
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { useUserStore } from '@/stores/userStore'
import ApiService from '../services/ApiService';  
const userStore = useUserStore()

const currentOrder = ref(null)
const cartItems = ref([])

// Buscar la orden en progreso para el usuario actual
const fetchCurrentOrder = async () => {
  try {
    const ordersResponse = await axios.get(ApiService.getPharmacyApiUrl("/orders"))
    const orders = ordersResponse.data
    const userId = userStore.getUser().idUser
    const inProgressOrder = orders.find(o => o.user.idUser === userId && o.status === 'En progreso')
    currentOrder.value = inProgressOrder || null
  } catch (error) {
    console.error('Error fetching current order:', error)
  }
}

// Obtener los ítems (OrderMedicine) de la orden en progreso
const fetchCartItems = async (orderId) => {
  try {
    const response = await axios.get(ApiService.getPharmacyApiUrl("/order_medicines"))
    const allItems = response.data
    // Filtrar solo los ítems que corresponden a la orden en progreso
    cartItems.value = allItems.filter(item => item.orders.idOrder === orderId)
  } catch (error) {
    console.error('Error fetching cart items:', error)
  }
}

// Eliminar un ítem del carrito (se elimina directamente de la DB)
const removeItem = async (item) => {
  try {
    const orderId = item.orders.idOrder
    const medicineId = item.medicine.idMedicine
    await axios.delete(ApiService.getPharmacyApiUrl(`/order_medicines?id=${orderId},${medicineId}`))
    // Actualizar la lista local eliminando el ítem borrado
    cartItems.value = cartItems.value.filter(i => i.medicine.idMedicine !== medicineId)
  } catch (error) {
    console.error('Error removing item from cart:', error)
  }
}

// Referencias y funciones para completar la compra
const isModalOpen = ref(false);
const cardNumber = ref('');
const cardExpiry = ref('');
const cardCvv = ref('');

const handleExpiryInput = () => {
  let val = cardExpiry.value.replace(/[^0-9]/g, '');
  if (val.length > 2) {
    cardExpiry.value = val.slice(0, 2) + '/' + val.slice(2, 4);
  } else {
    cardExpiry.value = val;
  }
};

const isCardValid = computed(() => {
  return cardNumber.value && cardExpiry.value && cardCvv.value;
});

const openModal = () => {
  isModalOpen.value = true;
};

const closeModal = () => {
  isModalOpen.value = false;
};

const completePurchase = async () => {
  if (!isCardValid.value) return;
  try {
    // Se asume que currentOrder es la orden en progreso
    const order = currentOrder.value;
    if (!order) return;

    // Actualizar la orden existente con estado 'Completado' enviando el objeto completo
    await axios.put(ApiService.getPharmacyApiUrl("/orders"), {
      idOrder: order.idOrder,
      status: 'Completado',
      user: order.user
    });

    closeModal();
    alert('Gracias por tu compra');
    window.location.href = '/';
  } catch (error) {
    console.error('Error completing purchase:', error);
    alert('Error al completar la compra. Intente de nuevo.');
  }
}

onMounted(async () => {
  await fetchCurrentOrder()
  if (currentOrder.value) {
    await fetchCartItems(currentOrder.value.idOrder)
  }
})
</script>

<style scoped>
.cart-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
}

th, td {
  padding: 8px;
  border: 1px solid #ccc;
  text-align: left;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}
.modal-content {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  width: 300px;
}
</style>