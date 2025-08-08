// src/stores/userStore.js
import { defineStore } from "pinia";

export const useUserStore = defineStore("user", {
  state: () => ({
    user: null, // Cambiado de {} a null para facilitar la verificación
  }),
  actions: {
    // Guarda el usuario en el estado y localStorage
    setUser(userData) {
      this.user = userData;
      console.log("Guardando sesión en localStorage:", userData);
      try {
        if (typeof window !== "undefined") {
          localStorage.setItem("session", JSON.stringify(userData));
          localStorage.setItem("user", JSON.stringify(userData));
          // También guardar el rol por separado para facilitar la verificación
          if (userData && userData.role) {
            localStorage.setItem("role", userData.role);
          }
        }
      } catch (error) {
        console.error("Error guardando datos de usuario:", error);
      }
    },
    
    // Obtiene el usuario del estado o localStorage
    getUser() {
      // Si ya tenemos el usuario en el estado, devolverlo
      if (this.user) {
        return this.user;
      }
      
      // Intentar recuperar de localStorage
      try {
        const session = localStorage.getItem("session");
        if (session && session !== "undefined" && session !== "") {
          const userData = JSON.parse(session);
          // Actualizar el estado
          this.user = userData;
          return userData;
        }
        
        // Si no hay sesión, intentar con 'user'
        const userStr = localStorage.getItem("user");
        if (userStr && userStr !== "undefined" && userStr !== "") {
          const userData = JSON.parse(userStr);
          // Actualizar el estado
          this.user = userData;
          return userData;
        }
      } catch (error) {
        console.error("Error recuperando datos de usuario:", error);
      }
      
      return {}; // Devolver objeto vacío si no hay datos
    },
    
    // Verifica si el usuario tiene rol admin
    isAdmin() {
      const user = this.getUser();
      return user && user.role === 'admin';
    },

    // Limpia el estado y localStorage
    logout() {
      this.user = null;
      
      if (typeof window !== "undefined") {
        localStorage.removeItem("session");
        localStorage.removeItem("user");
        localStorage.removeItem("role");
        console.log("Sesión cerrada: localStorage limpiado");
      }
    },
  },
});
