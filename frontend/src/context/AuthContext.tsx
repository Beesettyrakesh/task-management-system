import {
  AuthContextType,
  AuthResult,
  LoginData,
  SignupData,
  User,
} from "@/types";
import React, { createContext, ReactNode, useEffect, useState } from "react";
import toast from "react-hot-toast";
import API from "../services/api";

export const AuthContext = createContext<AuthContextType | null>(null);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string>(() => {
    return localStorage.getItem("token") || "";
  });
  const [loading, setLoading] = useState<boolean>(true);
  const [authLoading, setAuthLoading] = useState<boolean>(false);

  useEffect(() => {
    const verifyToken = async (): Promise<void> => {
      const savedToken = localStorage.getItem("token");

      if (savedToken) {
        try {
          setToken(savedToken);

          const response = await API.get<User>("/auth/me");
          setUser(response.data);

          console.log("Token verified successfully:", response.data);
        } catch (error) {
          console.log("Token verification failed");
          localStorage.removeItem("token");
          setToken("");
          setUser(null);
        }
      }
      setLoading(false);
    };

    verifyToken();
  }, []);

  useEffect(() => {
    if (token) {
      localStorage.setItem("token", token);
    } else {
      localStorage.removeItem("token");
    }
  }, [token]);

  useEffect(() => {
    const handleInterceptorLogout = () => {
      setToken("");
      setUser(null);
    };

    window.addEventListener("auth-logout", handleInterceptorLogout);
    return () =>
      window.removeEventListener("auth-logout", handleInterceptorLogout);
  }, []);

  const signup = async (userData: SignupData): Promise<AuthResult> => {
    setAuthLoading(true);
    try {
      const response = await API.post("/auth/signup", userData);
      setAuthLoading(false);
      return { success: true, data: response.data };
    } catch (error: any) {
      setAuthLoading(false);

      if (!error.response) {
        return {
          success: false,
          error: "Network error. Please check your internet connection.",
        };
      }

      if (error.code === "ECONNABORTED") {
        return {
          success: false,
          error: "Request timeout. Please try again.",
        };
      }

      switch (error.response.status) {
        case 400:
          return {
            success: false,
            error: error.response.data?.message || "Invalid input data",
          };
        case 409:
          return {
            success: false,
            error: "Username or email already exists",
          };
        case 500:
          return {
            success: false,
            error: "Server error. Please try again later.",
          };
        default:
          return {
            success: false,
            error: error.response.data?.message || "Something went wrong",
          };
      }
    }
  };

  const login = async (credentials: LoginData): Promise<AuthResult> => {
    setAuthLoading(true);
    try {
      const response = await API.post("/auth/login", credentials);
      const { token: newToken, username, email } = response.data;

      setToken(newToken);
      setUser({ username, email });
      toast.success(`Welcome back, ${username}!`);
      setAuthLoading(false);
      return { success: true };
    } catch (error: any) {
      setAuthLoading(false);

      if (!error.response) {
        const errorMsg = "Network error. Please check your connection.";
        return {
          success: false,
          error: errorMsg,
        };
      }

      const errorMessage =
        error.response?.data?.message || "Login failed. Please try again.";
      return { success: false, error: errorMessage };
    }
  };

  const logout = () => {
    const username = user?.username;
    setToken("");
    setUser(null);
    toast.success(
      `Goodbye${username ? `, ${username}` : ""}! You've been logged out.`,
    );
  };

  const value = {
    user,
    token,
    loading,
    authLoading,
    signup,
    login,
    logout,
    isAuthenticated: !!token && !!user,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
