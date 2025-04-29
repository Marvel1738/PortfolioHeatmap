// frontend/src/components/Login.js

// Import React and useState hook for managing component state
import React, { useState } from 'react';
// Import axios for HTTP requests
import axios from 'axios';
// Import useNavigate for redirecting after login
import { useNavigate } from 'react-router-dom';
// Import Link for navigation to register page
import { Link } from 'react-router-dom';
// Import custom CSS
import './Login.css';
// Import GuestLogin component
import GuestLogin from './GuestLogin';
// Import api for axios instance
import api from '../api/axios.js';

/**
 * Login component handles user authentication for the Portfolio Heatmap app.
 * Authenticates via /auth/login and redirects based on portfolio status using /portfolios/user.
 * 
 * @returns {JSX.Element} The rendered login form UI
 */
function Login({ updateAuthState }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  /**
   * Handles form submission, authenticates user, and redirects conditionally.
   * 
   * @param {Event} e - Form submission event
   */
  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const loginResponse = await axios.post('/auth/login', {
        email: email,
        password: password,
      }, {
        headers: { 'Content-Type': 'application/json' },
      });
      
      const token = loginResponse.data;
      const tokenPayload = JSON.parse(atob(token.split('.')[1]));
      
      updateAuthState({
        isAuthenticated: true,
        isGuest: false,
        username: tokenPayload.sub,
        token: token
      });

      setError('');
      navigate('/heatmap');
    } catch (err) {
      const errorMessage = err.response && err.response.data
        ? err.response.data
        : err.message;
      setError('Login failed: ' + errorMessage);
    }
  };

  return (
    <div className="login">
      <h1>Portfolio Heatmap Login</h1>
      <form onSubmit={handleLogin}>
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
        <button type="submit">Login</button>
      </form>
      {error && <p className="error-message">{error}</p>}
      <p className="register-link">
        New user? <Link to="/register">Create an account</Link>
      </p>
      <div className="guest-login-container">
        <p>Or</p>
        <GuestLogin updateAuthState={updateAuthState} />
      </div>
    </div>
  );
}

export default Login;