import React, { useState, useEffect } from 'react';
import { createRoot } from 'react-dom/client';
import './popup.css';

function Popup() {
  const [status, setStatus] = useState('loading...');
  const [error, setError] = useState(null);

  useEffect(() => {
    fetch('http://localhost:8080/api/health')
      .then((res) => res.json())
      .then((data) => setStatus(data.status))
      .catch((err) => setError(err.message));
  }, []);

  return (
    <div style={{ padding: '20px', width: '300px' }}>
      <h2>Oil On Canvas</h2>
      <p>Backend: {error ? <span style={{ color: 'red' }}>Error: {error}</span> : status}</p>
    </div>
  );
}

const root = createRoot(document.getElementById('root'));
root.render(<Popup />);
