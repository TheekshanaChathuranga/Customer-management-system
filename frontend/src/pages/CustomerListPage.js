import React, { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getCustomers, deleteCustomer } from '../services/customerApi';
import { formatDate } from '../utils/validators';
import Pagination from '../components/Pagination';
import LoadingSpinner from '../components/LoadingSpinner';
import Modal from '../components/Modal';
import { toast } from 'react-toastify';

const CustomerListPage = () => {
  const navigate = useNavigate();
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [deleteModal, setDeleteModal] = useState({ open: false, id: null, name: '' });
  const [searchTimeout, setSearchTimeout] = useState(null);

  const fetchCustomers = useCallback(async (currentPage, currentSearch) => {
    setLoading(true);
    try {
      const res = await getCustomers(currentPage, 10, currentSearch);
      setCustomers(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
      setTotalElements(res.data.totalElements || 0);
    } catch (err) {
      toast.error('Failed to load customers');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchCustomers(page, search);
  }, [page, fetchCustomers]); // eslint-disable-line

  const handleSearch = (value) => {
    setSearch(value);
    if (searchTimeout) clearTimeout(searchTimeout);
    const timeout = setTimeout(() => {
      setPage(0);
      fetchCustomers(0, value);
    }, 400);
    setSearchTimeout(timeout);
  };

  const handleDelete = async () => {
    try {
      await deleteCustomer(deleteModal.id);
      toast.success('Customer deleted successfully');
      setDeleteModal({ open: false, id: null, name: '' });
      fetchCustomers(page, search);
    } catch (err) {
      toast.error('Failed to delete customer');
    }
  };

  return (
    <div className="page-container fade-in">
      <div className="page-header">
        <div>
          <h1>Customers</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '0.25rem' }}>
            {totalElements} total customer{totalElements !== 1 ? 's' : ''}
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <div className="search-bar">
            <span className="search-icon">🔍</span>
            <input
              type="text"
              className="form-input"
              placeholder="Search by name or NIC..."
              value={search}
              onChange={(e) => handleSearch(e.target.value)}
              id="search-input"
            />
          </div>
          <Link to="/customers/new" className="btn btn-primary" id="btn-new-customer">
            ➕ New Customer
          </Link>
        </div>
      </div>

      {loading ? (
        <LoadingSpinner message="Loading customers..." />
      ) : customers.length === 0 ? (
        <div className="empty-state card">
          <div className="icon">👥</div>
          <h3>No customers found</h3>
          <p>{search ? 'Try adjusting your search' : 'Create your first customer to get started'}</p>
          {!search && (
            <Link to="/customers/new" className="btn btn-primary" style={{ marginTop: '1rem' }}>
              ➕ Create Customer
            </Link>
          )}
        </div>
      ) : (
        <>
          <div className="table-container slide-up">
            <table className="data-table" id="customers-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Name</th>
                  <th>NIC Number</th>
                  <th>Date of Birth</th>
                  <th>Created</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {customers.map((c, idx) => (
                  <tr key={c.id}>
                    <td style={{ color: 'var(--text-muted)' }}>{page * 10 + idx + 1}</td>
                    <td>
                      <span style={{ fontWeight: 600 }}>{c.name}</span>
                    </td>
                    <td>
                      <code style={{
                        background: 'var(--bg-input)',
                        padding: '0.2rem 0.5rem',
                        borderRadius: '4px',
                        fontSize: '0.8rem'
                      }}>
                        {c.nicNumber}
                      </code>
                    </td>
                    <td>{formatDate(c.dateOfBirth)}</td>
                    <td style={{ color: 'var(--text-secondary)', fontSize: '0.8rem' }}>
                      {formatDate(c.createdAt)}
                    </td>
                    <td>
                      <div className="actions">
                        <button
                          className="btn-icon"
                          title="View"
                          onClick={() => navigate(`/customers/${c.id}`)}
                          id={`btn-view-${c.id}`}
                        >👁️</button>
                        <button
                          className="btn-icon"
                          title="Edit"
                          onClick={() => navigate(`/customers/${c.id}/edit`)}
                          id={`btn-edit-${c.id}`}
                        >✏️</button>
                        <button
                          className="btn-icon"
                          title="Delete"
                          onClick={() => setDeleteModal({ open: true, id: c.id, name: c.name })}
                          id={`btn-delete-${c.id}`}
                          style={{ color: 'var(--danger)' }}
                        >🗑️</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}

      <Modal
        isOpen={deleteModal.open}
        title="Delete Customer"
        message={`Are you sure you want to delete "${deleteModal.name}"? This action cannot be undone.`}
        onConfirm={handleDelete}
        onCancel={() => setDeleteModal({ open: false, id: null, name: '' })}
        confirmText="Delete"
        danger
      />
    </div>
  );
};

export default CustomerListPage;
