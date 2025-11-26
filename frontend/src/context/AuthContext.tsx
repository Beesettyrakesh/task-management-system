import {
  AuthContextType,
  AuthResult,
  LoginData,
  SignupData,
  User,
} from "@/types";
import React, { createContext, ReactNode, useEffect, useState } from "react";
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
          API.defaults.headers.common["Authorization"] = `Bearer ${savedToken}`;

          const response = await API.get<User>("/auth/me");
          setUser(response.data);

          console.log("Token verified:", savedToken);
          console.log("User data:", response.data);
          console.log("Token verified successfully:", response.data);
        } catch (error) {
          console.log("Token verification failed");
          localStorage.removeItem("token");
          delete API.defaults.headers.common["Authorization"];
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
      API.defaults.headers.common["Authorization"] = `Bearer ${token}`;
      localStorage.setItem("token", token);
    } else {
      delete API.defaults.headers.common["Authorization"];
      localStorage.removeItem("token");
    }
  }, [token]);

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
      setAuthLoading(false);
      return { success: true };
    } catch (error: any) {
      setAuthLoading(false);

      if (!error.response) {
        return {
          success: false,
          error: "Network error. Please check your connection.",
        };
      }

      const errorMessage =
        error.response?.data?.message || "Login failed. Please try again.";
      return { success: false, error: errorMessage };
    }
  };

  const logout = () => {
    setToken("");
    setUser(null);
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
