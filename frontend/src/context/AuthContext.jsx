import { createContext, useContext, useEffect, useState } from "react";
import { loginUser, registerUser } from "../api/authApi";
import { getCurrentUser } from "../api/userApi";
import { decodeToken, isTokenExpired } from "../utils/jwt";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem("token"));
  const [user, setUser] = useState(null); // { id, username, email, role }
  const [loading, setLoading] = useState(true);

  // Whenever the token changes (login, logout, page refresh with a saved
  // token), fetch who the current user actually is from the backend.
  useEffect(() => {
    let cancelled = false;

    async function loadUser() {
      if (!token) {
        setUser(null);
        setLoading(false);
        return;
      }

      const decoded = decodeToken(token);
      if (!decoded || isTokenExpired(decoded)) {
        localStorage.removeItem("token");
        setToken(null);
        setUser(null);
        setLoading(false);
        return;
      }

      try {
        const res = await getCurrentUser();
        if (!cancelled) setUser(res.data);
      } catch (err) {
        // Token was rejected by the backend (expired/invalid) — log out.
        if (!cancelled) {
          localStorage.removeItem("token");
          setToken(null);
          setUser(null);
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
    const newToken = response.data; // backend returns the JWT as plain text
    localStorage.setItem("token", newToken);
    setToken(newToken); // triggers the effect above to fetch /api/users/me
  };

  const register = async (data) => {
    await registerUser(data);
  };

  const logout = () => {
    localStorage.removeItem("token");
    setToken(null);
    setUser(null);
  };

  // Role comes back as your Role enum's name (e.g. "ADMIN"). Using
  // "includes" instead of an exact match so this still works whether your
  // enum values are "ADMIN" or something like "ROLE_ADMIN".
  const isAdmin = typeof user?.role === "string" && user.role.toUpperCase().includes("ADMIN");

  return (
    <AuthContext.Provider value={{ token, user, isAdmin, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
