import React from 'react';

const Pagination = ({ currentPage, totalPages, onPageChange }) => {
  if (totalPages <= 1) return null;

  const getPageNumbers = () => {
    const pages = [];
    const maxVisible = 5;
    let start = Math.max(0, currentPage - Math.floor(maxVisible / 2));
    let end = Math.min(totalPages, start + maxVisible);

    if (end - start < maxVisible) {
      start = Math.max(0, end - maxVisible);
    }

    for (let i = start; i < end; i++) {
      pages.push(i);
    }
    return pages;
  };

  return (
    <div className="pagination" id="pagination">
      <button disabled={currentPage === 0} onClick={() => onPageChange(0)} title="First page">«</button>
      <button disabled={currentPage === 0} onClick={() => onPageChange(currentPage - 1)} title="Previous">‹</button>
      {getPageNumbers().map((page) => (
        <button
          key={page}
          className={page === currentPage ? 'active' : ''}
          onClick={() => onPageChange(page)}
        >
          {page + 1}
        </button>
      ))}
      <button disabled={currentPage >= totalPages - 1} onClick={() => onPageChange(currentPage + 1)} title="Next">›</button>
      <button disabled={currentPage >= totalPages - 1} onClick={() => onPageChange(totalPages - 1)} title="Last page">»</button>
      <span className="pagination-info">Page {currentPage + 1} of {totalPages}</span>
    </div>
  );
};

export default Pagination;
