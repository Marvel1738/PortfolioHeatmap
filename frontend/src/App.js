// frontend/src/App.js

// Import React library for building the UI
import React from 'react';
// Import the Login component, which handles authentication
import Login from './components/Login';
// Import custom CSS file for minimal App-level styling (optional)
import './App.css';

/**
 * App component serves as the root component of the React frontend for Portfolio Heatmap.
 * Acts as a container for rendering other components and will eventually manage routing.
 * Currently renders the Login component as the initial view for user authentication.
 * 
 * @returns {JSX.Element} The rendered App UI, containing the Login component
 */
function App() {
  // JSX to render the App component
  return (
    // Main container div with a CSS class for potential App-level styling
    <div className="App">
      {/* Render the Login component, which handles the login form and authentication */}
      <Login />
    </div>
  );
}

// Export the App component as the default export for use in the React app
export default App;