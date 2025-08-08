import { createRouter, createWebHistory } from 'vue-router';
import Home from '../pages/Home.vue'; // Ajustar rutas relativas
import Login from '../pages/Login.vue';
import Dashboard from '../pages/Dashboard.vue';
import Register from '../pages/Register.vue';
import Catalogo from '../pages/Catalogo.vue';
import Aseguradoras from '@/pages/Aseguradoras.vue';
import Ofertas from '@/pages/Ofertas.vue';
import ProductoDetalle from '@/pages/ProductoDetalle.vue'; // new import
import VerificarCompra from '@/pages/VerificarCompra.vue'; // nueva página de verificación
import Receta from '@/components/Receta.vue';
import CreateProduct from '@/pages/CreateProduct.vue';
import Prescriptions from '@/pages/Prescriptions.vue';
import { authService } from '@/services/authService';
import Cart from "@/components/Cart.vue";
import AdminDash from '@/pages/AdminDash.vue'; // Importar el dashboard administrativo
import DetalleReceta from '@/pages/DetalleReceta.vue'

// Rutas para usuarios públicos y autenticados
const userRoutes = [
  { path: '/', component: Home },
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { path: '/catalogo', component: Catalogo },
  { path: '/aseguradoras', component: Aseguradoras },
  { path: '/ofertas', component: Ofertas },
  { path: '/producto/:id', name: 'ProductoDetalle', component: ProductoDetalle },
  { path: '/verificar-compra/:id', name: 'VerificarCompra', component: VerificarCompra, meta: { requiresAuth: true } },
  { path: '/receta', component: Receta },
  { path: '/create-product', name: 'CreateProduct', component: CreateProduct },
  { path: '/prescriptions', name: 'Prescriptions', component: Prescriptions },
  { path: '/dashboard', component: Dashboard, meta: { requiresAuth: true } },
  {path: '/cart', name: 'Cart', component: Cart},
  {
    path: '/detalle-receta/:id',
    name: 'DetalleReceta',
    component: DetalleReceta,
    meta: { requiresAuth: true }
  },
];

// Rutas exclusivas para administradores
const adminRoutes = [
  {
    path: '/admin/create-product',
    name: 'AdminCreateProduct',
    component: CreateProduct,
    meta: { admin: true }
  },
  {
    path: '/admindash',
    name: 'AdminDash',
    component: AdminDash,
    meta: { admin: true }
  }
];

const routes = [
  ...userRoutes,
  ...adminRoutes
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const adminOnly = to.matched.some(record => record.meta.admin);
  
  // Intentar obtener el usuario del servicio de autenticación
  const currentUser = authService.getCurrentUser();
  console.log("Router: Usuario actual:", currentUser);
  
  // Para rutas administrativas, verificar también en localStorage
  if (adminOnly) {
    console.log("Ruta protegida para admin:", to.path);
    
    // Verificar en el servicio de autenticación
    if (currentUser && currentUser.role === 'admin') {
      console.log("Acceso permitido por authService");
      return next();
    }
    
    // Verificar en localStorage como respaldo
    try {
      // Verificar por el campo role directamente
      const storedRole = localStorage.getItem('role');
      if (storedRole === 'admin') {
        console.log("Acceso permitido por localStorage.role");
        return next();
      }
      
      // Verificar en el objeto user
      const storedUser = localStorage.getItem('user');
      if (storedUser) {
        const user = JSON.parse(storedUser);
        if (user.role === 'admin') {
          console.log("Acceso permitido por localStorage.user");
          return next();
        }
      }
      
      // Verificar en session
      const storedSession = localStorage.getItem('session');
      if (storedSession) {
        const session = JSON.parse(storedSession);
        if (session.role === 'admin') {
          console.log("Acceso permitido por localStorage.session");
          return next();
        }
      }
    } catch (e) {
      console.error("Error verificando el rol en localStorage:", e);
    }
    
    // Si ninguna verificación funcionó, redirigir
    console.log("Acceso denegado a ruta admin");
    return next('/');
  }
  
  // Para rutas que requieren autenticación (no admin)
  if (requiresAuth && !currentUser) {
    console.log("Ruta requiere autenticación - redirigiendo a login");
    return next('/login');
  }
  
  // Acceso permitido para otras rutas
  return next();
});

export default router;