import axios, { AxiosInstance, AxiosRequestConfig, AxiosError, InternalAxiosRequestConfig } from 'axios';
import { authService } from '@/demo/service/AuthService'; // Chemin à adapter

// EN PROD DECOMMENTER CETTE PARTIE
const axiosInstance2: AxiosInstance = axios.create({
  // baseURL: 'https://portailbac.ucad.sn/ob/api/v1/',
  baseURL: `${process.env.NEXT_PUBLIC_API_URL}/v1/`,
  // headers: {
  //   'Content-Type': 'application/json',
  //   'Accept': '*/*',
  // },
});

// const axiosInstance2: AxiosInstance = axios.create({
//   baseURL: 'http://localhost:8080/ob/api/v1/',
//   // headers: {
//   //   'Content-Type': 'application/json',
//   //   'Accept': '*/*',
//   // },
// });

// Interceptor pour ajouter le token dans chaque requête
axiosInstance2.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = authService.getToken();
    if (token) 
    {
      config.headers.set('Authorization', `Bearer ${token}`);
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor pour gérer le refresh token en cas de 401
axiosInstance2.interceptors.response.use(
  (response) => response, // Si OK, on retourne directement
  async (error: AxiosError) => {
    const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean };

    // Si 401 + pas déjà tenté de refresh
    if (error.response?.status === 401 || error.response?.status === 403 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const newToken = await authService.refreshToken();

        if (newToken) {
          // Mise à jour du header Authorization avec le nouveau token
          originalRequest.headers = originalRequest.headers || {};
          originalRequest.headers['Authorization'] = `Bearer ${newToken}`;

          // On rejoue la requête avec le nouveau token
          return axiosInstance2(originalRequest);
        }
      } catch (refreshError) {
        console.error('❌ Echec du refresh token:', refreshError);
        authService.logout();
        return Promise.reject(refreshError);
      }
    }

    // Si autre erreur ou déjà tenté → rejet
    return Promise.reject(error);
  }
);

export default axiosInstance2;

