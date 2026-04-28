import axios, { AxiosRequestConfig, AxiosResponse } from "axios";

const defaultOptions: AxiosRequestConfig = {
  // baseURL: "http://localhost:8080/ob/api/v1/",
  baseURL: `${process.env.NEXT_PUBLIC_API_URL}/v1/`,
  headers: {
    "Content-Type": "application/json",
    Accept: "*/*",
  },
};
const axiosInstance1 = axios.create(defaultOptions);
// 🔑 Intercepteur des requêtes
axiosInstance1.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 🔑 Intercepteur des réponses
axiosInstance1.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error) => {
    if (error.response?.status === 401) {
      clearLocalStorage();
      // window.location.href = "/connexion"; // Redirection vers login
    }
    return Promise.reject(error);
  },
);

// 🔑 Nettoyage du localStorage
function clearLocalStorage() {
  localStorage.removeItem("token");
  localStorage.removeItem("refreshToken");
  localStorage.removeItem("email");
  localStorage.removeItem("role");
  localStorage.removeItem("fullname");
  localStorage.removeItem("dateExpiration");
}

export default axiosInstance1;
