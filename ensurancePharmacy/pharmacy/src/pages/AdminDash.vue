<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/userStore";
import { Bar, Pie, Doughnut } from "vue-chartjs";
import {
  Chart,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  ArcElement,
  RadialLinearScale,
} from "chart.js";
import ApiService from '../services/ApiService';
import axios from "axios";

// Registrar componentes de Chart.js
Chart.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  ArcElement,
  RadialLinearScale
);

const router = useRouter();
const userStore = useUserStore();

// Obtener IP del servidor

// Estado para almacenar los datos de las gráficas
const topCategoriesData = ref({
  labels: [],
  datasets: [
    {
      backgroundColor: [
        "#FF6384",
        "#36A2EB",
        "#FFCE56",
        "#4BC0C0",
        "#9966FF",
        "#FF9F40",
        "#C9CBCF",
        "#7FB800",
        "#00A6ED",
        "#F6511D",
      ],
      data: [],
    },
  ],
});

const monthlySalesData = ref({
  labels: [
    "Enero",
    "Febrero",
    "Marzo",
    "Abril",
    "Mayo",
    "Junio",
    "Julio",
    "Agosto",
    "Septiembre",
    "Octubre",
    "Noviembre",
    "Diciembre",
  ],
  datasets: [
    {
      label: "Ventas Mensuales",
      backgroundColor: "#4BC0C0",
      data: [],
    },
  ],
});

const inventoryStatusData = ref({
  labels: ["En exceso", "Óptimo", "Bajo", "Crítico"],
  datasets: [
    {
      backgroundColor: ["#48bb78", "#4299e1", "#ecc94b", "#f56565"],
      data: [],
    },
  ],
});

// Estadísticas de resumen
const totalCategories = ref(0);
const totalSales = ref(0);
const totalInventory = ref(0);

// Opciones de gráficas
const categoryOptions = ref({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    title: {
      display: true,
      text: "Top 10 Categorías de Medicamentos",
    },
    legend: {
      position: "right",
    },
    tooltip: {
      callbacks: {
        label: function (context) {
          return `${context.label}: ${context.raw}%`;
        },
      },
    },
  },
});

const salesOptions = ref({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    title: {
      display: true,
      text: "Ventas Mensuales",
    },
    legend: {
      display: false,
    },
    tooltip: {
      callbacks: {
        label: function (context) {
          return `Q${context.raw.toLocaleString()}`;
        },
      },
    },
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        callback: function (value) {
          return "Q" + value.toLocaleString();
        },
      },
    },
  },
});

const inventoryOptions = ref({
  responsive: true,
  maintainAspectRatio: false,
  cutout: "70%",
  plugins: {
    title: {
      display: true,
      text: "Estado del Inventario",
    },
    legend: {
      position: "bottom",
    },
  },
});

// Variables para estado de carga
const isLoading = ref(true);
const error = ref(null);

// Variables para estado de la exportación XML
const isExportingXML = ref(false);
const exportSuccess = ref(false);
const exportError = ref(false);

// Comprobar si el usuario es administrador
const checkAdminStatus = () => {
  console.log("Verificando si el usuario es administrador");

  // Método 1: Verificar en el store
  if (userStore.isAdmin()) {
    console.log("El usuario es admin según el store");
    return true;
  }

  // Método 2: Verificar directamente en localStorage
  try {
    // Verificar en 'role'
    const storedRole = localStorage.getItem("role");
    if (storedRole === "admin") {
      console.log("Usuario es admin según localStorage.role");
      return true;
    }

    // Verificar en 'user'
    const storedUser = localStorage.getItem("user");
    if (storedUser) {
      const parsedUser = JSON.parse(storedUser);
      if (parsedUser.role === "admin") {
        console.log("Usuario es admin según localStorage.user");
        // Actualizar el store por si acaso
        userStore.setUser(parsedUser);
        return true;
      }
    }

    // Verificar en 'session'
    const storedSession = localStorage.getItem("session");
    if (storedSession) {
      const parsedSession = JSON.parse(storedSession);
      if (parsedSession.role === "admin") {
        console.log("Usuario es admin según localStorage.session");
        // Actualizar el store por si acaso
        userStore.setUser(parsedSession);
        return true;
      }
    }
  } catch (e) {
    console.error("Error al verificar admin en localStorage:", e);
  }

  // Si llegamos aquí, no es admin o no hay datos
  console.log("No se encontró rol admin. Redirigiendo...");
  router.push("/login");
  setTimeout(() => {
    alert("Acceso denegado: Solo administradores pueden acceder a este panel");
  }, 500);
  return false;
};

