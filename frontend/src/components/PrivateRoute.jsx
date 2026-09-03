import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function PrivateRoute({ requiredPermission }) {
  const { token, hasPermission, loading } = useAuth();

  if (loading) return null;
  if (!token) return <Navigate to="/login" replace />;

  if (requiredPermission && !hasPermission(requiredPermission)) {
    return (
      <div className="card alert alert-error m-4" style={{ textAlign: "center", margin: "2rem auto", maxWidth: "600px" }}>
        <h3>Access Denied (403 Forbidden)</h3>
        <p>You do not have the required permission (<code>{requiredPermission}</code>) to view this page.</p>
        <p className="hint-text" style={{ marginTop: "1rem" }}>Please contact an administrator if you believe this is an error.</p>
      </div>
    );
  }

  return <Outlet />;
}
