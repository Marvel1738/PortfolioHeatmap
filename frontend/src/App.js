// frontend/src/App.js

import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import Heatmap from './components/Heatmap';
import CreatePortfolio from './components/CreatePortfolio';
import Preview from './components/Preview';
import Header from './components/Header';
import Predictions from './components/Predictions';
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
            <Route path="/create-portfolio" element={<CreatePortfolio />} />
            <Route path="/preview" element={<Preview />} />
            <Route path="/predictions" element={<Predictions />} />
            <Route path="/" element={<Navigate to="/heatmap" replace />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;