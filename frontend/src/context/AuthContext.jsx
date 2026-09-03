import { createContext, useContext, useEffect, useState, useMemo } from "react";
import { loginUser, registerUser } from "../api/authApi";
import { getCurrentUser } from "../api/userApi";
import { getAllUsers } from "../api/adminApi";
import { decodeToken, isTokenExpired } from "../utils/jwt";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem("token"));
  const [user, setUser] = useState(null); // { id, username, email, groupName }
  const [isAdmin, setIsAdmin] = useState(false);
  const [loading, setLoading] = useState(true);

  // Extract permissions array from JWT token claims
  const permissions = useMemo(() => {
    if (!token) return [];
    const decoded = decodeToken(token);
    return Array.isArray(decoded?.permissions) ? decoded.permissions : [];
  }, [token]);

  // Helper method to check if current user has a specific permission
  const hasPermission = (permissionName) => {
    if (!permissionName) return true;
    return permissions.includes(permissionName);
  };

  // Whenever the token changes (login, logout, page refresh with a saved token)
  useEffect(() => {
    let cancelled = false;

    async function loadUser() {
      if (!token) {
        setUser(null);
        setIsAdmin(false);
        setLoading(false);
        return;
      }

      const decoded = decodeToken(token);
      if (!decoded || isTokenExpired(decoded)) {
        localStorage.removeItem("token");
        setToken(null);
        setUser(null);
        setIsAdmin(false);
        setLoading(false);
        return;
      }

      try {
        const res = await getCurrentUser();
        if (!cancelled) setUser(res.data);

        // Determine if user has admin access by probing admin endpoint
        try {
          await getAllUsers({ page: 0, size: 1 });
          if (!cancelled) setIsAdmin(true);
        } catch {
          if (!cancelled) setIsAdmin(false);
        }
      } catch (err) {
        if (!cancelled) {
          localStorage.removeItem("token");
          setToken(null);
          setUser(null);
          setIsAdmin(false);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    setLoading(true);
    loadUser();

    return () => {
      cancelled = true;
    };
  }, [token]);

  const login = async (credentials) => {
    const response = await loginUser(credentials);
    const newToken = response.data;
    localStorage.setItem("token", newToken);
    setToken(newToken);
  };

  const register = async (data) => {
    await registerUser(data);
  };

  const logout = () => {
    localStorage.removeItem("token");
    setToken(null);
    setUser(null);
    setIsAdmin(false);
  };

  return (
    <AuthContext.Provider
      value={{
        token,
        user,
        isAdmin,
        permissions,
        hasPermission,
        loading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
