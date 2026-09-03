import { useEffect, useState } from "react";
import { getAllUsers } from "../api/adminApi";
import { assignUserToGroup, getAllGroups } from "../api/groupApi";
import Pagination from "../components/Pagination";

export default function AdminUsersPage() {
  const [users, setUsers] = useState([]);
  const [groups, setGroups] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const loadData = async (pageNo = 0) => {
    setLoading(true);
    setError("");
    try {
      const [usersRes, groupsRes] = await Promise.all([
        getAllUsers({ page: pageNo, size: 10 }),
        getAllGroups({ page: 0, size: 100 }),
      ]);
      setUsers(usersRes.data.content || []);
      setTotalPages(usersRes.data.totalPages || 0);
      setPage(usersRes.data.number || 0);
      setGroups(groupsRes.data.content || groupsRes.data || []);
    } catch (err) {
      setError("Could not load users or groups data.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData(0);
  }, []);

  const handleAssignGroup = async (userId, targetGroupId) => {
    if (!targetGroupId) return;
    setError("");
    setSuccessMessage("");
    try {
      await assignUserToGroup(targetGroupId, userId);
      setSuccessMessage("User assigned to group successfully!");
      loadData(page);
    } catch (err) {
      setError(err.response?.data?.message || "Error assigning user to group.");
    }
  };

  return (
    <div className="page">
      <h2>Manage Users</h2>
      <p className="hint-text">
        View registered users and assign them to groups. Manage permissions on the{" "}
        <a href="/admin/groups">Group Management</a> page.
      </p>
      {error && <div className="alert alert-error">{error}</div>}
      {successMessage && <div className="alert alert-success">{successMessage}</div>}

      {loading ? (
        <p className="hint-text">Loading users...</p>
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Username</th>
                <th>Email</th>
                <th>Current Group</th>
                <th>Assign Group</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>{u.id}</td>
                  <td>{u.username}</td>
                  <td>{u.email}</td>
                  <td>
                    <span className="badge">{u.groupName ?? "—"}</span>
                  </td>
                  <td>
                    <select
                      defaultValue=""
                      onChange={(e) => {
                        if (e.target.value) handleAssignGroup(u.id, e.target.value);
                        e.target.value = "";
                      }}
                    >
                      <option value="" disabled>
                        Assign to group...
                      </option>
                      {groups.map((g) => (
                        <option key={g.id} value={g.id}>
                          {g.name}
                        </option>
                      ))}
                    </select>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Pagination page={page} totalPages={totalPages} onPageChange={(p) => loadData(p)} />
    </div>
  );
}
