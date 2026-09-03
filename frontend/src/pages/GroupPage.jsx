import { useState, useEffect } from "react";
import {
  createGroup,
  createPermission,
  assignUserToGroup,
  removeUserFromGroup,
  addPermissionToGroup,
  getAllGroups,
  getAllPermissions,
} from "../api/groupApi";
import { getAllUsers } from "../api/adminApi";

export default function GroupsPage() {
  const [groups, setGroups] = useState([]);
  const [users, setUsers] = useState([]);
  const [permissions, setPermissions] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  // Create Group
  const [newGroupName, setNewGroupName] = useState("");

  // Create Permission
  const [newPermissionName, setNewPermissionName] = useState("");

  // Assign User to Group
  const [assignGroupId, setAssignGroupId] = useState("");
  const [assignUserId, setAssignUserId] = useState("");

  // Remove User from Group
  const [removeGroupId, setRemoveGroupId] = useState("");
  const [removeUserId, setRemoveUserId] = useState("");

  // Add Permission to Group
  const [permGroupId, setPermGroupId] = useState("");
  const [permId, setPermId] = useState("");

  const loadData = async () => {
    setLoading(true);
    setError("");
    try {
      const [groupsRes, usersRes, permRes] = await Promise.all([
        getAllGroups({ page: 0, size: 100 }),
        getAllUsers({ page: 0, size: 100 }),
        getAllPermissions({ page: 0, size: 100 }),
      ]);

      setGroups(groupsRes.data.content || groupsRes.data || []);
      setUsers(usersRes.data.content || usersRes.data || []);
      setPermissions(permRes.data.content || permRes.data || []);
    } catch (err) {
      setError("Could not load groups, users, or permissions data.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const clearMessages = () => {
    setError("");
    setSuccessMessage("");
  };

  const handleCreateGroup = async (e) => {
    e.preventDefault();
    if (!newGroupName.trim()) return;
    clearMessages();
    try {
      await createGroup({ name: newGroupName.trim() });
      setNewGroupName("");
      setSuccessMessage(`Group "${newGroupName}" created successfully!`);
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to create group.");
    }
  };

  const handleCreatePermission = async (e) => {
    e.preventDefault();
    if (!newPermissionName.trim()) return;
    clearMessages();
    try {
      await createPermission({ name: newPermissionName.trim() });
      setNewPermissionName("");
      setSuccessMessage(`Permission "${newPermissionName}" created successfully!`);
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to create permission.");
    }
  };

  const handleAssignUser = async (e) => {
    e.preventDefault();
    if (!assignGroupId || !assignUserId) return;
    clearMessages();
    try {
      await assignUserToGroup(assignGroupId, assignUserId);
      setSuccessMessage("User assigned to group successfully!");
      setAssignGroupId("");
      setAssignUserId("");
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to assign user to group.");
    }
  };

  const handleRemoveUser = async (e) => {
    e.preventDefault();
    if (!removeGroupId || !removeUserId) return;
    clearMessages();
    try {
      await removeUserFromGroup(removeGroupId, removeUserId);
      setSuccessMessage("User removed from group successfully!");
      setRemoveGroupId("");
      setRemoveUserId("");
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to remove user from group.");
    }
  };

  const handleAddPermission = async (e) => {
    e.preventDefault();
    if (!permGroupId || !permId) return;
    clearMessages();
    try {
      await addPermissionToGroup(permGroupId, permId);
      setSuccessMessage("Permission added to group successfully!");
      setPermGroupId("");
      setPermId("");
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to add permission to group.");
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h2>Group & Permission Management</h2>
        <p className="hint-text">
          Create groups and permissions, assign users to groups, and grant permissions.
        </p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {successMessage && <div className="alert alert-success">{successMessage}</div>}

      {loading ? (
        <p className="hint-text">Loading management data...</p>
      ) : (
        <div className="group-management-container">
          {/* Create Group Form */}
          <div className="card mb-4">
            <h3>Create New Group</h3>
            <form onSubmit={handleCreateGroup} className="inline-form">
              <input
                type="text"
                placeholder="Group Name (e.g. MANAGERS)"
                value={newGroupName}
                onChange={(e) => setNewGroupName(e.target.value)}
                required
              />
              <button type="submit" className="btn btn-primary">+ Create Group</button>
            </form>
          </div>

          {/* Create Permission Form */}
          <div className="card mb-4">
            <h3>Create New Permission</h3>
            <form onSubmit={handleCreatePermission} className="inline-form">
              <input
                type="text"
                placeholder="Permission Name (e.g. EDIT_PROJECTS)"
                value={newPermissionName}
                onChange={(e) => setNewPermissionName(e.target.value)}
                required
              />
              <button type="submit" className="btn btn-primary">+ Create Permission</button>
            </form>
          </div>

          {/* Assign User to Group Form */}
          <div className="card mb-4">
            <h3>Assign User to Group</h3>
            <form onSubmit={handleAssignUser} className="inline-form">
              <select
                value={assignGroupId}
                onChange={(e) => setAssignGroupId(e.target.value)}
                required
              >
                <option value="">Select Group</option>
                {groups.map((g) => (
                  <option key={g.id} value={g.id}>
                    {g.name} (ID: {g.id})
                  </option>
                ))}
              </select>

              <select
                value={assignUserId}
                onChange={(e) => setAssignUserId(e.target.value)}
                required
              >
                <option value="">Select User</option>
                {users.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.username} ({u.email})
                  </option>
                ))}
              </select>

              <button type="submit" className="btn btn-primary">Assign User</button>
            </form>
          </div>

          {/* Remove User from Group Form */}
          <div className="card mb-4">
            <h3>Remove User from Group</h3>
            <form onSubmit={handleRemoveUser} className="inline-form">
              <select
                value={removeGroupId}
                onChange={(e) => setRemoveGroupId(e.target.value)}
                required
              >
                <option value="">Select Group</option>
                {groups.map((g) => (
                  <option key={g.id} value={g.id}>
                    {g.name} (ID: {g.id})
                  </option>
                ))}
              </select>

              <select
                value={removeUserId}
                onChange={(e) => setRemoveUserId(e.target.value)}
                required
              >
                <option value="">Select User</option>
                {users.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.username} ({u.email})
                  </option>
                ))}
              </select>

              <button type="submit" className="btn btn-danger">Remove User</button>
            </form>
          </div>

          {/* Add Permission to Group Form */}
          <div className="card mb-4">
            <h3>Add Permission to Group</h3>
            <form onSubmit={handleAddPermission} className="inline-form">
              <select
                value={permGroupId}
                onChange={(e) => setPermGroupId(e.target.value)}
                required
              >
                <option value="">Select Group</option>
                {groups.map((g) => (
                  <option key={g.id} value={g.id}>
                    {g.name} (ID: {g.id})
                  </option>
                ))}
              </select>

              <select
                value={permId}
                onChange={(e) => setPermId(e.target.value)}
                required
              >
                <option value="">Select Permission</option>
                {permissions.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name} (ID: {p.id})
                  </option>
                ))}
              </select>

              <button type="submit" className="btn btn-primary">Add Permission</button>
            </form>
          </div>

          {/* Existing Groups & Permissions Overview */}
          <div className="card mb-4">
            <h3>Existing Groups</h3>
            {groups.length === 0 ? (
              <p className="hint-text">No groups found.</p>
            ) : (
              <div className="table-wrap">
                <table className="table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Group Name</th>
                    </tr>
                  </thead>
                  <tbody>
                    {groups.map((g) => (
                      <tr key={g.id}>
                        <td>{g.id}</td>
                        <td>
                          <span className="badge">{g.name}</span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          <div className="card">
            <h3>Existing Permissions</h3>
            {permissions.length === 0 ? (
              <p className="hint-text">No permissions found.</p>
            ) : (
              <div className="table-wrap">
                <table className="table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Permission Name</th>
                    </tr>
                  </thead>
                  <tbody>
                    {permissions.map((p) => (
                      <tr key={p.id}>
                        <td>{p.id}</td>
                        <td>
                          <span className="badge">{p.name}</span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}