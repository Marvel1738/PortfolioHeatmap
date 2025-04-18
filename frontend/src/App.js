// frontend/src/App.js

import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import Heatmap from './components/Heatmap';
import Header from './components/Header';
import About from './components/About';
import DetailedChart from './components/DetailedChart';
import Footer from './components/Footer';
import './App.css';

/**
 * App content component to handle location-based class names
 */
function AppContent() {
  const location = useLocation();
  const isHeatmapPage = location.pathname === '/heatmap';

  return (
    <div className={`App ${isHeatmapPage ? 'heatmap-page' : ''}`}>
      <Header />
      <main className="main-content">
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/heatmap" element={<Heatmap />} />
          <Route path="/chart/:ticker" element={<DetailedChart />} />
          <Route path="/about" element={<About />} />
          <Route path="/privacy" element={<About />} /> {/* Placeholder until Privacy page is created */}
          <Route path="/terms" element={<About />} /> {/* Placeholder until Terms page is created */}
          <Route path="/help" element={<About />} /> {/* Placeholder until Help page is created */}
          <Route path="/" element={<Navigate to="/heatmap" replace />} />
        </Routes>
      </main>
      <Footer />
    </div>
  );
}

/**
 * App component serves as the root of the React frontend.
 * Configures routing for all app pages in the Portfolio Heatmap flow.
 * 
 * @returns {JSX.Element} The rendered App UI with routing
 */
function App() {
  return (
    <Router>
      <AppContent />
    </Router>
  );
}

export default App;