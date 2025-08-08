import { createMemoryHistory, createRouter, createWebHistory } from "vue-router";
import type { RouteLocationNormalized, NavigationGuardNext } from "vue-router";
import Login from "./pages/login.vue";
import Home from "./pages/home.vue";
import Register from "./pages/register.vue";
import AdminUsers from "./pages/admin/users.vue";
import InsuranceServices from "./pages/admin/insurance-services.vue";
import HospitalServices from "./pages/admin/hospital-services.vue";
import HospitalServicesImport from "./pages/admin/hospital-services-import.vue";
import HospitalConfiguration from "./pages/admin/hospital-configuration.vue";
import ProfileCompletion from "./pages/profile-completion.vue";
import InactiveAccount from "./pages/inactive-account.vue";
import CatalogInsuranceServices from "./pages/catalog/insurance-services.vue";
import CatalogHospitals from "./pages/catalog/hospitals.vue";
import CatalogHospitalServices from "./pages/catalog/hospital-services.vue";
import Policies from "./pages/admin/policies.vue";
import RegisterClient from "./pages/employee/register-client.vue";
import Appointments from "./pages/appointments.vue";
import ClientManagement from "./pages/admin/client-management.vue";
import DailyAppointments from "./pages/admin/daily-appointments.vue";
import PrescriptionApprovals from "./pages/admin/prescription-approvals.vue";
import SystemConfiguration from "./pages/admin/system-configuration.vue";
import UserServices from "./pages/user-services.vue";
import { checkMissingRequiredFields } from "./utils/profile-utils";


const requireAuth = (
  to: RouteLocationNormalized, 
  from: RouteLocationNormalized, 
  next: NavigationGuardNext
) => {
  const profile = JSON.parse(localStorage.getItem("user") || "null");
  
  if (!profile || profile === "null") {
    // No autenticado, redirigir a login
    next('/login');
  } else if (profile.enabled !== 1) {
    // Usuario no activado
    next('/inactive-account');
  } else {
    // COMENTADO: Verificar si el usuario necesita completar su perfil
    // Ya no verificamos si faltan campos de perfil para evitar redirecciones indeseadas
    /*
    const missingFields = checkMissingRequiredFields(profile);
    if (missingFields && to.path !== '/profile-completion') {
      next('/profile-completion');
    } else {
    */
      next(); // Usuario autenticado, continuar sin verificar perfil
    //}
  }
};

const requireAdmin = (
  to: RouteLocationNormalized, 
  from: RouteLocationNormalized, 
  next: NavigationGuardNext
) => {
  const profile = JSON.parse(localStorage.getItem("user") || "null");
  console.log("Profile en requireAdmin:", profile);
  
  if (!profile || profile === "null") {
    // No autenticado, redirigir a login
    console.log("No autenticado, redirigiendo a login");
    next('/login');
    return;
  } 
  
  console.log("Role:", profile.role);
  
  // Verificar si es admin (en minúsculas, como lo envía el backend)
  if (profile.role !== "admin") {
    // No es administrador, redirigir a home
    console.log("No es admin, redirigiendo a home");
    next('/home');
    return;
  }
  
  // Es administrador, continuar
  console.log("Es admin, continuando");
  next();
};


const requireCompleteProfile = (
  to: RouteLocationNormalized, 
  from: RouteLocationNormalized, 
  next: NavigationGuardNext
) => {
  const profile = JSON.parse(localStorage.getItem("user") || "null");
  
  if (!profile || profile === "null") {
    next('/login');
    return;
  }
  
  // Ya no verificamos campos faltantes en el perfil
  // Permitir acceso a todas las rutas independientemente del estado del perfil
  next();
  
  /* CÓDIGO ORIGINAL COMENTADO
  const missingFields = checkMissingRequiredFields(profile);
  
  if (missingFields) {
    // Solo permitir acceso a la ruta de completar perfil
    if (to.path !== '/profile-completion') {
      next('/profile-completion');
    } else {
      next();
    }
  } else {
    next(); // Perfil completo, continuar
  }
  */
};

// Middleware para usuarios inactivos
const inactiveUserOnly = (
  to: RouteLocationNormalized, 
  from: RouteLocationNormalized, 
  next: NavigationGuardNext
) => {
  const profile = JSON.parse(localStorage.getItem("user") || "null");
  
  if (!profile || profile === "null") {
    next('/login');
  } else if (profile.enabled === 1) {
    next('/home');
  } else {
    next(); // Usuario inactivo, puede ver la página
  }
};

// Middleware para requerir rol de empleado o admin
const requireEmployeeOrAdmin = (
  to: RouteLocationNormalized, 
  from: RouteLocationNormalized, 
  next: NavigationGuardNext
) => {
  const user = JSON.parse(localStorage.getItem("user") || "null");
  if (!user) {
    next('/login');
  } else if (user.role === 'employee' || user.role === 'admin') {
    next();
  } else {
    next('/home');
  }
};

const routes = [
  { path: "/", redirect: "/login" },
  { path: "/login", component: Login },
  { path: "/register", component: Register },
  { 
    path: "/home", 
    component: Home, 
    beforeEnter: requireAuth 
  },
  {
    path: "/admin/users",
    component: AdminUsers,
    beforeEnter: requireAdmin
  },
  {
    path: "/admin/insurance-services",
    component: InsuranceServices,
    beforeEnter: requireAdmin
  },
  {
    path: "/admin/hospital-services",
    component: HospitalServices,
    beforeEnter: requireAdmin
  },
  {
    path: "/admin/hospital-services-import",
    component: HospitalServicesImport,
    beforeEnter: requireAdmin
  },
  {
    path: "/admin/hospital-configuration",
    component: HospitalConfiguration,
    beforeEnter: requireAdmin
  },
  {
    path: "/admin/policies",
    component: Policies,
    beforeEnter: requireAdmin
  },
  {
    path: '/admin/client-management',
    component: ClientManagement,
    beforeEnter: requireAdmin
  },
  {
    path: '/admin/daily-appointments',
    component: DailyAppointments,
    beforeEnter: requireAdmin
  },
  {
    path: '/admin/prescription-approvals',
    component: PrescriptionApprovals,
    beforeEnter: requireEmployeeOrAdmin // Permitir a empleados y admins
  },
  {
    path: '/admin/configuration',
    component: SystemConfiguration,
    beforeEnter: requireEmployeeOrAdmin // Permitir a empleados y admins
  },
  {
    path: "/profile-completion",
    component: ProfileCompletion,
    beforeEnter: requireCompleteProfile
  },
  {
    path: "/inactive-account",
    component: InactiveAccount,
    beforeEnter: inactiveUserOnly
  },
  // Nuevas rutas de catálogo
  {
    path: "/catalog/insurance-services",
    component: CatalogInsuranceServices,
    beforeEnter: requireAuth
  },
  {
    path: "/catalog/hospitals",
    component: CatalogHospitals,
    beforeEnter: requireAuth
  },
  {
    path: "/catalog/hospital-services",
    component: CatalogHospitalServices,
    beforeEnter: requireAuth
  },
  {
    path: '/employee/register-client',
    component: RegisterClient,
    beforeEnter: requireEmployeeOrAdmin
  },
  // Nueva ruta para citas
  {
    path: '/appointments',
    component: Appointments,
    beforeEnter: requireAuth
  },
  {
    path: '/user-services',
    component: UserServices,
    beforeEnter: requireAuth
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
