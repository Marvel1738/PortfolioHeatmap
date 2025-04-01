// frontend/src/App.js

// Import React library
import React from 'react';
// Import routing components
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
// Import page components
import Login from './components/Login';
import Register from './components/Register';
import CreatePortfolio from './components/CreatePortfolio';
// Import App-level CSS
import './App.css';

/**
 * App component serves as the root of the React frontend.
 * Configures routing for login, registration, and portfolio creation pages.
 * 
 * @returns {JSX.Element} The rendered App UI with routing
 */
function App() {
  return (
    // Router enables navigation
    <Router>
      {/* Main container div */}
      <div className="App">
        {/* Routes map URLs to components */}
        <Routes>
          {/* Login page */}
          <Route path="/login" element={<Login />} />
          {/* Register page */}
          <Route path="/register" element={<Register />} />
          {/* Portfolio creation page */}
          <Route path="/create-portfolio" element={<CreatePortfolio />} />
          {/* Default route redirects to login */}
          <Route path="/" element={<Login />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;