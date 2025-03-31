// frontend/src/App.js

// Import React library
import React from 'react';
// Import the Login component for authentication
import Login from './components/Login';
// Import the Register component for account creation
import Register from './components/Register';
// Import custom CSS file for minimal App-level styling
import './App.css';

/**
 * App component serves as the root component of the React frontend for Portfolio Heatmap.
 * Temporarily renders both Login and Register components for testing.
 * Will be updated with routing to separate these into distinct paths.
 * 
 * @returns {JSX.Element} The rendered App UI with Login and Register components
 */
function App() {
  // JSX to render the App component
  return (
    // Main container div with a CSS class for styling
    <div className="App">
      {/* Section for login */}
      <div className="auth-section">
        <h2>Login</h2>
        <Login />
      </div>
      {/* Section for registration */}
      <div className="auth-section">
        <h2>Register</h2>
        <Register />
      </div>
    </div>
  );
}

// Export the App component as the default export
export default App;