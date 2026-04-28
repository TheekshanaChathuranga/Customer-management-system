import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import './Navbar.css';

const Navbar = () => {
  const location = useLocation();

  const isActive = (path) => {
    if (path === '/' && location.pathname === '/') return true;
    if (path !== '/' && location.pathname.startsWith(path)) return true;
    return false;
  };

  return (
    <nav className="navbar" id="main-navbar">
      <div className="navbar-inner">
        <Link to="/" className="navbar-brand">
          <span className="brand-icon">👥</span>
          <span className="brand-text">Customer<span className="brand-accent">Hub</span></span>
        </Link>
        <div className="navbar-links">
          <Link to="/" className={`nav-link ${isActive('/') && !isActive('/customers/new') && !isActive('/bulk-upload') ? 'active' : ''}`} id="nav-customers">
            <span className="nav-icon">📋</span> Customers
          </Link>
          <Link to="/customers/new" className={`nav-link ${isActive('/customers/new') ? 'active' : ''}`} id="nav-new-customer">
            <span className="nav-icon">➕</span> New Customer
          </Link>
          <Link to="/bulk-upload" className={`nav-link ${isActive('/bulk-upload') ? 'active' : ''}`} id="nav-bulk-upload">
            <span className="nav-icon">📤</span> Bulk Upload
          </Link>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
