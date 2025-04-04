import React from 'react';
import { Link } from 'react-router-dom';
import { FaTwitter, FaLinkedin, FaGithub, FaInstagram, FaQuestionCircle, FaRobot } from 'react-icons/fa';
import './Header.css';

const Header = () => {
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
          href="https://linkedin.com" 
          target="_blank" 
          rel="noopener noreferrer" 
          className="social-icon"
          aria-label="LinkedIn"
        >
          <FaLinkedin />
        </a>
        <a 
          href="https://github.com" 
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

      {/* Navigation Links (Right) */}
      <div className="nav-links">
        <Link to="/about" className="nav-link">
          <FaQuestionCircle className="nav-icon" />
          <span>About</span>
        </Link>
        <Link to="/predictions" className="nav-link">
          <FaRobot className="nav-icon" />
          <span>AI Stock Predictions</span>
        </Link>
      </div>
    </header>
  );
};

export default Header; 