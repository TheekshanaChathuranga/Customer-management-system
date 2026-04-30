import axios from 'axios';

const API_BASE = process.env.REACT_APP_API_URL || '/api';

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
});

// ---- Customers ----
export const getCustomers = (page = 0, size = 10, search = '', sortBy = 'id', sortDir = 'desc') =>
  api.get('/customers', { params: { page, size, search, sortBy, sortDir } });

export const getCustomer = (id) => api.get(`/customers/${id}`);

export const createCustomer = (data) => api.post('/customers', data);

export const updateCustomer = (id, data) => api.put(`/customers/${id}`, data);

export const deleteCustomer = (id) => api.delete(`/customers/${id}`);

// ---- Bulk Upload ----
export const uploadBulkFile = (file) => {
  const formData = new FormData();
  formData.append('file', file);
  return api.post('/bulk-upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 300000, // 5 min timeout for large files
  });
};

export const getBulkUploadStatus = (jobId) => api.get(`/bulk-upload/${jobId}`);

// ---- Master Data ----
export const getCountries = () => api.get('/master/countries');

export const getCities = (countryId) =>
  api.get('/master/cities', { params: countryId ? { countryId } : {} });

export default api;
