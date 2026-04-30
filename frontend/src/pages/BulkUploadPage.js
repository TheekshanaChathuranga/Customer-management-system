import React, { useState, useRef, useEffect } from 'react';
import { uploadBulkFile, getBulkUploadStatus } from '../services/customerApi';
import { BULK_STATUS, EXCEL_COLUMNS } from '../utils/constants';
import { toast } from 'react-toastify';

const BulkUploadPage = () => {
  const [file, setFile] = useState(null);
  const [dragover, setDragover] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [job, setJob] = useState(null);
  const [polling, setPolling] = useState(false);
  const fileRef = useRef(null);
  const pollRef = useRef(null);

  useEffect(() => { return () => { if (pollRef.current) clearInterval(pollRef.current); }; }, []);

  const handleDrop = (e) => { e.preventDefault(); setDragover(false); const f = e.dataTransfer.files[0]; if (f) setFile(f); };
  const handleDragOver = (e) => { e.preventDefault(); setDragover(true); };
  const handleDragLeave = () => setDragover(false);
  const handleFileSelect = (e) => { if (e.target.files[0]) setFile(e.target.files[0]); };

  const startPolling = (jobId) => {
    setPolling(true);
    pollRef.current = setInterval(async () => {
      try {
        const res = await getBulkUploadStatus(jobId);
        setJob(res.data);
        if (res.data.status === BULK_STATUS.COMPLETED || res.data.status === BULK_STATUS.FAILED) {
          clearInterval(pollRef.current);
          setPolling(false);
          if (res.data.status === BULK_STATUS.COMPLETED) toast.success(`Upload complete! ${res.data.successCount} records created.`);
          else toast.error('Upload failed. Check error log.');
        }
      } catch { clearInterval(pollRef.current); setPolling(false); }
    }, 2000);
  };

  const handleUpload = async () => {
    if (!file) return;
    setUploading(true);
    try {
      const res = await uploadBulkFile(file);
      setJob(res.data);
      toast.info('Upload started! Processing...');
      startPolling(res.data.jobId);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  const progress = job && job.totalRecords > 0 ? Math.round((job.processed / job.totalRecords) * 100) : 0;

  return (
    <div className="page-container fade-in">
      <div className="page-header">
        <div>
          <h1>Bulk Upload</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '0.25rem' }}>
            Upload an Excel file to create customers in bulk
          </p>
        </div>
      </div>

      {/* Excel Format Info */}
      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <h3 className="section-title">📋 Excel Format</h3>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '0.75rem' }}>
          Your .xlsx file should have the following columns in order:
        </p>
        <div className="table-container" style={{ border: 'none' }}>
          <table className="data-table">
            <thead><tr>{EXCEL_COLUMNS.map((col, i) => <th key={i}>{col}</th>)}</tr></thead>
            <tbody><tr><td>John Doe</td><td>1990-01-15</td><td>199012345678</td><td>0771234567</td></tr></tbody>
          </table>
        </div>
      </div>

      {/* Upload Zone */}
      <div className={`upload-zone ${dragover ? 'dragover' : ''}`}
           onDrop={handleDrop} onDragOver={handleDragOver} onDragLeave={handleDragLeave}
           onClick={() => fileRef.current?.click()} id="upload-zone">
        <div className="upload-icon">📂</div>
        {file ? (
          <div>
            <p style={{ color: 'var(--text-primary)', fontWeight: 600 }}>{file.name}</p>
            <p style={{ fontSize: '0.8rem' }}>{(file.size / 1024 / 1024).toFixed(2)} MB</p>
          </div>
        ) : (
          <div>
            <p style={{ fontWeight: 600, color: 'var(--text-primary)' }}>Drop your Excel file here</p>
            <p>or click to browse (.xlsx files only)</p>
          </div>
        )}
        <input ref={fileRef} type="file" accept=".xlsx,.xls" onChange={handleFileSelect} style={{ display: 'none' }} id="file-input" />
      </div>

      {file && !job && (
        <div style={{ textAlign: 'center', marginTop: '1rem' }}>
          <button className="btn btn-primary" onClick={handleUpload} disabled={uploading} id="btn-upload">
            {uploading ? <><span className="spinner spinner-sm"></span> Uploading...</> : '🚀 Start Upload'}
          </button>
        </div>
      )}

      {/* Progress */}
      {job && (
        <div className="card slide-up" style={{ marginTop: '1.5rem' }}>
          <h3 className="section-title">
            Upload Progress
            <span className={`badge ${job.status === BULK_STATUS.COMPLETED ? 'badge-success' : job.status === BULK_STATUS.FAILED ? 'badge-danger' : 'badge-info'}`}>
              {job.status}
            </span>
          </h3>

          <div className="progress-bar" style={{ marginBottom: '1rem' }}>
            <div className="progress-fill" style={{ width: `${progress}%` }}></div>
          </div>

          <div className="detail-grid">
            <div className="detail-item"><div className="label">Total Records</div><div className="value">{job.totalRecords.toLocaleString()}</div></div>
            <div className="detail-item"><div className="label">Processed</div><div className="value">{job.processed.toLocaleString()} ({progress}%)</div></div>
            <div className="detail-item"><div className="label">Success</div><div className="value" style={{color:'var(--success)'}}>{job.successCount.toLocaleString()}</div></div>
            <div className="detail-item"><div className="label">Failed</div><div className="value" style={{color:'var(--danger)'}}>{job.failCount.toLocaleString()}</div></div>
          </div>

          {job.errorLog && (
            <div style={{ marginTop: '1rem' }}>
              <h4 style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.5rem' }}>Error Log</h4>
              <pre style={{ background: 'var(--bg-input)', padding: '1rem', borderRadius: 'var(--radius-sm)', fontSize: '0.75rem', color: 'var(--danger)', maxHeight: '200px', overflow: 'auto', whiteSpace: 'pre-wrap' }}>
                {job.errorLog}
              </pre>
            </div>
          )}

          {(job.status === BULK_STATUS.COMPLETED || job.status === BULK_STATUS.FAILED) && (
            <div style={{ textAlign: 'center', marginTop: '1rem' }}>
              <button className="btn btn-secondary" onClick={() => { setJob(null); setFile(null); }}>Upload Another File</button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default BulkUploadPage;
