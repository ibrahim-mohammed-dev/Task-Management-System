import { createContext, useContext, useEffect, useState } from "react";
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

  // Whenever the token changes (login, logout, page refresh with a saved
  // token), fetch who the current user actually is from the backend.
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

        // Determine if the user belongs to a group that has admin permissions
        // by probing the admin endpoint — if it succeeds they have VIEW_ALL_USERS
        // permission (only the ADMIN group has it), so they are admin.
        try {
          await getAllUsers({ page: 0, size: 1 });
          if (!cancelled) setIsAdmin(true);
        } catch {
          if (!cancelled) setIsAdmin(false);
        }
      } catch (err) {
        // Token was rejected by the backend (expired/invalid) — log out.
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

    // Save to localStorage first so the interceptor attaches it immediately
    localStorage.setItem("token", newToken);

    // Then update state (triggers the useEffect above)
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
    <AuthContext.Provider value={{ token, user, isAdmin, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
