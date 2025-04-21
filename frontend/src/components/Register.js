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
function Register({ updateAuthState }) {
  // State for username input
  const [username, setUsername] = useState('');
  // State for password input
  const [password, setPassword] = useState('');
  // State for email input
  const [email, setEmail] = useState('');
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
    console.log('Starting registration process');
    try {
      // Check if user is a guest and save their ID
      const token = localStorage.getItem('token');
      const guestUserId = localStorage.getItem('guestUserId');
      console.log('Current guest state:', { hasToken: !!token, guestUserId });

      console.log('Sending registration request');
      const response = await axios.post('http://localhost:8080/auth/register', {
        username: username,
        password: password,
        email: email,
        guestUserId: guestUserId
      }, {
        headers: { 'Content-Type': 'application/json' },
      });

      // Store the new token
      const newToken = response.data;
      console.log('Received new token from registration');
      localStorage.setItem('token', newToken);
      
      // Clear guest user ID from localStorage after successful registration
      localStorage.removeItem('guestUserId');
      console.log('Cleared guest user ID from localStorage');

      // Parse the new token to get user info
      const tokenPayload = JSON.parse(atob(newToken.split('.')[1]));
      console.log('Parsed new token payload:', tokenPayload);
      
      // Update auth state
      updateAuthState({
        isAuthenticated: true,
        isGuest: false,
        username: username,
        token: newToken
      });
      console.log('Updated auth state for registered user');

      setError('');
      setSuccess('Registration successful!');
      setUsername('');
      setPassword('');
      setEmail('');
      setTimeout(() => navigate('/heatmap'), 1000);
    } catch (err) {
      console.error('Registration failed:', err);
      if (err.response) {
        console.error('Error response:', err.response.data);
      }
      const errorMessage = err.response && err.response.data
        ? err.response.data
        : err.message;
      setError('Registration failed: ' + errorMessage);
      setSuccess('');
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
          <label>Email:</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
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