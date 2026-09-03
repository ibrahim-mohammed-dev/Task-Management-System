import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function AdminRoute() {
  const { token, isAdmin, hasPermission, loading } = useAuth();

  if (loading) return null;
  if (!token) return <Navigate to="/login" replace />;

  const isAllowed = isAdmin || hasPermission("MANAGE_GROUPS") || hasPermission("VIEW_ALL_USERS");

  return isAllowed ? (
    <Outlet />
  ) : (
    <div className="card alert alert-error m-4" style={{ textAlign: "center", margin: "2rem auto", maxWidth: "600px" }}>
      <h3>Access Denied (403 Forbidden)</h3>
      <p>You do not have permission to access management features.</p>
    </div>
  );
}
