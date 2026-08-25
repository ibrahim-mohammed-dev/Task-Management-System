import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { token, user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <header className="navbar">
      <Link to="/" className="navbar-brand">
        TaskApp
      </Link>
      <nav className="navbar-links">
        {token ? (
          <>
            <Link to="/tasks">My Tasks</Link>
            {isAdmin && <Link to="/admin/users">Users</Link>}
            {isAdmin && <Link to="/admin/tasks">All Tasks</Link>}
            {user?.username && <span className="navbar-user">{user.username}</span>}
            <button className="btn btn-outline btn-small" onClick={handleLogout}>
              Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </nav>
    </header>
  );
}