// Obtener datos de medicamentos y procesarlos para las gráficas
const fetchMedicinesData = async () => {
  try {
    console.log("Obteniendo datos de medicamentos");
    const response = await axios.get(ApiService.getPharmacyApiUrl("/medicines"));
    const medicines = response.data;
    console.log("Datos de medicamentos obtenidos:", medicines);

    // Verificar si hay datos
    if (!medicines || medicines.length === 0) {
      console.log("No hay datos de medicamentos, usando datos de ejemplo");

      // Datos de ejemplo
      const exampleData = [
        { name: "Analgésicos", value: 320, percentage: 32 },
        { name: "Antibióticos", value: 245, percentage: 24 },
        { name: "Antiinflamatorios", value: 180, percentage: 18 },
        { name: "Antihistamínicos", value: 95, percentage: 9 },
        { name: "Antidepresivos", value: 70, percentage: 7 },
        { name: "Antihipertensivos", value: 50, percentage: 5 },
        { name: "Antigripales", value: 30, percentage: 3 },
        { name: "Vitaminas", value: 10, percentage: 1 },
        { name: "Suplementos", value: 5, percentage: 0.5 },
        { name: "Otros", value: 5, percentage: 0.5 },
      ];

      // Actualizar datos de categoría
      topCategoriesData.value.labels = exampleData.map((item) => item.name);
      topCategoriesData.value.datasets[0].data = exampleData.map(
        (item) => item.percentage
      );

      // Datos de ejemplo para inventario
      inventoryStatusData.value.datasets[0].data = [15, 45, 20, 10];

      // Actualizar estadísticas
      totalCategories.value = exampleData.length;
      totalInventory.value = 90;

      return [];
    }

    // Procesar datos para categorías (usando activeMedicament como categoría)
    const categories = {};
    let totalInventoryCount = 0;

    medicines.forEach((medicine) => {
      const category = medicine.activeMedicament || "Sin categoría";
      if (!categories[category]) {
        categories[category] = 0;
      }
      // Sumar unidades vendidas (o unidades en stock si no hay vendidas)
      categories[category] += medicine.soldUnits || 1;

      // Contar inventario total
      totalInventoryCount += medicine.stock || 0;
    });

    // Convertir a array y ordenar por ventas
    let sortedCategories = Object.entries(categories)
      .map(([name, value]) => ({ name, value }))
      .sort((a, b) => b.value - a.value);

    // Almacenar total de categorías
    totalCategories.value = sortedCategories.length;

    // Tomar solo las 10 primeras
    sortedCategories = sortedCategories.slice(0, 10);

    // Calcular porcentajes
    const totalCategorySales = sortedCategories.reduce(
      (sum, cat) => sum + cat.value,
      0
    );
    sortedCategories.forEach((cat) => {
      cat.percentage = Math.round((cat.value / totalCategorySales) * 100);
    });

    // Actualizar datos de gráfica de categorías
    topCategoriesData.value.labels = sortedCategories.map((cat) => cat.name);
    topCategoriesData.value.datasets[0].data = sortedCategories.map(
      (cat) => cat.percentage
    );

    // Procesar datos para estado de inventario
    const inventoryStatus = {
      "En exceso": 0,
      Óptimo: 0,
      Bajo: 0,
      Crítico: 0,
    };

    medicines.forEach((medicine) => {
      const stock = medicine.stock || 0;
      if (stock > 50) {
        inventoryStatus["En exceso"]++;
      } else if (stock > 20) {
        inventoryStatus["Óptimo"]++;
      } else if (stock > 5) {
        inventoryStatus["Bajo"]++;
      } else {
        inventoryStatus["Crítico"]++;
      }
    });

    // Actualizar datos de gráfica de inventario
    inventoryStatusData.value.datasets[0].data = [
      inventoryStatus["En exceso"],
      inventoryStatus["Óptimo"],
      inventoryStatus["Bajo"],
      inventoryStatus["Crítico"],
    ];

    // Actualizar conteo total de inventario
    totalInventory.value = totalInventoryCount;

    return medicines;
  } catch (error) {
    console.error("Error al obtener datos de medicamentos:", error);

    // Datos de ejemplo en caso de error
    const exampleData = [
      { name: "Analgésicos", value: 320, percentage: 32 },
      { name: "Antibióticos", value: 245, percentage: 24 },
      { name: "Antiinflamatorios", value: 180, percentage: 18 },
      { name: "Antihistamínicos", value: 95, percentage: 9 },
      { name: "Antidepresivos", value: 70, percentage: 7 },
      { name: "Antihipertensivos", value: 50, percentage: 5 },
      { name: "Antigripales", value: 30, percentage: 3 },
      { name: "Vitaminas", value: 10, percentage: 1 },
      { name: "Suplementos", value: 5, percentage: 0.5 },
      { name: "Otros", value: 5, percentage: 0.5 },
    ];

    // Actualizar datos
    topCategoriesData.value.labels = exampleData.map((item) => item.name);
    topCategoriesData.value.datasets[0].data = exampleData.map(
      (item) => item.percentage
    );
    inventoryStatusData.value.datasets[0].data = [15, 45, 20, 10];
    totalCategories.value = exampleData.length;
    totalInventory.value = 90;

    throw error;
  }
};

