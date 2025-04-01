// frontend/src/components/Loading.js

import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Loading.css';

/**
 * Loading component displays a spinner animation before redirecting to heatmap.
 * 
 * @returns {JSX.Element} The rendered loading UI
 */
function Loading() {
  const navigate = useNavigate();

  // Auto-redirect to heatmap after 2 seconds
  useEffect(() => {
    const timer = setTimeout(() => {
      navigate('/heatmap');
    }, 2000);
    return () => clearTimeout(timer);
  }, [navigate]);

  return (
    <div className="loading">
      <h1>Generating Your Heatmap</h1>
      <div className="spinner"></div>
    </div>
  );
}

export default Loading;