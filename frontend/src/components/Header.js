import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FaTwitter, FaLinkedin, FaGithub, FaInstagram } from 'react-icons/fa';
import './Header.css';

const Header = ({ authState, updateAuthState }) => {
  const navigate = useNavigate();

  const handleLogout = () => {
    updateAuthState({
      isAuthenticated: false,
      isGuest: false,
      username: '',
      token: null
    });
    navigate('/login');
  };

  return (
    <header className="site-header">
      {/* Social Media Icons (Left) */}
      <div className="social-icons">
        <a 
          href="https://twitter.com" 
          target="_blank" 
          rel="noopener noreferrer" 
          className="social-icon"
          aria-label="Twitter"
        >
          <FaTwitter />
        </a>
        <a 
          href="https://www.linkedin.com/in/marvel-bana-7aa697317/" 
          target="_blank" 
          rel="noopener noreferrer" 
          className="social-icon"
          aria-label="LinkedIn"
        >
          <FaLinkedin />
        </a>
        <a 
          href="https://github.com/Marvel1738" 
          target="_blank" 
          rel="noopener noreferrer" 
          className="social-icon"
          aria-label="GitHub"
        >
          <FaGithub />
        </a>
        <a 
          href="https://instagram.com" 
          target="_blank" 
          rel="noopener noreferrer" 
          className="social-icon"
          aria-label="Instagram"
        >
          <FaInstagram />
        </a>
      </div>

      {/* Logo (Center) */}
      <div className="logo">
        <Link to="/heatmap">
          <img 
            src="/ph.png" 
            alt="Portfolio Heatmap Logo" 
            className="logo-img"
            loading="lazy"
          />
        </Link>
      </div>

      {/* User Authentication (Right) */}
      <div className="auth-buttons">
        {authState.isAuthenticated && !authState.username.startsWith('guest_') ? (
          <div className="user-info">
            <span className="username">{authState.username}</span>
            <button className="logout-button" onClick={handleLogout}>Logout</button>
          </div>
        ) : (
          <div className="auth-links">
            <Link to="/login" className="auth-link">Login</Link>
            <Link to="/register" className="auth-link">Register</Link>
          </div>
        )}
      </div>
    </header>
  );
};

export default Header; 