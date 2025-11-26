import { Navigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth"
import { ReactNode } from "react"

interface ProtectedRouteProps {
    children: ReactNode
}
const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
    const {isAuthenticated, loading} = useAuth();

    if(loading) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <div className="text-xl">Loading...</div>
            </div>
        );
    }

    if(!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    return <>{children}</>;
}

export default ProtectedRoute;