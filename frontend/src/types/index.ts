// User related types
export interface User {
  username: string;
  email: string;
}

// Auth context types
export interface AuthContextType {
  user: User | null;
  token: string;
  loading: boolean;
  authLoading: boolean;
  signup: (userData: SignupData) => Promise<AuthResult>;
  login: (credentials: LoginData) => Promise<AuthResult>;
  logout: () => void;
  isAuthenticated: boolean;
}

// Form data types
export interface SignupData {
  username: string;
  email: string;
  password: string;
}

export interface LoginData {
  username: string;
  password: string;
}

// API response types
export interface AuthResult {
  success: boolean;
  error?: string;
  data?: any;
}

export interface LoginResponse {
  token: string;
  username: string;
  email: string;
}

export interface ApiError {
  message: string;
  status?: number;
}

// Form validation types
export interface SignupFormData {
  username: string;
  email: string;
  password: string;
}

export interface LoginFormData {
  username: string;
  password: string;
}
