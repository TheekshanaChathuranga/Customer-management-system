import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getCustomer } from '../services/customerApi';
import { formatDate, formatDateTime } from '../utils/validators';
import LoadingSpinner from '../components/LoadingSpinner';
import { toast } from 'react-toastify';

const CustomerViewPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [customer, setCustomer] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      try {
        const res = await getCustomer(id);
        setCustomer(res.data);
      } catch (err) {
        toast.error('Customer not found');
        navigate('/');
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, [id, navigate]);

  if (loading) return <LoadingSpinner />;
  if (!customer) return null;

  return (
    <div className="page-container fade-in">
      <div className="page-header">
        <div>
          <Link to="/" style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>← Back</Link>
          <h1 style={{ marginTop: '0.5rem' }}>{customer.name}</h1>
        </div>
        <button className="btn btn-primary" onClick={() => navigate(`/customers/${id}/edit`)} id="btn-edit">
          ✏️ Edit
        </button>
      </div>

      <div className="section">
        <h3 className="section-title">Basic Information</h3>
        <div className="detail-grid">
          <div className="detail-item"><div className="label">Name</div><div className="value">{customer.name}</div></div>
          <div className="detail-item"><div className="label">NIC</div><div className="value"><code>{customer.nicNumber}</code></div></div>
          <div className="detail-item"><div className="label">Date of Birth</div><div className="value">{formatDate(customer.dateOfBirth)}</div></div>
          <div className="detail-item"><div className="label">Created</div><div className="value">{formatDateTime(customer.createdAt)}</div></div>
        </div>
      </div>

      <div className="section">
        <h3 className="section-title">📱 Mobile Numbers</h3>
        {customer.mobileNumbers?.length > 0 ? (
          <div className="detail-grid">
            {customer.mobileNumbers.map((m, i) => (
              <div className="detail-item" key={i}><div className="label">Phone {i+1}</div><div className="value">{m}</div></div>
            ))}
          </div>
        ) : <p style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>None</p>}
      </div>

      <div className="section">
        <h3 className="section-title">📍 Addresses</h3>
        {customer.addresses?.length > 0 ? (
          <div className="detail-grid">
            {customer.addresses.map((a, i) => (
              <div className="detail-item" key={i}>
                <div className="label">Address {i+1}</div>
                <div className="value">{a.addressLine1}{a.addressLine2 && <><br/>{a.addressLine2}</>}<br/><span style={{color:'var(--text-secondary)'}}>{a.cityName}, {a.countryName}</span></div>
              </div>
            ))}
          </div>
        ) : <p style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>None</p>}
      </div>

      <div className="section">
        <h3 className="section-title">👨‍👩‍👧‍👦 Family Members</h3>
        {customer.familyMembers?.length > 0 ? (
          <div className="detail-grid">
            {customer.familyMembers.map((fm) => (
              <div className="detail-item" key={fm.id} style={{cursor:'pointer'}} onClick={() => navigate(`/customers/${fm.id}`)}>
                <div className="label">Member</div>
                <div className="value">{fm.name}<br/><span style={{color:'var(--text-secondary)',fontSize:'0.8rem'}}>NIC: {fm.nicNumber}</span></div>
              </div>
            ))}
          </div>
        ) : <p style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>None</p>}
      </div>
    </div>
  );
};

export default CustomerViewPage;
