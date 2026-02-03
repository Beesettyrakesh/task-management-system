import { Task, TaskStatistics, ValidationError } from "@/types";
import axios from "axios";
import toast from "react-hot-toast";

const API = axios.create({
  baseURL: "https://doqueue.ddns.net/api",
  timeout: 90000,
});

API.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

API.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const errorMessage = error.response?.data?.message || "";
      const isInvalidCredentials =
        errorMessage.toLowerCase().includes("invalid") ||
        errorMessage.toLowerCase().includes("incorrect");
      if (!isInvalidCredentials) {
        const currentPath = window.location.pathname;
        const isAuthPage =
          currentPath === "/login" || currentPath === "/signup";

        if (!isAuthPage) {
          toast.error("Session expired. Please login again.");
          localStorage.removeItem("token");
          window.dispatchEvent(new CustomEvent("auth-logout"));
          setTimeout(() => {
            window.location.href = "/login";
          }, 1500);
        }
      }
    }
    return Promise.reject(error);
  },
);

API.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 400 && error.response?.data?.fieldErrors) {
      const validationError: ValidationError = new Error("Validation failed");
      validationError.fieldErrors = error.response.data.fieldErrors;
      return Promise.reject(validationError);
    }
    return Promise.reject(error);
  },
);

export const getTaskStatistics = (): Promise<TaskStatistics> =>
  API.get("/tasks/statistics").then((response) => response.data);

export const getRecentTasks = (): Promise<Task[]> =>
  API.get("/tasks/recent").then((response) => response.data);

export default API;
