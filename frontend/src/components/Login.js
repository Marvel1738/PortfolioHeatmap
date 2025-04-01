// frontend/src/components/Login.js

// Import React and useState hook for managing component state
import React, { useState } from 'react';
// Import axios for HTTP requests to the Spring Boot backend
import axios from 'axios';
// Import useNavigate for redirecting after login
import { useNavigate } from 'react-router-dom';
// Import Link for navigation to the register page
import { Link } from 'react-router-dom';
// Import custom CSS for styling
import './Login.css';

/**
 * Login component handles user authentication for the Portfolio Heatmap app.
 * Provides a form for login, redirects based on portfolio status, and includes
 * a link to the registration page for new users.
 * 
 * @returns {JSX.Element} The rendered login form UI
 */
function Login() {
  // State to hold the username entered by the user, initialized as an empty string
  const [username, setUsername] = useState('');
  // State to hold the password entered by the user, initialized as an empty string
  const [password, setPassword] = useState('');
  // State to hold any error messages from login attempts, initialized as an empty string
  const [error, setError] = useState('');
  // Hook to navigate to other routes after successful login
  const navigate = useNavigate();

  /**
   * Handles form submission when the user clicks the login button.
   * Authenticates the user and redirects based on portfolio status.
   * 
   * @param {Event} e - The form submission event triggered by the user
   */
  const handleLogin = async (e) => {
    // Prevent the default form submission behavior which would reload the page
    e.preventDefault();

    // Try-catch block to handle the asynchronous API call safely
    try {
      // Send a POST request to the backend authentication endpoint
      const response = await axios.post('http://localhost:8080/auth/login', {
        username: username, // Send username from state
        password: password, // Send password from state
      }, {
        headers: {
          'Content-Type': 'application/json' // Specify JSON content type
        }
      });

      // Extract the JWT token from the response (raw string)
      const token = response.data;
      // Store the token in localStorage for authenticated requests
      localStorage.setItem('token', token);
      // Clear any previous error messages
      setError('');

      // Placeholder logic: Check if user has portfolios (to be replaced with API call)
      const hasPortfolios = false; // Hardcoded for now—will check backend later
      if (hasPortfolios) {
        // Redirect returning users to the heatmap page
        navigate('/heatmap');
      } else {
        // Redirect new users to create their first portfolio
        navigate('/create-portfolio');
      }
    } catch (err) {
      // Handle errors (e.g., wrong credentials, network issues)
      const errorMessage = err.response && err.response.data
        ? err.response.data // Use backend error if available
        : err.message; // Fallback to generic error
      // Set error state to display to the user
      setError('Login failed: ' + errorMessage);
    }
  };

  // JSX to render the login component UI
  return (
    // Main container div with CSS class for styling
    <div className="login">
      {/* Heading for the login form */}
      <h1>Portfolio Heatmap Login</h1>
      {/* Form element with onSubmit handler */}
      <form onSubmit={handleLogin}>
        {/* Username input section */}
        <div className="form-group">
          {/* Label for username input */}
          <label>Username:</label>
          {/* Controlled input bound to username state */}
          <input
            type="text" // Text input type
            value={username} // Value from state
            onChange={(e) => setUsername(e.target.value)} // Update state on change
            required // Browser-enforced requirement
          />
        </div>
        {/* Password input section */}
        <div className="form-group">
          {/* Label for password input */}
          <label>Password:</label>
          {/* Controlled input bound to password state */}
          <input
            type="password" // Password type to mask input
            value={password} // Value from state
            onChange={(e) => setPassword(e.target.value)} // Update state on change
            required // Browser-enforced requirement
          />
        </div>
        {/* Submit button to trigger login */}
        <button type="submit">Login</button>
      </form>
      {/* Conditionally render error message if login fails */}
      {error && (
        <p className="error-message">
          {error} {/* Display error in red via CSS */}
        </p>
      )}
      {/* Link to the registration page for new users */}
      <p className="register-link">
        New user? <Link to="/register">Create an account</Link>
      </p>
    </div>
  );
}

// Export the Login component as the default export
export default Login;