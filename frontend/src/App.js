// frontend/src/App.js

// Import React library and useState hook for managing component state
import React, { useState } from 'react';
// Import axios for making HTTP requests to the Spring Boot backend
import axios from 'axios';
// Import custom CSS file for styling the login page
import './App.css';

/**
 * App component serves as the root component of the React frontend.
 * Implements a login page that authenticates users via the Spring Boot
 * backend's /auth/login endpoint. Stores the JWT token in localStorage
 * upon successful login and displays errors if authentication fails.
 * 
 * @returns {JSX.Element} The rendered login page UI
 */
function App() {
  // State to hold the username entered by the user, initialized as an empty string
  const [username, setUsername] = useState('');
  // State to hold the password entered by the user, initialized as an empty string
  const [password, setPassword] = useState('');
  // State to hold any error messages from login attempts, initialized as an empty string
  const [error, setError] = useState('');

  /**
   * Handles form submission when the user clicks the login button.
   * Prevents default form behavior, sends a POST request to /auth/login with
   * user credentials, and processes the response. Stores the JWT token on success
   * or sets an error message on failure.
   * 
   * @param {Event} e - The form submission event triggered by the user
   */
  const handleLogin = async (e) => {
    // Prevent the default form submission behavior which would reload the page
    e.preventDefault();

    // Try-catch block to handle the asynchronous API call safely
    try {
      // Send a POST request to the backend authentication endpoint at localhost:8080
      const response = await axios.post('http://localhost:8080/auth/login', {
        username: username, // Send the username from state
        password: password, // Send the password from state
      }, {
        headers: {
          'Content-Type': 'application/json' // Specify JSON content type for the request
        }
      });

      // Extract the JWT token from the response data directly 
      const token = response.data;
      // Store the token in localStorage for use in subsequent authenticated requests
      localStorage.setItem('token', token);
      // Clear any previous error messages since login succeeded
      setError('');
      // Temporary alert to confirm login success and show the token (will replace with redirect later)
      alert('Login successful! Token stored: ' + token);
      // TODO: Add navigation to portfolio list page after login (e.g., using React Router)

    } catch (err) {
      // Handle any errors that occur during the API call
      // Check if the error has a response (e.g., 401 Unauthorized) or is a network error
      const errorMessage = err.response && err.response.data && err.response.data.message
        ? err.response.data.message // Use backend-provided error message if available
        : err.message; // Fallback to generic error message (e.g., "Network Error")
      // Set the error state to display the message to the user
      setError('Login failed: ' + errorMessage);
    }
  };

  // JSX to render the login page UI
  return (
    // Main container div with a CSS class for styling
    <div className="App">
      {/* Heading for the login page */}
      <h1>Portfolio Heatmap Login</h1>
      {/* Form element with onSubmit event handler linked to handleLogin */}
      <form onSubmit={handleLogin}>
        {/* Username input section */}
        <div className="form-group">
          {/* Label for the username input */}
          <label>Username:</label>
          {/* Controlled input field bound to the username state */}
          <input
            type="text" // Text input type
            value={username} // Value controlled by state
            onChange={(e) => setUsername(e.target.value)} // Update state on every keystroke
            required // Make the field required in the browser
          />
        </div>
        {/* Password input section */}
        <div className="form-group">
          {/* Label for the password input */}
          <label>Password:</label>
          {/* Controlled input field bound to the password state */}
          <input
            type="password" // Password input type to mask characters
            value={password} // Value controlled by state
            onChange={(e) => setPassword(e.target.value)} // Update state on every keystroke
            required // Make the field required in the browser
          />
        </div>
        {/* Submit button to trigger the form submission */}
        <button type="submit">Login</button>
      </form>
      {/* Conditionally render an error message if the error state is non-empty */}
      {error && (
        <p className="error-message">
          {error} {/* Display the error message in red via CSS */}
        </p>
      )}
    </div>
  );
}

// Export the App component as the default export for use in the React app
export default App;
