// frontend/src/App.js

import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import Heatmap from './components/Heatmap';
import Header from './components/Header';
import Predictions from './components/Predictions';
import About from './components/About';
import DetailedChart from './components/DetailedChart';
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
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/heatmap" element={<Heatmap />} />
            <Route path="/predictions" element={<Predictions />} />
            <Route path="/chart/:ticker" element={<DetailedChart />} />
            <Route path="/" element={<Navigate to="/heatmap" replace />} />
            <Route path="/about" element={<About/>} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;