// User related types
export interface User {
  username: string;
  email: string;
}

// Task related types
export interface Task {
  id: number;
  title: string;
  description: string;
  status: TaskStatus;
  priority: Priority;
  dueDate: string;
  user?: User;
  createdAt: string;
  updatedAt?: string;
  createdBy: string;
  lastModifiedBy?: string;
}

export interface TaskFormData {
  title: string;
  description: string;
  dueDate: Date | null;
  priority: { value: Priority; label: string } | null;
  status: { value: TaskStatus; label: string } | null;
}

// Option types for react-select
export interface SelectOption<T> {
  value: T;
  label: string;
}

export enum TaskStatus {
  TODO = "TODO",
  IN_PROGRESS = "IN_PROGRESS",
  DONE = "DONE",
}

export enum Priority {
  LOW = "LOW",
  MEDIUM = "MEDIUM",
  HIGH = "HIGH",
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
