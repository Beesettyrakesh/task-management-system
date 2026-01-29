import { lazy, Suspense } from "react";
import { Toaster } from "react-hot-toast";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import { LoadingPage, LoadingSpinner } from "./components/LoadingSpinner";
import ProtectedRoute from "./components/ProtectedRoutes";
import { AuthProvider } from "./context/AuthContext";
import { useAuth } from "./hooks/useAuth";

const Dashboard = lazy(() => import("./pages/Dashboard"));
const Signup = lazy(() => import("./pages/Signup"));
const Login = lazy(() => import("./pages/Login"));

function AppContent() {
  const { loading } = useAuth();

  if (loading) {
    return <LoadingPage />;
  }

  return (
    <Suspense fallback={<LoadingSpinner fullScreen text="Loading page..." />}>
      <Routes>
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />
        <Route path="/" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/login" element={<Login />} />
      </Routes>
    </Suspense>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppContent />
        <Toaster position="top-right" />
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
