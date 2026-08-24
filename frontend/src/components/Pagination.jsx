export default function Pagination({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null;

  return (
    <div className="pagination">
      <button
        className="btn btn-outline btn-small"
        disabled={page <= 0}
        onClick={() => onPageChange(page - 1)}
      >
        Previous
      </button>
      <span className="pagination-label">
        Page {page + 1} of {totalPages}
      </span>
      <button
        className="btn btn-outline btn-small"
        disabled={page + 1 >= totalPages}
        onClick={() => onPageChange(page + 1)}
      >
        Next
      </button>
    </div>
  );
}
