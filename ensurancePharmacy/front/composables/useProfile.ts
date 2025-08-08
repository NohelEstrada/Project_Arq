// composable: useProfile.ts
import { useState } from "#app";

export interface User {
    idUser: number;
    name: string;
    cui: number;
    phone: string;
    email: string;
    address: string;
    birthDate: number;
    role: string;
    policy: object;
    enabled: number;
    password: string;
}

// Crea el estado inicial sin leer localStorage, o con un valor por defecto.
export const useProfile = () =>
    useState<User | null>('profile', () => null);

// Método para inicializar (rehidratar) el perfil desde localStorage
export const initializeProfile = () => {
    if (import.meta.client) {
        const storedProfile = localStorage.getItem('profile');
        if (storedProfile) {
            try {
                const parsed = JSON.parse(storedProfile) as User;
                const profileState = useProfile();
                profileState.value = parsed;
            } catch (error) {
                console.error('Error al parsear el perfil desde localStorage', error);
            }
        }
    }
};

export const setProfile = (newProfile: User) => {
    if (import.meta.client) {
        localStorage.setItem('profile', JSON.stringify(newProfile));
        const profileState = useProfile();
        profileState.value = newProfile;
    }
};
