// frontend/src/App.js

import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Header from './components/Header';
import Footer from './components/Footer';
import Heatmap from './components/Heatmap';
import DetailedChart from './components/DetailedChart';
import About from './components/About';
import PrivacyPolicy from './components/PrivacyPolicy';
import TermsOfService from './components/TermsOfService';
import Help from './components/Help';
import Contact from './components/Contact';
import './App.css';

/**
 * App component serves as the root of the React frontend.
 * Configures routing for all app pages in the Portfolio Heatmap flow.
 * 
 * @returns {JSX.Element} The rendered App UI with routing
 */
function App() {
  return (
    <Router>
      <div className="App">
        <Header />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Navigate to="/heatmap" replace />} />
            <Route path="/heatmap" element={<Heatmap />} />
            <Route path="/chart/:ticker" element={<DetailedChart />} />
            <Route path="/about" element={<About />} />
            <Route path="/privacy" element={<PrivacyPolicy />} />
            <Route path="/terms" element={<TermsOfService />} />
            <Route path="/help" element={<Help />} />
            <Route path="/contact" element={<Contact />} />
          </Routes>
        </main>
        <Footer />
      </div>
    </Router>
  );
}

export default App;