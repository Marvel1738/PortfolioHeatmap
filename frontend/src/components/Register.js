// frontend/src/components/Register.js

// Import React and useState hook for managing component state
import React, { useState } from 'react';
// Import axios for making HTTP requests to the Spring Boot backend
import axios from 'axios';
// Import custom CSS file (reusing Login.css for consistency)
import './Login.css'; // Reuse Login.css for now—can split later if needed

/**
 * Register component handles user registration for the Portfolio Heatmap app.
 * Provides a form for new users to input username and password, sends credentials
 * to the backend /auth/register endpoint, and displays success or error messages.
 * 
 * @returns {JSX.Element} The rendered registration form UI
 */
function Register() {
  // State to hold the username entered by the user, initialized as an empty string
  const [username, setUsername] = useState('');
  // State to hold the password entered by the user, initialized as an empty string
  const [password, setPassword] = useState('');
  // State to hold any error messages from registration attempts, initialized as an empty string
  const [error, setError] = useState('');
  // State to hold success message after registration, initialized as an empty string
  const [success, setSuccess] = useState('');

  /**
   * Handles form submission when the user clicks the register button.
   * Prevents default form behavior, sends a POST request to /auth/register with
   * user credentials, and processes the response. Displays success or error messages.
   * 
   * @param {Event} e - The form submission event triggered by the user
   */
  const handleRegister = async (e) => {
    // Prevent the default form submission behavior which would reload the page
    e.preventDefault();

    // Try-catch block to handle the asynchronous API call safely
    try {
      // Send a POST request to the backend registration endpoint at localhost:8080
      const response = await axios.post('http://localhost:8080/auth/register', {
        username: username, // Send the username from state
        password: password, // Send the password from state
      }, {
        headers: {
          'Content-Type': 'application/json' // Specify JSON content type for the request
        }
      });

      // Clear any previous error messages since registration succeeded
      setError('');
      // Set success message from backend response (e.g., "User registered successfully")
      setSuccess(response.data);
      // Clear form inputs after success
      setUsername('');
      setPassword('');
      // TODO: Redirect to portfolio creation page after routing is added

    } catch (err) {
      // Handle any errors that occur during the API call
      // Check if the error has a response (e.g., 400 Bad Request) or is a network error
      const errorMessage = err.response && err.response.data
        ? err.response.data // Use backend-provided error message if available (e.g., string)
        : err.message; // Fallback to generic error message (e.g., "Network Error")
      // Set the error state to display the message to the user
      setError('Registration failed: ' + errorMessage);
      // Clear success message if there was an error
      setSuccess('');
    }
  };

  // JSX to render the registration component UI
  return (
    // Main container div reusing the 'login' class for consistent styling
    <div className="login">
      {/* Heading for the registration form */}
      <h1>Create Account</h1>
      {/* Form element with onSubmit event handler linked to handleRegister */}
      <form onSubmit={handleRegister}>
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
        <button type="submit">Register</button>
      </form>
      {/* Conditionally render a success message if registration succeeds */}
      {success && (
        <p className="success-message">
          {success} {/* Display success message in green */}
        </p>
      )}
      {/* Conditionally render an error message if registration fails */}
      {error && (
        <p className="error-message">
          {error} {/* Display error message in red */}
        </p>
      )}
    </div>
  );
}

// Export the Register component as the default export for use in other parts of the app
export default Register;