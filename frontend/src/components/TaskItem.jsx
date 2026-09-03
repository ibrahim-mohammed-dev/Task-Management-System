import { useAuth } from "../context/AuthContext";

export default function TaskItem({ task, onToggle, onEdit, onDelete }) {
  const { hasPermission } = useAuth();

  const canEdit = hasPermission("EDIT_TASK");
  const canDelete = hasPermission("DELETE_TASK");

  return (
    <div className={`card task-item ${task.completed ? "task-item-done" : ""}`}>
      {canEdit && (
        <label className="task-checkbox">
          <input
            type="checkbox"
            checked={!!task.completed}
            onChange={() => onToggle(task.id)}
          />
        </label>
      )}

      <div className="task-item-body">
        <h4>{task.title}</h4>
        {task.description && <p>{task.description}</p>}
      </div>

      <div className="task-item-actions">
        {canEdit && (
          <button className="btn btn-small btn-outline" onClick={() => onEdit(task)}>
            Edit
          </button>
        )}
        {canDelete && (
          <button className="btn btn-small btn-danger" onClick={() => onDelete(task.id)}>
            Delete
          </button>
        )}
      </div>
    </div>
  );
}
