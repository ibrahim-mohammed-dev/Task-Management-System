import { useEffect, useState } from "react";
import { getAllTasksAdmin, deleteTaskAdmin } from "../api/adminApi";
import Pagination from "../components/Pagination";

export default function AdminTasksPage() {
  const [tasks, setTasks] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadTasks = async (pageNo = 0) => {
    setLoading(true);
    setError("");
    try {
      const res = await getAllTasksAdmin({ page: pageNo, size: 10 });
      setTasks(res.data.content);
      setTotalPages(res.data.totalPages);
      setPage(res.data.number);
    } catch (err) {
      setError("Could not load tasks.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTasks(0);
  }, []);

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this task?")) return;
    await deleteTaskAdmin(id);
    loadTasks(page);
  };

  return (
    <div className="page">
      <h2>All tasks</h2>
      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <p className="hint-text">Loading...</p>
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Title</th>
                <th>Description</th>
                <th>Completed</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {tasks.map((t) => (
                <tr key={t.id}>
                  <td>{t.id}</td>
                  <td>{t.title}</td>
                  <td>{t.description}</td>
                  <td>
                    <span className={`badge ${t.completed ? "badge-success" : ""}`}>
                      {t.completed ? "Yes" : "No"}
                    </span>
                  </td>
                  <td>
                    <button
                      className="btn btn-small btn-danger"
                      onClick={() => handleDelete(t.id)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Pagination page={page} totalPages={totalPages} onPageChange={loadTasks} />
    </div>
  );
}
