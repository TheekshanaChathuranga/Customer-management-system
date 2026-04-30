import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import Navbar from './components/Navbar';
import CustomerListPage from './pages/CustomerListPage';
import CustomerFormPage from './pages/CustomerFormPage';
import CustomerViewPage from './pages/CustomerViewPage';
import BulkUploadPage from './pages/BulkUploadPage';

function App() {
  return (
    <Router>
      <Navbar />
      <Routes>
        <Route path="/" element={<CustomerListPage />} />
        <Route path="/customers/new" element={<CustomerFormPage />} />
        <Route path="/customers/:id" element={<CustomerViewPage />} />
        <Route path="/customers/:id/edit" element={<CustomerFormPage />} />
        <Route path="/bulk-upload" element={<BulkUploadPage />} />
      </Routes>
      <ToastContainer position="top-right" autoClose={3000} theme="dark"
        toastStyle={{ background: '#1a1a3e', border: '1px solid rgba(255,255,255,0.08)' }} />
    </Router>
  );
}

export default App;