// Obtener datos de transacciones/ventas
const fetchTransactionsData = async () => {
  try {
    console.log("Obteniendo datos de transacciones/facturas");
    // Primero intentamos con la API de transacciones
    let transactions = [];
    try {
      const response = await axios.get(ApiService.getPharmacyApiUrl("/transactions"));
      transactions = response.data;
      console.log("Datos de transacciones obtenidos:", transactions);
    } catch (e) {
      console.log("Error al obtener transacciones, intentando con bills:", e);
      // Si falla, intentamos con la API de facturas
      const response = await axios.get(ApiService.getPharmacyApiUrl("/bills"));
      transactions = response.data;
      console.log("Datos de facturas obtenidos:", transactions);
    }

    // Inicializar datos de ventas mensuales
    const monthlySales = Array(12).fill(0);

    // Procesar transacciones por mes
    transactions.forEach((transaction) => {
      // Verificar si el campo fecha existe
      const dateField = transaction.transDate || transaction.billDate || null;
      if (dateField) {
        const date = new Date(dateField);
        const month = date.getMonth();
        // Sumar el total de la transacción
        monthlySales[month] += transaction.total || 0;
      }
    });

    // Si no hay datos, usar datos de ejemplo
    if (monthlySales.every((value) => value === 0)) {
      console.log("No hay datos reales de ventas, usando datos de ejemplo");
      for (let i = 0; i < 12; i++) {
        monthlySales[i] = Math.floor(Math.random() * 10000) + 5000;
      }
    }

    // Actualizar datos de gráfica de ventas
    monthlySalesData.value.datasets[0].data = monthlySales;

    // Calcular total de ventas
    totalSales.value = monthlySales.reduce((sum, value) => sum + value, 0);

    return transactions;
  } catch (error) {
    console.error("Error al obtener datos de transacciones:", error);
    // Si no hay datos, generar datos de ejemplo
    const monthlySales = [];
    for (let i = 0; i < 12; i++) {
      monthlySales.push(Math.floor(Math.random() * 10000) + 5000);
    }
    monthlySalesData.value.datasets[0].data = monthlySales;
    totalSales.value = monthlySales.reduce((sum, value) => sum + value, 0);

    throw error;
  }
};

// Nuevo método para exportar medicamentos en XML
const exportMedicinesXML = async () => {
  try {
    // Mostrar indicador de carga
    isExportingXML.value = true;

    // Realizar solicitud al endpoint de XML
    const response = await axios.get(ApiService.getPharmacyApiUrl("/medicines-xml"), {
      responseType: "blob",
    });

    // Crear URL para el blob
    const blob = new Blob([response.data], { type: "application/xml" });
    const url = window.URL.createObjectURL(blob);

    // Crear elemento de enlace para descarga
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", "medicines.xml");
    document.body.appendChild(link);

    // Iniciar descarga
    link.click();

    // Limpiar
    window.URL.revokeObjectURL(url);
    document.body.removeChild(link);

    // Mostrar mensaje de éxito
    exportSuccess.value = true;
    setTimeout(() => {
      exportSuccess.value = false;
    }, 3000);
  } catch (error) {
    console.error("Error al exportar medicamentos en XML:", error);
    exportError.value = true;
    setTimeout(() => {
      exportError.value = false;
    }, 3000);
  } finally {
    isExportingXML.value = false;
  }
};

// Calcular el total de ventas
const calculateTotalSales = () => {
  return totalSales.value;
};

// Cargar todos los datos
const loadAllData = async () => {
  isLoading.value = true;
  error.value = null;

  try {
    await Promise.all([fetchMedicinesData(), fetchTransactionsData()]);
  } catch (err) {
    console.error("Error al cargar datos:", err);
    error.value =
      "No se pudieron cargar algunos datos. Se muestran datos parciales o de ejemplo.";
  } finally {
    isLoading.value = false;
  }
};

// Ejecutar al montar el componente
onMounted(async () => {
  if (checkAdminStatus()) {
    await loadAllData();
  }
});
</script>

