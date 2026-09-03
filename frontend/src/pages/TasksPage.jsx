import { useEffect, useState } from "react";
import { getTasks, createTask, updateTask, deleteTask, toggleTask } from "../api/taskApi";
import { useAuth } from "../context/AuthContext";
import TaskItem from "../components/TaskItem";
import TaskForm from "../components/TaskForm";
import Pagination from "../components/Pagination";

export default function TasksPage() {
  const { hasPermission } = useAuth();
  const [tasks, setTasks] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [showForm, setShowForm] = useState(false);
  const [editingTask, setEditingTask] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const canCreate = hasPermission("CREATE_TASK");

  const loadTasks = async (pageNo = 0) => {
    setLoading(true);
    setError("");
    try {
      const res = await getTasks({ page: pageNo, size: 10, sortBy: "id", sortDir: "desc" });
      setTasks(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
      setPage(res.data.number || 0);
    } catch (err) {
      setError("Could not load your tasks. Please check your permissions.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTasks(0);
  }, []);

  const handleCreate = async (data) => {
    await createTask(data);
    setShowForm(false);
    loadTasks(0);
  };

  const handleUpdate = async (data) => {
    await updateTask(editingTask.id, data);
    setEditingTask(null);
    loadTasks(page);
  };

  const handleToggle = async (id) => {
    await toggleTask(id);
    loadTasks(page);
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this task?")) return;
    await deleteTask(id);
    loadTasks(page);
  };

  return (
    <div className="page">
      <div className="page-header">
        <h2>My tasks</h2>
        {!showForm && !editingTask && canCreate && (
          <button className="btn btn-primary" onClick={() => setShowForm(true)}>
            + New task
          </button>
        )}
      </div>

      {showForm && canCreate && (
        <TaskForm onSubmit={handleCreate} onCancel={() => setShowForm(false)} />
      )}

      {editingTask && hasPermission("EDIT_TASK") && (
        <TaskForm
          initialData={editingTask}
          onSubmit={handleUpdate}
          onCancel={() => setEditingTask(null)}
        />
      )}

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <p className="hint-text">Loading...</p>
      ) : tasks.length === 0 ? (
        <p className="empty-state">
          {canCreate ? "No tasks yet — create your first one above." : "No tasks found."}
        </p>
      ) : (
        <div className="task-list">
          {tasks.map((task) => (
            <TaskItem
              key={task.id}
              task={task}
              onToggle={handleToggle}
              onEdit={(t) => {
                setShowForm(false);
                setEditingTask(t);
              }}
              onDelete={handleDelete}
            />
          ))}
        </div>
      )}

      <Pagination page={page} totalPages={totalPages} onPageChange={loadTasks} />
    </div>
  );
}
