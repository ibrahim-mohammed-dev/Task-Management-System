export default function TaskItem({ task, onToggle, onEdit, onDelete }) {
  return (
    <div className={`card task-item ${task.completed ? "task-item-done" : ""}`}>
      <label className="task-checkbox">
        <input
          type="checkbox"
          checked={!!task.completed}
          onChange={() => onToggle(task.id)}
        />
      </label>

      <div className="task-item-body">
        <h4>{task.title}</h4>
        {task.description && <p>{task.description}</p>}
      </div>

      <div className="task-item-actions">
        <button className="btn btn-small btn-outline" onClick={() => onEdit(task)}>
          Edit
        </button>
        <button className="btn btn-small btn-danger" onClick={() => onDelete(task.id)}>
          Delete
        </button>
      </div>
    </div>
  );
}
