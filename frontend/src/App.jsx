import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import Navbar from "./components/Navbar";
import PrivateRoute from "./components/PrivateRoute";
import AdminRoute from "./components/AdminRoute";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import TasksPage from "./pages/TasksPage";
import AdminUsersPage from "./pages/AdminUsersPage";
import AdminTasksPage from "./pages/AdminTasksPage";
import GroupsPage from "./pages/GroupPage";

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Navbar />
        <main className="container">
          <Routes>
            <Route path="/" element={<Navigate to="/tasks" replace />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            {/* Regular authenticated users */}
            <Route element={<PrivateRoute />}>
              <Route path="/tasks" element={<TasksPage />} />
            </Route>

            {/* Admin-only (requires VIEW_ALL_USERS / MANAGE_GROUPS permissions) */}
            <Route element={<AdminRoute />}>
              <Route path="/admin/users" element={<AdminUsersPage />} />
              <Route path="/admin/tasks" element={<AdminTasksPage />} />
              <Route path="/admin/groups" element={<GroupsPage />} />
            </Route>

            <Route path="*" element={<Navigate to="/tasks" replace />} />
          </Routes>
        </main>
      </AuthProvider>
    </BrowserRouter>
  );
}
