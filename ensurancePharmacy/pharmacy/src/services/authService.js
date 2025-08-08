// src/services/authService.js
import ApiService from './ApiService';

export const authService = {
  async login(credentials) {
    const response = await fetch(ApiService.getPharmacyApiUrl('/login'), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(credentials)
    });

    if (!response.ok) {
      throw new Error('Credenciales inválidas');
    }

    const data = await response.json();

    // Si el backend retorna un token (o información del usuario), lo guardamos
    if (data.token) {
      localStorage.setItem('user', JSON.stringify(data));
    }

    return data;
  },

  logout() {
    localStorage.removeItem('user');
  },

  getCurrentUser() {
    return JSON.parse(localStorage.getItem('user'));
  }
};