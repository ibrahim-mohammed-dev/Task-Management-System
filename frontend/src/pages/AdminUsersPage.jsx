import { useEffect, useState } from "react";
import { getAllUsers, changeUserRole } from "../api/adminApi";
import Pagination from "../components/Pagination";

export default function AdminUsersPage() {
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadUsers = async (pageNo = 0) => {
    setLoading(true);
    setError("");
    try {
      const res = await getAllUsers({ page: pageNo, size: 10 });
      setUsers(res.data.content);
      setTotalPages(res.data.totalPages);
      setPage(res.data.number);
    } catch (err) {
      setError("Could not load users.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers(0);
  }, []);

  const handleRoleChange = async (id, role) => {
    try {
      setError(""); // تصفير أي خطأ قديم قبل بدأ العملية الجديدة
      
      await changeUserRole(id, role);
      loadUsers(page); // لو العملية نجحت، اعمل ريفريش للجدول
      
    } catch (err) {
      // سحب رسالة الخطأ اللي جاية من السبرينج بوت (GlobalExceptionHandler)
      const backendMessage = err.response?.data?.message || "Error updating user role.";
      
      // عرضها في الـ div الأحمر الموجود عندك في الكود
      setError(backendMessage);
    }
  };

  return (
    <div className="page">
      <h2>Manage users</h2>
      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <p className="hint-text">Loading...</p>
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Username</th>
                <th>Email</th>
                <th>Role</th>
                <th>Change role</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>{u.id}</td>
                  <td>{u.username}</td>
                  <td>{u.email}</td>
                  <td>
                    <span className="badge">{u.role}</span>
                  </td>
                  <td>
                    <select
                      defaultValue=""
                      onChange={(e) => {
                        if (e.target.value) handleRoleChange(u.id, e.target.value);
                        e.target.value = "";
                      }}
                    >
                      <option value="" disabled>
                        Set role...
                      </option>
                      <option value="USER">USER</option>
                      <option value="ADMIN">ADMIN</option>
                    </select>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Pagination page={page} totalPages={totalPages} onPageChange={loadUsers} />
    </div>
  );
}
