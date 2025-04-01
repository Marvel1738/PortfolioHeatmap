// frontend/src/App.js

import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import CreatePortfolio from './components/CreatePortfolio';
import Preview from './components/Preview'; // New
import Loading from './components/Loading'; // New
import Heatmap from './components/Heatmap'; // New
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
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/create-portfolio" element={<CreatePortfolio />} />
          <Route path="/preview" element={<Preview />} />
          <Route path="/loading" element={<Loading />} />
          <Route path="/heatmap" element={<Heatmap />} />
          <Route path="/" element={<Login />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;