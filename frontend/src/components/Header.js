import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FaTwitter, FaGithub, FaRedditAlien } from 'react-icons/fa';
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

  // Determine if user is authenticated and not a guest
  const isAuthenticatedUser = authState.isAuthenticated && !authState.username.startsWith('guest_');

  // Prepare auth elements for mobile layout
  const leftElement = isAuthenticatedUser 
    ? <span className="username auth-left">{authState.username}</span>
    : <Link to="/login" className="auth-link auth-left">Login</Link>;
    
  const rightElement = isAuthenticatedUser
    ? <button className="logout-button auth-right" onClick={handleLogout}>Logout</button>
    : <Link to="/register" className="auth-link auth-right">Register</Link>;

  return (
    <header className="site-header">
      {/* Social Media Icons (Left on desktop, hidden on mobile) */}
      <div className="social-icons">
        <a 
          href="https://www.reddit.com/user/MarvBuilds//" 
          target="_blank" 
          rel="noopener noreferrer" 
          className="social-icon reddit"
          aria-label="Reddit"
        >
          <FaRedditAlien />
        </a>
        <a 
          href="https://github.com/Marvel1738/PortfolioHeatmap" 
          target="_blank" 
          rel="noopener noreferrer" 
          className="social-icon github"
          aria-label="GitHub"
        >
          <FaGithub />
        </a>
        <a 
          href="https://x.com/PortfolioHeat" 
          target="_blank" 
          rel="noopener noreferrer" 
          className="social-icon twitter"
          aria-label="X (Twitter)"
        >
          <FaTwitter />
        </a>
      </div>

      {/* Left Auth Element (Login or Username) - Shows on mobile */}
      {leftElement}

      {/* Logo (Center) */}
      <div className="logo">
        <Link to="/heatmap">
          <img 
            src="/PH.png" 
            alt="Portfolio Heatmap Logo" 
            className="logo-img"
            loading="lazy"
          />
        </Link>
      </div>

      {/* Right Auth Element (Register or Logout) - Shows on mobile */}
      {rightElement}
      
      {/* Desktop Auth Buttons - Hidden on mobile */}
      <div className="auth-buttons">
        {isAuthenticatedUser ? (
          <>
            <span className="username">{authState.username}</span>
            <button className="logout-button" onClick={handleLogout}>Logout</button>
          </>
        ) : (
          <>
            <Link to="/login" className="auth-link">Login</Link>
            <Link to="/register" className="auth-link">Register</Link>
          </>
        )}
      </div>
    </header>
  );
};

export default Header; 