<template>
  <div class="admin-dashboard">
    <Header />

    <div class="dashboard-container">
      <h1 class="dashboard-title">Dashboard Administrativo</h1>

      <!-- Información de conexión -->
      <div class="connection-info">
        <p class="text-gray-600">
          <span class="font-bold">IP del servidor:</span> {{ ip }}
          <span class="font-bold ml-4">Puerto:</span> 8081
        </p>
      </div>

      <!-- Botón para exportar medicamentos en XML -->
      <div class="export-container">
        <button
          class="export-btn"
          @click="exportMedicinesXML"
          :disabled="isExportingXML"
        >
          <span v-if="isExportingXML">
            <span class="spinner-small"></span> Exportando...
          </span>
          <span v-else>
            <i class="export-icon">📥</i> Exportar Medicamentos XML
          </span>
        </button>
        <div v-if="exportSuccess" class="export-message success">
          ¡Exportación exitosa!
        </div>
        <div v-if="exportError" class="export-message error">
          Error al exportar XML. Intente nuevamente.
        </div>
      </div>

      <!-- Mensaje de error si hay problemas con las APIs -->
      <div v-if="error" class="error-message">
        {{ error }}
      </div>

      <!-- Indicadores de resumen -->
      <div class="summary-cards">
        <div class="summary-card">
          <div class="card-icon">💊</div>
          <div class="card-content">
            <h3 class="card-title">Total Categorías</h3>
            <p class="card-value">{{ totalCategories }}</p>
          </div>
        </div>

        <div class="summary-card">
          <div class="card-icon">💰</div>
          <div class="card-content">
            <h3 class="card-title">Ventas Anuales</h3>
            <p class="card-value">
              Q{{ calculateTotalSales().toLocaleString() }}
            </p>
          </div>
        </div>

        <div class="summary-card">
          <div class="card-icon">📊</div>
          <div class="card-content">
            <h3 class="card-title">Inventario Total</h3>
            <p class="card-value">{{ totalInventory }} unidades</p>
          </div>
        </div>
      </div>

      <!-- Pantalla de carga -->
      <div v-if="isLoading" class="loading-container">
        <div class="spinner"></div>
        <p>Cargando datos del dashboard...</p>
      </div>

      <!-- Contenido principal -->
      <div v-else class="dashboard-grid">
        <!-- Gráfica 1: Top Categorías -->
        <div class="dashboard-card top-categories">
          <h2 class="card-title">Top 10 Categorías de Medicamentos</h2>
          <div class="chart-container">
            <Pie
              :data="topCategoriesData"
              :options="categoryOptions"
              :height="220"
            />
          </div>
          <div v-if="topCategoriesData.labels.length > 0" class="chart-legend">
            <div
              v-for="(value, index) in topCategoriesData.labels"
              :key="index"
              class="legend-item"
            >
              <span
                class="color-box"
                :style="{
                  backgroundColor:
                    topCategoriesData.datasets[0].backgroundColor[index % 10],
                }"
              ></span>
              <span class="legend-label">{{ value }}:</span>
              <span class="legend-value"
                >{{ topCategoriesData.datasets[0].data[index] }}%</span
              >
            </div>
          </div>
          <div v-else class="no-data-message">
            No hay datos de categorías disponibles
          </div>
        </div>

        <!-- Gráfica 2: Ventas Mensuales -->
        <div class="dashboard-card sales">
          <h2 class="card-title">Ventas Mensuales</h2>
          <div class="chart-container">
            <Bar
              :data="monthlySalesData"
              :options="salesOptions"
              :height="220"
            />
          </div>
          <div class="sales-summary">
            <p>
              Total anual:
              <span class="total-amount"
                >Q{{ calculateTotalSales().toLocaleString() }}</span
              >
            </p>
            <p>
              Promedio mensual:
              <span class="total-amount"
                >Q{{
                  Math.round(calculateTotalSales() / 12).toLocaleString()
                }}</span
              >
            </p>
          </div>
        </div>

        <!-- Gráfica 3: Estado del Inventario -->
        <div class="dashboard-card inventory">
          <h2 class="card-title">Estado del Inventario</h2>
          <div class="chart-container">
            <Doughnut
              :data="inventoryStatusData"
              :options="inventoryOptions"
              :height="180"
            />
          </div>
          <div class="inventory-status">
            <div
              v-for="(label, index) in inventoryStatusData.labels"
              :key="index"
              class="status-item"
              :class="`status-${label.toLowerCase().replace(' ', '-')}`"
            >
              <span class="status-label">{{ label }}:</span>
              <span class="status-value"
                >{{
                  inventoryStatusData.datasets[0].data[index]
                }}
                productos</span
              >
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-dashboard {
  background-color: #f8f9fa;
  min-height: 100vh;
}

