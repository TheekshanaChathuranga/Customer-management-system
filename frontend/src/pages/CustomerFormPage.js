import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import { createCustomer, updateCustomer, getCustomer, getCountries, getCities, getCustomers } from '../services/customerApi';
import { validateCustomerForm, hasErrors } from '../utils/validators';
import LoadingSpinner from '../components/LoadingSpinner';
import { toast } from 'react-toastify';

const CustomerFormPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(id);

  const [form, setForm] = useState({ name: '', dateOfBirth: null, nicNumber: '', mobileNumbers: [''], addresses: [], familyMemberIds: [] });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [countries, setCountries] = useState([]);
  const [citiesMap, setCitiesMap] = useState({});
  const [allCustomers, setAllCustomers] = useState([]);

  useEffect(() => {
    const init = async () => {
      try {
        const [countriesRes, customersRes] = await Promise.all([getCountries(), getCustomers(0, 100)]);
        setCountries(countriesRes.data);
        setAllCustomers(customersRes.data.content || []);

        if (isEdit) {
          setLoading(true);
          const res = await getCustomer(id);
          const c = res.data;
          setForm({
            name: c.name, dateOfBirth: c.dateOfBirth ? new Date(c.dateOfBirth) : null,
            nicNumber: c.nicNumber,
            mobileNumbers: c.mobileNumbers?.length > 0 ? c.mobileNumbers : [''],
            addresses: c.addresses?.map(a => ({ addressLine1: a.addressLine1, addressLine2: a.addressLine2 || '', cityId: a.cityId, countryId: a.countryId })) || [],
            familyMemberIds: c.familyMembers?.map(f => f.id) || [],
          });
          // Load cities for existing addresses
          const uniqueCountryIds = [...new Set(c.addresses?.map(a => a.countryId) || [])];
          const cMap = {};
          for (const cid of uniqueCountryIds) {
            const r = await getCities(cid);
            cMap[cid] = r.data;
          }
          setCitiesMap(cMap);
          setLoading(false);
        }
      } catch (err) {
        toast.error('Failed to load data');
        setLoading(false);
      }
    };
    init();
  }, [id, isEdit]);

  const loadCities = async (countryId) => {
    if (!citiesMap[countryId]) {
      const res = await getCities(countryId);
      setCitiesMap(prev => ({ ...prev, [countryId]: res.data }));
    }
  };

  const handleChange = (field, value) => {
    setForm(prev => ({ ...prev, [field]: value }));
    if (errors[field]) setErrors(prev => { const e = {...prev}; delete e[field]; return e; });
  };

  const handleMobileChange = (idx, value) => {
    const updated = [...form.mobileNumbers];
    updated[idx] = value;
    setForm(prev => ({ ...prev, mobileNumbers: updated }));
  };

  const addMobile = () => setForm(prev => ({ ...prev, mobileNumbers: [...prev.mobileNumbers, ''] }));
  const removeMobile = (idx) => setForm(prev => ({ ...prev, mobileNumbers: prev.mobileNumbers.filter((_, i) => i !== idx) }));

  const addAddress = () => setForm(prev => ({ ...prev, addresses: [...prev.addresses, { addressLine1: '', addressLine2: '', cityId: '', countryId: '' }] }));
  const removeAddress = (idx) => setForm(prev => ({ ...prev, addresses: prev.addresses.filter((_, i) => i !== idx) }));
  const handleAddressChange = (idx, field, value) => {
    const updated = [...form.addresses];
    updated[idx] = { ...updated[idx], [field]: value };
    if (field === 'countryId' && value) { updated[idx].cityId = ''; loadCities(value); }
    setForm(prev => ({ ...prev, addresses: updated }));
  };

  const toggleFamily = (custId) => {
    setForm(prev => ({
      ...prev,
      familyMemberIds: prev.familyMemberIds.includes(custId)
        ? prev.familyMemberIds.filter(fid => fid !== custId)
        : [...prev.familyMemberIds, custId],
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const formData = { ...form, dateOfBirth: form.dateOfBirth ? form.dateOfBirth.toISOString().split('T')[0] : null };
    const validationErrors = validateCustomerForm(formData);
    if (hasErrors(validationErrors)) { setErrors(validationErrors); return; }

    setSaving(true);
    try {
      const payload = {
        ...formData,
        mobileNumbers: formData.mobileNumbers.filter(m => m.trim()),
        addresses: formData.addresses.map(a => ({ ...a, cityId: Number(a.cityId), countryId: Number(a.countryId) })),
      };
      if (isEdit) {
        await updateCustomer(id, payload);
        toast.success('Customer updated!');
      } else {
        await createCustomer(payload);
        toast.success('Customer created!');
      }
      navigate('/');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to save');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div className="page-container fade-in">
      <div className="page-header">
        <div>
          <Link to="/" style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>← Back</Link>
          <h1 style={{ marginTop: '0.5rem' }}>{isEdit ? 'Edit Customer' : 'New Customer'}</h1>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="card" style={{ maxWidth: 800 }}>
        {/* Basic Info */}
        <div className="section">
          <h3 className="section-title">Basic Information</h3>
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Name *</label>
              <input className={`form-input ${errors.name ? 'error' : ''}`} value={form.name} onChange={e => handleChange('name', e.target.value)} placeholder="Full name" id="input-name" />
              {errors.name && <p className="form-error">{errors.name}</p>}
            </div>
            <div className="form-group">
              <label className="form-label">NIC Number *</label>
              <input className={`form-input ${errors.nicNumber ? 'error' : ''}`} value={form.nicNumber} onChange={e => handleChange('nicNumber', e.target.value)} placeholder="NIC number" id="input-nic" />
              {errors.nicNumber && <p className="form-error">{errors.nicNumber}</p>}
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">Date of Birth *</label>
            <DatePicker selected={form.dateOfBirth} onChange={date => handleChange('dateOfBirth', date)}
              dateFormat="yyyy-MM-dd" className={`form-input ${errors.dateOfBirth ? 'error' : ''}`}
              placeholderText="Select date" showYearDropdown dropdownMode="select" maxDate={new Date()} id="input-dob" />
            {errors.dateOfBirth && <p className="form-error">{errors.dateOfBirth}</p>}
          </div>
        </div>

        {/* Mobile Numbers */}
        <div className="section">
          <h3 className="section-title">📱 Mobile Numbers <button type="button" className="btn btn-sm btn-secondary" onClick={addMobile}>+ Add</button></h3>
          <div className="dynamic-list">
            {form.mobileNumbers.map((m, i) => (
              <div className="dynamic-list-item" key={i}>
                <input className="form-input" value={m} onChange={e => handleMobileChange(i, e.target.value)} placeholder="Mobile number" />
                {form.mobileNumbers.length > 1 && <button type="button" className="btn-icon" onClick={() => removeMobile(i)} style={{color:'var(--danger)'}}>✕</button>}
              </div>
            ))}
          </div>
        </div>

        {/* Addresses */}
        <div className="section">
          <h3 className="section-title">📍 Addresses <button type="button" className="btn btn-sm btn-secondary" onClick={addAddress}>+ Add</button></h3>
          {form.addresses.map((addr, i) => (
            <div key={i} className="card" style={{ marginBottom: '0.75rem', padding: '1rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Address {i + 1}</span>
                <button type="button" className="btn-icon btn-sm" onClick={() => removeAddress(i)} style={{color:'var(--danger)'}}>✕</button>
              </div>
              <div className="form-row">
                <div className="form-group"><label className="form-label">Line 1 *</label><input className="form-input" value={addr.addressLine1} onChange={e => handleAddressChange(i, 'addressLine1', e.target.value)} /></div>
                <div className="form-group"><label className="form-label">Line 2</label><input className="form-input" value={addr.addressLine2} onChange={e => handleAddressChange(i, 'addressLine2', e.target.value)} /></div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">Country *</label>
                  <select className="form-select" value={addr.countryId} onChange={e => handleAddressChange(i, 'countryId', e.target.value)}>
                    <option value="">Select country</option>
                    {countries.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">City *</label>
                  <select className="form-select" value={addr.cityId} onChange={e => handleAddressChange(i, 'cityId', e.target.value)} disabled={!addr.countryId}>
                    <option value="">Select city</option>
                    {(citiesMap[addr.countryId] || []).map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                  </select>
                </div>
              </div>
            </div>
          ))}
          {form.addresses.length === 0 && <p style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>No addresses added</p>}
        </div>

        {/* Family Members */}
        <div className="section">
          <h3 className="section-title">👨‍👩‍👧‍👦 Family Members</h3>
          {allCustomers.filter(c => String(c.id) !== String(id)).length > 0 ? (
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
              {allCustomers.filter(c => String(c.id) !== String(id)).map(c => (
                <button key={c.id} type="button"
                  className={`btn btn-sm ${form.familyMemberIds.includes(c.id) ? 'btn-primary' : 'btn-secondary'}`}
                  onClick={() => toggleFamily(c.id)}>
                  {c.name} ({c.nicNumber})
                </button>
              ))}
            </div>
          ) : <p style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>No other customers to link</p>}
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end', paddingTop: '1rem', borderTop: '1px solid var(--border-color)' }}>
          <Link to="/" className="btn btn-secondary">Cancel</Link>
          <button type="submit" className="btn btn-primary" disabled={saving} id="btn-save">
            {saving ? <><span className="spinner spinner-sm"></span> Saving...</> : isEdit ? '💾 Update' : '➕ Create'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default CustomerFormPage;
