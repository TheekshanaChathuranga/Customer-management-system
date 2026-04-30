import React from 'react';

const LoadingSpinner = ({ message = 'Loading...' }) => (
  <div className="loading-container fade-in" id="loading-spinner">
    <div className="spinner"></div>
    <p>{message}</p>
  </div>
);

export default LoadingSpinner;