.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.dashboard-title {
  color: #1e40af;
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 1rem;
  text-align: center;
}

/* Estilos para exportación de XML */
.export-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 2rem;
}

.export-btn {
  background-color: #1e40af;
  color: white;
  border: none;
  border-radius: 0.5rem;
  padding: 0.75rem 1.5rem;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.export-btn:hover {
  background-color: #1e3a8a;
  transform: translateY(-2px);
}

.export-btn:disabled {
  background-color: #a0aec0;
  cursor: not-allowed;
}

.export-icon {
  margin-right: 0.5rem;
  font-style: normal;
}

.export-message {
  margin-top: 0.75rem;
  padding: 0.5rem 1rem;
  border-radius: 0.25rem;
  font-size: 0.9rem;
  font-weight: 500;
}

.export-message.success {
  background-color: #c6f6d5;
  color: #2f855a;
}

.export-message.error {
  background-color: #fed7d7;
  color: #c53030;
}

.spinner-small {
  display: inline-block;
  width: 1rem;
  height: 1rem;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s linear infinite;
  margin-right: 0.5rem;
}

.error-message {
  background-color: #fecaca;
  color: #b91c1c;
  padding: 1rem;
  border-radius: 0.5rem;
  margin-bottom: 1.5rem;
  text-align: center;
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.summary-card {
  background-color: white;
  border-radius: 0.5rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  display: flex;
  align-items: center;
  transition: transform 0.2s;
}

.summary-card:hover {
  transform: translateY(-5px);
}

.card-icon {
  font-size: 2rem;
  margin-right: 1rem;
}

.card-title {
  color: #4a5568;
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.card-value {
  color: #1e40af;
  font-size: 1.5rem;
  font-weight: bold;
  margin: 0;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 2rem;
}

.dashboard-card {
  background-color: white;
  border-radius: 0.5rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  transition: transform 0.2s;
  overflow: hidden;
}

.dashboard-card:hover {
  transform: translateY(-5px);
}

.card-title {
  color: #1e40af;
  font-size: 1.25rem;
  font-weight: bold;
  margin-bottom: 1rem;
}

.chart-container {
  height: 220px;
  position: relative;
  margin-bottom: 1rem;
}

.no-data-message {
  text-align: center;
  color: #718096;
  padding: 1rem;
  background-color: #f7fafc;
  border-radius: 0.25rem;
}

/* Leyenda para categorías */
.chart-legend {
  margin-top: 1rem;
  max-height: 150px;
  overflow-y: auto;
  border-top: 1px solid #e2e8f0;
  padding-top: 0.5rem;
}

.legend-item {
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
}

.color-box {
  width: 12px;
  height: 12px;
  border-radius: 2px;
  margin-right: 0.5rem;
}

.legend-label {
  flex: 1;
  font-size: 0.85rem;
}

.legend-value {
  font-weight: bold;
  color: #1e40af;
}

/* Resumen de ventas */
.sales-summary {
  margin-top: 1rem;
  text-align: right;
  padding: 0.5rem;
  background-color: #f8f9fa;
  border-radius: 0.25rem;
}

.total-amount {
  font-weight: bold;
  color: #1e40af;
}

/* Estado de inventario */
.inventory-status {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.5rem;
  margin-top: 1rem;
}

.status-item {
  background-color: #f8f9fa;
  padding: 0.5rem;
  border-radius: 0.25rem;
  display: flex;
  flex-direction: column;
  font-size: 0.85rem;
}

.status-label {
  font-weight: 500;
}

.status-value {
  font-weight: bold;
}

.status-en-exceso {
  border-left: 3px solid #48bb78;
  background-color: rgba(198, 246, 213, 0.3);
}

.status-óptimo {
  border-left: 3px solid #4299e1;
  background-color: rgba(190, 227, 248, 0.3);
}

.status-bajo {
  border-left: 3px solid #ecc94b;
  background-color: rgba(254, 252, 191, 0.3);
}

.status-crítico {
  border-left: 3px solid #f56565;
  background-color: rgba(254, 215, 215, 0.3);
}

/* Pantalla de carga */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem;
}

.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-left-color: #1e40af;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .dashboard-container {
    padding: 1rem;
  }

  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .summary-cards {
    grid-template-columns: 1fr;
  }
}

.connection-info {
  background-color: #f8fafc;
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  margin-bottom: 1rem;
  border: 1px solid #e2e8f0;
}
</style>
