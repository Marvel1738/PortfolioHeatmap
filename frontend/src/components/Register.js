// frontend/src/components/Register.js

// Import React and useState hook for managing component state
import React, { useState } from 'react';
// Import axios for making HTTP requests to the Spring Boot backend
import axios from 'axios';
// Import useNavigate for redirecting after registration
import { useNavigate } from 'react-router-dom';
// Import custom CSS file (reusing Login.css)
import './Login.css';

/**
 * Register component handles user registration for the Portfolio Heatmap app.
 * Provides a form for new users to input credentials, sends them to /auth/register,
 * and redirects to the login page on success.
 * 
 * @returns {JSX.Element} The rendered registration form UI
 */
function Register() {
  // State for username input
  const [username, setUsername] = useState('');
  // State for password input
  const [password, setPassword] = useState('');
  // State for error messages
  const [error, setError] = useState('');
  // State for success messages
  const [success, setSuccess] = useState('');
  // Hook to navigate to other routes
  const navigate = useNavigate();

  /**
   * Handles form submission, registers the user, and redirects to login.
   * 
   * @param {Event} e - Form submission event
   */
  const handleRegister = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post('http://localhost:8080/auth/register', {
        username: username, // Send username from state
        password: password, // Send password from state
      }, {
        headers: { 'Content-Type': 'application/json' },
      });
      setError(''); // Clear errors on success
      setSuccess(response.data); // Show success message briefly
      setUsername(''); // Clear form
      setPassword('');
      // Redirect to login page after a short delay to show success
      setTimeout(() => navigate('/login'), 1000); // 1-second delay
    } catch (err) {
      const errorMessage = err.response && err.response.data
        ? err.response.data
        : err.message;
      setError('Registration failed: ' + errorMessage);
      setSuccess(''); // Clear success on error
    }
  };

  return (
    <div className="login">
      <h1>Create Account</h1>
      <form onSubmit={handleRegister}>
        <div className="form-group">
          <label>Username:</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label>Password:</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <button type="submit">Register</button>
      </form>
      {success && <p className="success-message">{success}</p>}
      {error && <p className="error-message">{error}</p>}
    </div>
  );
}

export default Register;