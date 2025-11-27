import axios from "axios";
import toast from 'react-hot-toast';

const API = axios.create({ baseURL: "http://localhost:8080/api" });

API.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    if(token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, (error) => Promise.reject(error));

API.interceptors.response.use((response) => response, (error) => {
    if(error.response?.status === 401) {
        toast.error("Session expired. Please login again.");
        localStorage.removeItem('token');
        window.dispatchEvent(new CustomEvent('auth-logout'));
        setTimeout(() => {
            window.location.href = '/login';
        }, 1500);
    }
    return Promise.reject(error);
});

export default API;
