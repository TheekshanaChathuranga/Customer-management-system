import React from 'react';

const Modal = ({ isOpen, title, message, onConfirm, onCancel, confirmText = 'Confirm', danger = false }) => {
  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onCancel} id="modal-overlay">
      <div className="modal-content slide-up" onClick={(e) => e.stopPropagation()} id="modal-content">
        <h3 className="modal-title">{title}</h3>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', lineHeight: '1.6' }}>{message}</p>
        <div className="modal-actions">
          <button className="btn btn-secondary" onClick={onCancel} id="modal-cancel">Cancel</button>
          <button className={`btn ${danger ? 'btn-danger' : 'btn-primary'}`} onClick={onConfirm} id="modal-confirm">
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
};

export default Modal;
